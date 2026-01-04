package com.campusplacement.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for eligibility calculation request to AI service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityRequestDTO {

    // Student data
    private Long studentId;
    private String studentName;
    private String department;
    private Double cgpa;
    private List<String> skills;
    private List<String> certifications;
    private Integer projectsCount;
    private Integer internshipMonths;

    // Job requirements
    private Double minCgpa;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private List<String> eligibleDepartments;
}
