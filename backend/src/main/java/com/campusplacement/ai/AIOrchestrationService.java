package com.campusplacement.ai;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.ai.dto.AIExplanation;
import com.campusplacement.ai.dto.RankingRequestDTO;
import com.campusplacement.ai.dto.RankingResponseDTO;
import com.campusplacement.ai.dto.ResumeParseRequestDTO;
import com.campusplacement.ai.dto.ResumeParseResponseDTO;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.model.ScopeContext;
import com.campusplacement.security.CustomUserDetails;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * Central orchestration service for all AI operations.
 *
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Coordinate AI service calls with proper fallbacks</li>
 * <li>Validate scope before any AI operation</li>
 * <li>Log all AI interactions for audit</li>
 * <li>Collect metrics for monitoring</li>
 * </ul>
 *
 * <p>
 * <strong>Design Principle:</strong> Rule-first, AI-second.
 * Business rules are always enforced before AI scoring.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AIOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(AIOrchestrationService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("AI_AUDIT");

    private final AIClient aiClient;
    private final OrganizationScopeService scopeService;
    private final AIResultPersistence resultPersistence;
    private final MeterRegistry meterRegistry;

    // ==================== Resume Parsing ====================

    /**
     * Parses a resume and extracts structured data.
     *
     * <p>
     * <strong>Access Control:</strong> Student can parse own resume only.
     * Coordinators/Admins cannot parse student resumes directly.
     * </p>
     *
     * @param studentId  ID of the student whose resume is being parsed
     * @param resumeText Raw text content of the resume
     * @return Parsed resume data with extracted skills and experience
     * @throws AccessDeniedException if caller is not the student owner
     */
    @Transactional
    public ResumeParseResponseDTO parseResume(Long studentId, String resumeText) {
        Timer.Sample timer = Timer.start(meterRegistry);
        String operation = "resume_parse";

        try {
            // Access control: only student can parse own resume
            Long currentUserId = getCurrentUserId();
            if (!currentUserId.equals(studentId)) {
                auditLog.warn("AI_ACCESS_DENIED: User {} attempted to parse resume for student {}",
                        currentUserId, studentId);
                throw new AccessDeniedException("Cannot parse another student's resume");
            }

            // Scope validation
            ScopeContext scope = scopeService.resolveScope(currentUserId);
            logAuditEntry(operation, studentId, null, scope);

            // Check cache first
            Optional<ResumeParseResponseDTO> cached = resultPersistence.getCachedResumeParse(
                    scope.collegeId(), studentId);
            if (cached.isPresent()) {
                incrementCounter(operation, "cache_hit");
                return cached.get();
            }

            // Call AI service with fallback
            ResumeParseRequestDTO request = ResumeParseRequestDTO.builder()
                    .studentId(studentId)
                    .resumeText(resumeText)
                    .build();

            ResumeParseResponseDTO response = aiClient.parseResume(request);

            // Cache result with scope keys
            resultPersistence.cacheResumeParse(scope.collegeId(), studentId, response);

            incrementCounter(operation, "success");
            return response;

        } catch (AccessDeniedException e) {
            incrementCounter(operation, "access_denied");
            throw e;
        } catch (Exception e) {
            log.error("Resume parsing failed for student {}: {}", studentId, e.getMessage());
            incrementCounter(operation, "fallback");
            return createFallbackResumeResponse(studentId);
        } finally {
            timer.stop(Timer.builder("ai.orchestration.time")
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }

    // ==================== Ranking & Similarity ====================

    /**
     * Ranks eligible students for a drive based on similarity scoring.
     *
     * <p>
     * <strong>Important:</strong> This is RANKING only, not decision-making.
     * Business eligibility must be checked BEFORE calling this method.
     * </p>
     *
     * @param driveId            ID of the placement drive
     * @param eligibleStudentIds List of already-eligible student IDs
     * @return Ranked list with similarity scores and explanations
     */
    @Transactional(readOnly = true)
    public RankingResponseDTO rankStudentsForDrive(Long driveId, List<Long> eligibleStudentIds) {
        Timer.Sample timer = Timer.start(meterRegistry);
        String operation = "student_ranking";

        try {
            Long currentUserId = getCurrentUserId();
            ScopeContext scope = scopeService.resolveScope(currentUserId);

            // Validate coordinator has access to this drive's college
            validateDriveAccess(driveId, scope);

            logAuditEntry(operation, null, driveId, scope);

            // Build request - only send sanitized data to AI
            RankingRequestDTO request = RankingRequestDTO.builder()
                    .driveId(driveId)
                    .studentIds(eligibleStudentIds)
                    .build();

            RankingResponseDTO response = aiClient.rankCandidates(request);

            incrementCounter(operation, "success");
            return response;

        } catch (AccessDeniedException e) {
            incrementCounter(operation, "access_denied");
            throw e;
        } catch (Exception e) {
            log.error("Ranking failed for drive {}: {}", driveId, e.getMessage());
            incrementCounter(operation, "fallback");
            return createFallbackRankingResponse(eligibleStudentIds);
        } finally {
            timer.stop(Timer.builder("ai.orchestration.time")
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }

    // ==================== Health Check ====================

    /**
     * Checks if the AI service is available.
     *
     * @return true if AI service is healthy
     */
    public boolean isAIServiceHealthy() {
        return aiClient.isServiceHealthy();
    }

    // ==================== Private Helpers ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    private void validateDriveAccess(Long driveId, ScopeContext scope) {
        // Drive access validation will be implemented when integrating with
        // DriveService
        // For now, scope is validated at the service layer
        if (!scope.isSuperAdmin() && scope.collegeId() == null) {
            throw new AccessDeniedException("No college scope for drive access");
        }
    }

    private void logAuditEntry(String operation, Long studentId, Long driveId, ScopeContext scope) {
        auditLog.info("AI_OPERATION: op={}, userId={}, studentId={}, driveId={}, collegeId={}, scope={}",
                operation,
                scope.userId(),
                studentId,
                driveId,
                scope.collegeId(),
                scope.isUniversityScope() ? "UNIVERSITY" : "SCOPED");
    }

    private void incrementCounter(String operation, String result) {
        Counter.builder("ai.orchestration.count")
                .tag("operation", operation)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    private ResumeParseResponseDTO createFallbackResumeResponse(Long studentId) {
        return ResumeParseResponseDTO.builder()
                .success(false)
                .studentId(studentId)
                .skills(List.of())
                .experienceLevel("UNKNOWN")
                .confidenceScore(0.0)
                .explanation(AIExplanation.builder()
                        .factors(List.of("AI service unavailable"))
                        .breakdown(Map.of())
                        .humanReadable(
                                "Resume parsing is temporarily unavailable. Please try again later or enter skills manually.")
                        .build())
                .build();
    }

    private RankingResponseDTO createFallbackRankingResponse(List<Long> studentIds) {
        return RankingResponseDTO.builder()
                .success(false)
                .rankings(List.of())
                .explanation(AIExplanation.builder()
                        .factors(List.of("AI service unavailable"))
                        .breakdown(Map.of())
                        .humanReadable("AI ranking is temporarily unavailable. Showing students in default order.")
                        .build())
                .build();
    }
}
