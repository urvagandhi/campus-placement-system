package com.campusplacement.students;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.auth.LoginAudit;
import com.campusplacement.auth.LoginAuditRepository;
import com.campusplacement.auth.SecurityAuditEventType;
import com.campusplacement.common.UserRole;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.dto.StudentProfileDTO;
import com.campusplacement.students.dto.StudentProfileResponseDTO;
import com.campusplacement.students.dto.StudentProfileUpdateDTO;
import com.campusplacement.students.dto.StudentSkillsDTO;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Service class for student profile operations.
 *
 * <p>
 * <strong>Security Model:</strong>
 * </p>
 * <ul>
 * <li>Students can ONLY update career-layer fields</li>
 * <li>Any attempt to modify academic fields is logged and ignored</li>
 * <li>Organization hierarchy is derived, never client-provided</li>
 * <li><strong>Organization scope enforcement via
 * OrganizationScopeService</strong></li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final OrganizationScopeService scopeService;
    private final com.campusplacement.eligibility.DriveEligibilityService driveEligibilityService;

    // ==================== Read Operations ====================

    /**
     * Retrieves all student profiles within the user's scope.
     *
     * <p>
     * <strong>Scope Enforcement:</strong>
     * </p>
     * <ul>
     * <li>SUPER_ADMIN: All students across all colleges</li>
     * <li>ADMIN: All students in their college</li>
     * <li>COORDINATOR with SUBTREE at UNIVERSITY: All students in college</li>
     * <li>COORDINATOR with limited scope: Only students in allowed departments</li>
     * <li>STUDENT: Should not reach here (handled by controller RBAC)</li>
     * </ul>
     *
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     */
    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public List<StudentProfileDTO> getAllStudents() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // 1. SUPER_ADMIN: Platform owner - no restrictions
        if (scope.isSuperAdmin()) {
            log.debug("SUPER_ADMIN access: returning all students");
            return studentRepository.findAll().stream()
                    .map(this::toBasicDTO)
                    .collect(Collectors.toList());
        }

        // 2. Full College Access: ADMIN role OR Coordinator with SUBTREE at UNIVERSITY
        // hasFullCollegeAccess() = isAdmin() || isUniversityScope
        if (scope.hasFullCollegeAccess()) {
            log.debug("Full college access for userId {}: returning all students in college {}",
                    userId, scope.collegeId());
            return studentRepository.findByCollegeId(scope.collegeId()).stream()
                    .map(this::toBasicDTO)
                    .collect(Collectors.toList());
        }

        // 3. Scoped Access: COORDINATOR with limited org assignments
        // Only sees students in their allowed departments
        if (scope.hasScopedAccess()) {
            log.debug("Scoped access for userId {}: {} departments allowed",
                    userId, scope.allowedDepartmentIds().size());
            return studentRepository.findByDepartmentIdInAndCollegeId(
                    scope.allowedDepartmentIds(),
                    scope.collegeId()).stream().map(this::toBasicDTO).collect(Collectors.toList());
        }

        // 4. STUDENT or no assignments: Fail-secure - return empty
        log.warn("No scope access for userId {} with role {}", userId, scope.role());
        return Collections.emptyList();
    }

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    /**
     * Retrieves a student profile by ID.
     * Access controls enforced at controller level.
     */
    @Transactional(readOnly = true)
    public StudentProfileDTO getStudentById(Long id) {
        @SuppressWarnings("null")
        StudentProfile profile = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student profile not found: " + id));
        return toBasicDTO(profile);
    }

    /**
     * Retrieves current authenticated student's profile with full organization
     * hierarchy.
     * Access: STUDENT only (their own profile).
     */
    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    public StudentProfileResponseDTO getCurrentStudentProfile() {
        Long userId = getCurrentUserId();

        StudentProfile profile = studentRepository.findByUserIdWithHierarchy(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Student profile not found for user: " + userId));

        return toFullResponseDTO(profile);
    }

    /**
     * Retrieves students by department ID with scope enforcement.
     *
     * <p>
     * <strong>Security:</strong> Validates that the requesting user has access
     * to the specified department before returning data.
     * </p>
     *
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     */
    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public List<StudentProfileDTO> getStudentsByDepartment(String departmentIdOrName) {
        try {
            Long departmentId = Long.parseLong(departmentIdOrName);
            return getStudentsByDepartmentId(departmentId);
        } catch (NumberFormatException e) {
            // Legacy support: search by name if not a numeric ID
            log.warn("Department lookup by name is deprecated, use ID instead: {}", departmentIdOrName);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves students by department ID with scope enforcement.
     *
     * <p>
     * <strong>Security:</strong> Validates that the requesting user has access
     * to the specified department before returning data. Used by both controller
     * (with method-level @PreAuthorize) and internal calls.
     * </p>
     *
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     */
    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public List<StudentProfileDTO> getStudentsByDepartmentId(Long departmentId) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Validate scope access to this department
        if (!scope.isSuperAdmin() && !scope.canAccessDepartment(departmentId)) {
            auditLog.warn("SECURITY: User {} attempted to access department {} without permission",
                    userId, departmentId);
            throw new AccessDeniedException("No access to department: " + departmentId);
        }

        return studentRepository.findByDepartmentId(departmentId).stream()
                .map(this::toBasicDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves eligible students for a placement drive.
     * Access: COORDINATOR, ADMIN only.
     */
    /**
     * Gets eligible students for a specific placement drive.
     *
     * <p>
     * Integrates with DriveEligibilityService to check CGPA, backlogs,
     * department eligibility, and college match.
     * </p>
     *
     * @param driveId ID of the placement drive
     * @return list of eligible students (scope-filtered)
     */
    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public List<StudentProfileDTO> getEligibleStudentsForDrive(Long driveId) {
        CustomUserDetails currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        log.info("Fetching eligible students for drive: {} by user: {} ({})",
                driveId, userId, currentUser.getRole());

        ScopeContext scope = scopeService.resolveScope(userId);
        log.debug("User scope: isSuperAdmin={}, hasFullCollegeAccess={}, collegeId={}",
                scope.isSuperAdmin(), scope.hasFullCollegeAccess(), scope.collegeId());

        try {
            // Use DriveEligibilityService to get eligible students (already scope-filtered)
            List<StudentProfile> eligibleProfiles = driveEligibilityService.getEligibleStudents(driveId, userId);

            return eligibleProfiles.stream()
                    .map(this::toBasicDTO)
                    .collect(Collectors.toList());

        } catch (ResourceNotFoundException e) {
            log.error("Drive {} not found", driveId, e);
            throw e;
        } catch (AccessDeniedException e) {
            log.warn("User {} attempted to access drive {} without permission", userId, driveId);
            throw e;
        }
    }

    // ==================== Write Operations ====================

    /**
     * Updates student's career-layer profile fields.
     *
     * <p>
     * <strong>Security:</strong> Only career-layer fields are updated.
     * Academic fields (cgpa, enrollmentNo, departmentId, backlogs, batchYear,
     * semester)
     * are IGNORED even if present in the DTO.
     * </p>
     *
     * @param updateDTO contains only student-editable fields
     * @return updated profile with full hierarchy
     */
    @Transactional
    public StudentProfileResponseDTO updateCareerProfile(StudentProfileUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();

        StudentProfile profile = studentRepository.findByUserIdWithHierarchy(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Student profile not found for user: " + userId));

        // Update ONLY career-layer fields
        if (updateDTO.getSkills() != null) {
            profile.setSkills(String.join(",", updateDTO.getSkills()));
        }
        if (updateDTO.getResumeUrl() != null) {
            profile.setResumeUrl(updateDTO.getResumeUrl());
        }
        if (updateDTO.getProjectsCount() != null) {
            profile.setProjectsCount(updateDTO.getProjectsCount());
        }
        if (updateDTO.getInternshipMonths() != null) {
            profile.setInternshipMonths(updateDTO.getInternshipMonths());
        }
        if (updateDTO.getCertifications() != null) {
            profile.setCertifications(String.join(",", updateDTO.getCertifications()));
        }
        if (updateDTO.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(updateDTO.getLinkedinUrl());
        }
        if (updateDTO.getGithubUrl() != null) {
            profile.setGithubUrl(updateDTO.getGithubUrl());
        }
        if (updateDTO.getCareerInterests() != null) {
            // Store as JSON array string
            profile.setCareerInterests("[\"" + String.join("\",\"", updateDTO.getCareerInterests()) + "\"]");
        }

        @SuppressWarnings("null")
        StudentProfile saved = studentRepository.save(profile);
        log.info("Career profile updated for user: {}", userId);

        // Audit the profile update
        auditProfileEvent(userId, SecurityAuditEventType.PROFILE_UPDATE, true);

        return toFullResponseDTO(saved);
    }

    /**
     * Creates or updates a student profile.
     *
     * <p>
     * <strong>Access Control:</strong>
     * <ul>
     * <li>STUDENT: Can only update career-layer fields of their own profile</li>
     * <li>COORDINATOR/ADMIN: Can update any fields including academic</li>
     * </ul>
     * </p>
     */
    @Transactional
    public StudentProfileDTO createOrUpdateProfile(StudentProfileDTO profileDTO) {
        Long userId = getCurrentUserId();
        UserRole currentRole = getCurrentUserRole();

        StudentProfile profile = studentRepository.findByUserId(userId)
                .orElse(new StudentProfile());

        boolean isNewProfile = profile.getId() == null;

        // If student role, log any attempt to set academic fields
        if (currentRole == UserRole.STUDENT) {
            logRestrictedFieldAttempts(profileDTO, userId);
        }

        // Set user if new profile
        if (isNewProfile) {
            @SuppressWarnings("null")
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            profile.setUser(user);
        }

        // Only allow full field updates for admins/coordinators
        if (currentRole == UserRole.COORDINATOR || currentRole == UserRole.ADMIN ||
                currentRole == UserRole.SUPER_ADMIN) {
            // Admins can update academic fields
            if (profileDTO.getEnrollmentNo() != null) {
                profile.setEnrollmentNo(profileDTO.getEnrollmentNo());
            }
            if (profileDTO.getCgpa() != null) {
                profile.setCgpa(profileDTO.getCgpa());
            }
            if (profileDTO.getBatchYear() != null) {
                profile.setBatchYear(profileDTO.getBatchYear());
            }
            if (profileDTO.getSemester() != null) {
                profile.setSemester(profileDTO.getSemester());
            }
        }

        // Career-layer fields can be updated by anyone
        updateCareerFields(profile, profileDTO);

        StudentProfile saved = studentRepository.save(profile);

        // Audit the profile update
        auditProfileEvent(userId, SecurityAuditEventType.PROFILE_UPDATE, true);

        return toBasicDTO(saved);
    }

    /**
     * Updates student skills.
     */
    @Transactional
    public StudentProfileDTO updateSkills(Long id, StudentSkillsDTO skillsDTO) {
        Long currentUserId = getCurrentUserId();
        UserRole currentRole = getCurrentUserRole();

        @SuppressWarnings("null")
        StudentProfile profile = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student profile not found: " + id));

        // Students can only update their own profile
        if (currentRole == UserRole.STUDENT && !profile.getUserId().equals(currentUserId)) {
            auditLog.warn("SECURITY: User {} attempted to update profile {}", currentUserId, id);
            throw new AccessDeniedException("Cannot update another student's profile");
        }

        if (skillsDTO.getSkills() != null) {
            profile.setSkills(String.join(",", skillsDTO.getSkills()));
        }

        @SuppressWarnings("null")
        StudentProfile saved = studentRepository.save(profile);
        log.info("Skills updated for profile: {}", id);

        // Audit the skill update
        auditProfileEvent(currentUserId, SecurityAuditEventType.PROFILE_UPDATE, true);

        return toBasicDTO(saved);
    }

    // ==================== Helper Methods ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }

    private UserRole getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ((CustomUserDetails) auth.getPrincipal()).getRole();
    }

    /**
     * Records a profile-related audit event.
     *
     * @param userId    the user ID
     * @param eventType the type of event (PROFILE_UPDATE, PROFILE_VIEW)
     * @param success   whether the operation was successful
     */
    @SuppressWarnings("null")
    private void auditProfileEvent(Long userId, SecurityAuditEventType eventType, boolean success) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            String email = user != null ? user.getEmail() : "unknown";

            LoginAudit audit = LoginAudit.builder()
                    .userId(userId)
                    .email(email)
                    .eventType(eventType)
                    .success(success)
                    .build();

            loginAuditRepository.save(audit);
            log.debug("Profile audit recorded: userId={}, event={}, success={}", userId, eventType, success);
        } catch (Exception e) {
            log.error("Failed to record profile audit for user: {}", userId, e);
            // Don't fail the operation if auditing fails
        }
    }

    private void updateCareerFields(StudentProfile profile, StudentProfileDTO dto) {
        if (dto.getSkills() != null) {
            profile.setSkills(String.join(",", dto.getSkills()));
        }
        if (dto.getResumeUrl() != null) {
            profile.setResumeUrl(dto.getResumeUrl());
        }
        if (dto.getProjectsCount() != null) {
            profile.setProjectsCount(dto.getProjectsCount());
        }
        if (dto.getInternshipMonths() != null) {
            profile.setInternshipMonths(dto.getInternshipMonths());
        }
        if (dto.getCertifications() != null) {
            profile.setCertifications(String.join(",", dto.getCertifications()));
        }
        if (dto.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(dto.getLinkedinUrl());
        }
        if (dto.getGithubUrl() != null) {
            profile.setGithubUrl(dto.getGithubUrl());
        }
    }

    private void logRestrictedFieldAttempts(StudentProfileDTO dto, Long userId) {
        StringBuilder attempts = new StringBuilder();

        if (dto.getCgpa() != null) {
            attempts.append("cgpa=").append(dto.getCgpa()).append("; ");
        }
        if (dto.getEnrollmentNo() != null) {
            attempts.append("enrollmentNo=").append(dto.getEnrollmentNo()).append("; ");
        }
        if (dto.getDepartment() != null) {
            attempts.append("department=").append(dto.getDepartment()).append("; ");
        }
        if (dto.getBatchYear() != null) {
            attempts.append("batchYear=").append(dto.getBatchYear()).append("; ");
        }
        if (dto.getSemester() != null) {
            attempts.append("semester=").append(dto.getSemester()).append("; ");
        }

        if (attempts.length() > 0) {
            auditLog.warn("SECURITY: Student {} attempted to modify restricted fields: {}",
                    userId, attempts.toString());
        }
    }

    private StudentProfileDTO toBasicDTO(StudentProfile profile) {
        return StudentProfileDTO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .enrollmentNo(profile.getEnrollmentNo())
                .department(profile.getDepartment() != null ? profile.getDepartment().getName() : null)
                .cgpa(profile.getCgpa())
                .skills(parseCommaSeparated(profile.getSkills()))
                .resumeUrl(profile.getResumeUrl())
                .batchYear(profile.getBatchYear())
                .semester(profile.getSemester())
                .projectsCount(profile.getProjectsCount())
                .internshipMonths(profile.getInternshipMonths())
                .certifications(parseCommaSeparated(profile.getCertifications()))
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .build();
    }

    private StudentProfileResponseDTO toFullResponseDTO(StudentProfile profile) {
        User user = profile.getUser();
        OrganizationUnit department = profile.getDepartment();
        OrganizationUnit institute = department != null ? department.getParent() : null;
        OrganizationUnit college = institute != null ? institute.getParent() : null;

        return StudentProfileResponseDTO.builder()
                // Identity
                .id(profile.getId())
                .userId(user != null ? user.getId() : null)
                .name(user != null ? user.getName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)

                // Organization Hierarchy
                .collegeId(college != null ? college.getId() : null)
                .collegeName(college != null ? college.getName() : null)
                .instituteId(institute != null ? institute.getId() : null)
                .instituteName(institute != null ? institute.getName() : null)
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)

                // Academic Truth
                .enrollmentNo(profile.getEnrollmentNo())
                .cgpa(profile.getCgpa())
                .backlogs(profile.getBacklogs())
                .batchYear(profile.getBatchYear())
                .semester(profile.getSemester())

                // Career Layer
                .skills(parseCommaSeparated(profile.getSkills()))
                .resumeUrl(profile.getResumeUrl())
                .projectsCount(profile.getProjectsCount())
                .internshipMonths(profile.getInternshipMonths())
                .certifications(parseCommaSeparated(profile.getCertifications()))
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .careerInterests(parseJsonArray(profile.getCareerInterests()))

                // Profile Status
                .isProfileComplete(isProfileComplete(profile))
                .build();
    }

    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank()) {
            return Collections.emptyList();
        }
        // Simple JSON array parsing: ["a","b","c"]
        String clean = jsonArray.replaceAll("[\\[\\]\"]", "");
        return parseCommaSeparated(clean);
    }

    private boolean isProfileComplete(StudentProfile profile) {
        return profile.getSkills() != null && !profile.getSkills().isBlank() &&
                profile.getResumeUrl() != null && !profile.getResumeUrl().isBlank();
    }
}
