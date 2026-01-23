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
    private String contactEmail;
    private String adminName;
    private String contactPhone;
    private Boolean isActive;
}
