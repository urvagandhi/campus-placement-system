package com.campusplacement.eligibility.dto;

import java.util.List;

import lombok.Builder;

/**
 * Result object for eligibility checks.
 * Used by DriveEligibilityService for in-memory eligibility checks.
 */
@Builder
public record EligibilityCheckResult(
        boolean isEligible,
        List<String> reasons,
        double score,
        Double cgpaScore,
        Double skillsScore,
        Double experienceScore,
        String skillGaps) {

    /**
     * Creates an eligible result with default score.
     */
    public static EligibilityCheckResult eligible(String reason) {
        return EligibilityCheckResult.builder()
                .isEligible(true)
                .reasons(List.of(reason))
                .score(100.0)
                .build();
    }

    /**
     * Creates an ineligible result.
     */
    public static EligibilityCheckResult ineligible(List<String> reasons) {
        return EligibilityCheckResult.builder()
                .isEligible(false)
                .reasons(reasons)
                .score(0.0)
                .build();
    }
}
