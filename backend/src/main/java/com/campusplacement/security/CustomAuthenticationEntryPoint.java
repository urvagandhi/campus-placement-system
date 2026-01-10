package com.campusplacement.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.campusplacement.auth.LoginAudit;
import com.campusplacement.auth.LoginAuditRepository;
import com.campusplacement.auth.SecurityAuditEventType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom entry point for 401 Unauthorized errors.
 * Logs the authentication failure to the database for security auditing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final LoginAuditRepository loginAuditRepository;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String ipAddress = request.getRemoteAddr();
        String path = request.getRequestURI();

        log.warn("Unauthorized access attempt from IP: {} at path: {}", ipAddress, path);

        // Record audit log
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail("Unknown"); // We don't know the user yet
            audit.setEventType(SecurityAuditEventType.AUTHENTICATION_FAILURE);
            audit.setIpAddress(ipAddress);
            audit.setLoginTime(LocalDateTime.now());
            audit.setSuccess(false);
            audit.setFailureReason("401 Unauthorized: " + path);

            loginAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to log authentication failure event", e);
        }

        // Send 401 response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Unauthorized: Please log in to access this resource.\",\"error\":\""
                        + authException.getMessage() + "\"}");
    }
}
