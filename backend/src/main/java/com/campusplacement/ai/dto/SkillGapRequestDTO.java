package com.campusplacement.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for skill gap analysis request to AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapRequestDTO {

    private Long studentId;
    private List<String> currentSkills;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private String targetRole;
}
