package com.campusplacement.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing audit log retention and cleanup.
 *
 * <p>
 * Provides:
 * <ul>
 * <li>Scheduled cleanup of old audit records</li>
 * <li>Manual cleanup trigger for administrators</li>
 * <li>Retention policy enforcement</li>
 * </ul>
 * </p>
 *
 * <p>
 * Configuration in application.yml:
 * 
 * <pre>
 * app:
 *   audit:
 *     retention-days: 90
 *     cleanup-enabled: true
 *     cleanup-cron: "0 0 2 * * ?"
 * </pre>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.audit.cleanup-enabled", havingValue = "true", matchIfMissing = false)
public class AuditCleanupService {

    private final LoginAuditRepository loginAuditRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.audit.retention-days:90}")
    private int retentionDays;

    /**
     * Scheduled cleanup job that runs according to the configured cron expression.
     * Default: Daily at 2 AM.
     *
     * <p>
     * Cleans up:
     * <ul>
     * <li>LoginAudit records older than retention period</li>
     * <li>Expired refresh tokens</li>
     * </ul>
     * </p>
     */
    @Scheduled(cron = "${app.audit.cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public void scheduledCleanup() {
        log.info("Starting scheduled audit log cleanup (retention: {} days)", retentionDays);

        try {
            CleanupResult result = performCleanup();
            log.info("Audit cleanup completed: {} audit records deleted, {} expired tokens deleted",
                    result.auditRecordsDeleted, result.expiredTokensDeleted);
        } catch (Exception e) {
            log.error("Audit cleanup failed", e);
        }
    }

    /**
     * Performs the cleanup operation.
     * Can be called manually or by the scheduled job.
     *
     * @return cleanup result with counts of deleted records
     */
    @Transactional
    public CleanupResult performCleanup() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        // Count before deletion for logging
        long auditCount = loginAuditRepository.countOlderThan(cutoffDate);
        log.debug("Found {} audit records older than {}", auditCount, cutoffDate);

        // Delete old audit records
        int auditDeleted = loginAuditRepository.deleteOlderThan(cutoffDate);

        // Also clean up expired refresh tokens
        int tokensDeleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        return new CleanupResult(auditDeleted, tokensDeleted);
    }

    /**
     * Gets statistics about records eligible for cleanup.
     *
     * @return cleanup statistics
     */
    public CleanupStats getCleanupStats() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        long eligibleAuditRecords = loginAuditRepository.countOlderThan(cutoffDate);
        long totalAuditRecords = loginAuditRepository.count();
        long activeRefreshTokens = refreshTokenRepository.count();

        return new CleanupStats(
                retentionDays,
                cutoffDate,
                eligibleAuditRecords,
                totalAuditRecords,
                activeRefreshTokens);
    }

    /**
     * Result of a cleanup operation.
     */
    public record CleanupResult(int auditRecordsDeleted, int expiredTokensDeleted) {
    }

    /**
     * Statistics about cleanup-eligible records.
     */
    public record CleanupStats(
            int retentionDays,
            LocalDateTime cutoffDate,
            long eligibleAuditRecords,
            long totalAuditRecords,
            long activeRefreshTokens) {
    }
}
