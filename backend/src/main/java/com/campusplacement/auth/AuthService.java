package com.campusplacement.auth;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.auth.dto.LoginRequestDTO;
import com.campusplacement.auth.dto.LoginResponseDTO;
import com.campusplacement.auth.dto.RegisterRequestDTO;
import com.campusplacement.auth.dto.TokenRefreshRequestDTO;
import com.campusplacement.auth.dto.TokenRefreshResponseDTO;
import com.campusplacement.auth.exception.AccountDeactivatedException;
import com.campusplacement.auth.exception.AuthenticationException;
import com.campusplacement.auth.exception.CollegeInactiveException;
import com.campusplacement.colleges.College;
import com.campusplacement.common.CookieUtils;
import com.campusplacement.common.UserRole;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.security.JwtTokenProvider;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for authentication operations.
 *
 * <p>
 * Handles the business logic for user authentication, registration, and token
 * management.
 * </p>
 *
 * <p>
 * <strong>Authentication is centralized and role-driven. User roles are
 * determined
 * by backend authentication logic and never selected by users, ensuring secure
 * and scalable role-based access control.</strong>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final LoginAuditRepository loginAuditRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest httpServletRequest;
    private final CookieUtils cookieUtils;
    private final com.campusplacement.settings.SystemSettingsService systemSettingsService;

    /**
     * Authenticates a user and generates a JWT token.
     *
     * <p>
     * Flow:
     * </p>
     * <ol>
     * <li>Find user by email</li>
     * <li>Verify password</li>
     * <li>Check maintenance mode (if not SUPER_ADMIN)</li>
     * <li>Check user is active</li>
     * <li>Check college is active (if not SUPER_ADMIN)</li>
     * <li>Generate JWT token</li>
     * <li>Build redirect URL based on role</li>
     * <li>Update last login and audit</li>
     * </ol>
     *
     * @param request login credentials
     * @return login response with token and user details
     * @throws AuthenticationException     if credentials are invalid
     * @throws AccountDeactivatedException if account is disabled
     * @throws CollegeInactiveException    if college is inactive
     */
    @SuppressWarnings("null")
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 0. HONEYPOT CHECK (Anti-Automation)
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            log.warn("BOT DETECTED: Honeypot field filled by robot! IP: {}", getClientIpAddress());
            // Return specific error so frontend can instruct user to refresh (handling
            // false positives)
            throw new AuthenticationException("Anti-automation check failed. Please refresh the page.");
        }

        String email = request.getEmail().toLowerCase().trim();

        // 1. Find user by email (with college eager fetch)
        User user = userRepository.findByEmailWithCollege(email)
                .orElseThrow(() -> {
                    // Audit failed login for unknown email
                    auditLogin(null, email, false, SecurityAuditEventType.LOGIN);
                    return new AuthenticationException("Invalid credentials");
                });

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogin(user.getId(), email, false, SecurityAuditEventType.LOGIN);
            throw new AuthenticationException("Invalid credentials");
        }

        // 2.5 MAINTENANCE MODE CHECK
        if (systemSettingsService.isMaintenanceMode() && user.getRole() != UserRole.SUPER_ADMIN) {
            log.warn("Login blocked due to maintenance mode for user: {}", email);
            auditLogin(user.getId(), email, false, SecurityAuditEventType.LOGIN);
            throw new AuthenticationException("System is under maintenance. Please try again later...!!!");
        }

        // 3. Check user is active
        if (user.getIsActive() == null || !user.getIsActive()) {
            auditLogin(user.getId(), email, false, SecurityAuditEventType.LOGIN);
            throw new AccountDeactivatedException("Account is deactivated. Please contact administrator.");
        }

        // 4. Check college is active (if not SUPER_ADMIN)
        if (user.getRole() != UserRole.SUPER_ADMIN) {
            College college = user.getCollege();
            if (college == null) {
                throw new CollegeInactiveException("User is not associated with any college");
            }
            if (college.getIsActive() == null || !college.getIsActive()) {
                auditLogin(user.getId(), email, false, SecurityAuditEventType.LOGIN);
                throw new CollegeInactiveException(
                        "Access Denied: Your institution has been deactivated. Please contact your administrator.");
            }
        }

        // 5. Generate JWT access token
        String token = jwtTokenProvider.generateToken(user);

        // 6. Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 7. Build redirect URL
        String redirectUrl = buildRedirectUrl(user.getRole());

        // 8. Update last login & audit
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        auditLogin(user.getId(), email, true, SecurityAuditEventType.LOGIN);

        log.info("User {} logged in successfully with role {}", email, user.getRole());

        // 9. Check if password change is required
        boolean mustChangePassword = Boolean.TRUE.equals(user.getMustChangePassword());
        String firstLoginToken = null;

        if (mustChangePassword) {
            // Create a first-login token for the password change flow
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenType(PasswordResetToken.TokenType.FIRST_LOGIN)
                    .expiresAt(java.time.LocalDateTime.now().plusHours(24))
                    .requestedIp(getClientIpAddress())
                    .build();
            resetToken = passwordResetTokenRepository.save(resetToken);
            firstLoginToken = resetToken.getToken();
            log.info("First-login token created for user {} - password change required", user.getId());
        }

        // 10. Return response with both tokens
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .userId(user.getId())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .redirectUrl(mustChangePassword ? "/change-password" : redirectUrl)
                .mustChangePassword(mustChangePassword)
                .firstLoginToken(firstLoginToken)
                .build();
    }

    /**
     * Registers a new user in the system.
     *
     * @param request registration details
     * @throws RuntimeException if email already exists
     */
    @SuppressWarnings("null")
    @Transactional
    public void register(RegisterRequestDTO request) {
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String email = request.getEmail().toLowerCase().trim();

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Create user with hashed password and default STUDENT role
        User user = User.builder()
                .name(request.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STUDENT) // Default role - system decided
                .isActive(true)
                .build();

        userRepository.save(user);
        auditLogin(user.getId(), email, true, SecurityAuditEventType.REGISTER);
        log.info("New user registered: {}", email);
    }

    /**
     * Logs out the current user.
     *
     * <p>
     * Revokes all refresh tokens and clears the security context.
     * </p>
     *
     * @param refreshToken the refresh token to revoke (optional)
     */
    @Transactional
    public void logout(String refreshToken) {
        // Revoke the specific refresh token if provided
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
        log.debug("User logged out, security context cleared");
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return new access and refresh tokens
     */
    @Transactional
    public TokenRefreshResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        return refreshTokenService.refreshAccessToken(request.getRefreshToken());
    }

    /**
     * Revokes all refresh tokens for a user (logout from all devices).
     *
     * @param userId the user ID
     * @return number of tokens revoked
     */
    @Transactional
    public int logoutAllDevices(Long userId) {
        return refreshTokenService.revokeAllUserTokens(userId);
    }

    /**
     * Retrieves all active sessions for the current authenticated user.
     *
     * @return list of active sessions
     */
    @Transactional(readOnly = true)
    public java.util.List<com.campusplacement.auth.dto.ActiveSessionDTO> getActiveSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AuthenticationException("Not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Get current refresh token from cookie to identify "current" session
        String currentRefreshToken = cookieUtils.getRefreshTokenFromCookies(httpServletRequest);

        return refreshTokenService.getActiveSessions(userDetails.getId(), currentRefreshToken);
    }

    /**
     * Revokes a specific session for the current user.
     *
     * @param sessionId the session/token ID to revoke
     */
    @Transactional
    public void revokeSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AuthenticationException("Not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        refreshTokenService.revokeSession(sessionId, userDetails.getId());
    }

    /**
     * Retrieves the current authenticated user's information.
     *
     * @return current user details
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("Not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new AuthenticationException("Invalid authentication principal");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        @SuppressWarnings("null")
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .redirectUrl(buildRedirectUrl(user.getRole()))
                .build();
    }

    /**
     * Builds the redirect URL based on user role.
     *
     * @param role the user role
     * @return the dashboard URL for the role
     */
    private String buildRedirectUrl(UserRole role) {
        return switch (role) {
            case STUDENT -> "/dashboard/student";
            case COORDINATOR -> "/dashboard/coordinator";
            case ADMIN -> "/dashboard/admin";
            case SUPER_ADMIN -> "/dashboard/superadmin";
        };
    }

    /**
     * Records a login attempt for security auditing.
     *
     * @param userId  the user ID (null for unknown email attempts)
     * @param email   the email used for login
     * @param success whether the login was successful
     */
    @SuppressWarnings("null")
    private void auditLogin(Long userId, String email, boolean success, SecurityAuditEventType eventType) {
        try {
            LoginAudit audit = LoginAudit.builder()
                    .userId(userId)
                    .email(email)
                    .eventType(eventType)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .userAgent(httpServletRequest.getHeader("User-Agent"))
                    .success(success)
                    .build();

            loginAuditRepository.save(audit);
            log.debug("Login audit recorded for email: {}, success: {}", email, success);
        } catch (Exception e) {
            log.error("Failed to record login audit", e);
            // Don't fail the login if auditing fails
        }
    }

    /**
     * Gets the client IP address from the request.
     *
     * @return the client IP address
     */
    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
