package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for RefreshToken entity operations.
 *
 * <p>
 * Provides CRUD operations and custom queries for refresh token management.
 * </p>
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token value.
     *
     * @param token the refresh token string
     * @return the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all active (non-revoked) refresh tokens for a user.
     *
     * @param userId the user ID
     * @return list of active refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") Long userId);

    /**
     * Delete all refresh tokens for a specific user.
     * Used for "logout from all devices" functionality.
     *
     * @param userId the user ID
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Revoke all refresh tokens for a specific user.
     * Preferred over deletion for audit trail purposes.
     *
     * @param userId the user ID
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId AND rt.isRevoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Delete expired tokens (cleanup job).
     *
     * @param before tokens expiring before this time will be deleted
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") LocalDateTime before);

    /**
     * Count active tokens for a user.
     * Can be used to limit the number of concurrent sessions.
     *
     * @param userId the user ID
     * @return count of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > CURRENT_TIMESTAMP")
    long countActiveTokensByUserId(@Param("userId") Long userId);
}
