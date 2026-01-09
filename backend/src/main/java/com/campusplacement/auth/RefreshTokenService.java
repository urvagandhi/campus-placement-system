package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.auth.dto.TokenRefreshResponseDTO;
import com.campusplacement.auth.exception.RefreshTokenException;
import com.campusplacement.security.JwtTokenProvider;
import com.campusplacement.users.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing refresh token lifecycle.
 *
 * <p>
 * Handles creation, validation, rotation, and revocation of refresh tokens.
 * </p>
 *
 * <p>
 * Security features:
 * <ul>
 * <li>Token rotation: old token is invalidated when a new one is issued</li>
 * <li>Single-use enforcement: each token can only be used once</li>
 * <li>Revocation support: tokens can be revoked on logout</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAuditRepository loginAuditRepository;
    private final HttpServletRequest httpServletRequest;

    @Value("${app.jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    /**
     * Creates a new refresh token for the given user.
     *
     * @param user the user to create the token for
     * @return the created refresh token
     */
    @SuppressWarnings("null")
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .isRevoked(false)
                .createdIp(getClientIpAddress())
                .userAgent(httpServletRequest.getHeader("User-Agent"))
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", user.getEmail());
        return refreshToken;
    }

    /**
     * Validates and rotates a refresh token, returning new access and refresh
     * tokens.
     *
     * <p>
     * This method:
     * <ol>
     * <li>Validates the provided refresh token</li>
     * <li>Revokes the old refresh token (rotation)</li>
     * <li>Creates a new refresh token</li>
     * <li>Generates a new access token</li>
     * </ol>
     * </p>
     *
     * @param refreshTokenStr the refresh token string
     * @return new access and refresh tokens
     * @throws RefreshTokenException if the token is invalid, expired, or revoked
     */
    @Transactional
    public TokenRefreshResponseDTO refreshAccessToken(String refreshTokenStr) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found: {}", refreshTokenStr.substring(0, 8) + "...");
                    return new RefreshTokenException("Invalid refresh token");
                });

        // Check if token is valid (not expired and not revoked)
        if (!existingToken.isValid()) {
            String reason = existingToken.getIsRevoked() ? "revoked" : "expired";
            log.warn("Refresh token is {}: {}", reason, refreshTokenStr.substring(0, 8) + "...");
            throw new RefreshTokenException("Refresh token is " + reason);
        }

        User user = existingToken.getUser();

        // Check if user is still active
        if (user.getIsActive() == null || !user.getIsActive()) {
            existingToken.revoke();
            refreshTokenRepository.save(existingToken);
            throw new RefreshTokenException("User account is deactivated");
        }

        // Revoke the old token (token rotation for security)
        existingToken.revoke();
        refreshTokenRepository.save(existingToken);

        // Create new refresh token
        RefreshToken newRefreshToken = createRefreshToken(user);

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateToken(user);

        // Audit the token refresh
        auditTokenRefresh(user.getId(), user.getEmail());

        log.info("Refreshed tokens for user: {}", user.getEmail());

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .build();
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param refreshTokenStr the refresh token to revoke
     */
    @Transactional
    public void revokeRefreshToken(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.debug("Revoked refresh token: {}", refreshTokenStr.substring(0, 8) + "...");
                });
    }

    /**
     * Revokes all refresh tokens for a user.
     * Used for "logout from all devices" functionality.
     *
     * @param userId the user ID
     * @return number of tokens revoked
     */
    @Transactional
    public int revokeAllUserTokens(Long userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh tokens for user ID: {}", count, userId);
        return count;
    }

    /**
     * Deletes expired tokens (cleanup method).
     * Should be called periodically via a scheduled job.
     *
     * @return number of tokens deleted
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int count = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up {} expired refresh tokens", count);
        return count;
    }

    /**
     * Records a token refresh event for security auditing.
     */
    @SuppressWarnings("null")
    private void auditTokenRefresh(Long userId, String email) {
        try {
            LoginAudit audit = LoginAudit.builder()
                    .userId(userId)
                    .email(email)
                    .eventType(SecurityAuditEventType.TOKEN_REFRESH)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .userAgent(httpServletRequest.getHeader("User-Agent"))
                    .success(true)
                    .build();

            loginAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to audit token refresh", e);
            // Don't fail the refresh if auditing fails
        }
    }

    /**
     * Gets the client IP address from the request.
     */
    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
