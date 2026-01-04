package com.campusplacement.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.auth.dto.LoginRequestDTO;
import com.campusplacement.auth.dto.LoginResponseDTO;
import com.campusplacement.auth.dto.RegisterRequestDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication endpoints.
 *
 * <p>
 * Handles user authentication including login, registration, and logout.
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
     * @param request login credentials
     * @return JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        // TODO: Implement login logic
        // 1. Validate credentials
        // 2. Generate JWT token
        // 3. Return token and user details

        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Registers a new user in the system.
     *
     * @param request registration details
     * @return success message
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        // TODO: Implement registration logic
        // 1. Validate unique email
        // 2. Hash password
        // 3. Create user record
        // 4. Send verification email (optional)

        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
    }

    /**
     * Logs out the current user.
     *
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // TODO: Implement logout logic
        // 1. Invalidate JWT token (add to blacklist)
        // 2. Clear any server-side session

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
        // TODO: Implement get current user logic
        // 1. Extract user from security context
        // 2. Return user details

        LoginResponseDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
