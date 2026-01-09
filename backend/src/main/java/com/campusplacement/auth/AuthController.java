package com.campusplacement.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.auth.dto.LoginRequestDTO;
import com.campusplacement.auth.dto.LoginResponseDTO;
import com.campusplacement.auth.dto.TokenRefreshRequestDTO;
import com.campusplacement.auth.dto.TokenRefreshResponseDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.common.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication endpoints.
 *
 * <p>
 * Authentication is centralized and role-driven. User roles are determined by
 * backend authentication logic and never selected by users, ensuring secure and
 * scalable role-based access control.
 * </p>
 *
 * <p>
 * <strong>Endpoints:</strong>
 * </p>
 * <ul>
 * <li>POST /api/v1/auth/login - User login</li>
 * <li>POST /api/v1/auth/refresh - Refresh access token</li>
 * <li>POST /api/v1/auth/logout - User logout</li>
 * <li>GET /api/v1/auth/me - Get current user info</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Value("${app.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-days:7}")
    private int refreshExpirationDays;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p>
     * Response includes:
     * <ul>
     * <li>token - JWT access token</li>
     * <li>refreshToken - Refresh token for obtaining new access tokens</li>
     * <li>expiresIn - Access token expiration time in milliseconds</li>
     * <li>userId - User's database ID</li>
     * <li>role - User's role (STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN)</li>
     * <li>collegeId - User's college ID (null for SUPER_ADMIN)</li>
     * <li>redirectUrl - Role-based dashboard URL</li>
     * </ul>
     * </p>
     *
     * @param request login credentials (email + password)
     * @return JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse httpResponse) {
        LoginResponseDTO response = authService.login(request);

        // Set httpOnly cookies for tokens
        long accessMaxAgeSec = accessExpirationMs / 1000;
        long refreshMaxAgeSec = refreshExpirationDays * 24 * 60 * 60L;

        cookieUtils.addCookie(httpResponse,
                cookieUtils.createAccessTokenCookie(response.getToken(), accessMaxAgeSec));
        cookieUtils.addCookie(httpResponse,
                cookieUtils.createRefreshTokenCookie(response.getRefreshToken(), refreshMaxAgeSec));

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * <p>
     * This endpoint:
     * <ul>
     * <li>Validates the refresh token</li>
     * <li>Rotates the refresh token (old one is invalidated)</li>
     * <li>Returns new access and refresh tokens</li>
     * </ul>
     * </p>
     *
     * @param request the refresh token
     * @return new access token and rotated refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponseDTO>> refreshToken(
            @RequestBody(required = false) TokenRefreshRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        // Get refresh token from cookie if not in body
        String refreshToken = (request != null && request.getRefreshToken() != null)
                ? request.getRefreshToken()
                : cookieUtils.getRefreshTokenFromCookies(httpRequest);

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new com.campusplacement.auth.exception.RefreshTokenException("Refresh token is required");
        }

        TokenRefreshRequestDTO refreshRequest = TokenRefreshRequestDTO.builder()
                .refreshToken(refreshToken)
                .build();

        TokenRefreshResponseDTO response = authService.refreshToken(refreshRequest);

        // Set new cookies
        long accessMaxAgeSec = accessExpirationMs / 1000;
        long refreshMaxAgeSec = refreshExpirationDays * 24 * 60 * 60L;

        cookieUtils.addCookie(httpResponse,
                cookieUtils.createAccessTokenCookie(response.getAccessToken(), accessMaxAgeSec));
        cookieUtils.addCookie(httpResponse,
                cookieUtils.createRefreshTokenCookie(response.getRefreshToken(), refreshMaxAgeSec));

        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Logs out the current user.
     *
     * <p>
     * Revokes the provided refresh token and clears the security context.
     * </p>
     *
     * @param request optional refresh token to revoke
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody(required = false) TokenRefreshRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        // Get refresh token from cookie if not in body
        String refreshToken = (request != null && request.getRefreshToken() != null)
                ? request.getRefreshToken()
                : cookieUtils.getRefreshTokenFromCookies(httpRequest);

        authService.logout(refreshToken);

        // Clear cookies
        cookieUtils.addCookie(httpResponse, cookieUtils.createAccessTokenClearCookie());
        cookieUtils.addCookie(httpResponse, cookieUtils.createRefreshTokenClearCookie());

        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    /**
     * Returns the current authenticated user's information.
     *
     * @return current user details
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> getCurrentUser() {
        LoginResponseDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
