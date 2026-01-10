package com.campusplacement.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.campusplacement.ai.dto.AIExplanation;
import com.campusplacement.ai.dto.EligibilityRequestDTO;
import com.campusplacement.ai.dto.RankingRequestDTO;
import com.campusplacement.ai.dto.RankingResponseDTO;
import com.campusplacement.ai.dto.ResumeParseRequestDTO;
import com.campusplacement.ai.dto.ResumeParseResponseDTO;
import com.campusplacement.ai.dto.SkillGapRequestDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP client for communicating with the Python AI Service.
 *
 * <p>
 * The AI service provides decision support for:
 * </p>
 * <ul>
 * <li>Eligibility scoring</li>
 * <li>Skill gap analysis</li>
 * <li>Resume parsing and skill extraction</li>
 * <li>Student-drive similarity ranking</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> The AI module acts strictly as a decision-support
 * system and does not autonomously make placement decisions.
 * </p>
 *
 * <p>
 * <strong>Python Service Isolation:</strong> The Python AI service is kept
 * "dumb" - it does NOT know about roles, scope, college, or eligibility.
 * All filtering and access control is handled in Java.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AIClient {

    @Value("${ai-service.base-url}")
    private String aiServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== Resume Parsing ====================

    /**
     * Parses a resume and extracts structured data.
     *
     * @param request the resume parse request with text content
     * @return parsed resume data with extracted skills
     */
    @SuppressWarnings("null")
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackParseResume")
    @Retry(name = "aiService")
    public ResumeParseResponseDTO parseResume(ResumeParseRequestDTO request) {
        String url = aiServiceBaseUrl + "/resume/parse";
        log.debug("Calling AI service for resume parsing: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ResumeParseRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ResumeParseResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ResumeParseResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling AI service for resume parsing: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable for resume parsing", e);
        }
    }

    /**
     * Fallback for resume parsing when AI service is unavailable.
     */
    public ResumeParseResponseDTO fallbackParseResume(ResumeParseRequestDTO request, Exception e) {
        log.warn("AI resume parsing fallback triggered for student {}. Reason: {}",
                request.getStudentId(), e.getMessage());

        return ResumeParseResponseDTO.builder()
                .success(false)
                .studentId(request.getStudentId())
                .skills(List.of())
                .experienceLevel("UNKNOWN")
                .confidenceScore(0.0)
                .explanation(AIExplanation.builder()
                        .factors(List.of("AI service unavailable: " + e.getMessage()))
                        .breakdown(Map.of())
                        .humanReadable("Resume parsing is temporarily unavailable. Please enter skills manually.")
                        .build())
                .build();
    }

    // ==================== Ranking & Similarity ====================

    /**
     * Ranks students for a drive based on similarity scoring.
     *
     * <p>
     * <strong>Important:</strong> This provides RANKING only, not decisions.
     * Business eligibility must be checked before calling this method.
     * </p>
     *
     * @param request the ranking request with student IDs and requirements
     * @return ranked list with similarity scores and explanations
     */
    @SuppressWarnings("null")
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackRankCandidates")
    @Retry(name = "aiService")
    public RankingResponseDTO rankCandidates(RankingRequestDTO request) {
        String url = aiServiceBaseUrl + "/ranking/rank";
        log.debug("Calling AI service for ranking: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RankingRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<RankingResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, RankingResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling AI service for ranking: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable for ranking", e);
        }
    }

    /**
     * Fallback for ranking when AI service is unavailable.
     */
    public RankingResponseDTO fallbackRankCandidates(RankingRequestDTO request, Exception e) {
        log.warn("AI ranking fallback triggered for drive {}. Reason: {}",
                request.getDriveId(), e.getMessage());

        return RankingResponseDTO.builder()
                .success(false)
                .rankings(List.of())
                .explanation(AIExplanation.builder()
                        .factors(List.of("AI service unavailable: " + e.getMessage()))
                        .breakdown(Map.of())
                        .humanReadable("AI ranking is temporarily unavailable. Showing students in default order.")
                        .build())
                .build();
    }

    // ==================== Eligibility (Existing) ====================

    /**
     * Calculates eligibility score for a student against job requirements.
     *
     * @param request the eligibility request containing student and job data
     * @return AI response with eligibility score
     */
    @SuppressWarnings("null")
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackEligibilityScore")
    @Retry(name = "aiService")
    public AIResponseDTO calculateEligibilityScore(EligibilityRequestDTO request) {
        String url = aiServiceBaseUrl + "/eligibility/score";
        log.debug("Calling AI service for eligibility: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<EligibilityRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AIResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AIResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling AI service: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable", e);
        }
    }

    /**
     * Fallback for eligibility score when AI service is down or circuit is open.
     */
    public AIResponseDTO fallbackEligibilityScore(EligibilityRequestDTO request, Exception e) {
        log.warn("AI Service fallback triggered for student {}. Reason: {}", request.getStudentId(), e.getMessage());
        return AIResponseDTO.builder()
                .success(true) // Fallback succeeded
                .score(50.0) // Neutral score
                .isEligible(true) // Be permissive in fallback
                .reasons(java.util.Collections.singletonList("AI service call failed: " + e.getMessage()))
                .recommendations(java.util.Collections
                        .singletonList("Manual review recommended due to temporary AI service unavailability."))
                .build();
    }

    // ==================== Skill Gap (Existing) ====================

    /**
     * Analyzes skill gaps for a student.
     *
     * @param request the skill gap request
     * @return AI response with skill gap analysis
     */
    @SuppressWarnings("null")
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackAnalyzeSkillGaps")
    @Retry(name = "aiService")
    public AIResponseDTO analyzeSkillGaps(SkillGapRequestDTO request) {
        String url = aiServiceBaseUrl + "/skills/gap-analysis";
        log.debug("Calling AI service for skill gap analysis: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SkillGapRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AIResponseDTO> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AIResponseDTO.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling AI service for skill gap: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable for skill gap analysis", e);
        }
    }

    /**
     * Fallback for skill gap analysis when AI service is unavailable.
     */
    public AIResponseDTO fallbackAnalyzeSkillGaps(SkillGapRequestDTO request, Exception e) {
        log.warn("AI skill gap fallback triggered. Reason: {}", e.getMessage());
        return AIResponseDTO.builder()
                .success(false)
                .score(0.0)
                .isEligible(true)
                .reasons(java.util.Collections.singletonList("Skill gap analysis unavailable: " + e.getMessage()))
                .recommendations(java.util.Collections
                        .singletonList("Please try again later or consult with your placement coordinator."))
                .build();
    }

    // ==================== Health Check ====================

    /**
     * Health check for AI service.
     */
    public boolean isServiceHealthy() {
        try {
            String url = aiServiceBaseUrl.replace("/api/v1", "") + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
