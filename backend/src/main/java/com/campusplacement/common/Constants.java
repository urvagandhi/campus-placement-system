package com.campusplacement.common;

/**
 * Application-wide constants for the Campus Placement System.
 *
 * <p>
 * Contains role definitions, status enums, and configuration constants.
 * </p>
 *
 * <p>
 * <strong>RBAC Roles:</strong>
 * </p>
 * <ul>
 * <li>STUDENT - Student user with access to profile and applications</li>
 * <li>TPO - Training and Placement Officer managing drives</li>
 * <li>ADMIN - College administrator for system configuration</li>
 * <li>SUPER_ADMIN - Global administrator with full access</li>
 * </ul>
 */
public final class Constants {

    private Constants() {
        // Private constructor to prevent instantiation
    }

    // ==================== API Configuration ====================

    /**
     * API version prefix for all endpoints.
     */
    public static final String API_VERSION = "/api/v1";

    // ==================== Role Constants ====================

    /**
     * Student role - Access to profile, drives, applications, eligibility results.
     */
    public static final String ROLE_STUDENT = "STUDENT";

    /**
     * TPO role - Manage placement drives, shortlist students, view analytics.
     */
    public static final String ROLE_TPO = "TPO";

    /**
     * Admin role - Manage users, departments, system configuration.
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * Super Admin role - Global system administration, role management, audit logs.
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // ==================== Application Status ====================

    /**
     * Application status values for placement applications.
     */
    public static final class ApplicationStatus {
        public static final String PENDING = "PENDING";
        public static final String SHORTLISTED = "SHORTLISTED";
        public static final String REJECTED = "REJECTED";
        public static final String SELECTED = "SELECTED";
        public static final String WITHDRAWN = "WITHDRAWN";

        private ApplicationStatus() {
        }
    }

    // ==================== Drive Status ====================

    /**
     * Placement drive status values.
     */
    public static final class DriveStatus {
        public static final String UPCOMING = "UPCOMING";
        public static final String ONGOING = "ONGOING";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";

        private DriveStatus() {
        }
    }

    // ==================== Eligibility Constants ====================

    /**
     * Eligibility scoring weights (used by AI service).
     * The AI module acts strictly as a decision-support system.
     */
    public static final class EligibilityWeights {
        public static final double CGPA_WEIGHT = 0.30;
        public static final double SKILLS_WEIGHT = 0.40;
        public static final double EXPERIENCE_WEIGHT = 0.20;
        public static final double CERTIFICATIONS_WEIGHT = 0.10;

        private EligibilityWeights() {
        }
    }

    // ==================== Validation Constants ====================

    /**
     * Validation limits and patterns.
     */
    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 100;
        public static final double MIN_CGPA = 0.0;
        public static final double MAX_CGPA = 10.0;
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

        private Validation() {
        }
    }
}
