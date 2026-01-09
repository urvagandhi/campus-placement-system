package com.campusplacement.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling security alerts and notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAlertService {

    private final SecurityAlertRepository securityAlertRepository;

    /**
     * Triggers a security alert for a detected anomaly.
     */
    @SuppressWarnings("null")
    @Transactional
    public void triggerAlert(Long userId, String email, SecurityAuditEventType type, String message, String severity,
            String ip) {
        log.error("SECURITY ALERT [%s]: %s (User: %s, IP: %s)", severity, message, email, ip);

        SecurityAlert alert = SecurityAlert.builder()
                .userId(userId)
                .email(email)
                .alertType(type)
                .message(message)
                .severity(severity)
                .ipAddress(ip)
                .build();

        securityAlertRepository.save(alert);

        // TODO: Integrate with EmailService to notify administrators
        sendEmailToAdmins(alert);
    }

    private void sendEmailToAdmins(SecurityAlert alert) {
        // Mock email notification
        log.info("Sending security alert email to admins for event: {}", alert.getAlertType());
    }
}
