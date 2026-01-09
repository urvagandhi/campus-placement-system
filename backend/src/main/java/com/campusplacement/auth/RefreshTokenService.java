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
    private final SecurityAlertService securityAlertService;
    private final SecurityMetricsService securityMetricsService;

    @Value("${app.jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    @Value("${app.auth.session.max-concurrent-sessions:5}")
    private int maxConcurrentSessions;

    @Value("${app.auth.session.idle-timeout-minutes:30}")
    private int idleTimeoutMinutes;

    /**
     * Creates a new refresh token for the given user.
     *
     * @param user the user to create the token for
     * @return the created refresh token
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        return createRefreshToken(user, null, null);
    }

    /**
     * Creates a new refresh token with family tracking.
     */
    @SuppressWarnings("null")
    @Transactional
    public RefreshToken createRefreshToken(User user, String familyId, String parentToken) {
        // Enforce concurrent session limit (only for new families)
        if (familyId == null) {
            long activeCount = refreshTokenRepository.countActiveTokensByUserId(user.getId());
            if (activeCount >= maxConcurrentSessions) {
                log.warn("User {} reached max concurrent sessions ({}). Revoking oldest.", user.getEmail(),
                        maxConcurrentSessions);
                refreshTokenRepository.findValidTokensByUserId(user.getId(), LocalDateTime.now())
                        .stream()
                        .skip(maxConcurrentSessions - 1)
                        .forEach(RefreshToken::revoke);
            }
        }

        String actualFamilyId = (familyId != null) ? familyId : UUID.randomUUID().toString();
        String ipAddress = getClientIpAddress();
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String fingerprint = generateDeviceFingerprint(ipAddress, userAgent);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .isRevoked(false)
                .createdIp(ipAddress)
                .userAgent(userAgent)
                .deviceFingerprint(fingerprint)
                .familyId(actualFamilyId)
                .parentToken(parentToken)
                .lastUsedAt(LocalDateTime.now())
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token [Family: {}] for user: {}", actualFamilyId.substring(0, 8), user.getEmail());
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

        // 1. REUSE DETECTION (Token Theft Protection)
        if (existingToken.getIsRevoked()) {
            log.error("REFRESH TOKEN REUSE DETECTED! Family: {}. Revoking all tokens in family.",
                    existingToken.getFamilyId());
            refreshTokenRepository.revokeByFamilyId(existingToken.getFamilyId());
            auditSecurityEvent(existingToken.getUser().getId(), existingToken.getUser().getEmail(),
                    SecurityAuditEventType.TOKEN_REUSE_DETECTED, "Token already revoked. Potential theft/replay.");

            securityAlertService.triggerAlert(existingToken.getUser().getId(), existingToken.getUser().getEmail(),
                    SecurityAuditEventType.TOKEN_REUSE_DETECTED,
                    "Refresh token reuse detected. Potential theft by attacker.", "CRITICAL", getClientIpAddress());

            securityMetricsService.incrementTokenReuse();
            securityMetricsService.incrementRefreshFailure();
            throw new RefreshTokenException("Security alert: Session compromised. All devices logged out.");
        }

        // 2. EXPIRATION CHECK
        if (!existingToken.isValid()) {
            log.warn("Refresh token expired: {}", refreshTokenStr.substring(0, 8) + "...");
            throw new RefreshTokenException("Refresh token is expired");
        }

        // 3. IDLE TIMEOUT
        if (existingToken.getLastUsedAt().plusMinutes(idleTimeoutMinutes).isBefore(LocalDateTime.now())) {
            log.warn("Session idle timeout reached for user: {}", existingToken.getUser().getEmail());
            existingToken.revoke();
            refreshTokenRepository.save(existingToken);
            securityMetricsService.incrementIdleTimeout();
            securityMetricsService.incrementRefreshFailure();
            throw new RefreshTokenException("Session expired due to inactivity.");
        }

        // 4. DEVICE BINDING (Fingerprinting)
        String currentIp = getClientIpAddress();
        String currentUserAgent = httpServletRequest.getHeader("User-Agent");
        String currentFingerprint = generateDeviceFingerprint(currentIp, currentUserAgent);

        if (existingToken.getDeviceFingerprint() != null
                && !existingToken.getDeviceFingerprint().equals(currentFingerprint)) {
            log.warn("DEVIATION DETECTED: Device fingerprint mismatch for family {}. Possible token theft.",
                    existingToken.getFamilyId());
            auditSecurityEvent(existingToken.getUser().getId(), existingToken.getUser().getEmail(),
                    SecurityAuditEventType.UNAUTHORIZED_DEVICE,
                    "Fingerprint mismatch. Request device != Creation device");

            securityAlertService.triggerAlert(existingToken.getUser().getId(), existingToken.getUser().getEmail(),
                    SecurityAuditEventType.UNAUTHORIZED_DEVICE, "Token presented from unauthorized device.", "HIGH",
                    getClientIpAddress());

            securityMetricsService.incrementUnauthorizedDevice();
            securityMetricsService.incrementRefreshFailure();
            // Optional: revoke family on mismatch for extreme security
            // refreshTokenRepository.revokeByFamilyId(existingToken.getFamilyId());
            // For now, just block the refresh
            throw new RefreshTokenException("Unauthorized device. Please login again.");
        }

        User user = existingToken.getUser();

        // 5. USER STATUS CHECK
        if (user.getIsActive() == null || !user.getIsActive()) {
            existingToken.revoke();
            refreshTokenRepository.save(existingToken);
            throw new RefreshTokenException("User account is deactivated");
        }

        // 6. ROTATION & ISSUANCE
        existingToken.revoke();
        refreshTokenRepository.save(existingToken);

        // Create new token in same family
        RefreshToken newRefreshToken = createRefreshToken(user, existingToken.getFamilyId(), existingToken.getToken());

        String newAccessToken = jwtTokenProvider.generateToken(user);
        auditTokenRefresh(user.getId(), user.getEmail());
        securityMetricsService.incrementRefreshSuccess();

        log.info("Successfully rotated token for family: {}", existingToken.getFamilyId().substring(0, 8));

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
     * Retrieves all active sessions for a user.
     *
     * @param userId       current user ID
     * @param currentToken the token currently being used
     * @return list of active sessions
     */
    @Transactional(readOnly = true)
    public java.util.List<com.campusplacement.auth.dto.ActiveSessionDTO> getActiveSessions(Long userId,
            String currentToken) {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now())
                .stream()
                .map(token -> com.campusplacement.auth.dto.ActiveSessionDTO.builder()
                        .id(token.getId())
                        .ipAddress(token.getCreatedIp())
                        .userAgent(token.getUserAgent())
                        .createdAt(token.getCreatedAt())
                        .lastActiveAt(token.getLastUsedAt())
                        .expiresAt(token.getExpiresAt())
                        .isCurrent(token.getToken().equals(currentToken))
                        .build())
                .toList();
    }

    /**
     * Revokes a specific session by ID.
     *
     * @param sessionId the session/token ID
     * @param userId    current user ID (for security verification)
     */
    @SuppressWarnings("null")
    @Transactional
    public void revokeSession(Long sessionId, Long userId) {
        refreshTokenRepository.findById(sessionId)
                .ifPresent(token -> {
                    if (token.getUser().getId().equals(userId)) {
                        token.revoke();
                        refreshTokenRepository.save(token);
                        log.info("Revoked session {} for user {}", sessionId, userId);
                    } else {
                        log.warn("Unauthorized attempt to revoke session {} by user {}", sessionId, userId);
                    }
                });
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
    private void auditTokenRefresh(Long userId, String email) {
        auditSecurityEvent(userId, email, SecurityAuditEventType.TOKEN_REFRESH, null);
    }

    /**
     * Records a generic security event for auditing.
     */
    @SuppressWarnings("null")
    private void auditSecurityEvent(Long userId, String email, SecurityAuditEventType eventType, String reason) {
        try {
            LoginAudit audit = LoginAudit.builder()
                    .userId(userId)
                    .email(email)
                    .eventType(eventType)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .userAgent(httpServletRequest.getHeader("User-Agent"))
                    .success(reason == null)
                    .failureReason(reason)
                    .build();

            loginAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to audit security event: {}", eventType, e);
        }
    }

    /**
     * Generates a unique device fingerprint based on IP and User-Agent.
     */
    private String generateDeviceFingerprint(String ip, String ua) {
        try {
            String raw = (ip != null ? ip : "unknown") + "|" + (ua != null ? ua : "generic");
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate device fingerprint", e);
            return "fallback-fingerprint";
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
