package com.campusplacement.auth.dto;

import java.util.List;
import java.util.Map;

import com.campusplacement.auth.LoginAudit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for security dashboard statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityStatsDTO {
    private long totalLogins24h;
    private long failedLogins24h;
    private long tokenReuseAttempts24h;
    private long unauthorizedDevices24h;
    private double failureRate;
    private List<LoginAudit> recentAnomalies;
    private Map<String, Long> eventsByType;
}
