package com.campusplacement.students.dto;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student profile data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDTO {

    private Long id;
    private Long userId;

    @NotBlank(message = "Enrollment number is required")
    private String enrollmentNo;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "CGPA must be at most 10.0")
    private Double cgpa;

    private List<String> skills;
    private String resumeUrl;
    private Integer batchYear;
    private String semester;
    private Integer projectsCount;
    private Integer internshipMonths;
    private List<String> certifications;
    private String linkedinUrl;
    private String githubUrl;
}
