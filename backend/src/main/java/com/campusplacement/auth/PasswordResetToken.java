package com.campusplacement.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import com.campusplacement.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a password reset token.
 *
 * <p>
 * Used for two scenarios:
 * </p>
 * <ul>
 * <li><strong>FIRST_LOGIN:</strong> When TPO/Coordinator creates accounts,
 * students must change password on first login</li>
 * <li><strong>FORGOT_PASSWORD:</strong> Self-service password reset via
 * email</li>
 * </ul>
 *
 * <p>
 * <strong>Security Features:</strong>
 * </p>
 * <ul>
 * <li>Cryptographically secure token (32 bytes, Base64 encoded)</li>
 * <li>Single-use: token is invalidated after use</li>
 * <li>Time-limited: configurable expiration (default 24h for first-login, 1h
 * for forgot)</li>
 * <li>Tracks usage for audit purposes</li>
 * </ul>
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH_BYTES = 32;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The secure token value (Base64 encoded, 32 bytes).
     * Sent via email link or used in first-login flow.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * The user this token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of password reset token.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    /**
     * Expiration timestamp.
     * Token is invalid after this time.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been used.
     * Used tokens cannot be reused.
     */
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    /**
     * Timestamp when this token was used (password changed).
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * IP address from which the password was reset.
     * For security auditing.
     */
    @Column(name = "used_ip")
    private String usedIp;

    /**
     * Timestamp when this token was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * IP address from which the reset was requested.
     * For security auditing.
     */
    @Column(name = "requested_ip")
    private String requestedIp;

    /**
     * Token type enum.
     */
    public enum TokenType {
        /**
         * First login token - issued when account is created by coordinator.
         * Longer validity (24-72 hours).
         */
        FIRST_LOGIN,

        /**
         * Forgot password token - self-service password reset.
         * Shorter validity (1 hour).
         */
        FORGOT_PASSWORD
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.token == null) {
            this.token = generateSecureToken();
        }
    }

    /**
     * Generates a cryptographically secure token.
     *
     * @return Base64-encoded secure token
     */
    public static String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Checks if this token is valid for use.
     *
     * @return true if token is not used and not expired
     */
    public boolean isValid() {
        return !isUsed && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Marks this token as used.
     *
     * @param ip IP address from which the password was reset
     */
    public void markAsUsed(String ip) {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
        this.usedIp = ip;
    }

    /**
     * Checks if this token has expired.
     *
     * @return true if token is expired
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
