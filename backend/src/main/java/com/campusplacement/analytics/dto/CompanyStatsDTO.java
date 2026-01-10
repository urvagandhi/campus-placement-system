package com.campusplacement.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for company-wise placement statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatsDTO {

    private Long companyId;
    private String companyName;
    private String industry;
    private Long totalDrives;
    private Long totalApplications;
    private Long selectedCandidates;
    private Double selectionRate;
    private Double averagePackage;
    private Double highestPackage;
}
