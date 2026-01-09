package com.campusplacement.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for JWT token generation and validation.
 *
 * <p>
 * <strong>JWT Payload Structure (Phase-1):</strong>
 * </p>
 *
 * <pre>
 * {
 *   "ver": 1,
 *   "sub": "user@email.com",
 *   "uid": 12,
 *   "role": "COORDINATOR",
 *   "cid": 3,
 *   "iat": 1704547200,
 *   "exp": 1704633600
 * }
 * </pre>
 *
 * <p>
 * <strong>JWT Payload Structure (Phase-2 - Future):</strong>
 * </p>
 *
 * <pre>
 * {
 *   "ver": 2,
 *   "sub": "user@email.com",
 *   "uid": 12,
 *   "role": "COORDINATOR",
 *   "cid": 3,
 *   "oid": 6,
 *   "iat": 1704547200,
 *   "exp": 1704633600
 * }
 * </pre>
 *
 * <p>
 * Custom claims:
 * </p>
 * <ul>
 * <li>ver - Token version (for backward compatibility)</li>
 * <li>uid - User ID</li>
 * <li>cid - College ID (null for SUPER_ADMIN)</li>
 * <li>oid - Organization Unit ID (Phase-2, optional)</li>
 * </ul>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * Token version for backward compatibility.
     * Increment when breaking changes are made to token structure.
     */
    private static final int TOKEN_VERSION = 1;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for the given user (without org unit).
     *
     * @param user the user entity
     * @return JWT token string
     */
    public String generateToken(User user) {
        return generateToken(user, null);
    }

    /**
     * Generates a JWT token for the given user with optional org unit ID.
     * Future-ready for Phase-2 org-aware authorization.
     *
     * @param user      the user entity
     * @param orgUnitId the organization unit ID (optional, for Phase-2)
     * @return JWT token string
     */
    public String generateToken(User user, Long orgUnitId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpirationMs);

        var builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("ver", TOKEN_VERSION) // IMPROVEMENT #4: Token versioning
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey());

        // Add college ID only if user has a college (not SUPER_ADMIN)
        if (user.getCollege() != null) {
            builder.claim("cid", user.getCollege().getId());
        }

        // Phase-2 ready: Add org unit ID if provided
        if (orgUnitId != null) {
            builder.claim("oid", orgUnitId);
        }

        return builder.compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gets the email (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the email
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Gets the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public Long getUserIdFromToken(String token) {
        Object uid = getClaims(token).get("uid");
        if (uid instanceof Integer) {
            return ((Integer) uid).longValue();
        }
        return (Long) uid;
    }

    /**
     * Gets the role from a JWT token.
     *
     * @param token the JWT token
     * @return the user role
     */
    public UserRole getRoleFromToken(String token) {
        String roleName = getClaims(token).get("role", String.class);
        return UserRole.valueOf(roleName);
    }

    /**
     * Gets the college ID from a JWT token.
     *
     * @param token the JWT token
     * @return the college ID, or null for SUPER_ADMIN
     */
    public Long getCollegeIdFromToken(String token) {
        Object cid = getClaims(token).get("cid");
        if (cid == null) {
            return null;
        }
        if (cid instanceof Integer) {
            return ((Integer) cid).longValue();
        }
        return (Long) cid;
    }

    /**
     * Gets the token version from a JWT token.
     * Used for backward compatibility checks.
     *
     * @param token the JWT token
     * @return the version, or null if not present (old tokens)
     */
    public Integer getTokenVersion(String token) {
        Object ver = getClaims(token).get("ver");
        if (ver == null) {
            return null;
        }
        if (ver instanceof Integer) {
            return (Integer) ver;
        }
        return null;
    }

    /**
     * Gets the organization unit ID from a JWT token (Phase-2).
     *
     * @param token the JWT token
     * @return the org unit ID, or null if not present
     */
    public Long getOrgUnitIdFromToken(String token) {
        Object oid = getClaims(token).get("oid");
        if (oid == null) {
            return null;
        }
        if (oid instanceof Integer) {
            return ((Integer) oid).longValue();
        }
        return (Long) oid;
    }

    /**
     * Gets the current token version.
     *
     * @return the current version
     */
    public int getCurrentTokenVersion() {
        return TOKEN_VERSION;
    }

    /**
     * Gets the access token expiration time in milliseconds.
     *
     * @return expiration time in ms
     */
    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }
}
