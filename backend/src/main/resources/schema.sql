-- ============================================
-- PlacementPro - Consolidated Schema (Idempotent)
-- Handles both initialization and migration
-- Separator: ;;
-- ============================================

-- 1. Create Enums (Idempotent)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'org_unit_type') THEN
        CREATE TYPE org_unit_type AS ENUM ('UNIVERSITY', 'INSTITUTE', 'DEPARTMENT');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('STUDENT', 'COORDINATOR', 'ADMIN', 'SUPER_ADMIN');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'scope_level') THEN
        CREATE TYPE scope_level AS ENUM ('SELF', 'CHILDREN', 'SUBTREE');
    END IF;
END $$;;

-- NOTE: Migration from varchar to enum removed.
-- If you have an existing database with varchar role column, run this manually:
-- ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::user_role;
-- 2. Safe Migration of Users Role Column (Text -> Enum)
DO $$
BEGIN
    -- Only run if the column is currently character varying (text)
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'role'
          AND data_type = 'character varying'
    ) THEN
        -- Explicitly cast the column
        ALTER TABLE users
        ALTER COLUMN role TYPE user_role
        USING role::user_role;
    END IF;
END $$;;


-- 3. Create Tables (IF NOT EXISTS)

-- 3.1 colleges
CREATE TABLE IF NOT EXISTS colleges (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);;
-- Indexes for colleges
CREATE INDEX IF NOT EXISTS idx_colleges_code ON colleges(code);;
CREATE INDEX IF NOT EXISTS idx_colleges_active ON colleges(is_active);;

-- 3.2 organization_units
CREATE TABLE IF NOT EXISTS organization_units (
    id BIGSERIAL PRIMARY KEY,
    college_id BIGINT NOT NULL REFERENCES colleges(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    type org_unit_type NOT NULL,
    parent_id BIGINT REFERENCES organization_units(id) ON DELETE CASCADE,
    email_domain VARCHAR(100),
    is_root BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,

    CONSTRAINT chk_org_hierarchy_valid CHECK (
        (type = 'UNIVERSITY' AND parent_id IS NULL) OR
        (type = 'INSTITUTE' AND parent_id IS NOT NULL) OR
        (type = 'DEPARTMENT' AND parent_id IS NOT NULL)
    )
);;
CREATE INDEX IF NOT EXISTS idx_org_units_college ON organization_units(college_id);;
CREATE INDEX IF NOT EXISTS idx_org_units_parent ON organization_units(parent_id);;
CREATE INDEX IF NOT EXISTS idx_org_units_type ON organization_units(type);;
CREATE INDEX IF NOT EXISTS idx_org_units_active ON organization_units(is_active);;
CREATE INDEX IF NOT EXISTS idx_org_units_root ON organization_units(is_root) WHERE is_root = true;;
CREATE INDEX IF NOT EXISTS idx_org_units_not_deleted ON organization_units(id) WHERE deleted_at IS NULL;;

-- 3.3 users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    college_id BIGINT REFERENCES colleges(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    phone_number VARCHAR(50),
    profile_image_url VARCHAR(500),
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,

    CONSTRAINT uq_users_email_college UNIQUE(email, college_id)
);;
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);;
CREATE INDEX IF NOT EXISTS idx_users_college ON users(college_id);;
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);;
CREATE INDEX IF NOT EXISTS idx_users_not_deleted ON users(id) WHERE deleted_at IS NULL;;

-- 3.4 user_assignments
CREATE TABLE IF NOT EXISTS user_assignments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_unit_id BIGINT NOT NULL REFERENCES organization_units(id) ON DELETE CASCADE,
    designation VARCHAR(100),
    scope_level scope_level DEFAULT 'SUBTREE',
    is_primary BOOLEAN DEFAULT true,
    start_date DATE DEFAULT CURRENT_DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_org_unit UNIQUE(user_id, organization_unit_id)
);;
CREATE INDEX IF NOT EXISTS idx_assignments_user ON user_assignments(user_id);;
CREATE INDEX IF NOT EXISTS idx_assignments_org_unit ON user_assignments(organization_unit_id);;
CREATE INDEX IF NOT EXISTS idx_assignments_primary ON user_assignments(is_primary);;
CREATE INDEX IF NOT EXISTS idx_assignments_scope ON user_assignments(scope_level);;

-- 3.5 login_audit
CREATE TABLE IF NOT EXISTS login_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255),
    login_time TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL
);;
CREATE INDEX IF NOT EXISTS idx_audit_user ON login_audit(user_id);;
CREATE INDEX IF NOT EXISTS idx_audit_time ON login_audit(login_time);;
CREATE INDEX IF NOT EXISTS idx_audit_success ON login_audit(success);;


-- 4. Helper Functions (Idempotent replacement)
CREATE OR REPLACE FUNCTION get_org_ancestors(org_unit_id BIGINT)
RETURNS TABLE(id BIGINT, name VARCHAR, type org_unit_type, level INT) AS $$
WITH RECURSIVE ancestors AS (
    SELECT ou.id, ou.name, ou.type, ou.parent_id, 0 as level
    FROM organization_units ou
    WHERE ou.id = org_unit_id

    UNION ALL

    SELECT ou.id, ou.name, ou.type, ou.parent_id, a.level + 1
    FROM organization_units ou
    JOIN ancestors a ON ou.id = a.parent_id
)
SELECT ancestors.id, ancestors.name, ancestors.type, ancestors.level
FROM ancestors
ORDER BY level DESC;
$$ LANGUAGE SQL;;

CREATE OR REPLACE FUNCTION get_org_descendants(org_unit_id BIGINT)
RETURNS TABLE(id BIGINT, name VARCHAR, type org_unit_type, level INT) AS $$
WITH RECURSIVE descendants AS (
    SELECT ou.id, ou.name, ou.type, ou.parent_id, 0 as level
    FROM organization_units ou
    WHERE ou.id = org_unit_id

    UNION ALL

    SELECT ou.id, ou.name, ou.type, ou.parent_id, d.level + 1
    FROM organization_units ou
    JOIN descendants d ON ou.parent_id = d.id
)
SELECT descendants.id, descendants.name, descendants.type, descendants.level
FROM descendants
ORDER BY level;
$$ LANGUAGE SQL;;
