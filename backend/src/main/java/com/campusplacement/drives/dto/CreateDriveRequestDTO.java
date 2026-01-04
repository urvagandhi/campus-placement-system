package com.campusplacement.drives.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new placement drive.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriveRequestDTO {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    @Positive(message = "Package must be positive")
    private Double packageLpa;

    @NotNull(message = "Drive date is required")
    @FutureOrPresent(message = "Drive date must be in the future")
    private LocalDate driveDate;

    @NotNull(message = "Registration deadline is required")
    private LocalDate registrationDeadline;

    // Eligibility criteria
    @DecimalMin(value = "0.0", message = "Minimum CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Minimum CGPA must be at most 10.0")
    private Double minCgpa;

    @NotEmpty(message = "At least one eligible department is required")
    private List<String> eligibleDepartments;

    private List<String> requiredSkills;

    @Min(value = 0, message = "Max backlogs cannot be negative")
    private Integer maxBacklogs;

    private String location;
    private Boolean isRemote;
}
