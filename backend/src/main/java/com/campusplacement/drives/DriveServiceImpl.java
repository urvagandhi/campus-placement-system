package com.campusplacement.drives;

import java.time.LocalDate;
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

import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.dto.CreateDriveRequestDTO;
import com.campusplacement.drives.dto.DriveDTO;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for placement drive operations.
 *
 * <p>
 * <strong>Scope Enforcement:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: All drives across all colleges</li>
 * <li>ADMIN: All drives in their college</li>
 * <li>COORDINATOR: All drives in their college (can manage)</li>
 * <li>STUDENT: All drives in their college (read-only)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DriveServiceImpl implements DriveService {

    private static final Logger log = LoggerFactory.getLogger(DriveServiceImpl.class);

    private final DriveRepository driveRepository;
    private final CollegeRepository collegeRepository;
    private final OrganizationScopeService scopeService;
    private final com.campusplacement.organizations.OrganizationUnitRepository orgUnitRepository;

    // ==================== Read Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getAllDrives() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // 1. SUPER_ADMIN: Platform owner - all drives
        if (scope.isSuperAdmin()) {
            log.debug("SUPER_ADMIN access: returning all drives");
            return driveRepository.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // 2. All other roles: Only drives in their college
        if (scope.collegeId() == null) {
            log.warn("User {} has no college, returning empty drive list", userId);
            return Collections.emptyList();
        }

        log.debug("Returning drives for college {}", scope.collegeId());
        return driveRepository.findByCollegeId(scope.collegeId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DriveDTO getDriveById(Long id) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate access
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                throw new AccessDeniedException("No access to this drive");
            }
        }

        return mapToDTO(drive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getUpcomingDrives() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        if (scope.isSuperAdmin()) {
            return driveRepository.findByDriveDateAfter(LocalDate.now()).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        if (scope.collegeId() == null) {
            return Collections.emptyList();
        }

        return driveRepository.findUpcomingDrivesByCollegeId(scope.collegeId(), LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getDrivesByCompany(Long companyId) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Filter by company AND college
        return driveRepository.findByCompanyId(companyId).stream()
                .filter(drive -> scope.isSuperAdmin() ||
                        (drive.getCollege() != null &&
                                scope.collegeId().equals(drive.getCollege().getId())))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Write Operations ====================

    @SuppressWarnings("null")
    @Override
    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public DriveDTO createDrive(CreateDriveRequestDTO request) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Only COORDINATOR and ADMIN can create drives
        if (!scope.isCoordinator() && !scope.isAdmin() && !scope.isSuperAdmin()) {
            throw new AccessDeniedException("Only coordinators and admins can create drives");
        }

        // Get college for the drive
        College college = null;
        if (scope.isSuperAdmin() && request.getCollegeId() != null) {
            college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"));
        } else if (scope.collegeId() != null) {
            // CRITICAL: Validate coordinator can only create drives for their college
            if (request.getCollegeId() != null && !request.getCollegeId().equals(scope.collegeId())) {
                log.warn("SECURITY: User {} attempted to create drive for different college. User college: {}, Requested: {}",
                        userId, scope.collegeId(), request.getCollegeId());
                throw new AccessDeniedException("Cannot create drive for a different college");
            }
            college = collegeRepository.findById(scope.collegeId())
                    .orElseThrow(() -> new ResourceNotFoundException("College not found"));
        } else {
            throw new IllegalStateException("Cannot create drive without college context");
        }

        PlacementDrive drive = new PlacementDrive();
        drive.setCollege(college);
        drive.setCompanyId(request.getCompanyId());
        drive.setTitle(request.getTitle());
        drive.setJobRole(request.getJobRole());
        drive.setDescription(request.getDescription());
        drive.setPackageLpa(request.getPackageLpa());
        drive.setLocation(request.getLocation());
        drive.setIsRemote(request.getIsRemote());

        drive.setMinCgpa(request.getMinCgpa());
        drive.setMaxBacklogs(request.getMaxBacklogs());

        if (request.getEligibleDepartments() != null && !request.getEligibleDepartments().isEmpty()) {
            java.util.Set<com.campusplacement.organizations.OrganizationUnit> depts = request.getEligibleDepartments()
                    .stream()
                    .map(deptId -> orgUnitRepository.findById(deptId)
                            .filter(d -> d.getType() == com.campusplacement.common.OrganizationUnitType.DEPARTMENT)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Department not found or invalid: " + deptId)))
                    .collect(Collectors.toSet());

            // Validate departments belong to the same college
            for (com.campusplacement.organizations.OrganizationUnit dept : depts) {
                if (dept.getCollege() == null || !dept.getCollege().getId().equals(college.getId())) {
                    throw new IllegalArgumentException(
                            "Department " + dept.getId() + " does not belong to college " + college.getId());
                }
            }
            drive.setEligibleDepartments(depts);
        }

        if (request.getRequiredSkills() != null) {
            drive.setRequiredSkills(String.join(",", request.getRequiredSkills()));
        }

        drive.setRegistrationDeadline(request.getRegistrationDeadline());
        drive.setDriveDate(request.getDriveDate());
        drive.setStatus("SCHEDULED");

        log.info("Creating drive '{}' for college {}", request.getTitle(), college.getId());
        PlacementDrive saved = driveRepository.save(drive);
        return mapToDTO(saved);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public DriveDTO updateDrive(Long id, DriveDTO driveDTO) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate access - coordinators can only update drives in their college
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                log.warn("SECURITY: User {} attempted to update drive {} from different college. User college: {}, Drive college: {}",
                        userId, id, scope.collegeId(), driveCollegeId);
                throw new AccessDeniedException("No access to update this drive");
            }
        }

        if (driveDTO.getTitle() != null)
            drive.setTitle(driveDTO.getTitle());
        if (driveDTO.getJobRole() != null)
            drive.setJobRole(driveDTO.getJobRole());
        if (driveDTO.getDescription() != null)
            drive.setDescription(driveDTO.getDescription());
        if (driveDTO.getPackageLpa() != null)
            drive.setPackageLpa(driveDTO.getPackageLpa());
        if (driveDTO.getLocation() != null)
            drive.setLocation(driveDTO.getLocation());
        if (driveDTO.getIsRemote() != null)
            drive.setIsRemote(driveDTO.getIsRemote());

        if (driveDTO.getMinCgpa() != null)
            drive.setMinCgpa(driveDTO.getMinCgpa());
        if (driveDTO.getMaxBacklogs() != null)
            drive.setMaxBacklogs(driveDTO.getMaxBacklogs());

        if (driveDTO.getEligibleDepartments() != null) {
            // If caller passes empty set, it means clear all departments
            java.util.Set<com.campusplacement.organizations.OrganizationUnit> depts = new java.util.HashSet<>();
            if (!driveDTO.getEligibleDepartments().isEmpty()) {
                depts = driveDTO.getEligibleDepartments().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(deptId -> orgUnitRepository.findById(deptId)
                                .filter(d -> d.getType() == com.campusplacement.common.OrganizationUnitType.DEPARTMENT)
                                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + deptId)))
                        .collect(Collectors.toSet());
            }
            drive.setEligibleDepartments(depts);
        }

        if (driveDTO.getRequiredSkills() != null) {
            drive.setRequiredSkills(String.join(",", driveDTO.getRequiredSkills()));
        }

        if (driveDTO.getRegistrationDeadline() != null)
            drive.setRegistrationDeadline(driveDTO.getRegistrationDeadline());
        if (driveDTO.getDriveDate() != null)
            drive.setDriveDate(driveDTO.getDriveDate());

        PlacementDrive updated = driveRepository.save(drive);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public DriveDTO updateDriveStatus(Long id, String status) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate access
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                log.warn("SECURITY: User {} attempted to update status for drive {} from different college",
                        userId, id);
                throw new AccessDeniedException("No access to update this drive");
            }
        }

        drive.setStatus(status);
        return mapToDTO(driveRepository.save(drive));
    }

    @SuppressWarnings("null")
    @Override
    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteDrive(Long id) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate access - only ADMIN and SUPER_ADMIN can delete
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                log.warn("SECURITY: User {} attempted to delete drive {} from different college",
                        userId, id);
                throw new AccessDeniedException("No access to delete this drive");
            }
        }

        driveRepository.deleteById(id);
    }

    // ==================== Helper Methods ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    private DriveDTO mapToDTO(PlacementDrive drive) {
        return DriveDTO.builder()
                .id(drive.getId())
                .companyId(drive.getCompanyId())
                .collegeId(drive.getCollege() != null ? drive.getCollege().getId() : null)
                .title(drive.getTitle())
                .jobRole(drive.getJobRole())
                .description(drive.getDescription())
                .packageLpa(drive.getPackageLpa())
                .location(drive.getLocation())
                .isRemote(drive.getIsRemote())
                .minCgpa(drive.getMinCgpa())
                .maxBacklogs(drive.getMaxBacklogs())
                .eligibleDepartments(drive.getEligibleDepartments() != null
                        ? drive.getEligibleDepartments().stream()
                                .map(com.campusplacement.organizations.OrganizationUnit::getId)
                                .collect(Collectors.toSet())
                        : java.util.Collections.emptySet())
                .requiredSkills(drive.getRequiredSkills() != null
                        ? java.util.Arrays.asList(drive.getRequiredSkills().split(","))
                        : java.util.Collections.emptyList())
                .registrationDeadline(drive.getRegistrationDeadline())
                .driveDate(drive.getDriveDate())
                .status(drive.getStatus())
                .build();
    }
}
