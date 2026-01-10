package com.campusplacement.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
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
 * Custom handler for 403 Forbidden errors.
 * Logs the access denial to the database for security auditing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final LoginAuditRepository loginAuditRepository;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (auth != null) ? auth.getName() : "Anonymous";
        String ipAddress = request.getRemoteAddr();
        String path = request.getRequestURI();

        log.warn("Access denied for user: {} at path: {}", userEmail, path);

        // Record audit log
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(userEmail);
            audit.setEventType(SecurityAuditEventType.ACCESS_DENIED);
            audit.setIpAddress(ipAddress);
            audit.setLoginTime(LocalDateTime.now());
            audit.setSuccess(false);
            audit.setFailureReason("403 Forbidden: " + path);
            // audit.setUserId(); // We might not have easy access to userId here without
            // casting Principal

            loginAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to log access denied event", e);
        }

        // Send 403 response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Access Denied: You do not have permission to access this resource.\"}");
    }
}
