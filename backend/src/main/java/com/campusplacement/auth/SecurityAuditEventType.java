package com.campusplacement.auth;

/**
 * Enum defining types of security audit events.
 */
public enum SecurityAuditEventType {
    LOGIN,
    REGISTER,
    LOGOUT,
    TOKEN_REFRESH,
    PASSWORD_RESET,
    PASSWORD_CHANGE,
    ACCOUNT_LOCKED,
    PROFILE_UPDATE,
    PROFILE_VIEW,
    TOKEN_REUSE_DETECTED,
    UNAUTHORIZED_DEVICE,
    ACCESS_DENIED,
    AUTHENTICATION_FAILURE
}
