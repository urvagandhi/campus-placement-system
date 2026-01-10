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

    private Long departmentId;
    private String departmentName;
    private Long totalStudents;
    private Long placedStudents;
    private Double placementRate;
    private Double averagePackage;
    private Double highestPackage;
}
