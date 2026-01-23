package com.campusplacement.organizations.dto;

import com.campusplacement.common.OrganizationUnitType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUnitDTO {
    private Long id;
    private String name;
    private String code;
    private OrganizationUnitType type;
    private Long parentId;
    private Boolean isActive;
    // For Department stats
    private Integer studentCount;
    private Integer placementCount;
}
