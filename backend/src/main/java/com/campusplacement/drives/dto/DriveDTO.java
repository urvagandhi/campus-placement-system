package com.campusplacement.drives.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PlacementDrive entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveDTO {

    private Long id;
    private Long companyId;
    private String companyName;
    private String title;
    private String description;
    private String jobRole;
    private Double packageLpa;
    private LocalDate driveDate;
    private LocalDate registrationDeadline;
    private String status;

    // Eligibility criteria
    private Double minCgpa;
    private List<String> eligibleDepartments;
    private List<String> requiredSkills;
    private Integer maxBacklogs;

    private String location;
    private Boolean isRemote;
}
