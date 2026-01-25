package com.campusplacement.analytics;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.analytics.dto.DepartmentStatsDTO;
import com.campusplacement.analytics.dto.InstituteStatsDTO;
import com.campusplacement.analytics.dto.PlacementStatsDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for placement analytics.
 *
 * <p>
 * Provides scope-enforced analytics endpoints for placement statistics,
 * department stats, institute stats, and academic year filtering.
 * </p>
 *
 * <p>
 * <strong>Scope Behavior:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: All colleges statistics</li>
 * <li>ADMIN: College-wide stats with institute/department breakdown</li>
 * <li>COORDINATOR (Institute level): Institute stats with department
 * breakdown</li>
 * <li>COORDINATOR (Department level): Department stats only</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get overall placement statistics with optional academic year filter.
     *
     * @param year Academic year in format "2025-26" (optional, defaults to current
     *             year)
     * @return PlacementStatsDTO with hierarchical breakdown based on user scope
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PlacementStatsDTO>> getOverallStats(
            @RequestParam(required = false) String year) {
        PlacementStatsDTO stats = analyticsService.getOverallStats(year);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get department-wise statistics with optional academic year filter.
     *
     * @param year Academic year in format "2025-26" (optional, defaults to current
     *             year)
     * @return List of department statistics visible to current user
     */
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentStatsDTO>>> getDepartmentStats(
            @RequestParam(required = false) String year) {
        List<DepartmentStatsDTO> stats = analyticsService.getDepartmentStats(year);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get institute-wise statistics with optional academic year filter.
     * Only available to ADMIN and SUPER_ADMIN roles.
     *
     * @param year Academic year in format "2025-26" (optional, defaults to current
     *             year)
     * @return List of institute statistics with department breakdowns
     */
    @GetMapping("/institutes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<InstituteStatsDTO>>> getInstituteStats(
            @RequestParam(required = false) String year) {
        List<InstituteStatsDTO> stats = analyticsService.getInstituteStats(year);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get the current academic year string.
     *
     * @return Current academic year in format "2025-26"
     */
    @GetMapping("/current-year")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> getCurrentAcademicYear() {
        String year = analyticsService.getCurrentAcademicYear();
        return ResponseEntity.ok(ApiResponse.success(year));
    }
}
