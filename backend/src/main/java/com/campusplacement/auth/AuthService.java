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
import com.campusplacement.auth.exception.AccountDeactivatedException;
import com.campusplacement.auth.exception.AuthenticationException;
import com.campusplacement.auth.exception.CollegeInactiveException;
import com.campusplacement.colleges.College;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest httpServletRequest;

    /**
     * Authenticates a user and generates a JWT token.
     *
     * <p>
     * Flow:
     * </p>
     * <ol>
     * <li>Find user by email</li>
     * <li>Verify password</li>
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
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.getEmail().toLowerCase().trim();

        // 1. Find user by email (with college eager fetch)
        User user = userRepository.findByEmailWithCollege(email)
                .orElseThrow(() -> {
                    // Audit failed login for unknown email
                    auditLogin(null, email, false);
                    return new AuthenticationException("Invalid credentials");
                });

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogin(user.getId(), email, false);
            throw new AuthenticationException("Invalid credentials");
        }

        // 3. Check user is active
        if (user.getIsActive() == null || !user.getIsActive()) {
            auditLogin(user.getId(), email, false);
            throw new AccountDeactivatedException("Account is deactivated. Please contact administrator.");
        }

        // 4. Check college is active (if not SUPER_ADMIN)
        if (user.getRole() != UserRole.SUPER_ADMIN) {
            College college = user.getCollege();
            if (college == null) {
                throw new CollegeInactiveException("User is not associated with any college");
            }
            if (college.getIsActive() == null || !college.getIsActive()) {
                auditLogin(user.getId(), email, false);
                throw new CollegeInactiveException("College is not active. Please contact administrator.");
            }
        }

        // 5. Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        // 6. Build redirect URL
        String redirectUrl = buildRedirectUrl(user.getRole());

        // 7. Update last login & audit
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        auditLogin(user.getId(), email, true);

        log.info("User {} logged in successfully with role {}", email, user.getRole());

        // 8. Return response
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .redirectUrl(redirectUrl)
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
        log.info("New user registered: {}", email);
    }

    /**
     * Logs out the current user.
     *
     * <p>
     * Note: With JWT, logout is handled client-side by clearing the token.
     * This method clears the security context on the server.
     * </p>
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        log.debug("User logged out, security context cleared");
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
    private void auditLogin(Long userId, String email, boolean success) {
        try {
            LoginAudit audit = LoginAudit.builder()
                    .userId(userId)
                    .email(email)
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
