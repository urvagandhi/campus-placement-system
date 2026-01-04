package com.campusplacement.companies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Company entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {

    private Long id;

    @NotBlank(message = "Company name is required")
    private String name;

    private String industry;
    private String website;
    private String description;
    private String logoUrl;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
}
