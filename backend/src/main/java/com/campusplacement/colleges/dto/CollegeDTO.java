package com.campusplacement.colleges.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeDTO {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String website;
    private String contactEmail; // Institutional contact email
    private String contactPhone; // Institutional contact phone
    private String adminName; // Primary admin's name
    private String adminEmail; // Primary admin's login email
    private String adminPhone; // Primary admin's phone
    private Boolean isActive;
}
