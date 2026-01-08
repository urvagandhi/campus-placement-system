package com.campusplacement.auth.exception;

/**
 * Exception thrown when a user account is deactivated.
 * Results in HTTP 403 Forbidden response.
 */
public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException(String message) {
        super(message);
    }

    public AccountDeactivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
