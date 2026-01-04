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

    private Integer totalStudents;
    private Integer placedStudents;
    private Integer unplacedStudents;
    private Double placementPercentage;

    private Integer totalDrives;
    private Integer completedDrives;
    private Integer upcomingDrives;

    private Integer totalCompanies;
    private Integer companiesVisited;

    private Double averagePackage;
    private Double highestPackage;
    private Double lowestPackage;

    private Integer totalApplications;
    private Integer totalOffers;
}
