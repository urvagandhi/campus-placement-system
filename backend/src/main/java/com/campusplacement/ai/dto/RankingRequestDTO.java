package com.campusplacement.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for ranking students for a drive.
 *
 * <p>
 * <strong>Note:</strong> This contains only sanitized data.
 * The Python AI service does NOT receive role, scope, or college info.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingRequestDTO {

    /**
     * Drive ID for context (used to fetch requirements).
     */
    private Long driveId;

    /**
     * List of student IDs to rank.
     * These are already filtered by eligibility rules.
     */
    private List<Long> studentIds;

    /**
     * Drive requirements for similarity matching.
     */
    private List<String> requiredSkills;

    /**
     * Preferred skills (optional bonus).
     */
    private List<String> preferredSkills;

    /**
     * Job role for context.
     */
    private String jobRole;
}
