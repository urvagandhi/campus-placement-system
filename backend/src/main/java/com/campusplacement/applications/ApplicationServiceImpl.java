package com.campusplacement.applications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.applications.dto.ApplicationDTO;
import com.campusplacement.applications.dto.ApplyRequestDTO;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.eligibility.DriveEligibilityService;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of ApplicationService with organization scope enforcement.
 *
 * <p>
 * <strong>Scope Enforcement:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: All applications across all colleges</li>
 * <li>ADMIN: All applications in their college</li>
 * <li>COORDINATOR: Only applications from allowed departments</li>
 * <li>STUDENT: Only their own applications</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final ApplicationRepository applicationRepository;
    private final DriveRepository driveRepository;
    private final StudentRepository studentRepository;
    private final DriveEligibilityService eligibilityService;
    private final OrganizationScopeService scopeService;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public List<ApplicationDTO> getAllApplications() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        log.debug("Fetching applications for user {} with role {}", userId, scope.role());

        List<Application> applications;
        if (scope.isStudent()) {
            return getCurrentStudentApplications();
        } else if (scope.isSuperAdmin()) {
            log.debug("SUPER_ADMIN access: returning all applications");
            applications = applicationRepository.findAll();
        } else if (scope.hasFullCollegeAccess()) {
            // ADMIN or COORDINATOR with UNIVERSITY SUBTREE: all applications in college
            log.debug("Full college access: returning applications for college {}", scope.collegeId());
            applications = applicationRepository.findByCollegeId(scope.collegeId());
        } else {
            // COORDINATOR with limited scope: only applications from allowed departments
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                log.warn("User {} has no allowed departments, returning empty list", userId);
                return List.of();
            }
            log.debug("Scoped access: returning applications for departments {}", scope.allowedDepartmentIds());
            applications = applicationRepository.findByStudentDepartmentIdInAndCollegeId(
                    scope.allowedDepartmentIds(),
                    scope.collegeId());
        }

        return applications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationDTO> getCurrentStudentApplications() {
        Long userId = getCurrentUserId();
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return applicationRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public List<ApplicationDTO> getApplicationsByDrive(Long driveId) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (scope.collegeId() != null && !scope.collegeId().equals(driveCollegeId)) {
                throw new AccessDeniedException("No access to this drive");
            }
        }

        List<Application> applications;
        if (scope.isSuperAdmin() || scope.hasFullCollegeAccess()) {
            applications = applicationRepository.findByDriveId(driveId);
        } else if (scope.hasScopedAccess()) {
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                return List.of();
            }
            applications = applicationRepository.findByDriveIdAndStudentDepartmentIdIn(driveId,
                    scope.allowedDepartmentIds());
        } else {
            return List.of();
        }

        return applications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationDTO applyToDrive(ApplyRequestDTO request) {
        Long userId = getCurrentUserId();
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        PlacementDrive drive = driveRepository.findById(request.getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        if (applicationRepository.existsByStudentIdAndDriveId(student.getId(), drive.getId())) {
            throw new IllegalStateException("Already applied to this drive");
        }

        Long studentCollegeId = student.getUser().getCollege().getId();
        Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
        if (!studentCollegeId.equals(driveCollegeId)) {
            throw new AccessDeniedException("Cannot apply to drives outside your college");
        }

        eligibilityService.validateApplication(student.getId(), drive.getId());

        Application application = Application.builder()
                .student(student)
                .driveId(drive.getId())
                .drive(drive)
                .status("APPLIED")
                .appliedAt(LocalDateTime.now())
                .resumeUrl(request.getResumeUrl() != null ? request.getResumeUrl() : student.getResumeUrl())
                .coverLetter(request.getCoverLetter())
                .build();

        application = applicationRepository.save(application);

        return mapToDTO(application);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public ApplicationDTO updateApplicationStatus(Long id, String status) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        @SuppressWarnings("null")
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(application.getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (scope.collegeId() != null && !scope.collegeId().equals(driveCollegeId)) {
                auditLog.warn("SECURITY: User {} attempted to update application {} outside their college. " +
                        "User college: {}, Drive college: {}",
                        userId, id, scope.collegeId(), driveCollegeId);
                throw new AccessDeniedException("No access to this application");
            }
        }

        if (!scope.isSuperAdmin() && !scope.hasFullCollegeAccess()) {
            Long studentDeptId = application.getStudent().getDepartment() != null
                    ? application.getStudent().getDepartment().getId()
                    : null;
            if (studentDeptId != null && !scope.canAccessDepartment(studentDeptId)) {
                auditLog.warn("SECURITY: User {} attempted to update application {} for student in department {} " +
                        "outside their scope. Allowed departments: {}",
                        userId, id, studentDeptId, scope.allowedDepartmentIds());
                throw new AccessDeniedException("No access to this student's application");
            }
        }

        application.setStatus(status);
        if ("SHORTLISTED".equalsIgnoreCase(status)) {
            application.setShortlistedAt(LocalDateTime.now());
        } else if ("SELECTED".equalsIgnoreCase(status)) {
            application.setSelectedAt(LocalDateTime.now());
        }

        return mapToDTO(applicationRepository.save(application));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public void withdrawApplication(Long id) {
        Long userId = getCurrentUserId();
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        @SuppressWarnings("null")
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("Cannot withdraw another student's application");
        }

        if (!List.of("APPLIED", "SHORTLISTED").contains(application.getStatus())) {
            throw new IllegalStateException("Cannot withdraw application in status: " + application.getStatus());
        }

        application.setStatus("WITHDRAWN");
        // application.setWithdrawnAt(LocalDateTime.now()); // Field not in entity yet

        applicationRepository.save(application);
    }

    private ApplicationDTO mapToDTO(Application app) {
        String studentName = (app.getStudent() != null && app.getStudent().getUser() != null)
                ? app.getStudent().getUser().getName()
                : "Unknown Student";
        String driveTitle = "";
        String companyName = "";

        if (app.getDrive() != null) {
            driveTitle = app.getDrive().getTitle();
            if (app.getDrive().getCompany() != null) {
                companyName = app.getDrive().getCompany().getName();
            }
        } else {
            // Fallback if relation not loaded (lazy) or set
            @SuppressWarnings("null")
            Optional<PlacementDrive> driveOpt = driveRepository.findById(app.getDriveId());
            if (driveOpt.isPresent()) {
                PlacementDrive d = driveOpt.get();
                driveTitle = d.getTitle();
                if (d.getCompany() != null) {
                    companyName = d.getCompany().getName();
                }
            }
        }

        return ApplicationDTO.builder()
                .id(app.getId())
                .studentId(app.getStudent().getId())
                .studentName(studentName)
                .driveId(app.getDriveId())
                .driveTitle(driveTitle)
                .companyName(companyName)
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .shortlistedAt(app.getShortlistedAt())
                .selectedAt(app.getSelectedAt())
                .notes(app.getInternalNotes())
                .build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
