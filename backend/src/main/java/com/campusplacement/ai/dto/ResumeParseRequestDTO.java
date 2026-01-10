package com.campusplacement.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resume parsing.
 * Sent to the Python AI service for skill extraction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseRequestDTO {

    /**
     * Student ID for reference (not used by AI for decisions).
     */
    private Long studentId;

    /**
     * Raw text content extracted from the resume file.
     * The AI service does NOT have access to the original file.
     */
    private String resumeText;
}
