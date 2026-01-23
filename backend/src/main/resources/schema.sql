-- ================================================================================
-- PlacementPro Database Schem
-- ================================================================================
-- Generated from Java Entity classes on 2026-01-21
-- This script creates all tables matching the JPA entities exactly.
-- Run this on a fresh database.
-- ================================================================================

-- ================================================================================
-- ENUMS (PostgreSQL native enums are optional - we use VARCHAR for flexibility)
-- ================================================================================

-- ================================================================================
-- TABLE: colleges (Tenant Boundary)
-- Entity: com.campusplacement.colleges.College
-- ================================================================================
CREATE TABLE IF NOT EXISTS colleges (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(255)    NOT NULL,
    code                VARCHAR(50)     NOT NULL UNIQUE,
    address             TEXT,
    website             VARCHAR(255),
    contact_email       VARCHAR(255),
    admin_name          VARCHAR(255),
    contact_phone       VARCHAR(50),
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_colleges_code ON colleges(code);
CREATE INDEX IF NOT EXISTS idx_colleges_active ON colleges(is_active);


-- ================================================================================
-- TABLE: organization_units (Hierarchical Structure)
-- Entity: com.campusplacement.organizations.OrganizationUnit
-- ================================================================================
CREATE TABLE IF NOT EXISTS organization_units (
    id                  BIGSERIAL       PRIMARY KEY,
    college_id          BIGINT          NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,
    name                VARCHAR(255)    NOT NULL,
    code                VARCHAR(50),
    type                VARCHAR(50)     NOT NULL, -- UNIVERSITY, INSTITUTE, DEPARTMENT
    parent_unit_id      BIGINT          REFERENCES organization_units(id) ON DELETE CASCADE,
    email_domain        VARCHAR(100),
    is_root             BOOLEAN         DEFAULT FALSE,
    is_active           BOOLEAN         DEFAULT TRUE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_org_units_college ON organization_units(college_id);
CREATE INDEX IF NOT EXISTS idx_org_units_parent ON organization_units(parent_unit_id);
CREATE INDEX IF NOT EXISTS idx_org_units_type ON organization_units(type);
CREATE INDEX IF NOT EXISTS idx_org_units_active ON organization_units(is_active);


-- ================================================================================
-- TABLE: users (Authentication & Identity)
-- Entity: com.campusplacement.users.User
-- ================================================================================
CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL       PRIMARY KEY,
    username            VARCHAR(100)    NOT NULL UNIQUE,
    full_name           VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    role                VARCHAR(50)     NOT NULL, -- STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN
    college_id          BIGINT          REFERENCES colleges(id) ON DELETE CASCADE,
    phone_number        VARCHAR(50), -- E.164 format (e.g. +919876543210)
    profile_image_url   VARCHAR(500),
    last_login          TIMESTAMP,
    is_active           BOOLEAN         DEFAULT TRUE,
    must_change_password BOOLEAN        DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    CONSTRAINT uq_users_email_college UNIQUE(email, college_id)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_college ON users(college_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);


-- ================================================================================
-- TABLE: user_assignments (Role-Based Scope)
-- Entity: com.campusplacement.organizations.UserAssignment
-- ================================================================================
CREATE TABLE IF NOT EXISTS user_assignments (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_unit_id    BIGINT          NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,
    designation             VARCHAR(100),
    scope                   VARCHAR(50)     DEFAULT 'SUBTREE', -- SELF, CHILDREN, SUBTREE
    is_primary              BOOLEAN         DEFAULT TRUE,
    start_date              DATE            DEFAULT CURRENT_DATE,
    end_date                DATE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_org_assignment UNIQUE(user_id, organization_unit_id)
);

CREATE INDEX IF NOT EXISTS idx_assignments_user ON user_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_assignments_org_unit ON user_assignments(organization_unit_id);
CREATE INDEX IF NOT EXISTS idx_assignments_primary ON user_assignments(is_primary);


-- ================================================================================
-- TABLE: student_profiles (Academic Profile)
-- Entity: com.campusplacement.students.StudentProfile
-- ================================================================================
CREATE TABLE IF NOT EXISTS student_profiles (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    enrollment_number       VARCHAR(50)     NOT NULL UNIQUE,
    department_id           BIGINT          REFERENCES organization_units(id) ON DELETE SET NULL,
    cgpa                    DOUBLE PRECISION NOT NULL,
    active_backlogs         INTEGER         DEFAULT 0,
    batch_year              INTEGER,
    current_semester        INTEGER,
    skills                  TEXT,
    resume_url              VARCHAR(500),
    projects_count          INTEGER         DEFAULT 0,
    internship_months       INTEGER         DEFAULT 0,
    certifications          TEXT,
    linkedin_url            VARCHAR(500),
    github_url              VARCHAR(500),
    career_interests        TEXT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_student_user ON student_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_student_enrollment ON student_profiles(enrollment_number);
CREATE INDEX IF NOT EXISTS idx_student_department ON student_profiles(department_id);
CREATE INDEX IF NOT EXISTS idx_student_cgpa ON student_profiles(cgpa);


-- ================================================================================
-- TABLE: companies (Recruiters)
-- Entity: com.campusplacement.companies.Company
-- ================================================================================
CREATE TABLE IF NOT EXISTS companies (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(255)    NOT NULL,
    industry            VARCHAR(100),
    website             VARCHAR(255),
    description         TEXT,
    logo_url            VARCHAR(500),
    location            VARCHAR(255),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(50),
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name);
CREATE INDEX IF NOT EXISTS idx_companies_active ON companies(is_active);


-- ================================================================================
-- TABLE: placement_drives (Recruitment Events)
-- Entity: com.campusplacement.drives.PlacementDrive
-- ================================================================================
CREATE TABLE IF NOT EXISTS placement_drives (
    id                      BIGSERIAL       PRIMARY KEY,
    college_id              BIGINT          NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,
    company_id              BIGINT          NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title                   VARCHAR(255)    NOT NULL,
    description             TEXT,
    job_role                VARCHAR(255),
    package_lpa             DOUBLE PRECISION,
    drive_date              DATE,
    registration_deadline   DATE,
    status                  VARCHAR(50)     DEFAULT 'UPCOMING',
    min_cgpa                DOUBLE PRECISION,
    max_backlogs            INTEGER         DEFAULT 0,
    required_skills         TEXT,
    location                VARCHAR(255),
    is_remote               BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_drives_college ON placement_drives(college_id);
CREATE INDEX IF NOT EXISTS idx_drives_company ON placement_drives(company_id);
CREATE INDEX IF NOT EXISTS idx_drives_status ON placement_drives(status);
CREATE INDEX IF NOT EXISTS idx_drives_date ON placement_drives(drive_date);


-- ================================================================================
-- TABLE: drive_eligible_departments (Many-to-Many)
-- Used by: PlacementDrive.eligibleDepartments
-- ================================================================================
CREATE TABLE IF NOT EXISTS drive_eligible_departments (
    drive_id                BIGINT          NOT NULL REFERENCES placement_drives(id) ON DELETE CASCADE,
    department_id           BIGINT          NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,
    PRIMARY KEY (drive_id, department_id)
);

CREATE INDEX IF NOT EXISTS idx_drive_elig_dept_drive ON drive_eligible_departments(drive_id);
CREATE INDEX IF NOT EXISTS idx_drive_elig_dept_dept ON drive_eligible_departments(department_id);


-- ================================================================================
-- TABLE: applications (Student Applications)
-- Entity: com.campusplacement.applications.Application
-- ================================================================================
CREATE TABLE IF NOT EXISTS applications (
    id                      BIGSERIAL       PRIMARY KEY,
    student_id              BIGINT          NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    drive_id                BIGINT          NOT NULL REFERENCES placement_drives(id) ON DELETE CASCADE,
    status                  VARCHAR(50)     DEFAULT 'PENDING',
    applied_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    cover_letter            TEXT,
    internal_notes          TEXT,
    resume_url              VARCHAR(500),
    shortlisted_at          TIMESTAMP,
    selected_at             TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,
    CONSTRAINT uq_application_student_drive UNIQUE(student_id, drive_id)
);

CREATE INDEX IF NOT EXISTS idx_applications_student ON applications(student_id);
CREATE INDEX IF NOT EXISTS idx_applications_drive ON applications(drive_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);


-- ================================================================================
-- TABLE: eligibility_results (AI Scoring Cache)
-- Entity: com.campusplacement.eligibility.EligibilityResult
-- ================================================================================
CREATE TABLE IF NOT EXISTS eligibility_results (
    id                      BIGSERIAL       PRIMARY KEY,
    student_id              BIGINT          NOT NULL,
    drive_id                BIGINT          NOT NULL,
    score                   DOUBLE PRECISION NOT NULL,
    is_eligible             BOOLEAN         NOT NULL,
    reasons                 TEXT,
    calculated_at           TIMESTAMP       NOT NULL,
    cgpa_score              DOUBLE PRECISION,
    skills_score            DOUBLE PRECISION,
    experience_score        DOUBLE PRECISION,
    skill_gaps              TEXT,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_eligibility_student ON eligibility_results(student_id);
CREATE INDEX IF NOT EXISTS idx_eligibility_drive ON eligibility_results(drive_id);


-- ================================================================================
-- TABLE: refresh_tokens (JWT Refresh Tokens)
-- Entity: com.campusplacement.auth.RefreshToken
-- ================================================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                      BIGSERIAL       PRIMARY KEY,
    token                   VARCHAR(36)     NOT NULL UNIQUE,
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at              TIMESTAMP       NOT NULL,
    is_revoked              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_ip              VARCHAR(50),
    user_agent              VARCHAR(512),
    device_fingerprint      VARCHAR(64),
    family_id               VARCHAR(36),
    parent_token            VARCHAR(36),
    last_used_at            TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_family ON refresh_tokens(family_id);


-- ================================================================================
-- TABLE: login_audit (Security Audit Log)
-- Entity: com.campusplacement.auth.LoginAudit
-- ================================================================================
CREATE TABLE IF NOT EXISTS login_audit (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT,
    email                   VARCHAR(255),
    login_time              TIMESTAMP       NOT NULL DEFAULT NOW(),
    ip_address              VARCHAR(50),
    user_agent              VARCHAR(512),
    event_type              VARCHAR(50)     NOT NULL DEFAULT 'LOGIN',
    success                 BOOLEAN         NOT NULL,
    failure_reason          VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON login_audit(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_time ON login_audit(login_time);


-- ================================================================================
-- TABLE: security_alerts (Security Alerts)
-- Entity: com.campusplacement.auth.SecurityAlert
-- ================================================================================
CREATE TABLE IF NOT EXISTS security_alerts (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT,
    email                   VARCHAR(255),
    alert_type              VARCHAR(50),
    severity                VARCHAR(20),
    message                 TEXT,
    ip_address              VARCHAR(50),
    created_at              TIMESTAMP       DEFAULT NOW(),
    is_resolved             BOOLEAN         DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_alert_user ON security_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_resolved ON security_alerts(is_resolved);


-- ================================================================================
-- TABLE: password_reset_tokens
-- Entity: com.campusplacement.auth.PasswordResetToken
-- ================================================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id                      BIGSERIAL       PRIMARY KEY,
    token                   VARCHAR(64)     NOT NULL UNIQUE,
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_type              VARCHAR(50)     NOT NULL,
    expires_at              TIMESTAMP       NOT NULL,
    is_used                 BOOLEAN         NOT NULL DEFAULT FALSE,
    used_at                 TIMESTAMP,
    used_ip                 VARCHAR(50),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    requested_ip            VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens(user_id);


-- ================================================================================
-- TABLE: parsed_resume_data (AI Resume Parsing)
-- Entity: com.campusplacement.resume.ParsedResumeData
-- ================================================================================
CREATE TABLE IF NOT EXISTS parsed_resume_data (
    id                      BIGSERIAL       PRIMARY KEY,
    student_id              BIGINT          NOT NULL UNIQUE,
    extracted_skills        TEXT,
    experience_level        VARCHAR(50),
    confidence_score        DOUBLE PRECISION,
    project_keywords        TEXT,
    education_signals       TEXT,
    parsed_at               TIMESTAMP,
    model_version           VARCHAR(50)     DEFAULT 'v1.0.0',
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_parsed_resume_student ON parsed_resume_data(student_id);


-- ================================================================================
-- TABLE: academic_events (Scheduling)
-- Entity: com.campusplacement.organizations.AcademicEvent
-- ================================================================================
CREATE TABLE IF NOT EXISTS academic_events (
    id                      BIGSERIAL       PRIMARY KEY,
    organization_unit_id    BIGINT          NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,
    name                    VARCHAR(255)    NOT NULL,
    event_type              VARCHAR(50)     NOT NULL,
    start_date              DATE            NOT NULL,
    end_date                DATE            NOT NULL,
    description             TEXT,
    deleted_at              TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_events_org_unit ON academic_events(organization_unit_id);
CREATE INDEX IF NOT EXISTS idx_events_dates ON academic_events(start_date, end_date);


-- ================================================================================
-- TABLE: system_settings (Platform Configuration)
-- Entity: com.campusplacement.settings.SystemSettings
-- ================================================================================
CREATE TABLE IF NOT EXISTS system_settings (
    id                      BIGINT          PRIMARY KEY DEFAULT 1,
    registration_enabled    BOOLEAN         DEFAULT TRUE,
    maintenance_mode        BOOLEAN         DEFAULT FALSE,
    app_version             VARCHAR(50)     DEFAULT '1.0.0'
);

-- Insert default settings row
INSERT INTO system_settings (id, registration_enabled, maintenance_mode, app_version)
VALUES (1, TRUE, FALSE, '1.0.0')
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- SEQUENCE RESETS (For PostgreSQL)
-- ================================================================================
-- These are automatically managed by BIGSERIAL, no manual reset needed.


-- ================================================================================
-- TABLE: notifications
-- Entity: com.campusplacement.notifications.Notification
-- ================================================================================
-- Purpose: Stores user notifications for system events, alerts, and updates.
--          Supports read/unread status and notification types.
--
-- Notification Types:
--   - SYSTEM      - System-wide announcements
--   - SECURITY    - Security alerts and warnings
--   - DRIVE       - Placement drive updates
--   - APPLICATION - Application status changes
--   - GENERAL     - General notifications
--
-- Key Design Decisions:
--   - user_id is optional (NULL for system-wide notifications)
--   - is_read flag for tracking read status
--   - type enum for filtering and categorization
--   - created_at for chronological ordering
--
-- References: users(id) - optional
-- ================================================================================

CREATE TABLE IF NOT EXISTS notifications (
    -- Primary identifier
    id                  BIGSERIAL       PRIMARY KEY,

    -- User who receives the notification (NULL for system-wide notifications)
    user_id             BIGINT          REFERENCES users(id) ON DELETE CASCADE,

    -- Notification type for categorization
    type                VARCHAR(50)     NOT NULL DEFAULT 'GENERAL',

    -- Notification title/heading
    title               VARCHAR(255)    NOT NULL,

    -- Notification message/content
    message             TEXT            NOT NULL,

    -- Optional link/URL for action
    link                VARCHAR(500),

    -- Read status
    is_read             BOOLEAN         DEFAULT FALSE,

    -- Read timestamp (NULL if unread)
    read_at             TIMESTAMP,

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- INDEXES: notifications
-- -----------------------------------------------------------------------------
-- idx_notifications_user: Find all notifications for a user
-- idx_notifications_unread: Filter unread notifications
-- idx_notifications_type: Filter by notification type
-- idx_notifications_created: Sort by creation date (newest first)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(is_read) WHERE is_read = FALSE;
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;


-- ================================================================================
-- END OF SCHEMA DEFINITION
-- ================================================================================
-- Next Steps:
--   1. Run seed-data-v2.sql to populate test data
--   2. Start the Spring Boot application to validate Hibernate mapping
--   3. Test login with credentials from seed data
