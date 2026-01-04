package com.campusplacement.eligibility.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for eligibility result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResultDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long driveId;
    private String driveTitle;
    private Double score;
    private Boolean isEligible;
    private List<String> reasons;
    private LocalDateTime calculatedAt;

    // Score breakdown
    private Double cgpaScore;
    private Double skillsScore;
    private Double experienceScore;

    private List<String> skillGaps;
}
