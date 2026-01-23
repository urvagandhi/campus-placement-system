package com.campusplacement.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for overall placement statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementStatsDTO {

    private Long totalStudents;
    private Long placedStudents;
    private Long shortlistedStudents;
    private Double placementRate;

    private Long totalApplications;

    private Long totalDrives;
    private Long activeDrives;

    private Long companiesVisited;

    private Double averagePackage;
    private Double highestPackage;
    private Double lowestPackage;
}
