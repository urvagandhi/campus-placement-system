package com.campusplacement.organizations.dto;

import com.campusplacement.common.ScopeLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStaffDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String designation;
    private ScopeLevel scopeLevel;
    private Boolean isPrimary;
    private Boolean isActive;
    // We need assignment ID to handle deletion of assignment specifically if
    // needed,
    // or we can use userId if we delete user. Assuming we manage USER status here.
    // The requirement says "active or inactive status for all the persons", which
    // implies USER status.
    private Boolean userIsActive;
    private String phoneNumber;
}
