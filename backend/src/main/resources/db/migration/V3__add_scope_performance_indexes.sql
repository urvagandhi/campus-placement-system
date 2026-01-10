-- ================================================================================
-- Migration: Add Performance Indexes for Scope Enforcement
-- ================================================================================
-- Version: V3
-- Description: Adds database indexes to optimize scope resolution and filtered queries
-- Author: PlacementPro Development Team
-- Date: 2026-01-10
-- ================================================================================

-- ==================== Scope Resolution Indexes ====================

-- Index for active user assignments (used by OrganizationScopeService)
-- Filters active assignments by user_id for scope resolution
CREATE INDEX IF NOT EXISTS idx_user_assignments_active
ON user_assignments(user_id, is_active, start_date, end_date)
WHERE is_active = TRUE;

-- ==================== Hierarchy Traversal Indexes ====================

-- Index for CTE-based subtree traversal in organization hierarchy (DESCENDANTS)
-- Supports recursive queries that follow parent-child relationships
-- Used by: findSubtreeIds(), findDepartmentIdsInSubtree()
CREATE INDEX IF NOT EXISTS idx_org_units_parent_college
ON organization_units(parent_unit_id, college_id, type)
WHERE deleted_at IS NULL;

-- Index for reverse CTE traversal (ANCESTORS - opposite direction)
-- Supports upward traversal from child to parent to root
-- Used by: findAncestorIds()
CREATE INDEX IF NOT EXISTS idx_org_units_child_college
ON organization_units(id, college_id, parent_unit_id)
WHERE deleted_at IS NULL;

-- Additional index for type-specific queries (finding all departments)
CREATE INDEX IF NOT EXISTS idx_org_units_type_college
ON organization_units(type, college_id)
WHERE deleted_at IS NULL;

-- ==================== Scope-Filtered Query Indexes ====================

-- Index for student queries filtered by department and college
CREATE INDEX IF NOT EXISTS idx_students_dept_college
ON student_profiles(department_id, user_id);

-- Index for application queries filtered by student department
CREATE INDEX IF NOT EXISTS idx_applications_student_dept
ON applications(student_id, drive_id, status, applied_at);

-- Composite index for drive eligibility lookups
CREATE INDEX IF NOT EXISTS idx_drive_eligible_depts
ON drive_eligible_departments(drive_id, department_id);

-- ==================== Analytics Indexes ====================

-- Index for placement statistics grouped by department
CREATE INDEX IF NOT EXISTS idx_applications_analytics
ON applications(status, student_id, applied_at)
WHERE status IN ('SELECTED', 'SHORTLISTED');

-- ==================== College Guard Indexes ====================

-- Index for college-based filtering (tenant isolation)
CREATE INDEX IF NOT EXISTS idx_users_college
ON users(college_id, role)
WHERE deleted_at IS NULL;

-- Index for drives by college (frequently accessed in scope checks)
CREATE INDEX IF NOT EXISTS idx_drives_college
ON placement_drives(college_id, status, drive_date)
WHERE deleted_at IS NULL;

-- ================================================================================
-- End of Migration
-- ================================================================================
