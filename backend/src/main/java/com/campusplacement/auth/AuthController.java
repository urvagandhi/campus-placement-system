package com.campusplacement.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.auth.dto.LoginRequestDTO;
import com.campusplacement.auth.dto.LoginResponseDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

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
 * <li>POST /api/v1/auth/register - User registration</li>
 * <li>POST /api/v1/auth/logout - User logout</li>
 * <li>GET /api/v1/auth/me - Get current user info</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p>
     * Response includes:
     * <ul>
     * <li>token - JWT access token</li>
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
            @Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Logs out the current user.
     *
     * <p>
     * Note: With JWT, logout is primarily client-side (clear token).
     * This endpoint clears the server-side security context.
     * </p>
     *
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        authService.logout();
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
