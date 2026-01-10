package com.campusplacement.ranking;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.ai.dto.RankingResponseDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.ranking.dto.RankingRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for student-drive ranking operations.
 *
 * <p>
 * <strong>Language Clarity:</strong> This provides "ranking" and
 * "similarity scoring" - never "matching" or "decision-making".
 * </p>
 *
 * <p>
 * <strong>Access Control:</strong>
 * </p>
 * <ul>
 * <li>COORDINATOR: Can rank students for drives within their scope</li>
 * <li>ADMIN: Can rank students for drives within their college</li>
 * <li>STUDENT: Can view their own ranking for a drive</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ranking & Similarity", description = "AI-assisted student-drive similarity ranking")
@SecurityRequirement(name = "bearerAuth")
public class RankingController {

    private final RankingService rankingService;

    /**
     * Rank eligible students for a drive.
     *
     * <p>
     * <strong>Important:</strong> This ranks already-eligible students.
     * Business eligibility rules are applied BEFORE ranking.
     * </p>
     *
     * @param driveId Drive ID to rank students for
     * @param request Optional filter criteria
     * @return Ranked list with similarity scores
     */
    @PostMapping("/drive/{driveId}/students")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    @Operation(summary = "Rank students for a drive", description = "Ranks eligible students by similarity to drive requirements. "
            +
            "Only includes students within coordinator's scope.")
    public ResponseEntity<ApiResponse<RankingResponseDTO>> rankStudentsForDrive(
            @PathVariable Long driveId,
            @Valid @RequestBody(required = false) RankingRequest request) {

        log.info("Ranking students for drive {}", driveId);

        RankingResponseDTO result = rankingService.rankStudentsForDrive(driveId, request);

        return ResponseEntity.ok(ApiResponse.success(result, "Students ranked successfully"));
    }

    /**
     * Get current student's ranking for a drive.
     *
     * @param driveId Drive ID
     * @return Student's ranking position and similarity score
     */
    @GetMapping("/drive/{driveId}/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my ranking for a drive", description = "Returns the current student's similarity ranking for a specific drive.")
    public ResponseEntity<ApiResponse<RankingResponseDTO.StudentRanking>> getMyRankingForDrive(
            @PathVariable Long driveId) {

        log.info("Getting student ranking for drive {}", driveId);

        RankingResponseDTO.StudentRanking ranking = rankingService.getStudentRankingForDrive(driveId);

        if (ranking == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No ranking available for this drive"));
        }

        return ResponseEntity.ok(ApiResponse.success(ranking, "Ranking retrieved"));
    }

    /**
     * Get recommended drives for the current student.
     *
     * @return List of drives ranked by similarity to student profile
     */
    @GetMapping("/drives/recommended")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get recommended drives", description = "Returns drives ranked by similarity to the student's profile.")
    public ResponseEntity<ApiResponse<List<DriveRecommendation>>> getRecommendedDrives() {
        log.info("Getting drive recommendations for current student");

        List<DriveRecommendation> recommendations = rankingService.getRecommendedDrivesForStudent();

        return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommendations retrieved"));
    }

    /**
     * Represents a drive recommendation for a student.
     */
    public record DriveRecommendation(
            Long driveId,
            String driveName,
            String companyName,
            Double similarityScore,
            List<String> matchedSkills,
            List<String> missingSkills,
            String reason) {
    }
}
