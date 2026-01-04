package com.campusplacement.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for department-wise statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDTO {

    private String department;
    private Integer totalStudents;
    private Integer placedStudents;
    private Double placementPercentage;
    private Double averagePackage;
    private Double highestPackage;
    private Integer totalOffers;
}
