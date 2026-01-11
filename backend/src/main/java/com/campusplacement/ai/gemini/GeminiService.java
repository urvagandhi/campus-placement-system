package com.campusplacement.ai.gemini;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.students.StudentProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interacting with Google Gemini API.
 * Uses REST calls to avoid heavy SDK dependencies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Calculates an AI-based eligibility score (0-100) for a student against a
     * drive.
     */
    public double calculateEligibilityScore(StudentProfile student, PlacementDrive drive) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key not configured. Returning neutral score.");
            return 50.0;
        }

        String prompt = buildScoringPrompt(student, drive);
        try {
            String responseText = generateContent(prompt);
            return parseScore(responseText);
        } catch (Exception e) {
            log.error("Failed to get AI score from Gemini: {}", e.getMessage());
            return 50.0; // Fallback score
        }
    }

    /**
     * Generates career recommendations for a student.
     */
    public String generateCareerRecommendations(StudentProfile student) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "AI recommendations unavailable (API key missing).";
        }

        // Default to a generic tech role since specific role isn't passed
        String targetRole = "Software Engineer";

        String prompt = buildRecommendationPrompt(student, targetRole);
        try {
            return generateContent(prompt);
        } catch (Exception e) {
            log.error("Failed to get recommendations from Gemini: {}", e.getMessage());
            return "Unable to generate recommendations at this time.";
        }
    }

    @SuppressWarnings("null")
    private String generateContent(String prompt) {
        String url = String.format("%s/%s:generateContent?key=%s", baseUrl, model, apiKey);

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiContent(List.of(new GeminiPart(prompt)))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, GeminiResponse.class);

        if (response.getBody() == null || response.getBody().getCandidates() == null
                || response.getBody().getCandidates().isEmpty()) {
            throw new RuntimeException("Empty response from Gemini");
        }

        return response.getBody().getCandidates().get(0).getContent().getParts().get(0).getText();
    }

    private String buildScoringPrompt(StudentProfile student, PlacementDrive drive) {
        return String.format("""
                Act as a strict recruitment officer.

                Evaluate the candidate and return ONLY a numeric score between 0 and 100.

                Scoring rules:
                - Skills match: 40%%
                - CGPA relevance: 30%%
                - Projects quality: 15%%
                - Internship/Experience: 10%%
                - Backlogs penalty: -5%% per backlog

                Job Role: %s
                Minimum CGPA: %.2f

                Required Skills:
                %s

                Candidate Profile:
                CGPA: %.2f
                Skills:
                %s
                Projects Count: %d
                Internship Duration: %d months
                Backlogs: %d

                Final Fit Score (0–100):
                """,
                drive.getTitle(),
                drive.getMinCgpa(),
                formatSkills(drive.getRequiredSkills()),
                student.getCgpa(),
                formatSkills(student.getSkills()),
                student.getProjectsCount(),
                student.getInternshipMonths(),
                student.getBacklogs());
    }

    private String buildRecommendationPrompt(StudentProfile student, String targetRole) {
        return String.format("""
                You are a placement mentor.

                Suggest 3 concrete, actionable improvements for this student
                to get placed as a %s in top companies.

                Base suggestions strictly on gaps in:
                - Skills
                - Projects
                - Practical experience

                Student Profile:
                Skills:
                %s
                Projects: %d
                Internship Experience: %d months

                Return bullet points only.
                """,
                targetRole,
                formatSkills(student.getSkills()),
                student.getProjectsCount(),
                student.getInternshipMonths());
    }

    private String formatSkills(String skills) {
        if (skills == null || skills.isBlank()) {
            return "None";
        }
        return java.util.Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "- " + s)
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private double parseScore(String text) {
        try {
            String cleanText = text.replaceAll("[^0-9.]", "").trim();
            if (cleanText.isEmpty())
                return 50.0;
            double score = Double.parseDouble(cleanText);
            return Math.min(100.0, Math.max(0.0, score));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse AI score from text: {}", text);
            return 50.0;
        }
    }

    // ==================== Gemini DTOs ====================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GeminiRequest {
        private List<GeminiContent> contents;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GeminiContent {
        private List<GeminiPart> parts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GeminiPart {
        private String text;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        private GeminiContent content;
    }
}
