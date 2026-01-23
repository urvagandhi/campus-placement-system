package com.campusplacement.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for system-wide statistics (Super Admin Dashboard).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsDTO {

    private Long totalColleges;
    private Long totalUsers;
    private Long uptimeSeconds;
    private Long activeSessions;
    private Boolean maintenanceMode;
}
