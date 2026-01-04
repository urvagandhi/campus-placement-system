package com.campusplacement.ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI service response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {

    private boolean success;
    private Double score;
    private Boolean isEligible;
    private List<String> reasons;

    // Score breakdown
    private Double cgpaScore;
    private Double skillsScore;
    private Double experienceScore;
    private Double certificationsScore;

    // Skill gap analysis
    private List<String> missingSkills;
    private List<String> recommendations;
}
