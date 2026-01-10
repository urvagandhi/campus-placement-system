package com.campusplacement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing token and user details.
 *
 * <p>
 * Matches the API contract:
 *
 * <pre>
 * {
 *   "token": "jwt-token",
 *   "userId": 12,
 *   "role": "COORDINATOR",
 *   "collegeId": 3,
 *   "redirectUrl": "/dashboard/coordinator"
 * }
 * </pre>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /**
     * JWT access token.
     */
    private String token;

    /**
     * Refresh token for obtaining new access tokens.
     */
    private String refreshToken;

    /**
     * Access token expiration time in milliseconds.
     * Used by frontend to schedule automatic refresh.
     */
    private Long expiresIn;

    /**
     * User's database ID.
     */
    private Long userId;

    /**
     * User's role (STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN).
     */
    private String role;

    /**
     * User's college ID (null for SUPER_ADMIN).
     */
    private Long collegeId;

    /**
     * Role-based redirect URL for frontend navigation.
     */
    private String redirectUrl;

    /**
     * Flag indicating user must change their password.
     * When true, frontend should redirect to password change page.
     */
    private Boolean mustChangePassword;

    /**
     * First-login token for password change.
     * Provided when mustChangePassword is true.
     * Used to complete password change without current password.
     */
    private String firstLoginToken;
}
