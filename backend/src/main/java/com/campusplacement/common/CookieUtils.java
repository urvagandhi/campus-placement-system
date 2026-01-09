package com.campusplacement.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for secure cookie management.
 *
 * <p>
 * Provides methods for creating httpOnly, Secure cookies for token storage.
 * </p>
 *
 * <p>
 * Cookie settings:
 * <ul>
 * <li>httpOnly: true (prevents JavaScript access - XSS protection)</li>
 * <li>Secure: true in production (HTTPS only)</li>
 * <li>SameSite: Strict (CSRF protection)</li>
 * </ul>
 * </p>
 */
@Component
public class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    /**
     * Creates an access token cookie.
     *
     * @param token     the JWT access token
     * @param maxAgeSec the cookie max age in seconds
     * @return the ResponseCookie
     */
    public ResponseCookie createAccessTokenCookie(String token, long maxAgeSec) {
        @SuppressWarnings("null")
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeSec)
                .sameSite("Strict");

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    /**
     * Creates a refresh token cookie.
     * Path is restricted to /api/v1/auth to minimize exposure.
     *
     * @param token     the refresh token
     * @param maxAgeSec the cookie max age in seconds
     * @return the ResponseCookie
     */
    public ResponseCookie createRefreshTokenCookie(String token, long maxAgeSec) {
        @SuppressWarnings("null")
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/v1/auth")
                .maxAge(maxAgeSec)
                .sameSite("Strict");

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    /**
     * Creates a cookie that clears the access token.
     *
     * @return the ResponseCookie with maxAge=0
     */
    public ResponseCookie createAccessTokenClearCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /**
     * Creates a cookie that clears the refresh token.
     *
     * @return the ResponseCookie with maxAge=0
     */
    public ResponseCookie createRefreshTokenClearCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /**
     * Adds a cookie to the response.
     *
     * @param response the HTTP response
     * @param cookie   the cookie to add
     */
    public void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Gets a cookie value from the request.
     *
     * @param request    the HTTP request
     * @param cookieName the cookie name
     * @return the cookie value, or null if not found
     */
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Gets the access token from cookies.
     *
     * @param request the HTTP request
     * @return the access token, or null if not found
     */
    public String getAccessTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    /**
     * Gets the refresh token from cookies.
     *
     * @param request the HTTP request
     * @return the refresh token, or null if not found
     */
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }
}
