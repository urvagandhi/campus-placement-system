package com.campusplacement.analytics.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for institute-level placement statistics.
 * 
 * <p>
 * Used when returning hierarchical analytics data with institute and department
 * breakdown.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstituteStatsDTO {

    private Long instituteId;
    private String instituteName;
    private String instituteCode;

    private Long totalStudents;
    private Long placedStudents;
    private Double placementRate;

    private Double averagePackage;
    private Double highestPackage;

    private Long totalDrives;
    private Long activeDrives;
    private Long totalApplications;

    /**
     * Department-wise breakdown for this institute.
     */
    private List<DepartmentStatsDTO> departmentStats;
}
