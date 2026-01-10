package com.campusplacement.insights;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.insights.dto.AggregatedInsight;
import com.campusplacement.insights.dto.SkillTrendInsight;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for AI-generated insights.
 *
 * <p>
 * <strong>Aggregation Rules by Role:</strong>
 * </p>
 * <table>
 * <tr>
 * <td>STUDENT</td>
 * <td>Self-only insights</td>
 * </tr>
 * <tr>
 * <td>COORDINATOR</td>
 * <td>Aggregated stats (no student-level PII)</td>
 * </tr>
 * <tr>
 * <td>ADMIN</td>
 * <td>College-wide aggregates</td>
 * </tr>
 * <tr>
 * <td>SUPER_ADMIN</td>
 * <td>Platform-wide aggregates</td>
 * </tr>
 * </table>
 *
 * <p>
 * <strong>Critical:</strong> AI Insights must NEVER expose
 * student-level identifiable data unless role explicitly permits it.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Insights", description = "Aggregated AI-generated analytics and insights")
@SecurityRequirement(name = "bearerAuth")
public class AIInsightsController {

    private final AIInsightsService insightsService;

    // ==================== Student Insights (Self-Only) ====================

    /**
     * Get personalized insights for the current student.
     *
     * @return Student-specific career insights
     */
    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my career insights", description = "Returns personalized insights based on the student's profile and application history.")
    public ResponseEntity<ApiResponse<StudentInsights>> getMyInsights() {
        log.info("Getting insights for current student");

        StudentInsights insights = insightsService.getStudentInsights();

        return ResponseEntity.ok(ApiResponse.success(insights, "Insights retrieved"));
    }

    // ==================== Coordinator Insights (Aggregated) ====================

    /**
     * Get aggregated insights for drives in coordinator's scope.
     *
     * <p>
     * Returns aggregated statistics without student-level PII.
     * </p>
     *
     * @return Aggregated drive insights
     */
    @GetMapping("/coordinator/drives")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Get drive insights (aggregated)", description = "Returns aggregated insights for drives. No student-level data exposed.")
    public ResponseEntity<ApiResponse<List<AggregatedInsight>>> getDriveInsights() {
        log.info("Getting aggregated drive insights for coordinator");

        List<AggregatedInsight> insights = insightsService.getAggregatedDriveInsights();

        return ResponseEntity.ok(ApiResponse.success(insights, "Drive insights retrieved"));
    }

    /**
     * Get skill gap trends in coordinator's scope.
     *
     * @return Aggregated skill gap analysis
     */
    @GetMapping("/coordinator/skill-gaps")
    @PreAuthorize("hasRole('COORDINATOR')")
    @Operation(summary = "Get skill gap trends", description = "Returns aggregated skill gap patterns. Useful for training recommendations.")
    public ResponseEntity<ApiResponse<List<SkillTrendInsight>>> getSkillGapTrends() {
        log.info("Getting skill gap trends for coordinator");

        List<SkillTrendInsight> trends = insightsService.getSkillGapTrends();

        return ResponseEntity.ok(ApiResponse.success(trends, "Skill gap trends retrieved"));
    }

    // ==================== Admin Insights (College-Wide) ====================

    /**
     * Get college-wide placement insights.
     *
     * @return College-wide aggregated statistics
     */
    @GetMapping("/admin/college")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get college-wide insights", description = "Returns aggregated placement insights for the entire college.")
    public ResponseEntity<ApiResponse<CollegeInsights>> getCollegeInsights() {
        log.info("Getting college-wide insights for admin");

        CollegeInsights insights = insightsService.getCollegeInsights();

        return ResponseEntity.ok(ApiResponse.success(insights, "College insights retrieved"));
    }

    // ==================== Super Admin Insights (Platform-Wide)
    // ====================

    /**
     * Get platform-wide insights.
     *
     * @return Platform-wide aggregated statistics
     */
    @GetMapping("/platform")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get platform-wide insights", description = "Returns aggregated insights across all colleges.")
    public ResponseEntity<ApiResponse<PlatformInsights>> getPlatformInsights() {
        log.info("Getting platform-wide insights for super admin");

        PlatformInsights insights = insightsService.getPlatformInsights();

        return ResponseEntity.ok(ApiResponse.success(insights, "Platform insights retrieved"));
    }

    // ==================== Response Records ====================

    /**
     * Student-specific insights (self-only).
     */
    public record StudentInsights(
            Double overallReadiness,
            List<String> strongSkills,
            List<String> skillsToImprove,
            List<String> recommendedCertifications,
            String careerAdvice) {
    }

    /**
     * College-wide insights (admin only).
     */
    public record CollegeInsights(
            Integer totalStudents,
            Integer placedCount,
            Double placementRate,
            Double avgPackageLpa,
            List<String> topSkillsInDemand,
            List<String> mostCommonSkillGaps) {
    }

    /**
     * Platform-wide insights (super admin only).
     */
    public record PlatformInsights(
            Integer totalColleges,
            Integer totalStudents,
            Integer totalDrives,
            Double overallPlacementRate,
            List<String> topPerformingColleges,
            List<String> emergingSkillTrends) {
    }
}
