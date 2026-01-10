package com.campusplacement.analytics;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.analytics.dto.CompanyStatsDTO;
import com.campusplacement.analytics.dto.DepartmentStatsDTO;
import com.campusplacement.analytics.dto.PlacementStatsDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for placement analytics.
 *
 * <p>
 * Provides scope-enforced analytics endpoints for placement statistics,
 * department stats, batch analysis, and company-wise performance.
 * </p>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get overall placement statistics.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PlacementStatsDTO>> getOverallStats() {
        PlacementStatsDTO stats = analyticsService.getOverallStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get department-wise statistics.
     */
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentStatsDTO>>> getDepartmentStats() {
        List<DepartmentStatsDTO> stats = analyticsService.getDepartmentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get statistics for a specific batch year.
     */
    @GetMapping("/batch/{year}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<PlacementStatsDTO>> getBatchStats(
            @PathVariable Integer year) {
        PlacementStatsDTO stats = analyticsService.getBatchStats(year);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get company-wise placement statistics.
     * Shows hiring metrics per company: drives, applications, selections, rates.
     */
    @GetMapping("/companies")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CompanyStatsDTO>>> getCompanyStats() {
        List<CompanyStatsDTO> stats = analyticsService.getCompanyStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
