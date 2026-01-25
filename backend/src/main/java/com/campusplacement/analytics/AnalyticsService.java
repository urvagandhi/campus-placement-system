package com.campusplacement.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.analytics.dto.DepartmentStatsDTO;
import com.campusplacement.analytics.dto.InstituteStatsDTO;
import com.campusplacement.analytics.dto.PlacementStatsDTO;
import com.campusplacement.applications.Application;
import com.campusplacement.applications.ApplicationRepository;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.model.ScopeContext;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for calculating placement analytics with organization scope
 * enforcement and academic year filtering.
 *
 * <p>
 * <strong>Scope Enforcement:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: Statistics across all colleges</li>
 * <li>ADMIN at University/College level: College-wide stats with institute +
 * department breakdown</li>
 * <li>ADMIN/COORDINATOR at Institute level: Institute stats with department
 * breakdown</li>
 * <li>ADMIN/COORDINATOR at Department level: Only department stats</li>
 * <li>STUDENT: Not allowed (403)</li>
 * </ul>
 *
 * <p>
 * <strong>Academic Year:</strong> July 1 to June 30 (e.g., 2025-26 = July 1,
 * 2025 to June 30, 2026)
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ApplicationRepository applicationRepository;
    private final DriveRepository driveRepository;
    private final StudentRepository studentRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final OrganizationScopeService scopeService;

    // ==================== Public API ====================

    /**
     * Get overall placement statistics with scope enforcement.
     * Uses current academic year if not specified.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public PlacementStatsDTO getOverallStats() {
        return getOverallStats(null);
    }

    /**
     * Get overall placement statistics for a specific academic year with scope
     * enforcement.
     *
     * @param academicYear Academic year string (e.g., "2025-26"), null for current
     *                     year
     * @return PlacementStatsDTO with hierarchical breakdown based on user's scope
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public PlacementStatsDTO getOverallStats(String academicYear) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Determine academic year dates
        String year = academicYear != null ? academicYear : getCurrentAcademicYear();
        LocalDateTime startDate = getAcademicYearStartDate(year);
        LocalDateTime endDate = getAcademicYearEndDate(year);
        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        log.debug("Fetching placement stats for user {} with scope: {}, academic year: {}",
                userId, scope.role(), year);

        if (scope.isSuperAdmin()) {
            return calculateStatsForAllColleges(year, startDate, endDate, startLocalDate, endLocalDate);
        }

        // Check if user has full college/university level access
        // This is true for:
        // - ADMIN with SUBTREE scope at UNIVERSITY level (isUniversityScope = true)
        // - COORDINATOR with SUBTREE scope at UNIVERSITY level
        if (scope.isUniversityScope()) {
            return calculateStatsForCollege(scope.collegeId(), year, startDate, endDate, startLocalDate, endLocalDate);
        }

        // ADMIN or COORDINATOR with limited scope (Institute or Department level)
        // - Institute Admin: sees only their institute's departments
        // - Department Admin/Coordinator: sees only their assigned departments
        return calculateStatsForScopedUser(scope, year, startDate, endDate, startLocalDate, endLocalDate);
    }

    /**
     * Get department-wise placement statistics with scope enforcement.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public List<DepartmentStatsDTO> getDepartmentStats() {
        return getDepartmentStats(null);
    }

    /**
     * Get department-wise statistics for a specific academic year.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public List<DepartmentStatsDTO> getDepartmentStats(String academicYear) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        String year = academicYear != null ? academicYear : getCurrentAcademicYear();
        LocalDateTime startDate = getAcademicYearStartDate(year);
        LocalDateTime endDate = getAcademicYearEndDate(year);
        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        List<OrganizationUnit> departments;
        if (scope.isSuperAdmin()) {
            departments = organizationUnitRepository.findByType(OrganizationUnitType.DEPARTMENT);
        } else if (scope.hasFullCollegeAccess()) {
            departments = organizationUnitRepository.findByCollegeIdAndType(scope.collegeId(),
                    OrganizationUnitType.DEPARTMENT);
        } else {
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                return List.of();
            }
            departments = organizationUnitRepository.findAllById(scope.allowedDepartmentIds());
        }

        return departments.stream()
                .map(dept -> calculateDepartmentStats(dept, startDate, endDate, startLocalDate, endLocalDate))
                .collect(Collectors.toList());
    }

    /**
     * Get statistics for institutes (for college-level admins).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<InstituteStatsDTO> getInstituteStats() {
        return getInstituteStats(null);
    }

    /**
     * Get institute statistics for a specific academic year.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<InstituteStatsDTO> getInstituteStats(String academicYear) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        String year = academicYear != null ? academicYear : getCurrentAcademicYear();
        LocalDateTime startDate = getAcademicYearStartDate(year);
        LocalDateTime endDate = getAcademicYearEndDate(year);
        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        List<OrganizationUnit> institutes;
        if (scope.isSuperAdmin()) {
            institutes = organizationUnitRepository.findByType(OrganizationUnitType.INSTITUTE);
        } else if (scope.hasFullCollegeAccess()) {
            institutes = organizationUnitRepository.findByCollegeIdAndType(scope.collegeId(),
                    OrganizationUnitType.INSTITUTE);
        } else {
            // Coordinators can't access this endpoint
            return List.of();
        }

        return institutes.stream()
                .map(inst -> calculateInstituteStats(inst, startDate, endDate, startLocalDate, endLocalDate, true))
                .collect(Collectors.toList());
    }

    // ==================== Academic Year Helpers ====================

    /**
     * Get the current academic year string.
     * Academic year runs July to June.
     * Example: If current date is January 2026, returns "2025-26"
     */
    public String getCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        // July onwards = new academic year
        if (month >= 7) {
            return year + "-" + String.valueOf(year + 1).substring(2);
        } else {
            return (year - 1) + "-" + String.valueOf(year).substring(2);
        }
    }

    /**
     * Parse academic year string and return start date (July 1).
     */
    private LocalDateTime getAcademicYearStartDate(String academicYear) {
        int startYear = Integer.parseInt(academicYear.substring(0, 4));
        return LocalDateTime.of(startYear, Month.JULY, 1, 0, 0, 0);
    }

    /**
     * Parse academic year string and return end date (June 30 next year, end of
     * day).
     */
    private LocalDateTime getAcademicYearEndDate(String academicYear) {
        int startYear = Integer.parseInt(academicYear.substring(0, 4));
        return LocalDateTime.of(startYear + 1, Month.JULY, 1, 0, 0, 0);
    }

    // ==================== Scope-Based Calculation Methods ====================

    private PlacementStatsDTO calculateStatsForAllColleges(String academicYear,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate) {
        // Get all applications and aggregate
        List<Application> applications = applicationRepository.findAll().stream()
                .filter(a -> a.getAppliedAt() != null &&
                        !a.getAppliedAt().isBefore(startDate) &&
                        a.getAppliedAt().isBefore(endDate))
                .collect(Collectors.toList());

        long totalStudents = studentRepository.count();
        List<PlacementDrive> drives = driveRepository.findAll().stream()
                .filter(d -> d.getDriveDate() != null &&
                        !d.getDriveDate().isBefore(startLocalDate) &&
                        d.getDriveDate().isBefore(endLocalDate))
                .collect(Collectors.toList());

        return buildStatsDTO(applications, totalStudents, drives, academicYear, "SYSTEM", "All Colleges", null);
    }

    private PlacementStatsDTO calculateStatsForCollege(Long collegeId, String academicYear,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate) {

        List<Application> applications = applicationRepository.findByCollegeIdAndDateRange(
                collegeId, startDate, endDate);
        Long totalStudents = studentRepository.countByCollegeId(collegeId);
        List<PlacementDrive> drives = driveRepository.findByCollegeIdAndDateRange(
                collegeId, startLocalDate, endLocalDate);
        Long totalDrives = driveRepository.countByCollegeIdAndDateRange(collegeId, startLocalDate, endLocalDate);
        Long activeDrives = driveRepository.countActiveByCollegeIdAndDateRange(collegeId, startLocalDate, endLocalDate);

        // Get institute breakdown
        List<OrganizationUnit> institutes = organizationUnitRepository.findByCollegeIdAndType(
                collegeId, OrganizationUnitType.INSTITUTE);
        List<InstituteStatsDTO> instituteStats = institutes.stream()
                .map(inst -> calculateInstituteStats(inst, startDate, endDate, startLocalDate, endLocalDate, true))
                .collect(Collectors.toList());

        // Get department breakdown
        List<OrganizationUnit> departments = organizationUnitRepository.findByCollegeIdAndType(
                collegeId, OrganizationUnitType.DEPARTMENT);
        List<DepartmentStatsDTO> departmentStats = departments.stream()
                .map(dept -> calculateDepartmentStats(dept, startDate, endDate, startLocalDate, endLocalDate))
                .collect(Collectors.toList());

        PlacementStatsDTO stats = buildStatsDTO(applications, totalStudents != null ? totalStudents : 0L,
                drives, academicYear, "COLLEGE", "College", collegeId);
        stats.setTotalDrives(totalDrives != null ? totalDrives : 0L);
        stats.setActiveDrives(activeDrives != null ? activeDrives : 0L);
        stats.setInstituteStats(instituteStats);
        stats.setDepartmentStats(departmentStats);

        return stats;
    }

    private PlacementStatsDTO calculateStatsForScopedUser(ScopeContext scope, String academicYear,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate) {

        Set<Long> allowedDeptIds = scope.allowedDepartmentIds();
        Set<Long> allowedOrgUnitIds = scope.allowedOrgUnitIds();

        // If no department IDs but we have org unit IDs, check if any are institutes
        // This handles ADMIN assigned at INSTITUTE level with SUBTREE scope
        if ((allowedDeptIds == null || allowedDeptIds.isEmpty()) &&
                allowedOrgUnitIds != null && !allowedOrgUnitIds.isEmpty()) {

            log.debug("No department IDs found, checking org unit IDs for institute-level scope");

            // Find institutes in the allowed org units
            for (Long orgUnitId : allowedOrgUnitIds) {
                OrganizationUnit orgUnit = organizationUnitRepository.findById(orgUnitId).orElse(null);
                if (orgUnit != null && orgUnit.getType() == OrganizationUnitType.INSTITUTE) {
                    log.debug("Found institute {} in allowed org units, calculating institute stats",
                            orgUnit.getName());
                    return calculateStatsForInstitute(orgUnit, academicYear, startDate, endDate,
                            startLocalDate, endLocalDate);
                }
            }

            // Check if org unit is a single department
            for (Long orgUnitId : allowedOrgUnitIds) {
                OrganizationUnit orgUnit = organizationUnitRepository.findById(orgUnitId).orElse(null);
                if (orgUnit != null && orgUnit.getType() == OrganizationUnitType.DEPARTMENT) {
                    DepartmentStatsDTO deptStats = calculateDepartmentStats(orgUnit, startDate, endDate,
                            startLocalDate, endLocalDate);
                    return PlacementStatsDTO.builder()
                            .academicYear(academicYear)
                            .scopeLevel("DEPARTMENT")
                            .scopeName(orgUnit.getName())
                            .scopeId(orgUnit.getId())
                            .totalStudents(deptStats.getTotalStudents())
                            .placedStudents(deptStats.getPlacedStudents())
                            .placementRate(deptStats.getPlacementRate())
                            .averagePackage(deptStats.getAveragePackage())
                            .highestPackage(deptStats.getHighestPackage())
                            .lowestPackage(0.0)
                            .totalApplications(deptStats.getTotalApplications())
                            .totalDrives(deptStats.getTotalDrives())
                            .activeDrives(0L)
                            .companiesVisited(0L)
                            .departmentStats(List.of(deptStats))
                            .build();
                }
            }

            return buildEmptyStats(academicYear, "SCOPED", "No Access", null);
        }

        if (allowedDeptIds == null || allowedDeptIds.isEmpty()) {
            return buildEmptyStats(academicYear, "DEPARTMENT", "No Departments", null);
        }

        // Check if user has institute-level scope (multiple departments under same
        // institute) or just single department
        List<OrganizationUnit> departments = organizationUnitRepository.findAllById(allowedDeptIds);
        if (departments.isEmpty()) {
            return buildEmptyStats(academicYear, "DEPARTMENT", "No Departments", null);
        }

        // Check if all departments belong to the same institute
        Set<Long> instituteIds = departments.stream()
                .filter(d -> d.getParent() != null)
                .map(d -> d.getParent().getId())
                .collect(Collectors.toSet());

        if (instituteIds.size() == 1) {
            // Institute-level scope
            Long instituteId = instituteIds.iterator().next();
            OrganizationUnit institute = organizationUnitRepository.findById(instituteId).orElse(null);
            if (institute != null) {
                return calculateStatsForInstitute(institute, academicYear, startDate, endDate, startLocalDate,
                        endLocalDate);
            }
        }

        // Multiple institutes or no parent - aggregate department stats
        List<Application> applications = applicationRepository.findByDepartmentIdsAndDateRange(
                allowedDeptIds, startDate, endDate);
        Long totalStudents = studentRepository.countByDepartmentIdInAndCollegeId(allowedDeptIds, scope.collegeId());
        List<PlacementDrive> drives = driveRepository.findDistinctByEligibleDepartments_IdInAndCollegeId(
                allowedDeptIds, scope.collegeId());

        List<DepartmentStatsDTO> departmentStats = departments.stream()
                .map(dept -> calculateDepartmentStats(dept, startDate, endDate, startLocalDate, endLocalDate))
                .collect(Collectors.toList());

        PlacementStatsDTO stats = buildStatsDTO(applications, totalStudents != null ? totalStudents : 0L,
                drives, academicYear, "DEPARTMENT", "Scoped Departments", null);
        stats.setDepartmentStats(departmentStats);

        return stats;
    }

    private PlacementStatsDTO calculateStatsForInstitute(OrganizationUnit institute, String academicYear,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate) {

        InstituteStatsDTO instituteStats = calculateInstituteStats(institute, startDate, endDate,
                startLocalDate, endLocalDate, true);

        return PlacementStatsDTO.builder()
                .academicYear(academicYear)
                .scopeLevel("INSTITUTE")
                .scopeName(institute.getName())
                .scopeId(institute.getId())
                .totalStudents(instituteStats.getTotalStudents())
                .placedStudents(instituteStats.getPlacedStudents())
                .placementRate(instituteStats.getPlacementRate())
                .averagePackage(instituteStats.getAveragePackage())
                .highestPackage(instituteStats.getHighestPackage())
                .lowestPackage(0.0)
                .totalApplications(instituteStats.getTotalApplications())
                .totalDrives(instituteStats.getTotalDrives())
                .activeDrives(instituteStats.getActiveDrives())
                .companiesVisited(0L)
                .departmentStats(instituteStats.getDepartmentStats())
                .build();
    }

    // ==================== Entity Calculation Methods ====================

    private InstituteStatsDTO calculateInstituteStats(OrganizationUnit institute,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate,
            boolean includeDepartmentBreakdown) {

        Long instituteId = institute.getId();

        // Get applications from institute
        List<Application> applications = applicationRepository.findByInstituteIdAndDateRange(
                instituteId, startDate, endDate);

        // Student count
        Long totalStudents = studentRepository.countByInstituteId(instituteId);

        // Drive counts
        Long totalDrives = driveRepository.countByInstituteIdAndDateRange(instituteId, startLocalDate, endLocalDate);
        Long activeDrives = driveRepository.countActiveByInstituteIdAndDateRange(instituteId, startLocalDate,
                endLocalDate);

        // Calculate statistics
        long totalApplications = applications.size();
        long placedStudents = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .map(Application::getStudentId)
                .distinct()
                .count();

        double placementRate = (totalStudents != null && totalStudents > 0)
                ? (placedStudents * 100.0 / totalStudents)
                : 0.0;

        Double avgPackage = calculateAveragePackage(applications);
        Double highestPackage = calculateHighestPackage(applications);

        List<DepartmentStatsDTO> departmentStats = null;
        if (includeDepartmentBreakdown) {
            List<OrganizationUnit> departments = organizationUnitRepository
                    .findActiveDepartmentsByInstituteId(instituteId);
            departmentStats = departments.stream()
                    .map(dept -> calculateDepartmentStats(dept, startDate, endDate, startLocalDate, endLocalDate))
                    .collect(Collectors.toList());
        }

        return InstituteStatsDTO.builder()
                .instituteId(instituteId)
                .instituteName(institute.getName())
                .instituteCode(institute.getCode())
                .totalStudents(totalStudents != null ? totalStudents : 0L)
                .placedStudents(placedStudents)
                .placementRate(placementRate)
                .averagePackage(avgPackage)
                .highestPackage(highestPackage)
                .totalDrives(totalDrives != null ? totalDrives : 0L)
                .activeDrives(activeDrives != null ? activeDrives : 0L)
                .totalApplications(totalApplications)
                .departmentStats(departmentStats)
                .build();
    }

    private DepartmentStatsDTO calculateDepartmentStats(OrganizationUnit department,
            LocalDateTime startDate, LocalDateTime endDate,
            LocalDate startLocalDate, LocalDate endLocalDate) {

        Long deptId = department.getId();

        List<Application> applications = applicationRepository.findByDepartmentIdAndDateRange(
                deptId, startDate, endDate);

        Long totalStudents = studentRepository.countByDepartmentId(deptId);

        Long totalDrives = driveRepository.countByDepartmentIdAndDateRange(deptId, startLocalDate, endLocalDate);

        long totalApplications = applications.size();
        long placedStudents = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .map(Application::getStudentId)
                .distinct()
                .count();

        double placementRate = (totalStudents != null && totalStudents > 0)
                ? (placedStudents * 100.0 / totalStudents)
                : 0.0;

        // Get parent institute info
        Long instituteId = null;
        String instituteName = null;
        if (department.getParent() != null) {
            instituteId = department.getParent().getId();
            instituteName = department.getParent().getName();
        }

        return DepartmentStatsDTO.builder()
                .departmentId(deptId)
                .departmentName(department.getName())
                .departmentCode(department.getCode())
                .instituteId(instituteId)
                .instituteName(instituteName)
                .totalStudents(totalStudents != null ? totalStudents : 0L)
                .placedStudents(placedStudents)
                .placementRate(placementRate)
                .averagePackage(calculateAveragePackage(applications))
                .highestPackage(calculateHighestPackage(applications))
                .totalApplications(totalApplications)
                .totalDrives(totalDrives != null ? totalDrives : 0L)
                .build();
    }

    // ==================== DTO Builders ====================

    private PlacementStatsDTO buildStatsDTO(List<Application> applications, long totalStudents,
            List<PlacementDrive> drives, String academicYear, String scopeLevel, String scopeName, Long scopeId) {

        long totalApplications = applications.size();

        Map<String, Long> statusCounts = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        long placedStudents = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .map(Application::getStudentId)
                .distinct()
                .count();
        long shortlistedStudents = statusCounts.getOrDefault("SHORTLISTED", 0L);

        long totalDrives = drives.size();
        long activeDrives = drives.stream().filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()) ||
                "OPEN".equalsIgnoreCase(d.getStatus()) ||
                "UPCOMING".equalsIgnoreCase(d.getStatus())).count();

        double placementRate = totalStudents > 0 ? (placedStudents * 100.0 / totalStudents) : 0.0;

        return PlacementStatsDTO.builder()
                .academicYear(academicYear)
                .scopeLevel(scopeLevel)
                .scopeName(scopeName)
                .scopeId(scopeId)
                .totalStudents(totalStudents)
                .totalApplications(totalApplications)
                .placedStudents(placedStudents)
                .shortlistedStudents(shortlistedStudents)
                .placementRate(placementRate)
                .averagePackage(calculateAveragePackage(applications))
                .highestPackage(calculateHighestPackage(applications))
                .lowestPackage(calculateLowestPackage(applications))
                .companiesVisited(countCompanies(applications))
                .totalDrives(totalDrives)
                .activeDrives(activeDrives)
                .build();
    }

    private PlacementStatsDTO buildEmptyStats(String academicYear, String scopeLevel, String scopeName, Long scopeId) {
        return PlacementStatsDTO.builder()
                .academicYear(academicYear)
                .scopeLevel(scopeLevel)
                .scopeName(scopeName)
                .scopeId(scopeId)
                .totalStudents(0L)
                .totalApplications(0L)
                .placedStudents(0L)
                .shortlistedStudents(0L)
                .placementRate(0.0)
                .averagePackage(0.0)
                .highestPackage(0.0)
                .lowestPackage(0.0)
                .companiesVisited(0L)
                .totalDrives(0L)
                .activeDrives(0L)
                .instituteStats(new ArrayList<>())
                .departmentStats(new ArrayList<>())
                .build();
    }

    // ==================== Calculation Helpers ====================

    private Double calculateAveragePackage(List<Application> applications) {
        return applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getDrive() != null && a.getDrive().getPackageLpa() != null)
                .mapToDouble(a -> a.getDrive().getPackageLpa())
                .average()
                .orElse(0.0);
    }

    private Double calculateHighestPackage(List<Application> applications) {
        return applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getDrive() != null && a.getDrive().getPackageLpa() != null)
                .mapToDouble(a -> a.getDrive().getPackageLpa())
                .max()
                .orElse(0.0);
    }

    private Double calculateLowestPackage(List<Application> applications) {
        return applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getDrive() != null && a.getDrive().getPackageLpa() != null)
                .mapToDouble(a -> a.getDrive().getPackageLpa())
                .min()
                .orElse(0.0);
    }

    private Long countCompanies(List<Application> applications) {
        return applications.stream()
                .map(a -> a.getDrive() != null ? a.getDrive().getCompany() : null)
                .filter(company -> company != null)
                .map(company -> company.getId())
                .distinct()
                .count();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
