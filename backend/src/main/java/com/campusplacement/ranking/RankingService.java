package com.campusplacement.ranking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.ai.AIOrchestrationService;
import com.campusplacement.ai.dto.RankingResponseDTO;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.eligibility.DriveEligibilityService;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.ranking.dto.RankingRequest;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for student-drive ranking operations.
 *
 * <p>
 * <strong>Design Principle:</strong> Rule-first, AI-second.
 * Business eligibility is always checked before AI ranking.
 * </p>
 *
 * <p>
 * <strong>Scope Enforcement:</strong> All ranking respects
 * organization scope. Coordinators only see students in their scope.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);

    private final AIOrchestrationService aiOrchestrationService;
    private final DriveEligibilityService eligibilityService;
    private final OrganizationScopeService scopeService;
    private final DriveRepository driveRepository;
    private final StudentRepository studentRepository;

    /**
     * Ranks eligible students for a drive.
     *
     * <p>
     * <strong>Important:</strong> Only eligible students are ranked.
     * Ineligible students are filtered out BEFORE AI ranking.
     * </p>
     *
     * @param driveId Drive ID
     * @param request Optional filter criteria
     * @return Ranked list with similarity scores
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public RankingResponseDTO rankStudentsForDrive(Long driveId, RankingRequest request) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Validate drive access
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        validateDriveAccess(drive, scope);

        // Get eligible students (business rules FIRST)
        List<StudentProfile> eligibleStudents = eligibilityService.getEligibleStudentsOptimized(driveId);

        if (eligibleStudents.isEmpty()) {
            log.info("No eligible students found for drive {}", driveId);
            return RankingResponseDTO.builder()
                    .success(true)
                    .rankings(List.of())
                    .build();
        }

        // Apply optional filters
        List<Long> studentIds = eligibleStudents.stream()
                .map(StudentProfile::getId)
                .collect(Collectors.toList());

        if (request != null && request.getStudentIds() != null) {
            studentIds.retainAll(request.getStudentIds());
        }

        if (request != null && request.getLimit() != null) {
            studentIds = studentIds.subList(0, Math.min(request.getLimit(), studentIds.size()));
        }

        // AI ranking (ranking only, not decision-making)
        return aiOrchestrationService.rankStudentsForDrive(driveId, studentIds);
    }

    /**
     * Gets the current student's ranking for a drive.
     *
     * @param driveId Drive ID
     * @return Student's ranking or null if not ranked
     */
    @Transactional(readOnly = true)
    public RankingResponseDTO.StudentRanking getStudentRankingForDrive(Long driveId) {
        Long userId = getCurrentUserId();

        // Get student profile
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Check if eligible first
        var eligibilityResult = eligibilityService.checkEligibility(student.getId(), driveId);

        if (!eligibilityResult.isEligible()) {
            return null; // Not eligible, no ranking
        }

        // Get ranking for just this student
        RankingResponseDTO result = aiOrchestrationService.rankStudentsForDrive(
                driveId, List.of(student.getId()));

        if (result.getRankings() != null && !result.getRankings().isEmpty()) {
            return result.getRankings().get(0);
        }

        return null;
    }

    /**
     * Gets recommended drives for the current student.
     *
     * @return List of drive recommendations
     */
    @Transactional(readOnly = true)
    public List<RankingController.DriveRecommendation> getRecommendedDrivesForStudent() {
        Long userId = getCurrentUserId();

        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        ScopeContext scope = scopeService.resolveScope(userId);

        // Get active drives in student's college
        List<PlacementDrive> activeDrives = driveRepository.findActiveByCollegeId(scope.collegeId());

        List<RankingController.DriveRecommendation> recommendations = new ArrayList<>();

        for (PlacementDrive drive : activeDrives) {
            // Check eligibility first
            var eligibility = eligibilityService.checkEligibility(student.getId(), drive.getId());

            if (eligibility.isEligible()) {
                // Get similarity ranking
                var ranking = aiOrchestrationService.rankStudentsForDrive(
                        drive.getId(), List.of(student.getId()));

                if (ranking.getRankings() != null && !ranking.getRankings().isEmpty()) {
                    var studentRanking = ranking.getRankings().get(0);

                    recommendations.add(new RankingController.DriveRecommendation(
                            drive.getId(),
                            drive.getTitle(),
                            drive.getCompany() != null ? drive.getCompany().getName() : "Unknown",
                            studentRanking.getSimilarityScore(),
                            studentRanking.getMatchedSkills(),
                            studentRanking.getMissingSkills(),
                            studentRanking.getReason()));
                }
            }
        }

        // Sort by similarity score descending
        recommendations.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));

        return recommendations;
    }

    // ==================== Private Helpers ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    private void validateDriveAccess(PlacementDrive drive, ScopeContext scope) {
        if (scope.isSuperAdmin()) {
            return; // Super admin has access to all
        }

        Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;

        if (!scope.collegeId().equals(driveCollegeId)) {
            throw new AccessDeniedException("No access to this drive");
        }
    }
}
