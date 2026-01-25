package com.campusplacement.analytics.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for overall placement statistics with scope context.
 * 
 * <p>
 * Includes hierarchical breakdown based on user's organizational scope:
 * <ul>
 * <li>COLLEGE scope: Shows institute and department breakdowns</li>
 * <li>INSTITUTE scope: Shows department breakdown</li>
 * <li>DEPARTMENT scope: Shows only department-level stats</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementStatsDTO {

    // Scope context
    private String academicYear; // e.g., "2025-26"
    private String scopeLevel; // "COLLEGE", "INSTITUTE", or "DEPARTMENT"
    private String scopeName; // Name of the scoped entity
    private Long scopeId; // ID of the scoped entity

    // Core statistics
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

    // Hierarchical breakdowns (populated based on scope)
    private List<InstituteStatsDTO> instituteStats; // For COLLEGE scope
    private List<DepartmentStatsDTO> departmentStats; // For COLLEGE/INSTITUTE scope
}
