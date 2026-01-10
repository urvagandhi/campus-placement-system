package com.campusplacement.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.auth.PasswordResetToken.TokenType;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing password reset tokens and password changes.
 *
 * <p>
 * Handles two flows:
 * </p>
 * <ul>
 * <li><strong>First Login:</strong> When TPO creates accounts, students must
 * change password on first login</li>
 * <li><strong>Forgot Password:</strong> Self-service password reset via
 * email</li>
 * </ul>
 *
 * <p>
 * <strong>Security Measures:</strong>
 * </p>
 * <ul>
 * <li>Rate limiting: Max 5 reset requests per hour per user</li>
 * <li>Token expiration: 24h for first-login, 1h for forgot-password</li>
 * <li>Single-use tokens</li>
 * <li>Old tokens invalidated when new token is issued</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${app.password-reset.first-login-expiry-hours:72}")
    private int firstLoginExpiryHours;

    @Value("${app.password-reset.forgot-password-expiry-hours:1}")
    private int forgotPasswordExpiryHours;

    @Value("${app.password-reset.max-requests-per-hour:5}")
    private int maxRequestsPerHour;

    // ==================== Token Creation ====================

    /**
     * Creates a first-login token for a newly created user.
     * Called when TPO/Coordinator creates a student account.
     *
     * @param user      the newly created user
     * @param requestIp IP address of the creator
     * @return the generated token
     */
    @Transactional
    public PasswordResetToken createFirstLoginToken(User user, String requestIp) {
        // Invalidate any existing first-login tokens
        tokenRepository.invalidateTokensByUserAndType(user.getId(), TokenType.FIRST_LOGIN);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenType(TokenType.FIRST_LOGIN)
                .expiresAt(LocalDateTime.now().plusHours(firstLoginExpiryHours))
                .requestedIp(requestIp)
                .build();

        token = tokenRepository.save(token);

        // Mark user as needing password change
        user.setMustChangePassword(true);
        userRepository.save(user);

        log.info("Created first-login token for user {} (expires in {}h)",
                user.getId(), firstLoginExpiryHours);

        return token;
    }

    /**
     * Creates a forgot-password token for a user.
     * Called from forgot password endpoint.
     *
     * @param email     user's email
     * @param requestIp IP address of the requester
     * @return the generated token, or null if user not found (silent failure for
     *         security)
     */
    @Transactional
    public PasswordResetToken createForgotPasswordToken(String email, String requestIp) {
        return userRepository.findByEmail(email)
                .filter(User::getIsActive)
                .map(user -> {
                    // Rate limiting check
                    long recentRequests = tokenRepository.countRecentTokensForUser(
                            user.getId(),
                            LocalDateTime.now().minusHours(1));

                    if (recentRequests >= maxRequestsPerHour) {
                        log.warn("Rate limit exceeded for password reset: user={}", user.getId());
                        return null;
                    }

                    // Invalidate existing forgot-password tokens
                    tokenRepository.invalidateTokensByUserAndType(user.getId(), TokenType.FORGOT_PASSWORD);

                    PasswordResetToken token = PasswordResetToken.builder()
                            .user(user)
                            .tokenType(TokenType.FORGOT_PASSWORD)
                            .expiresAt(LocalDateTime.now().plusHours(forgotPasswordExpiryHours))
                            .requestedIp(requestIp)
                            .build();

                    token = tokenRepository.save(token);

                    log.info("Created forgot-password token for user {} (expires in {}h)",
                            user.getId(), forgotPasswordExpiryHours);

                    return token;
                })
                .orElse(null); // Silent failure - don't reveal if user exists
    }

    // ==================== Token Validation ====================

    /**
     * Validates a password reset token.
     *
     * @param tokenValue the token to validate
     * @return the valid token
     * @throws ResourceNotFoundException if token is invalid or expired
     */
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String tokenValue) {
        return tokenRepository.findValidToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid or expired password reset token"));
    }

    /**
     * Checks if a user needs to change their password on login.
     *
     * @param userId the user ID
     * @return true if password change is required
     */
    @Transactional(readOnly = true)
    public boolean isPasswordChangeRequired(Long userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getMustChangePassword()))
                .orElse(false);
    }

    // ==================== Password Change ====================

    /**
     * Completes the password reset process.
     * Marks the token as used and clears the mustChangePassword flag.
     *
     * @param tokenValue      the token value
     * @param newPasswordHash the new BCrypt hashed password
     * @param usedIp          IP address where password was changed
     * @return the user whose password was changed
     */
    @Transactional
    public User completePasswordReset(String tokenValue, String newPasswordHash, String usedIp) {
        PasswordResetToken token = validateToken(tokenValue);
        User user = token.getUser();

        // Update password
        user.setPasswordHash(newPasswordHash);
        user.setMustChangePassword(false);

        // Mark token as used
        token.markAsUsed(usedIp);

        // Invalidate all other tokens for this user (security measure)
        tokenRepository.invalidateAllTokensForUser(user.getId());

        userRepository.save(user);
        tokenRepository.save(token);

        log.info("Password reset completed for user {} via {} token",
                user.getId(), token.getTokenType());

        return user;
    }

    /**
     * Forces a password reset for a user (admin action).
     * Creates a first-login token and sets mustChangePassword flag.
     *
     * @param userId    the user ID
     * @param requestIp IP address of the admin
     * @return the generated token
     */
    @Transactional
    public PasswordResetToken forcePasswordReset(Long userId, String requestIp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return createFirstLoginToken(user, requestIp);
    }

    // ==================== Cleanup ====================

    /**
     * Cleans up expired tokens.
     * Should be called by a scheduled job.
     *
     * @return number of tokens deleted
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now().minusDays(7));
        if (deleted > 0) {
            log.info("Cleaned up {} expired password reset tokens", deleted);
        }
        return deleted;
    }
}
