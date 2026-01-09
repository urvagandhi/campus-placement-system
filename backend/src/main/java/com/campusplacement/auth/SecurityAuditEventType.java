package com.campusplacement.auth;

/**
 * Enum defining types of security audit events.
 */
public enum SecurityAuditEventType {
    LOGIN,
    REGISTER,
    LOGOUT,
    PASSWORD_RESET,
    PASSWORD_CHANGE,
    ACCOUNT_LOCKED
}
