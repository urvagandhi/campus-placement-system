package com.campusplacement.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.campusplacement.common.CookieUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 *
 * <p>
 * Flow:
 * <ol>
 * <li>Extract token from Authorization header or httpOnly cookie</li>
 * <li>Validate token using JwtTokenProvider</li>
 * <li>Load user details and set authentication context</li>
 * <li>Continue filter chain</li>
 * </ol>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtils cookieUtils;
    private final com.campusplacement.settings.SystemSettingsService systemSettingsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", userDetails.getUsername());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // MAITENANCE MODE CHECK
        // Must be done AFTER authentication attempt so we know the user's role
        // EXCEPTION: Allow login endpoint to pass through
        // EXCEPTION: Allow health/actuator endpoints
        // EXCEPTION: Allow OPTIONS requests (CORS Preflight)
        String path = request.getRequestURI();
        boolean isPublicEndpoint = path.contains("/auth/login") ||
                path.contains("/actuator") ||
                path.contains("/health") ||
                path.contains("/swagger") ||
                path.contains("/api-docs") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod());

        if (!isPublicEndpoint && systemSettingsService.isMaintenanceMode()) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (!isSuperAdmin) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"System is under maintenance\",\"error\":\"SERVICE_UNAVAILABLE\"}");
                return; // Stop chain
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the request.
     * Priority: Authorization header > Cookie
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // First, try Authorization header
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // Fall back to httpOnly cookie
        String cookieToken = cookieUtils.getAccessTokenFromCookies(request);
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        return null;
    }
}
