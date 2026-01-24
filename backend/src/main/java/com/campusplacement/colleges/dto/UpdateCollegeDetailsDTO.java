package com.campusplacement.colleges.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollegeDetailsDTO {
    private String name;
    private String address;
    private String website;
    private String contactPhone;
}
