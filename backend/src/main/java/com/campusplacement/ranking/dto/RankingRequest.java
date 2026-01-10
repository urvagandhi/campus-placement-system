package com.campusplacement.ranking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for ranking operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingRequest {

    /**
     * Optional: Specific student IDs to rank.
     * If null, all eligible students in scope are ranked.
     */
    private List<Long> studentIds;

    /**
     * Optional: Maximum number of results to return.
     */
    private Integer limit;

    /**
     * Optional: Minimum similarity score threshold.
     */
    private Double minScore;
}
