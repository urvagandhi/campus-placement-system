-- ================================================================================
-- PlacementPro Database Schema
-- ================================================================================
-- Project:        PlacementPro - Campus Placement Management System
-- Database:       PostgreSQL 14+
-- Author:         PlacementPro Development Team
-- Created:        2025-01-01
-- Last Modified:  2026-01-09
-- Version:        2.0.0
-- ================================================================================
--
-- DESCRIPTION:
--   This schema defines the complete database structure for the PlacementPro
--   campus placement management system. It supports multi-tenancy (multiple
--   colleges), hierarchical organization units, role-based access control,
--   and comprehensive placement drive management.
--
-- EXECUTION NOTES:
--   - Statement separator: ;;
--   - All DDL is idempotent (safe to re-run)
--   - Run seed-data-v2.sql after this file for test data
--
-- TABLE DEPENDENCIES (execution order):
--   1. colleges          (no dependencies)
--   2. organization_units (depends on: colleges, self-referential)
--   3. users             (depends on: colleges)
--   4. user_assignments  (depends on: users, organization_units)
--   5. student_profiles  (depends on: users, organization_units)
--   6. companies         (no dependencies)
--   7. placement_drives  (depends on: companies)
--   8. applications      (depends on: student_profiles, placement_drives)
--   9. login_audit       (depends on: users - optional)
--
-- CHANGELOG:
--   v2.0.0 (2026-01-09): Comprehensive documentation and industry-standard formatting
--   v1.1.0 (2025-12-01): Added login_audit event_type migration
--   v1.0.0 (2025-01-01): Initial schema release
--
-- ================================================================================


-- ================================================================================
-- SECTION 1: EXTENSIONS
-- ================================================================================
-- Enable required PostgreSQL extensions for advanced functionality.
-- UUID extension is used for generating unique identifiers where needed.
-- ================================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";;


-- ================================================================================
-- SECTION 2: CUSTOM ENUM TYPES
-- ================================================================================
-- PostgreSQL custom types provide type safety and improve query performance.
-- These enums mirror the Java enum classes in the application layer.
--
-- IMPORTANT: Enum values must match exactly with Java enum names.
-- ================================================================================

DO $$
DECLARE
    type_exists BOOLEAN;
    old_value_exists BOOLEAN;
BEGIN
    -- =========================================================================
    -- MIGRATION: organization_unit_type
    -- Transition: (INSTITUTE, COLLEGE, DEPARTMENT) -> (UNIVERSITY, INSTITUTE, DEPARTMENT)
    -- =========================================================================

    -- Check if type exists
    SELECT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'organization_unit_type') INTO type_exists;

    -- Check for old value 'COLLEGE' safely without casting to regtype (which fails if type doesn't exist)
    old_value_exists := FALSE;
    IF type_exists THEN
        SELECT EXISTS (
            SELECT 1
            FROM pg_enum e
            JOIN pg_type t ON e.enumtypid = t.oid
            WHERE t.typname = 'organization_unit_type' AND e.enumlabel = 'COLLEGE'
        ) INTO old_value_exists;
    END IF;

    IF old_value_exists THEN
        RAISE NOTICE 'Migrating organization_unit_type enum...';

        ALTER TYPE organization_unit_type RENAME TO organization_unit_type_old;
        CREATE TYPE organization_unit_type AS ENUM ('UNIVERSITY', 'INSTITUTE', 'DEPARTMENT');

        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'organization_units') THEN
            ALTER TABLE organization_units
            ALTER COLUMN type TYPE organization_unit_type
            USING CASE
                WHEN type::text = 'INSTITUTE' THEN 'UNIVERSITY'::organization_unit_type
                WHEN type::text = 'COLLEGE' THEN 'INSTITUTE'::organization_unit_type
                ELSE 'DEPARTMENT'::organization_unit_type
            END;
        END IF;

        DROP TYPE organization_unit_type_old;
        -- Update the existence flag as we just recreated it
        type_exists := TRUE;
    END IF;

    -- Create if not exists (for fresh installs)
    IF NOT type_exists THEN
        CREATE TYPE organization_unit_type AS ENUM ('UNIVERSITY', 'INSTITUTE', 'DEPARTMENT');
    END IF;


    -- =========================================================================
    -- MIGRATION: scope_level
    -- Transition: (GLOBAL, ORGANIZATION_UNIT, SELF) -> (SELF, CHILDREN, SUBTREE)
    -- =========================================================================

    SELECT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'scope_level') INTO type_exists;

    old_value_exists := FALSE;
    IF type_exists THEN
        SELECT EXISTS (
            SELECT 1
            FROM pg_enum e
            JOIN pg_type t ON e.enumtypid = t.oid
            WHERE t.typname = 'scope_level' AND e.enumlabel = 'GLOBAL'
        ) INTO old_value_exists;
    END IF;

    IF old_value_exists THEN
        RAISE NOTICE 'Migrating scope_level enum...';

        ALTER TYPE scope_level RENAME TO scope_level_old;
        CREATE TYPE scope_level AS ENUM ('SELF', 'CHILDREN', 'SUBTREE');

        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_assignments') THEN
            ALTER TABLE user_assignments
            ALTER COLUMN scope TYPE scope_level
            USING CASE
                WHEN scope::text = 'GLOBAL' THEN 'SUBTREE'::scope_level
                WHEN scope::text = 'ORGANIZATION_UNIT' THEN 'SUBTREE'::scope_level
                ELSE 'SELF'::scope_level
            END;
        END IF;

        DROP TYPE scope_level_old;
        type_exists := TRUE;
    END IF;

    IF NOT type_exists THEN
        CREATE TYPE scope_level AS ENUM ('SELF', 'CHILDREN', 'SUBTREE');
    END IF;


    -- =========================================================================
    -- user_role (Standard creation)
    -- =========================================================================
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('STUDENT', 'COORDINATOR', 'ADMIN', 'SUPER_ADMIN');
    END IF;
END $$;;


-- ================================================================================
-- SECTION 3: CORE TABLES
-- ================================================================================
-- Tables are created in dependency order. Each table includes:
-- - Purpose documentation
-- - Column-level comments
-- - Appropriate constraints and defaults
-- - Indexes for common query patterns
-- ================================================================================


-- ================================================================================
-- 3.1 COLLEGES TABLE
-- ================================================================================
-- Purpose: Represents the top-level tenant boundary in the multi-college system.
--          Each college is an isolated tenant with its own users, org units, etc.
--
-- Key Design Decisions:
--   - College is the TENANT BOUNDARY for data isolation
--   - All entities (except SUPER_ADMIN users) belong to exactly one college
--   - Soft-active flag allows disabling without data loss
--
-- Referenced By: organization_units(college_id), users(college_id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS colleges (
    -- Primary identifier (auto-incrementing)
    id                  BIGSERIAL       PRIMARY KEY,

    -- College name (e.g., "Nirma University", "IIT Delhi")
    name                VARCHAR(255)    NOT NULL,

    -- Unique college code for identification (e.g., "NIRMA001", "IITD")
    -- Used in enrollment numbers and system references
    code                VARCHAR(50)     UNIQUE NOT NULL,

    -- Physical address of the college campus
    address             TEXT,

    -- Official website URL
    website             VARCHAR(255),

    -- Primary contact email for administrative communication
    contact_email       VARCHAR(255),

    -- Primary contact phone number
    contact_phone       VARCHAR(50),

    -- Accreditation details (e.g., "NAAC-A++", "NBA Accredited")
    accreditation_code  VARCHAR(50),

    -- Date when the institution was established
    established_date    DATE,

    -- Soft-active flag: FALSE disables the college without deleting data
    is_active           BOOLEAN         DEFAULT TRUE,

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);;

-- -----------------------------------------------------------------------------
-- INDEXES: colleges
-- -----------------------------------------------------------------------------
-- idx_colleges_code: Fast lookup by college code (common in authentication)
-- idx_colleges_active: Filter active colleges in listings
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_colleges_code ON colleges(code);;
CREATE INDEX IF NOT EXISTS idx_colleges_active ON colleges(is_active);;


-- ================================================================================
-- 3.2 ORGANIZATION UNITS TABLE
-- ================================================================================
-- Purpose: Represents the hierarchical structure within a college.
--          Models real-world academic structures: Institute → College → Department
--
-- Hierarchy Model:
--   - Self-referential via parent_unit_id for unlimited nesting depth
--   - Root nodes have parent_unit_id = NULL
--   - Each node belongs to exactly one college
--
-- Key Design Decisions:
--   - Soft-delete via is_active flag preserves referential integrity
--   - Code is optional but useful for quick identification
--   - Type enum enforces valid hierarchy levels
--
-- Referenced By: user_assignments(organization_unit_id), student_profiles(department_id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS organization_units (
    -- Primary identifier
    id                  BIGSERIAL       PRIMARY KEY,

    -- Display name (e.g., "Computer Science & Engineering")
    name                VARCHAR(255)    NOT NULL,

    -- Hierarchy level type (UNIVERSITY/INSTITUTE/DEPARTMENT)
    -- Stored as pg_enum type
    type                organization_unit_type NOT NULL,

    -- Short code for quick reference (e.g., "CSE", "ECE", "ME")
    code                VARCHAR(50),

    -- Self-referential: parent organization unit in hierarchy
    -- NULL indicates this is a root node (typically UNIVERSITY level)
    parent_unit_id      BIGINT          REFERENCES organization_units(id) ON DELETE CASCADE,

    -- Tenant boundary: which college this unit belongs to
    college_id          BIGINT          NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,

    -- Email domain for this org unit (used for email validation)
    email_domain        VARCHAR(100),

    -- Flag indicating if this is the root unit for the college
    is_root             BOOLEAN         DEFAULT FALSE,

    -- Soft-active flag for enabling/disabling without deletion
    is_active           BOOLEAN         DEFAULT TRUE,

    -- Soft-delete timestamp (NULL = not deleted)
    deleted_at          TIMESTAMP,

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

-- MIGRATION: Ensure parent_unit_id exists (Fixes "column not found" error on legacy DBs)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'organization_units') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'organization_units' AND column_name = 'parent_unit_id') THEN
            -- Handle rename from legacy 'parent_id' if exists
            IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'organization_units' AND column_name = 'parent_id') THEN
                ALTER TABLE organization_units RENAME COLUMN parent_id TO parent_unit_id;
            ELSE
                -- Add missing column
                ALTER TABLE organization_units ADD COLUMN parent_unit_id BIGINT REFERENCES organization_units(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- INDEXES: organization_units
-- -----------------------------------------------------------------------------
-- idx_org_units_college: Filter by tenant boundary (most common query pattern)
-- idx_org_units_parent: Navigate hierarchy (tree traversal queries)
-- idx_org_units_type: Filter by level (e.g., "all departments")
-- idx_org_units_active: Filter active units in listings
-- idx_org_units_root: Fast lookup of root nodes
-- idx_org_units_not_deleted: Soft-delete filter (partial index for efficiency)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_org_units_college ON organization_units(college_id);;
CREATE INDEX IF NOT EXISTS idx_org_units_parent ON organization_units(parent_unit_id);;
CREATE INDEX IF NOT EXISTS idx_org_units_type ON organization_units(type);;
CREATE INDEX IF NOT EXISTS idx_org_units_active ON organization_units(is_active);;
CREATE INDEX IF NOT EXISTS idx_org_units_root ON organization_units(is_root) WHERE is_root = TRUE;;
CREATE INDEX IF NOT EXISTS idx_org_units_not_deleted ON organization_units(id) WHERE deleted_at IS NULL;;


-- ================================================================================
-- 3.3 USERS TABLE
-- ================================================================================
-- Purpose: Authentication and identity management for all system users.
--          Stores ONLY identity data - organizational roles are in user_assignments.
--
-- Role Model:
--   - STUDENT     - End user who applies to drives
--   - COORDINATOR - TPO staff, faculty coordinators, corporate relations
--   - ADMIN       - College governance and administration
--   - SUPER_ADMIN - Platform owner (college_id is NULL for these users)
--
-- Key Design Decisions:
--   - Email uniqueness is PER COLLEGE (not global) for multi-tenancy
--   - Username is globally unique for login purposes
--   - Password stored as BCrypt hash (cost factor 12)
--   - Soft-delete via is_active preserves audit history
--
-- Referenced By: user_assignments(user_id), student_profiles(user_id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS users (
    -- Primary identifier
    id                  BIGSERIAL       PRIMARY KEY,

    -- Login username (globally unique)
    username            VARCHAR(100)    NOT NULL UNIQUE,

    -- BCrypt password hash (e.g., "$2a$12$...")
    -- SECURITY: Never store plain text passwords
    password_hash       VARCHAR(255)    NOT NULL,

    -- Email address (unique per college, not globally)
    email               VARCHAR(255)    NOT NULL,

    -- Display name (e.g., "Dr. Amit Patel")
    full_name           VARCHAR(255)    NOT NULL,

    -- User role for RBAC (stored as VARCHAR for JPA compatibility)
    role                VARCHAR(50)     NOT NULL,

    -- Tenant boundary: NULL only for SUPER_ADMIN users
    college_id          BIGINT          REFERENCES colleges(id) ON DELETE CASCADE,

    -- Contact phone number
    phone_number        VARCHAR(50),

    -- Profile image URL (cloud storage path)
    profile_image_url   VARCHAR(500),

    -- Last successful login timestamp (for security audit)
    last_login          TIMESTAMP,

    -- Soft-active flag: FALSE disables login without deleting data
    is_active           BOOLEAN         DEFAULT TRUE,

    -- Soft-delete timestamp (used with @SQLRestriction in JPA)
    deleted_at          TIMESTAMP,

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,

    -- ---------------------------------------------------------------------
    -- CONSTRAINT: Email uniqueness is scoped to college
    -- This allows the same email in different colleges (multi-tenancy)
    -- ---------------------------------------------------------------------
    CONSTRAINT uq_users_email_college UNIQUE(email, college_id)
);;

-- -----------------------------------------------------------------------------
-- INDEXES: users
-- -----------------------------------------------------------------------------
-- idx_users_username: Primary login lookup (critical for authentication)
-- idx_users_email: Email-based lookup (password reset, notifications)
-- idx_users_college: Filter by tenant boundary
-- idx_users_role: Filter by access level (e.g., "all coordinators")
-- idx_users_active: Filter active users in listings
-- idx_users_not_deleted: Soft-delete filter (partial index)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);;
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);;
CREATE INDEX IF NOT EXISTS idx_users_college ON users(college_id);;
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);;
CREATE INDEX IF NOT EXISTS idx_users_not_deleted ON users(id) WHERE deleted_at IS NULL;;


-- ================================================================================
-- 3.4 USER ASSIGNMENTS TABLE
-- ================================================================================
-- Purpose: Maps users to organization units with role-based scopes.
--          This is the REAL AUTHORITY MODEL - same role can have different scopes.
--
-- Authority Model Examples:
--   - TPO at Institute level with ORGANIZATION_UNIT scope → manages all departments
--   - Faculty Coordinator at Department level with SELF scope → manages only that dept
--
-- Key Design Decisions:
--   - One user can have multiple assignments (primary flag indicates main role)
--   - Scope level defines operational reach within the hierarchy
--   - Time-bounded assignments via start_date/end_date (e.g., visiting faculty)
--   - Designation is a human-readable title, NOT for authorization
--
-- References: users(id), organization_units(id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS user_assignments (
    -- Primary identifier
    id                      BIGSERIAL       PRIMARY KEY,

    -- User being assigned
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Organization unit being assigned to
    organization_unit_id    BIGINT          NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,

    -- Role in this assignment context (may differ from user.role)
    role                    VARCHAR(50)     NOT NULL,

    -- Operational reach: GLOBAL, ORGANIZATION_UNIT, SELF
    scope                   VARCHAR(50)     NOT NULL,

    -- Human-readable title (e.g., "Head - Training & Placement", "Faculty Coordinator")
    designation             VARCHAR(100),

    -- Is this the primary assignment? (for users with multiple assignments)
    is_primary              BOOLEAN         DEFAULT TRUE,

    -- Assignment validity period
    start_date              DATE            DEFAULT CURRENT_DATE,
    end_date                DATE,           -- NULL means indefinite

    -- Active status
    is_active               BOOLEAN         DEFAULT TRUE,

    -- Assignment creation timestamp
    assigned_at             TIMESTAMP       DEFAULT NOW(),

    -- Audit timestamps
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,

    -- ---------------------------------------------------------------------
    -- CONSTRAINT: One user cannot have duplicate assignments to same org unit
    -- ---------------------------------------------------------------------
    CONSTRAINT uq_user_org_assignment UNIQUE(user_id, organization_unit_id)
);;

-- -----------------------------------------------------------------------------
-- INDEXES: user_assignments
-- -----------------------------------------------------------------------------
-- idx_assignments_user: Find all assignments for a user (auth context)
-- idx_assignments_org_unit: Find all users assigned to an org unit
-- idx_assignments_primary: Filter primary assignments
-- idx_assignments_scope: Filter by scope level
-- idx_assignments_active: Filter active assignments
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_assignments_user ON user_assignments(user_id);;
CREATE INDEX IF NOT EXISTS idx_assignments_org_unit ON user_assignments(organization_unit_id);;
CREATE INDEX IF NOT EXISTS idx_assignments_primary ON user_assignments(is_primary);;
CREATE INDEX IF NOT EXISTS idx_assignments_scope ON user_assignments(scope);;
CREATE INDEX IF NOT EXISTS idx_assignments_active ON user_assignments(is_active);;


-- ================================================================================
-- 3.5 STUDENT PROFILES TABLE
-- ================================================================================
-- Purpose: Extended profile data for users with STUDENT role.
--          Contains academic and career information for eligibility matching.
--
-- Data Ownership Model:
--   - Institution-Owned (immutable by student): enrollment_number, department,
--     cgpa, backlogs, current_semester
--   - Student-Owned (editable): skills, resume_url, linkedin_url, portfolio_url
--
-- Key Design Decisions:
--   - One-to-one relationship with users table (user_id is unique)
--   - Department stored as reference to organization_units(id)
--   - Skills stored as comma-separated values for simplicity
--   - Academic percentages stored for eligibility criteria matching
--
-- References: users(id), organization_units(id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS student_profiles (
    -- Primary identifier
    id                      BIGSERIAL       PRIMARY KEY,

    -- Reference to user record (one-to-one, unique)
    user_id                 BIGINT          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Unique enrollment/registration number (e.g., "23BCE078")
    enrollment_number       VARCHAR(50)     UNIQUE NOT NULL,

    -- Department within organization hierarchy
    -- This should reference organization_units but kept as VARCHAR for legacy
    department              VARCHAR(100),

    -- Department ID (proper FK to organization_units) - preferred over department
    department_id           BIGINT          REFERENCES organization_units(id) ON DELETE SET NULL,

    -- Academic program (e.g., "B.Tech", "M.Tech", "MBA")
    program                 VARCHAR(100),

    -- Current semester (1-8 for UG, 1-4 for PG)
    current_semester        INTEGER,

    -- Batch/admission year (e.g., 2023)
    batch_year              INTEGER,

    -- Cumulative Grade Point Average (0.0 - 10.0)
    cgpa                    DOUBLE PRECISION,

    -- 10th standard percentage
    tenth_percentage        DOUBLE PRECISION,

    -- 12th standard percentage
    twelfth_percentage      DOUBLE PRECISION,

    -- Number of active backlogs (failed subjects pending)
    active_backlogs         INTEGER         DEFAULT 0,

    -- Number of historical backlogs (cleared failed subjects)
    history_backlogs        INTEGER         DEFAULT 0,

    -- Technical and soft skills (comma-separated: "Java,Python,React")
    skills                  TEXT,

    -- Resume URL (cloud storage path)
    resume_url              VARCHAR(500),

    -- LinkedIn profile URL
    linkedin_url            VARCHAR(500),

    -- GitHub profile URL
    github_url              VARCHAR(500),

    -- Portfolio/personal website URL
    portfolio_url           VARCHAR(500),

    -- Number of completed projects
    projects_count          INTEGER         DEFAULT 0,

    -- Total months of internship experience
    internship_months       INTEGER         DEFAULT 0,

    -- Professional certifications (comma-separated)
    certifications          TEXT,

    -- Career interests as JSON array string: ["Backend", "DevOps", "ML"]
    career_interests        TEXT,

    -- Audit timestamps
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);;

-- -----------------------------------------------------------------------------
-- INDEXES: student_profiles
-- -----------------------------------------------------------------------------
-- idx_student_user: Lookup profile by user ID (common join pattern)
-- idx_student_enrollment: Lookup by enrollment number (unique identifier)
-- idx_student_department: Filter students by department
-- idx_student_cgpa: Range queries for eligibility (e.g., cgpa >= 7.0)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_student_user ON student_profiles(user_id);;
CREATE INDEX IF NOT EXISTS idx_student_enrollment ON student_profiles(enrollment_number);;
CREATE INDEX IF NOT EXISTS idx_student_department ON student_profiles(department_id);;
CREATE INDEX IF NOT EXISTS idx_student_cgpa ON student_profiles(cgpa);;


-- ================================================================================
-- 3.6 COMPANIES TABLE
-- ================================================================================
-- Purpose: Master data for recruiting companies/organizations.
--          Contains company profiles for placement drive management.
--
-- Key Design Decisions:
--   - Companies are global entities (not scoped to a single college)
--   - Logo stored as URL (cloud storage) for performance
--   - Soft-active flag allows hiding without deletion
--
-- Referenced By: placement_drives(company_id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS companies (
    -- Primary identifier
    id                  BIGSERIAL       PRIMARY KEY,

    -- Company name (e.g., "Google", "Tata Consultancy Services")
    name                VARCHAR(255)    NOT NULL,

    -- Industry sector (e.g., "Technology", "Finance", "Manufacturing")
    industry            VARCHAR(100),

    -- Official company website
    website             VARCHAR(255),

    -- Company description/about text
    description         TEXT,

    -- Logo image URL (cloud storage path)
    logo_url            VARCHAR(500),

    -- Headquarters or primary office location
    location            VARCHAR(255),

    -- HR/recruitment contact email
    contact_email       VARCHAR(255),

    -- HR/recruitment contact phone
    contact_phone       VARCHAR(50),

    -- Soft-active flag: FALSE hides from listings without deletion
    is_active           BOOLEAN         DEFAULT TRUE,

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);;

-- -----------------------------------------------------------------------------
-- INDEXES: companies
-- -----------------------------------------------------------------------------
-- idx_companies_name: Search by company name (autocomplete, search)
-- idx_companies_industry: Filter by industry sector
-- idx_companies_active: Filter active companies in listings
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name);;
CREATE INDEX IF NOT EXISTS idx_companies_industry ON companies(industry);;
CREATE INDEX IF NOT EXISTS idx_companies_active ON companies(is_active);;


-- ================================================================================
-- 3.7 PLACEMENT DRIVES TABLE
-- ================================================================================
-- Purpose: Represents a recruitment drive conducted by a company.
--          Contains job details, eligibility criteria, and scheduling.
--
-- Eligibility Criteria:
--   - min_cgpa: Minimum CGPA required
--   - max_backlogs: Maximum active backlogs allowed
--   - eligible_departments: Comma-separated department codes
--   - required_skills: Comma-separated required skills
--
-- Drive Status Lifecycle:
--   DRAFT → UPCOMING → ONGOING → COMPLETED
--                   ↘ CANCELLED
--
-- Key Design Decisions:
--   - Eligibility stored as denormalized fields for query performance
--   - Status as VARCHAR allows business-defined statuses
--   - Company ID required (drives don't exist without a company)
--
-- References: companies(id)
-- Referenced By: applications(drive_id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS placement_drives (
    -- Primary identifier
    id                      BIGSERIAL       PRIMARY KEY,

    -- Company conducting the drive
    company_id              BIGINT          NOT NULL REFERENCES companies(id) ON DELETE CASCADE,

    -- Tenant/College owner (added in v2.0.1)
    college_id              BIGINT          REFERENCES colleges(id) ON DELETE CASCADE,

    -- Drive title (e.g., "Google SDE Campus Drive 2026")
    title                   VARCHAR(255)    NOT NULL,

    -- Detailed description of the opportunity
    description             TEXT,

    -- Job role/position (e.g., "Software Engineer", "Data Analyst")
    job_role                VARCHAR(255),

    -- Package/CTC in LPA (Lakhs Per Annum)
    package_lpa             DOUBLE PRECISION,

    -- Date when the drive will be conducted
    drive_date              DATE,

    -- Last date to register for the drive
    registration_deadline   DATE,

    -- Current status: DRAFT, UPCOMING, ONGOING, COMPLETED, CANCELLED
    status                  VARCHAR(50)     DEFAULT 'UPCOMING',

    -- -------------------------------------------------------------------------
    -- ELIGIBILITY CRITERIA
    -- -------------------------------------------------------------------------

    -- Minimum CGPA required (e.g., 7.0, 8.0)
    min_cgpa                DOUBLE PRECISION,

    -- Maximum active backlogs allowed (0 = no backlogs allowed)
    max_backlogs            INTEGER         DEFAULT 0,

    -- Comma-separated required skills (e.g., "Java,Spring,SQL")
    required_skills         TEXT,

    -- -------------------------------------------------------------------------
    -- JOB DETAILS
    -- -------------------------------------------------------------------------

    -- Job location(s) - can be comma-separated for multiple locations
    location                VARCHAR(255),

    -- Is this a remote/WFH position?
    is_remote               BOOLEAN         DEFAULT FALSE,

    -- Audit timestamps
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);;

-- -----------------------------------------------------------------------------
-- INDEXES: placement_drives
-- -----------------------------------------------------------------------------
-- idx_drives_company: Filter drives by company (dashboard view)
-- idx_drives_status: Filter by status (upcoming/ongoing drives)
-- idx_drives_date: Sort/filter by drive date (chronological listings)
-- idx_drives_deadline: Find drives with approaching deadlines
-- idx_drives_min_cgpa: Range queries for eligibility matching
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_drives_company ON placement_drives(company_id);;
CREATE INDEX IF NOT EXISTS idx_drives_status ON placement_drives(status);;
CREATE INDEX IF NOT EXISTS idx_drives_date ON placement_drives(drive_date);;
CREATE INDEX IF NOT EXISTS idx_drives_deadline ON placement_drives(registration_deadline);;
CREATE INDEX IF NOT EXISTS idx_drives_min_cgpa ON placement_drives(min_cgpa);;


-- -----------------------------------------------------------------------------
-- Migration: Add college_id to placement_drives if missing
-- -----------------------------------------------------------------------------
ALTER TABLE placement_drives ADD COLUMN IF NOT EXISTS college_id BIGINT REFERENCES colleges(id) ON DELETE CASCADE;;
CREATE INDEX IF NOT EXISTS idx_drives_college ON placement_drives(college_id);;


-- ================================================================================
-- 3.7.1 DRIVE ELIGIBLE DEPARTMENTS (Join Table)
-- ================================================================================
-- Purpose: Many-to-Many mapping between drives and eligible departments.
--          Replaces the fragile comma-separated string approach.
--
-- Key Design Decisions:
--   - FKs to placement_drives and organization_units
--   - Composite PK for uniqueness
-- ================================================================================

CREATE TABLE IF NOT EXISTS drive_eligible_departments (
    drive_id                BIGINT          NOT NULL REFERENCES placement_drives(id) ON DELETE CASCADE,
    department_id           BIGINT          NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,

    PRIMARY KEY (drive_id, department_id)
);;

CREATE INDEX IF NOT EXISTS idx_drive_elig_dept_drive ON drive_eligible_departments(drive_id);;
CREATE INDEX IF NOT EXISTS idx_drive_elig_dept_dept ON drive_eligible_departments(department_id);;


-- ================================================================================
-- 3.8 APPLICATIONS TABLE
-- ================================================================================
-- Purpose: Tracks student applications to placement drives.
--          Represents the many-to-many relationship: Student ↔ Drive
--
-- Application Status Lifecycle:
--   APPLIED → SHORTLISTED → SELECTED
--          ↘ REJECTED
--          ↘ WITHDRAWN
--
-- Key Design Decisions:
--   - Unique constraint ensures one application per student per drive
--   - Resume snapshot URL captures resume at application time (immutable)
--   - Status timestamps provide audit trail for process tracking
--
-- References: student_profiles(id), placement_drives(id)
-- ================================================================================

CREATE TABLE IF NOT EXISTS applications (
    -- Primary identifier
    id                      BIGSERIAL       PRIMARY KEY,

    -- Student who applied (references student_profiles, not users)
    student_id              BIGINT          NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,

    -- Drive being applied to
    drive_id                BIGINT          NOT NULL REFERENCES placement_drives(id) ON DELETE CASCADE,

    -- Current application status
    status                  VARCHAR(50)     DEFAULT 'APPLIED',

    -- When the application was submitted
    applied_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Resume URL at time of application (snapshot - may differ from current)
    resume_url              VARCHAR(500),

    -- Cover letter or application notes
    cover_letter            TEXT,

    -- Coordinator/TPO notes (internal, not visible to student)
    internal_notes          TEXT,

    -- Status change timestamps for audit trail
    shortlisted_at          TIMESTAMP,
    rejected_at             TIMESTAMP,
    selected_at             TIMESTAMP,
    withdrawn_at            TIMESTAMP,

    -- Audit timestamps
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP,

    -- ---------------------------------------------------------------------
    -- CONSTRAINT: One application per student per drive
    -- Prevents duplicate applications to the same drive
    -- ---------------------------------------------------------------------
    CONSTRAINT uq_application_student_drive UNIQUE(student_id, drive_id)
);;

-- -----------------------------------------------------------------------------
-- INDEXES: applications
-- -----------------------------------------------------------------------------
-- idx_applications_student: Find all applications for a student (history)
-- idx_applications_drive: Find all applicants for a drive (coordinator view)
-- idx_applications_status: Filter by status (shortlisted, selected, etc.)
-- idx_applications_applied_at: Sort by application date
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_applications_student ON applications(student_id);;
CREATE INDEX IF NOT EXISTS idx_applications_drive ON applications(drive_id);;
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);;
CREATE INDEX IF NOT EXISTS idx_applications_applied_at ON applications(applied_at);;


-- ================================================================================
-- 3.9 LOGIN AUDIT TABLE
-- ================================================================================
-- Purpose: Security audit log for all authentication events.
--          Captures login attempts (success/failure), logouts, and token refreshes.
--
-- Event Types:
--   - LOGIN           - User login attempt
--   - LOGOUT          - Explicit logout
--   - TOKEN_REFRESH   - JWT token refresh
--   - PASSWORD_CHANGE - Password modification
--   - LOCKOUT         - Account locked due to failed attempts
--
-- Key Design Decisions:
--   - user_id is optional (failed logins may not have valid user)
--   - IP address and user agent captured for security analysis
--   - Failure reason provides debugging info for support
--
-- References: users(id) - optional
-- ================================================================================

CREATE TABLE IF NOT EXISTS login_audit (
    -- Primary identifier
    id                  BIGSERIAL       PRIMARY KEY,

    -- User who attempted login (NULL if user not found)
    user_id             BIGINT          REFERENCES users(id) ON DELETE SET NULL,

    -- Username/email used in login attempt
    email               VARCHAR(255)    NOT NULL,

    -- Time of the authentication event
    login_time          TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Client IP address (IPv4 or IPv6)
    ip_address          VARCHAR(50),

    -- HTTP User-Agent header (browser/client identification)
    user_agent          VARCHAR(500),

    -- Type of authentication event
    event_type          VARCHAR(50)     NOT NULL DEFAULT 'LOGIN',

    -- Was the authentication attempt successful?
    success             BOOLEAN         NOT NULL DEFAULT FALSE,

    -- Reason for failure (NULL if successful)
    failure_reason      VARCHAR(255),

    -- Audit timestamps
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);;

-- -----------------------------------------------------------------------------
-- Migration: Ensure login_audit has all required columns before creating indexes
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    -- Add success column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'success'
    ) THEN
        ALTER TABLE login_audit ADD COLUMN success BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    -- Add email column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'email'
    ) THEN
        ALTER TABLE login_audit ADD COLUMN email VARCHAR(255);
        UPDATE login_audit SET email = 'unknown@system' WHERE email IS NULL;
        ALTER TABLE login_audit ALTER COLUMN email SET NOT NULL;
    END IF;

    -- Add event_type column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'event_type'
    ) THEN
        ALTER TABLE login_audit ADD COLUMN event_type VARCHAR(50);
        UPDATE login_audit SET event_type = 'LOGIN' WHERE event_type IS NULL;
        ALTER TABLE login_audit ALTER COLUMN event_type SET DEFAULT 'LOGIN';
        ALTER TABLE login_audit ALTER COLUMN event_type SET NOT NULL;
    END IF;

    -- Add failure_reason column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'failure_reason'
    ) THEN
        ALTER TABLE login_audit ADD COLUMN failure_reason VARCHAR(255);
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- INDEXES: login_audit
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_audit_user ON login_audit(user_id);;
CREATE INDEX IF NOT EXISTS idx_audit_time ON login_audit(login_time DESC);;
CREATE INDEX IF NOT EXISTS idx_audit_status ON login_audit(success);;
CREATE INDEX IF NOT EXISTS idx_audit_event_type ON login_audit(event_type);;
CREATE INDEX IF NOT EXISTS idx_audit_ip ON login_audit(ip_address);;



-- ================================================================================
-- 3.10 REFRESH TOKENS TABLE
-- ================================================================================
-- Purpose: Manages JWT refresh tokens for session continuity and security.
--          Supports token rotation, revocation, and device fingerprinting.
-- ================================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                      BIGSERIAL       PRIMARY KEY,
    token                   VARCHAR(255)    NOT NULL UNIQUE,
    user_id                 BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at              TIMESTAMP       NOT NULL,
    is_revoked              BOOLEAN         DEFAULT FALSE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_ip              VARCHAR(50),
    user_agent              VARCHAR(512),
    device_fingerprint      VARCHAR(64),
    family_id               VARCHAR(36),
    parent_token            VARCHAR(36),
    last_used_at            TIMESTAMP       NOT NULL DEFAULT NOW()
);;

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens(user_id);;
CREATE INDEX IF NOT EXISTS idx_refresh_token_lookup ON refresh_tokens(token);;
CREATE INDEX IF NOT EXISTS idx_refresh_token_family ON refresh_tokens(family_id);;


-- ================================================================================
-- 3.11 SECURITY ALERTS TABLE
-- ================================================================================
-- Purpose: Tracks security anomalies and alerts.
-- ================================================================================

CREATE TABLE IF NOT EXISTS security_alerts (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    email                   VARCHAR(255),
    alert_type              VARCHAR(50),    -- Enum: BRUTE_FORCE, SUSPICIOUS_IP, etc.
    severity                VARCHAR(20),    -- LOW, MEDIUM, HIGH, CRITICAL
    message                 TEXT,
    ip_address              VARCHAR(50),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    is_resolved             BOOLEAN         DEFAULT FALSE
);;

CREATE INDEX IF NOT EXISTS idx_security_alerts_user ON security_alerts(user_id);;
CREATE INDEX IF NOT EXISTS idx_security_alerts_type ON security_alerts(alert_type);;


-- ================================================================================
-- 3.12 ELIGIBILITY RESULTS TABLE
-- ================================================================================
-- Purpose: Stores the outcome of AI/Rule-based eligibility checks.
--          Links students to drives with a generated score and reasons.
-- ================================================================================

CREATE TABLE IF NOT EXISTS eligibility_results (
    id                      BIGSERIAL       PRIMARY KEY,
    student_id              BIGINT          NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    drive_id                BIGINT          NOT NULL REFERENCES placement_drives(id) ON DELETE CASCADE,

    -- Eligibility Determination
    is_eligible             BOOLEAN         NOT NULL DEFAULT FALSE,
    score                   DOUBLE PRECISION NOT NULL,

    -- Component Scores
    cgpa_score              DOUBLE PRECISION,
    skills_score            DOUBLE PRECISION,
    experience_score        DOUBLE PRECISION,

    -- AI/System Explanation
    reasons                 TEXT,           -- JSON or comma-separated reasons
    skill_gaps              TEXT,           -- Missing skills analysis

    calculated_at           TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Audit fields
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);;

CREATE INDEX IF NOT EXISTS idx_eligibility_drive ON eligibility_results(drive_id);;
CREATE INDEX IF NOT EXISTS idx_eligibility_student ON eligibility_results(student_id);;


-- ================================================================================
-- SECTION 4: MIGRATION BLOCKS
-- ================================================================================
-- Idempotent migration scripts for evolving the schema safely.
-- Each block checks for existing state before making changes.
-- ================================================================================

-- -----------------------------------------------------------------------------
-- Migration: Add event_type column to login_audit (v1.1.0)
-- -----------------------------------------------------------------------------
-- This migration safely adds the event_type column if it doesn't exist,
-- backfills existing rows with 'LOGIN', and sets the NOT NULL constraint.
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'event_type'
    ) THEN
        -- Step 1: Add column as nullable
        ALTER TABLE login_audit ADD COLUMN event_type VARCHAR(50);

        -- Step 2: Backfill existing rows
        UPDATE login_audit SET event_type = 'LOGIN' WHERE event_type IS NULL;

        -- Step 3: Set default and NOT NULL constraint
        ALTER TABLE login_audit ALTER COLUMN event_type SET DEFAULT 'LOGIN';
        ALTER TABLE login_audit ALTER COLUMN event_type SET NOT NULL;
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- Migration: Add email column to login_audit (v2.0.0)
-- -----------------------------------------------------------------------------
-- For existing databases that don't have the email column.
-- Backfills with 'unknown@system' for existing records.
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'login_audit' AND column_name = 'email'
    ) THEN
        -- Step 1: Add column as nullable
        ALTER TABLE login_audit ADD COLUMN email VARCHAR(255);

        -- Step 2: Backfill existing rows
        UPDATE login_audit SET email = 'unknown@system' WHERE email IS NULL;

        -- Step 3: Set NOT NULL constraint
        ALTER TABLE login_audit ALTER COLUMN email SET NOT NULL;
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- Migration: Add missing columns to colleges (v2.0.0)
-- -----------------------------------------------------------------------------
-- Adds code, address, website, contact_email columns if they don't exist.
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'colleges' AND column_name = 'code'
    ) THEN
        ALTER TABLE colleges ADD COLUMN code VARCHAR(50);
        -- Backfill with generated code from name
        UPDATE colleges SET code = UPPER(REPLACE(SUBSTRING(name, 1, 8), ' ', ''))
        WHERE code IS NULL;
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- Migration: Add missing columns to student_profiles (v2.0.0)
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    -- Add career_interests if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'student_profiles' AND column_name = 'career_interests'
    ) THEN
        ALTER TABLE student_profiles ADD COLUMN career_interests TEXT;
    END IF;

    -- Add github_url if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'student_profiles' AND column_name = 'github_url'
    ) THEN
        ALTER TABLE student_profiles ADD COLUMN github_url VARCHAR(500);
    END IF;

    -- Add batch_year if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'student_profiles' AND column_name = 'batch_year'
    ) THEN
        ALTER TABLE student_profiles ADD COLUMN batch_year INTEGER;
    END IF;
END $$;;

-- -----------------------------------------------------------------------------
-- Migration: Add missing columns to applications (v2.0.0)
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    -- Add status timestamps if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'applications' AND column_name = 'shortlisted_at'
    ) THEN
        ALTER TABLE applications ADD COLUMN shortlisted_at TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'applications' AND column_name = 'rejected_at'
    ) THEN
        ALTER TABLE applications ADD COLUMN rejected_at TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'applications' AND column_name = 'selected_at'
    ) THEN
        ALTER TABLE applications ADD COLUMN selected_at TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'applications' AND column_name = 'withdrawn_at'
    ) THEN
        ALTER TABLE applications ADD COLUMN withdrawn_at TIMESTAMP;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'applications' AND column_name = 'internal_notes'
    ) THEN
        ALTER TABLE applications ADD COLUMN internal_notes TEXT;
    END IF;
END $$;;


-- ================================================================================
-- SECTION 5: HELPER FUNCTIONS
-- ================================================================================
-- Utility functions for common operations like hierarchy traversal.
-- These are used by application code and can be called directly in SQL.
-- ================================================================================

-- -----------------------------------------------------------------------------
-- Function: get_org_ancestors(org_unit_id)
-- -----------------------------------------------------------------------------
-- Returns all ancestor organization units for a given org unit ID.
-- Used for: Breadcrumb navigation, permission inheritance, reporting.
--
-- Returns: Table of (id, name, type, level) ordered from root to target.
-- Level 0 = root, increasing towards the target node.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS get_org_ancestors(BIGINT);;

CREATE OR REPLACE FUNCTION get_org_ancestors(target_org_unit_id BIGINT)
RETURNS TABLE(id BIGINT, name VARCHAR, type VARCHAR, level INT) AS $$
WITH RECURSIVE ancestors AS (
    -- Base case: start with the target org unit
    SELECT
        ou.id,
        ou.name,
        CAST(ou.type AS VARCHAR),
        ou.parent_unit_id,
        0 as level
    FROM organization_units ou
    WHERE ou.id = target_org_unit_id

    UNION ALL

    -- Recursive case: traverse up the parent chain
    SELECT
        ou.id,
        ou.name,
        CAST(ou.type AS VARCHAR),
        ou.parent_unit_id,
        a.level + 1
    FROM organization_units ou
    JOIN ancestors a ON ou.id = a.parent_unit_id
)
SELECT ancestors.id, ancestors.name, ancestors.type, ancestors.level
FROM ancestors
ORDER BY level DESC;
$$ LANGUAGE SQL STABLE;;

-- -----------------------------------------------------------------------------
-- Function: get_org_descendants(org_unit_id)
-- -----------------------------------------------------------------------------
-- Returns all descendant organization units for a given org unit ID.
-- Used for: Scope-based authorization (SUBTREE scope), reporting.
--
-- Returns: Table of (id, name, type, level) ordered by depth.
-- Level 0 = the target node, increasing towards leaf nodes.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS get_org_descendants(BIGINT);;

CREATE OR REPLACE FUNCTION get_org_descendants(target_org_unit_id BIGINT)
RETURNS TABLE(id BIGINT, name VARCHAR, type VARCHAR, level INT) AS $$
WITH RECURSIVE descendants AS (
    -- Base case: start with the target org unit
    SELECT
        ou.id,
        ou.name,
        CAST(ou.type AS VARCHAR),
        ou.parent_unit_id,
        0 as level
    FROM organization_units ou
    WHERE ou.id = target_org_unit_id

    UNION ALL

    -- Recursive case: traverse down to children
    SELECT
        ou.id,
        ou.name,
        CAST(ou.type AS VARCHAR),
        ou.parent_unit_id,
        d.level + 1
    FROM organization_units ou
    JOIN descendants d ON ou.parent_unit_id = d.id
)
SELECT descendants.id, descendants.name, descendants.type, descendants.level
FROM descendants
ORDER BY level ASC;
$$ LANGUAGE SQL STABLE;;


-- ================================================================================
-- END OF SCHEMA DEFINITION
-- ================================================================================
-- Next Steps:
--   1. Run seed-data-v2.sql to populate test data
--   2. Start the Spring Boot application to validate Hibernate mapping
--   3. Test login with credentials from seed data
-- ================================================================================
