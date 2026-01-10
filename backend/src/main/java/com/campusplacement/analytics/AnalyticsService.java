package com.campusplacement.analytics;

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

import com.campusplacement.analytics.dto.CompanyStatsDTO;
import com.campusplacement.analytics.dto.DepartmentStatsDTO;
import com.campusplacement.analytics.dto.PlacementStatsDTO;
import com.campusplacement.applications.Application;
import com.campusplacement.applications.ApplicationRepository;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.organizations.OrganizationUnitRepository;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for calculating placement analytics with organization scope
 * enforcement.
 *
 * <p>
 * <strong>Scope Enforcement:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: Statistics across all colleges</li>
 * <li>ADMIN: Statistics for their entire college</li>
 * <li>COORDINATOR: Statistics only for their allowed departments</li>
 * <li>STUDENT: Not allowed (403)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final OrganizationScopeService scopeService;

    /**
     * Get overall placement statistics with scope enforcement.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public PlacementStatsDTO getOverallStats() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        log.debug("Fetching placement stats for user {} with scope: {}", userId, scope.role());

        if (scope.isSuperAdmin()) {
            return calculateStatsForAllColleges();
        }

        if (scope.hasFullCollegeAccess()) {
            return calculateStatsForCollege(scope.collegeId());
        }

        // Coordinator with limited scope
        return calculateStatsForDepartments(scope.allowedDepartmentIds(), scope.collegeId());
    }

    /**
     * Get department-wise placement statistics with scope enforcement.
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public List<DepartmentStatsDTO> getDepartmentStats() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        log.debug("Fetching department stats for user {} with scope: {}", userId, scope.role());

        List<OrganizationUnit> departments;
        if (scope.isSuperAdmin()) {
            departments = organizationUnitRepository.findByType(OrganizationUnitType.DEPARTMENT);
        } else if (scope.hasFullCollegeAccess()) {
            departments = organizationUnitRepository.findByCollegeIdAndType(scope.collegeId(),
                    OrganizationUnitType.DEPARTMENT);
        } else {
            // Only allowed departments
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                return List.of();
            }
            departments = organizationUnitRepository.findAllById(scope.allowedDepartmentIds());
        }

        return departments.stream()
                .map(this::calculateDepartmentStats)
                .collect(Collectors.toList());
    }

    /**
     * Get statistics for a specific batch year with scope enforcement.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'COORDINATOR')")
    public PlacementStatsDTO getBatchStats(Integer year) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        log.debug("Fetching batch {} stats for user {}", year, userId);

        List<Application> applications;
        if (scope.isSuperAdmin()) {
            // All applications for the year
            applications = applicationRepository.findAll().stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().getYear() == year)
                    .collect(Collectors.toList());
        } else if (scope.hasFullCollegeAccess()) {
            applications = applicationRepository.findByCollegeIdAndYear(scope.collegeId(), year);
        } else {
            // Scoped departments
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                return buildEmptyStats();
            }
            applications = applicationRepository.findByDepartmentIdsAndYear(scope.allowedDepartmentIds(), year);
        }

        return aggregateStats(applications, scope);
    }

    /**
     * Get company-wise placement statistics with scope enforcement.
     *
     * @return List of company statistics visible to the current user
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public List<CompanyStatsDTO> getCompanyStats() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        log.debug("Fetching company stats for user {} with scope: {}", userId, scope.role());

        List<Application> applications;
        if (scope.isSuperAdmin()) {
            applications = applicationRepository.findAll();
        } else if (scope.hasFullCollegeAccess()) {
            applications = applicationRepository.findByCollegeId(scope.collegeId());
        } else {
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                return List.of();
            }
            applications = applicationRepository.findByStudentDepartmentIdInAndCollegeId(
                    scope.allowedDepartmentIds(), scope.collegeId());
        }

        // Group applications by company
        Map<Long, List<Application>> appsByCompany = applications.stream()
                .filter(a -> a.getDrive() != null && a.getDrive().getCompany() != null)
                .collect(Collectors.groupingBy(a -> a.getDrive().getCompany().getId()));

        return appsByCompany.entrySet().stream()
                .map(entry -> calculateCompanyStats(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getSelectedCandidates(), a.getSelectedCandidates()))
                .collect(Collectors.toList());
    }

    private CompanyStatsDTO calculateCompanyStats(Long companyId, List<Application> applications) {
        if (applications.isEmpty()) {
            return CompanyStatsDTO.builder()
                    .companyId(companyId)
                    .companyName("Unknown")
                    .build();
        }

        // Get company info from first application
        var company = applications.get(0).getDrive().getCompany();

        // Count distinct drives
        long totalDrives = applications.stream()
                .map(a -> a.getDrive().getId())
                .distinct()
                .count();

        long totalApplications = applications.size();

        long selectedCandidates = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .count();

        double selectionRate = totalApplications > 0
                ? (selectedCandidates * 100.0 / totalApplications)
                : 0.0;

        // Calculate packages from selected applications
        List<Application> selectedApps = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getDrive().getPackageLpa() != null)
                .collect(Collectors.toList());

        Double avgPackage = selectedApps.isEmpty() ? 0.0
                : selectedApps.stream()
                        .mapToDouble(a -> a.getDrive().getPackageLpa())
                        .average()
                        .orElse(0.0);

        Double highestPackage = selectedApps.isEmpty() ? 0.0
                : selectedApps.stream()
                        .mapToDouble(a -> a.getDrive().getPackageLpa())
                        .max()
                        .orElse(0.0);

        return CompanyStatsDTO.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .industry(company.getIndustry())
                .totalDrives(totalDrives)
                .totalApplications(totalApplications)
                .selectedCandidates(selectedCandidates)
                .selectionRate(selectionRate)
                .averagePackage(avgPackage)
                .highestPackage(highestPackage)
                .build();
    }

    // ==================== Private Helper Methods ====================

    private PlacementStatsDTO calculateStatsForAllColleges() {
        List<Application> allApplications = applicationRepository.findAll();
        List<StudentProfile> allStudents = studentRepository.findAll();

        return buildStatsDTO(allApplications, allStudents);
    }

    private PlacementStatsDTO calculateStatsForCollege(Long collegeId) {
        List<Application> applications = applicationRepository.findByCollegeId(collegeId);
        List<StudentProfile> students = studentRepository.findByCollegeId(collegeId);

        return buildStatsDTO(applications, students);
    }

    private PlacementStatsDTO calculateStatsForDepartments(Set<Long> departmentIds, Long collegeId) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return buildEmptyStats();
        }

        List<Application> applications = applicationRepository.findByStudentDepartmentIdInAndCollegeId(
                departmentIds, collegeId);
        List<StudentProfile> students = studentRepository.findByDepartmentIdInAndCollegeId(
                departmentIds, collegeId);

        return buildStatsDTO(applications, students);
    }

    private DepartmentStatsDTO calculateDepartmentStats(OrganizationUnit department) {
        List<Long> deptIds = List.of(department.getId());
        List<StudentProfile> students = studentRepository.findByDepartmentIdInAndCollegeId(
                Set.copyOf(deptIds), department.getCollege().getId());

        List<Application> applications = applicationRepository.findByStudentDepartmentIdInAndCollegeId(
                Set.copyOf(deptIds), department.getCollege().getId());

        long placed = applications.stream()
                .filter(a -> "SELECTED".equalsIgnoreCase(a.getStatus()))
                .count();

        return DepartmentStatsDTO.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .totalStudents((long) students.size())
                .placedStudents(placed)
                .placementRate(students.isEmpty() ? 0.0 : (placed * 100.0 / students.size()))
                .averagePackage(calculateAveragePackage(applications))
                .highestPackage(calculateHighestPackage(applications))
                .build();
    }

    private PlacementStatsDTO buildStatsDTO(List<Application> applications, List<StudentProfile> students) {
        long totalStudents = students.size();
        long totalApplications = applications.size();

        Map<String, Long> statusCounts = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        long placedStudents = statusCounts.getOrDefault("SELECTED", 0L);
        long shortlistedStudents = statusCounts.getOrDefault("SHORTLISTED", 0L);

        double placementRate = totalStudents > 0 ? (placedStudents * 100.0 / totalStudents) : 0.0;

        return PlacementStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalApplications(totalApplications)
                .placedStudents(placedStudents)
                .shortlistedStudents(shortlistedStudents)
                .placementRate(placementRate)
                .averagePackage(calculateAveragePackage(applications))
                .highestPackage(calculateHighestPackage(applications))
                .lowestPackage(calculateLowestPackage(applications))
                .companiesVisited(countCompanies(applications))
                .build();
    }

    private PlacementStatsDTO buildEmptyStats() {
        return PlacementStatsDTO.builder()
                .totalStudents(0L)
                .totalApplications(0L)
                .placedStudents(0L)
                .shortlistedStudents(0L)
                .placementRate(0.0)
                .averagePackage(0.0)
                .highestPackage(0.0)
                .lowestPackage(0.0)
                .companiesVisited(0L)
                .build();
    }

    private PlacementStatsDTO aggregateStats(List<Application> applications, ScopeContext scope) {
        // Count unique students from applications (for reference)
        Set<Long> uniqueStudentIds = applications.stream()
                .map(a -> a.getStudent().getId())
                .collect(Collectors.toSet());

        // Calculate total students based on scope
        long totalStudents;
        if (scope.isSuperAdmin()) {
            totalStudents = studentRepository.count();
        } else if (scope.hasFullCollegeAccess()) {
            totalStudents = studentRepository.countByCollegeId(scope.collegeId());
        } else {
            if (scope.allowedDepartmentIds() == null || scope.allowedDepartmentIds().isEmpty()) {
                totalStudents = 0;
            } else {
                totalStudents = studentRepository.countByDepartmentIdInAndCollegeId(
                        scope.allowedDepartmentIds(), scope.collegeId());
            }
        }

        // Build stats using calculated totalStudents
        long totalApplications = applications.size();

        Map<String, Long> statusCounts = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        long placedStudents = statusCounts.getOrDefault("SELECTED", 0L);
        long shortlistedStudents = statusCounts.getOrDefault("SHORTLISTED", 0L);

        double placementRate = totalStudents > 0 ? (placedStudents * 100.0 / totalStudents) : 0.0;

        log.debug("Aggregated stats: {} total students, {} unique applicants, {} placed",
                totalStudents, uniqueStudentIds.size(), placedStudents);

        return PlacementStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalApplications(totalApplications)
                .placedStudents(placedStudents)
                .shortlistedStudents(shortlistedStudents)
                .placementRate(placementRate)
                .averagePackage(calculateAveragePackage(applications))
                .highestPackage(calculateHighestPackage(applications))
                .lowestPackage(calculateLowestPackage(applications))
                .companiesVisited(countCompanies(applications))
                .build();
    }

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
