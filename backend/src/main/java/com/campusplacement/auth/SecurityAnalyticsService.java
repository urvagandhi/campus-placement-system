package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.auth.dto.SecurityStatsDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for providing security analytics and monitoring data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAnalyticsService {

    private final LoginAuditRepository loginAuditRepository;

    /**
     * Retrieves aggregated security statistics for the last 24 hours.
     */
    @Transactional(readOnly = true)
    public SecurityStatsDTO getSecurityStats() {
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);

        long totalLogins = loginAuditRepository.countByEventTypeAndLoginTimeAfter(SecurityAuditEventType.LOGIN,
                since24h);
        // long failedLogins = loginAuditRepository.countByEmailAndSuccessAndLoginTimeAfter("", false, since24h);
        // Note: Repository needs refinement for "all failed", let's use a more generic
        // count
        long totalFailed = loginAuditRepository.countBySuccessAndLoginTimeAfter(false, since24h);
        long reuseAttempts = loginAuditRepository
                .countByEventTypeAndLoginTimeAfter(SecurityAuditEventType.TOKEN_REUSE_DETECTED, since24h);
        long unauthorizedDevices = loginAuditRepository
                .countByEventTypeAndLoginTimeAfter(SecurityAuditEventType.UNAUTHORIZED_DEVICE, since24h);

        Map<String, Long> eventsByType = new HashMap<>();
        for (SecurityAuditEventType type : SecurityAuditEventType.values()) {
            eventsByType.put(type.name(), loginAuditRepository.countByEventTypeAndLoginTimeAfter(type, since24h));
        }

        double failureRate = totalLogins > 0 ? (double) totalFailed / (totalLogins + totalFailed) * 100 : 0;

        return SecurityStatsDTO.builder()
                .totalLogins24h(totalLogins)
                .failedLogins24h(totalFailed)
                .tokenReuseAttempts24h(reuseAttempts)
                .unauthorizedDevices24h(unauthorizedDevices)
                .failureRate(Math.round(failureRate * 100.0) / 100.0)
                .eventsByType(eventsByType)
                .recentAnomalies(loginAuditRepository.findLatestAnomalies(PageRequest.of(0, 50)))
                .build();
    }

    /**
     * Generates a CSV string of audit logs for export.
     */
    @Transactional(readOnly = true)
    public String exportAuditLogsCsv() {
        java.util.List<LoginAudit> logs = loginAuditRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "loginTime"));
        StringBuilder csv = new StringBuilder("ID,Time,User ID,Email,Event,IP,Status,Reason\n");

        for (LoginAudit log : logs) {
            csv.append(log.getId()).append(",")
                    .append(log.getLoginTime()).append(",")
                    .append(log.getUserId() != null ? log.getUserId() : "N/A").append(",")
                    .append(log.getEmail()).append(",")
                    .append(log.getEventType()).append(",")
                    .append(log.getIpAddress()).append(",")
                    .append(log.getSuccess() ? "SUCCESS" : "FAILED").append(",")
                    .append(log.getFailureReason() != null ? log.getFailureReason().replace(",", ";") : "")
                    .append("\n");
        }

        return csv.toString();
    }
}
