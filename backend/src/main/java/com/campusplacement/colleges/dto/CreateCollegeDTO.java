package com.campusplacement.colleges.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new College.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollegeDTO {

    @NotBlank(message = "College Name is required")
    @Size(min = 2, max = 100, message = "College Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "College Code is required")
    @Size(min = 2, max = 20, message = "College Code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "College Code must contain only uppercase letters and numbers")
    private String code;

    private String address;
    private String website;
    private String contactEmail;
}
