package com.campusplacement.insights.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skill trend insight for gap analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTrendInsight {

    /**
     * Skill name.
     */
    private String skill;

    /**
     * Number of students with this skill.
     */
    private Integer studentsWithSkill;

    /**
     * Number of drives requiring this skill.
     */
    private Integer drivesRequiring;

    /**
     * Gap ratio: (drives requiring - students with) / drives requiring.
     * Higher = more demand than supply.
     */
    private Double gapRatio;

    /**
     * Priority for training: HIGH, MEDIUM, LOW.
     */
    private String priority;

    /**
     * Recommended action.
     */
    private String recommendation;
}
