package com.campusplacement.students.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating student skills.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSkillsDTO {

    @NotEmpty(message = "Skills list cannot be empty")
    private List<String> skills;

    private List<String> certifications;
}
