package com.campusplacement.auth.exception;

/**
 * Exception thrown when a college is not active or not found.
 * Results in HTTP 404 Not Found response.
 */
public class CollegeInactiveException extends RuntimeException {

    public CollegeInactiveException(String message) {
        super(message);
    }

    public CollegeInactiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
