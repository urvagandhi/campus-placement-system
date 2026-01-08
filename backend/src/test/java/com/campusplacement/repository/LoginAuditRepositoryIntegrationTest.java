package com.campusplacement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.AbstractIntegrationTest;
import com.campusplacement.auth.LoginAudit;
import com.campusplacement.auth.LoginAuditRepository;

/**
 * Integration tests for LoginAuditRepository using real PostgreSQL via
 * Testcontainers.
 *
 * <p>
 * Tests verify:
 * <ul>
 * <li>Audit records are persisted correctly</li>
 * <li>Query methods return correct data</li>
 * <li>Time-based filtering works</li>
 * </ul>
 * </p>
 */
@Transactional
class LoginAuditRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LoginAuditRepository loginAuditRepository;

    private static final String TEST_EMAIL = "user@test.edu";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 Test Browser";

    @BeforeEach
    void setUp() {
        loginAuditRepository.deleteAll();
    }

    // ========== Save Tests ==========

    @Nested
    @DisplayName("Audit Record Persistence")
    class AuditRecordPersistenceTests {

        @Test
        @DisplayName("Successful login audit is persisted correctly")
        void save_SuccessfulLoginAudit_PersistsCorrectly() {
            LoginAudit audit = LoginAudit.builder()
                    .userId(1L)
                    .email(TEST_EMAIL)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_USER_AGENT)
                    .success(true)
                    .build();

            @SuppressWarnings("null")
            LoginAudit saved = loginAuditRepository.save(audit);

            assertNotNull(saved.getId());
            assertEquals(1L, saved.getUserId());
            assertEquals(TEST_EMAIL, saved.getEmail());
            assertEquals(TEST_IP, saved.getIpAddress());
            assertEquals(TEST_USER_AGENT, saved.getUserAgent());
            assertTrue(saved.getSuccess());
        }

        @Test
        @DisplayName("Failed login audit with null userId is persisted correctly")
        void save_FailedLoginAuditWithNullUserId_PersistsCorrectly() {
            LoginAudit audit = LoginAudit.builder()
                    .userId(null) // Unknown user
                    .email("unknown@test.edu")
                    .loginTime(LocalDateTime.now())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_USER_AGENT)
                    .success(false)
                    .build();

            @SuppressWarnings("null")
            LoginAudit saved = loginAuditRepository.save(audit);

            assertNotNull(saved.getId());
            assertEquals(null, saved.getUserId());
            assertFalse(saved.getSuccess());
        }

        @Test
        @DisplayName("PrePersist sets loginTime if null")
        void save_NullLoginTime_SetsAutomatically() {
            LoginAudit audit = LoginAudit.builder()
                    .userId(1L)
                    .email(TEST_EMAIL)
                    .loginTime(null) // Will be set by @PrePersist
                    .success(true)
                    .build();

            @SuppressWarnings("null")
            LoginAudit saved = loginAuditRepository.save(audit);

            assertNotNull(saved.getLoginTime());
        }
    }

    // ========== Query by User ID Tests ==========

    @Nested
    @DisplayName("findByUserIdOrderByLoginTimeDesc")
    class FindByUserIdTests {

        @Test
        @DisplayName("Returns audit records ordered by login time descending")
        void findByUserId_ReturnsOrderedByLoginTimeDesc() {
            Long userId = 1L;
            LocalDateTime now = LocalDateTime.now();

            // Create audits at different times
            createAudit(userId, TEST_EMAIL, now.minusHours(2), true);
            createAudit(userId, TEST_EMAIL, now, true);
            createAudit(userId, TEST_EMAIL, now.minusHours(1), false);

            List<LoginAudit> results = loginAuditRepository.findByUserIdOrderByLoginTimeDesc(userId);

            assertEquals(3, results.size());
            // Most recent should be first
            assertTrue(results.get(0).getLoginTime().isAfter(results.get(1).getLoginTime()));
            assertTrue(results.get(1).getLoginTime().isAfter(results.get(2).getLoginTime()));
        }

        @Test
        @DisplayName("Returns empty list for unknown user ID")
        void findByUserId_UnknownUser_ReturnsEmpty() {
            List<LoginAudit> results = loginAuditRepository.findByUserIdOrderByLoginTimeDesc(99999L);

            assertTrue(results.isEmpty());
        }
    }

    // ========== Query by Email Tests ==========

    @Nested
    @DisplayName("findByEmailOrderByLoginTimeDesc")
    class FindByEmailTests {

        @Test
        @DisplayName("Returns audit records for email ordered by login time")
        void findByEmail_ReturnsOrderedByLoginTimeDesc() {
            LocalDateTime now = LocalDateTime.now();

            createAudit(1L, TEST_EMAIL, now.minusMinutes(30), true);
            createAudit(1L, TEST_EMAIL, now, false);
            createAudit(2L, "other@test.edu", now, true); // Different email

            List<LoginAudit> results = loginAuditRepository.findByEmailOrderByLoginTimeDesc(TEST_EMAIL);

            assertEquals(2, results.size());
            assertTrue(results.get(0).getLoginTime().isAfter(results.get(1).getLoginTime()));
        }
    }

    // ========== Count Failed Attempts Tests ==========

    @Nested
    @DisplayName("countByEmailAndSuccessAndLoginTimeAfter")
    class CountFailedAttemptsTests {

        @Test
        @DisplayName("Counts failed login attempts after given time")
        void countFailedAttempts_ReturnsCorrectCount() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);

            // Create failed attempts
            createAudit(null, TEST_EMAIL, now.minusMinutes(30), false);
            createAudit(null, TEST_EMAIL, now.minusMinutes(15), false);
            createAudit(null, TEST_EMAIL, now.minusMinutes(5), false);
            // Create successful attempt
            createAudit(1L, TEST_EMAIL, now.minusMinutes(10), true);
            // Create old failed attempt (should not be counted)
            createAudit(null, TEST_EMAIL, now.minusHours(2), false);

            long failedCount = loginAuditRepository.countByEmailAndSuccessAndLoginTimeAfter(
                    TEST_EMAIL, false, oneHourAgo);

            assertEquals(3, failedCount);
        }

        @Test
        @DisplayName("Returns zero when no failed attempts in time window")
        void countFailedAttempts_NoFailures_ReturnsZero() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);

            // Create only successful attempts
            createAudit(1L, TEST_EMAIL, now.minusMinutes(10), true);

            long failedCount = loginAuditRepository.countByEmailAndSuccessAndLoginTimeAfter(
                    TEST_EMAIL, false, oneHourAgo);

            assertEquals(0, failedCount);
        }
    }

    // ========== Time-Based Query Tests ==========

    @Nested
    @DisplayName("findByEmailAndSuccessAndLoginTimeAfter")
    class TimeBasedQueryTests {

        @Test
        @DisplayName("Returns failed attempts after given time")
        void findFailedAfterTime_ReturnsCorrectRecords() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

            createAudit(null, TEST_EMAIL, now.minusMinutes(10), false);
            createAudit(null, TEST_EMAIL, now.minusMinutes(20), false);
            createAudit(null, TEST_EMAIL, now.minusHours(1), false); // Should not be included

            List<LoginAudit> results = loginAuditRepository.findByEmailAndSuccessAndLoginTimeAfter(
                    TEST_EMAIL, false, thirtyMinutesAgo);

            assertEquals(2, results.size());
        }
    }

    // ========== Helper Methods ==========

    @SuppressWarnings("null")
    private LoginAudit createAudit(Long userId, String email, LocalDateTime loginTime, boolean success) {
        LoginAudit audit = LoginAudit.builder()
                .userId(userId)
                .email(email)
                .loginTime(loginTime)
                .ipAddress(TEST_IP)
                .userAgent(TEST_USER_AGENT)
                .success(success)
                .build();
        return loginAuditRepository.save(audit);
    }
}
