package com.campusplacement.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for student ranking.
 * Contains ranked students with similarity scores and explanations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDTO {

    /**
     * Whether the ranking was successful.
     */
    private boolean success;

    /**
     * Ranked list of students with scores.
     */
    private List<StudentRanking> rankings;

    /**
     * Overall explanation for the ranking methodology.
     */
    private AIExplanation explanation;

    /**
     * Individual student ranking entry.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentRanking {

        /**
         * Student ID.
         */
        private Long studentId;

        /**
         * Similarity score (0-100).
         */
        private Double similarityScore;

        /**
         * Rank position (1 = best match).
         */
        private Integer rank;

        /**
         * Skills that matched the requirements.
         */
        private List<String> matchedSkills;

        /**
         * Skills the student is missing.
         */
        private List<String> missingSkills;

        /**
         * Individual explanation for this ranking.
         */
        private String reason;
    }
}
