package com.campusplacement.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import com.campusplacement.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entity representing a refresh token for JWT token renewal.
 *
 * <p>
 * Refresh tokens are stored in the database to enable:
 * <ul>
 * <li>Token rotation (new token on each refresh)</li>
 * <li>Revocation (logout invalidates tokens)</li>
 * <li>Multi-device support (multiple active tokens per user)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Security features:
 * <ul>
 * <li>Single-use: tokens are rotated on each refresh</li>
 * <li>Revocable: can be individually or bulk revoked</li>
 * <li>Expirable: automatic expiration after configured period</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The refresh token value (UUID-based).
     * Unique and used to look up the token during refresh requests.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * The user this refresh token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Expiration timestamp.
     * Tokens are invalid after this time.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been revoked.
     * Revoked tokens cannot be used even if not expired.
     */
    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    /**
     * Timestamp when this token was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * IP address from which this token was created.
     * Used for security auditing.
     */
    @Column(name = "created_ip")
    private String createdIp;

    /**
     * User-Agent header from the client that created this token.
     * Used for security auditing and device identification.
     */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }
    }

    /**
     * Checks if this refresh token is valid for use.
     *
     * @return true if token is not expired and not revoked
     */
    public boolean isValid() {
        return !isRevoked && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Revokes this refresh token.
     */
    public void revoke() {
        this.isRevoked = true;
    }
}
