package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.auth.PasswordResetToken.TokenType;

/**
 * Repository interface for PasswordResetToken entity.
 *
 * <p>
 * Provides methods for managing password reset tokens including:
 * </p>
 * <ul>
 * <li>Token lookup by value</li>
 * <li>Token lookup by user</li>
 * <li>Cleanup of expired/used tokens</li>
 * </ul>
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a valid (unused, unexpired) token by its value.
     *
     * @param token the token value
     * @return Optional containing the token if found and valid
     */
    @Query("SELECT prt FROM PasswordResetToken prt " +
            "WHERE prt.token = :token " +
            "AND prt.isUsed = false " +
            "AND prt.expiresAt > CURRENT_TIMESTAMP")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token);

    /**
     * Finds a token by its value (regardless of validity).
     *
     * @param token the token value
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Finds all valid tokens for a user.
     *
     * @param userId the user ID
     * @return List of valid tokens
     */
    @Query("SELECT prt FROM PasswordResetToken prt " +
            "WHERE prt.user.id = :userId " +
            "AND prt.isUsed = false " +
            "AND prt.expiresAt > CURRENT_TIMESTAMP")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") Long userId);

    /**
     * Finds the latest valid first-login token for a user.
     * Used to check if a new user needs to reset password.
     *
     * @param userId the user ID
     * @return Optional containing the token if found
     */
    @Query("SELECT prt FROM PasswordResetToken prt " +
            "WHERE prt.user.id = :userId " +
            "AND prt.tokenType = 'FIRST_LOGIN' " +
            "AND prt.isUsed = false " +
            "AND prt.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findValidFirstLoginToken(@Param("userId") Long userId);

    /**
     * Invalidates all existing tokens for a user.
     * Called when issuing a new token to prevent token hoarding.
     *
     * @param userId the user ID
     * @return number of tokens invalidated
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.isUsed = true " +
            "WHERE prt.user.id = :userId AND prt.isUsed = false")
    int invalidateAllTokensForUser(@Param("userId") Long userId);

    /**
     * Invalidates all tokens of a specific type for a user.
     *
     * @param userId    the user ID
     * @param tokenType the token type to invalidate
     * @return number of tokens invalidated
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.isUsed = true " +
            "WHERE prt.user.id = :userId " +
            "AND prt.tokenType = :tokenType " +
            "AND prt.isUsed = false")
    int invalidateTokensByUserAndType(
            @Param("userId") Long userId,
            @Param("tokenType") TokenType tokenType);

    /**
     * Deletes expired tokens older than the specified date.
     * Used for periodic cleanup job.
     *
     * @param before delete tokens created before this timestamp
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt " +
            "WHERE prt.expiresAt < :before")
    int deleteExpiredTokens(@Param("before") LocalDateTime before);

    /**
     * Counts unused tokens for rate limiting.
     * Prevents abuse of password reset requests.
     *
     * @param userId the user ID
     * @param since  count tokens created after this timestamp
     * @return number of tokens created since the given time
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt " +
            "WHERE prt.user.id = :userId " +
            "AND prt.createdAt > :since")
    long countRecentTokensForUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    /**
     * Checks if a valid first-login token exists for a user.
     *
     * @param userId the user ID
     * @return true if a valid first-login token exists
     */
    @Query("SELECT COUNT(prt) > 0 FROM PasswordResetToken prt " +
            "WHERE prt.user.id = :userId " +
            "AND prt.tokenType = 'FIRST_LOGIN' " +
            "AND prt.isUsed = false " +
            "AND prt.expiresAt > CURRENT_TIMESTAMP")
    boolean hasValidFirstLoginToken(@Param("userId") Long userId);
}
