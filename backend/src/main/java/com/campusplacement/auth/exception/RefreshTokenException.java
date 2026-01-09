package com.campusplacement.auth.exception;

/**
 * Exception thrown when refresh token operations fail.
 *
 * <p>
 * This exception is thrown when:
 * <ul>
 * <li>Refresh token is not found</li>
 * <li>Refresh token is expired</li>
 * <li>Refresh token is revoked</li>
 * <li>Refresh token is invalid</li>
 * </ul>
 * </p>
 *
 * <p>
 * Results in HTTP 401 Unauthorized response.
 * </p>
 */
public class RefreshTokenException extends RuntimeException {

    public RefreshTokenException(String message) {
        super(message);
    }

    public RefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
