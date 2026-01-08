package com.campusplacement.auth.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 * Results in HTTP 401 Unauthorized response.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
