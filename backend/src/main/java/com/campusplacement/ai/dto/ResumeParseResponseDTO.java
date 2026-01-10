package com.campusplacement.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for resume parsing.
 * Contains extracted skills and metadata with explanation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseResponseDTO {

    /**
     * Whether the parsing was successful.
     */
    private boolean success;

    /**
     * Student ID for reference.
     */
    private Long studentId;

    /**
     * Extracted and normalized skills.
     * Example: ["Java", "Spring Boot", "SQL", "Docker"]
     */
    private List<String> skills;

    /**
     * Detected experience level.
     * Values: FRESHER, BEGINNER, INTERMEDIATE, ADVANCED
     */
    private String experienceLevel;

    /**
     * Confidence score for the extraction (0.0 to 1.0).
     */
    private Double confidenceScore;

    /**
     * Extracted project keywords (optional).
     */
    private List<String> projectKeywords;

    /**
     * Extracted education signals (optional).
     * Example: ["B.Tech CSE", "IIT Delhi"]
     */
    private List<String> educationSignals;

    /**
     * Mandatory explanation of how results were derived.
     */
    private AIExplanation explanation;
}
