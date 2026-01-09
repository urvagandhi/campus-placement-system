package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for LoginAudit entity operations.
 */
@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    /**
     * Find all login attempts for a specific user.
     *
     * @param userId the user ID
     * @return list of login audit records
     */
    List<LoginAudit> findByUserIdOrderByLoginTimeDesc(Long userId);

    /**
     * Find all login attempts for an email address.
     *
     * @param email the email address
     * @return list of login audit records
     */
    List<LoginAudit> findByEmailOrderByLoginTimeDesc(String email);

    /**
     * Find failed login attempts within a time range (for rate limiting).
     *
     * @param email the email address
     * @param since the start time
     * @return list of failed login attempts
     */
    List<LoginAudit> findByEmailAndSuccessAndLoginTimeAfter(String email, Boolean success, LocalDateTime since);

    /**
     * Count failed login attempts for an email since a given time.
     *
     * @param email   the email address
     * @param success whether the login was successful
     * @param since   the start time
     * @return count of attempts
     */
    long countByEmailAndSuccessAndLoginTimeAfter(String email, Boolean success, LocalDateTime since);

    /**
     * Delete all audit records older than a given timestamp.
     * Used for retention policy cleanup.
     *
     * @param before records older than this will be deleted
     * @return number of records deleted
     */
    @Modifying
    @Query("DELETE FROM LoginAudit la WHERE la.loginTime < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);

    /**
     * Count records older than a given timestamp.
     * Used for reporting before cleanup.
     *
     * @param before records older than this
     * @return count of old records
     */
    @Query("SELECT COUNT(la) FROM LoginAudit la WHERE la.loginTime < :before")
    long countOlderThan(@Param("before") LocalDateTime before);
}
