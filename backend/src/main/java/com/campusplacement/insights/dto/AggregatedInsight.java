package com.campusplacement.insights.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated insight for drives.
 * Contains only aggregated statistics, no student-level PII.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedInsight {

    /**
     * Drive ID this insight is for.
     */
    private Long driveId;

    /**
     * Drive title for display.
     */
    private String driveTitle;

    /**
     * Total applications received.
     */
    private Integer totalApplications;

    /**
     * Number of students shortlisted.
     */
    private Integer shortlistedCount;

    /**
     * Number of students selected.
     */
    private Integer selectedCount;

    /**
     * Selection rate percentage.
     */
    private Double selectionRate;

    /**
     * Most common skills among selected candidates.
     */
    private Map<String, Integer> topSkillsSelected;

    /**
     * Most common skill gaps among rejected candidates.
     */
    private Map<String, Integer> commonSkillGaps;

    /**
     * Average eligibility score of selected candidates.
     */
    private Double avgSelectedScore;

    /**
     * Human-readable insight summary.
     */
    private String insightSummary;
}
