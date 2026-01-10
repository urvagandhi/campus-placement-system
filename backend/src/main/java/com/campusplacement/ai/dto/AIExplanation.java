package com.campusplacement.ai.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized explanation format for all AI outputs.
 *
 * <p>
 * <strong>Explainability Requirement:</strong>
 * Every AI result MUST include an explanation. If an AI result
 * cannot be explained, it must not be exposed to users.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIExplanation {

    /**
     * List of input factors used in the AI calculation.
     * Example: ["CGPA: 8.5", "Skills: Java, Python", "Projects: 5"]
     */
    private List<String> factors;

    /**
     * Score breakdown by component.
     * Example: {"cgpa": 85.0, "skills": 70.0, "experience": 60.0}
     */
    private Map<String, Double> breakdown;

    /**
     * Human-readable explanation in plain English.
     * This is what gets shown to end users.
     */
    private String humanReadable;

    /**
     * Creates a minimal explanation for fallback scenarios.
     */
    public static AIExplanation fallback(String reason) {
        return AIExplanation.builder()
                .factors(List.of(reason))
                .breakdown(Map.of())
                .humanReadable(reason)
                .build();
    }
}
