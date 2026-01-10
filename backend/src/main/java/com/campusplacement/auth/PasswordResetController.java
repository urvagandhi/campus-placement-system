package com.campusplacement.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.auth.dto.ChangePasswordRequest;
import com.campusplacement.auth.dto.ForgotPasswordRequest;
import com.campusplacement.auth.dto.ResetPasswordRequest;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for password reset and change operations.
 *
 * <p>
 * <strong>Endpoints:</strong>
 * </p>
 * <ul>
 * <li>POST /forgot-password - Initiate forgot password flow (public)</li>
 * <li>POST /reset-password - Complete password reset with token (public)</li>
 * <li>GET /validate-token/{token} - Validate reset token (public)</li>
 * <li>POST /change-password - Change password (authenticated)</li>
 * <li>POST /force-reset/{userId} - Force password reset (admin only)</li>
 * <li>GET /must-change-password - Check if password change is required</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/password")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Management", description = "Password reset and change operations")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // ==================== Public Endpoints ====================

    /**
     * Initiates forgot password flow.
     * Sends password reset email if user exists (silent failure for security).
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate forgot password", description = "Sends reset email if user exists. Always returns success for security.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        PasswordResetToken token = passwordResetService.createForgotPasswordToken(
                request.getEmail(), clientIp);

        if (token != null) {
            // TODO: Send email with reset link
            // emailService.sendPasswordResetEmail(token.getUser().getEmail(),
            // token.getToken());
            log.info("Password reset email sent to: {}", request.getEmail());
        }

        // Always return success (don't reveal if user exists)
        return ResponseEntity.ok(ApiResponse.success(null,
                "If an account exists with this email, you will receive a password reset link."));
    }

    /**
     * Validates a password reset token.
     * Used by frontend to check if token is valid before showing reset form.
     */
    @GetMapping("/validate-token/{token}")
    @Operation(summary = "Validate reset token")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @PathVariable String token) {

        try {
            PasswordResetToken resetToken = passwordResetService.validateToken(token);
            TokenValidationResponse response = new TokenValidationResponse(
                    true,
                    resetToken.getTokenType().name(),
                    resetToken.getUser().getEmail().replaceAll("(?<=.{2}).(?=.*@)", "*"));
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            TokenValidationResponse response = new TokenValidationResponse(false, null, null);
            return ResponseEntity.ok(ApiResponse.success(response, "Invalid or expired token"));
        }
    }

    /**
     * Completes password reset with token.
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match"));
        }

        // Validate password strength
        String strengthError = validatePasswordStrength(request.getNewPassword());
        if (strengthError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(strengthError));
        }

        String clientIp = getClientIp(httpRequest);
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        passwordResetService.completePasswordReset(request.getToken(), hashedPassword, clientIp);

        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. You can now login."));
    }

    // ==================== Authenticated Endpoints ====================

    /**
     * Checks if the current user must change their password.
     */
    @GetMapping("/must-change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if password change required")
    public ResponseEntity<ApiResponse<MustChangePasswordResponse>> checkMustChangePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean mustChange = passwordResetService.isPasswordChangeRequired(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(
                new MustChangePasswordResponse(mustChange)));
    }

    /**
     * Changes password for authenticated user.
     * Supports both regular change (with current password) and first-login (with
     * token).
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest) {

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match"));
        }

        // Validate password strength
        String strengthError = validatePasswordStrength(request.getNewPassword());
        if (strengthError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(strengthError));
        }

        String clientIp = getClientIp(httpRequest);

        // First-login flow with token
        if (request.getFirstLoginToken() != null && !request.getFirstLoginToken().isBlank()) {
            String hashedPassword = passwordEncoder.encode(request.getNewPassword());
            passwordResetService.completePasswordReset(
                    request.getFirstLoginToken(), hashedPassword, clientIp);
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
        }

        // Regular password change flow
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Current password is required"));
        }

        @SuppressWarnings("null")
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Current password is incorrect"));
        }

        // Prevent reusing current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password cannot be the same as current password"));
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        log.info("Password changed for user: {}", currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }

    // ==================== Admin Endpoints ====================

    /**
     * Forces a password reset for a user (admin action).
     * User will be required to change password on next login.
     */
    @PostMapping("/force-reset/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Force password reset (admin)")
    public ResponseEntity<ApiResponse<ForceResetResponse>> forcePasswordReset(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        PasswordResetToken token = passwordResetService.forcePasswordReset(userId, clientIp);

        // Return token for admin to share with user (or send via email)
        return ResponseEntity.ok(ApiResponse.success(
                new ForceResetResponse(token.getToken(), token.getExpiresAt().toString()),
                "Password reset initiated. User must change password on next login."));
    }

    // ==================== Helper Methods ====================

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Password must contain at least one special character";
        }
        return null;
    }

    // ==================== Response Records ====================

    public record TokenValidationResponse(boolean valid, String tokenType, String maskedEmail) {
    }

    public record MustChangePasswordResponse(boolean mustChangePassword) {
    }

    public record ForceResetResponse(String token, String expiresAt) {
    }
}
