package com.campusplacement.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token refresh response.
 *
 * <p>
 * Contains new access and refresh tokens after successful token rotation.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDTO {

    /**
     * New JWT access token.
     */
    private String accessToken;

    /**
     * New refresh token (rotated).
     * The old refresh token is invalidated after use.
     */
    private String refreshToken;

    /**
     * Access token expiration time in milliseconds.
     * Used by frontend to schedule automatic refresh.
     */
    private Long expiresIn;
}
