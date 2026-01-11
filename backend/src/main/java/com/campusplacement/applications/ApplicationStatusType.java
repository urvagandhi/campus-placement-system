package com.campusplacement.applications;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Allowed application statuses and valid transitions.
 */
public enum ApplicationStatusType {

    PENDING,
    SHORTLISTED,
    REJECTED,
    SELECTED,
    WITHDRAWN;

    private Set<ApplicationStatusType> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> EnumSet.of(SELECTED, REJECTED, WITHDRAWN);
            default -> EnumSet.noneOf(ApplicationStatusType.class);
        };
    }

    ApplicationStatusType() {
    }

    /**
     * Parses a status string into the enum while enforcing allowed values.
     *
     * @param value incoming status string
     * @return normalized enum value
     * @throws IllegalArgumentException if the value is null or not supported
     */
    public static ApplicationStatusType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Application status is required");
        }
        try {
            return ApplicationStatusType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid application status: " + value);
        }
    }

    /**
     * Checks whether a transition from the current status to the target is allowed.
     * Idempotent updates (same status) are permitted.
     */
    public boolean canTransitionTo(ApplicationStatusType target) {
        if (target == null) {
            throw new IllegalArgumentException("Target status is required");
        }
        if (this == target) {
            return true;
        }
        return getAllowedTransitions().contains(target);
    }
}
