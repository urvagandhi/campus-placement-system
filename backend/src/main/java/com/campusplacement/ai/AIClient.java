package com.campusplacement.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.campusplacement.ai.dto.EligibilityRequestDTO;
import com.campusplacement.ai.dto.SkillGapRequestDTO;

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
 * <li>Career insights</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> The AI module acts strictly as a decision-support
 * system
 * and does not autonomously make placement decisions.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AIClient {

    @Value("${ai-service.base-url}")
    private String aiServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Calculates eligibility score for a student against job requirements.
     *
     * @param request the eligibility request containing student and job data
     * @return AI response with eligibility score
     */
    @SuppressWarnings("null")
    public AIResponseDTO calculateEligibilityScore(EligibilityRequestDTO request) {
        // TODO: Implement HTTP call to AI service
        // POST {aiServiceBaseUrl}/eligibility/score

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
     * Analyzes skill gaps for a student.
     *
     * @param request the skill gap request
     * @return AI response with skill gap analysis
     */
    public AIResponseDTO analyzeSkillGaps(SkillGapRequestDTO request) {
        // TODO: Implement HTTP call to AI service
        // POST {aiServiceBaseUrl}/skills/gap-analysis

        String url = aiServiceBaseUrl + "/skills/gap-analysis";
        log.debug("Calling AI service for skill gap analysis: {}", url);

        throw new UnsupportedOperationException("Skill gap analysis not implemented yet");
    }

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
