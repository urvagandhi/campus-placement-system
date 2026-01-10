-- ================================================================================
-- PlacementPro Seed Data - Multi-College Hierarchy
-- ================================================================================
-- Project:        PlacementPro - Campus Placement Management System
-- Database:       PostgreSQL 14+
-- Author:         PlacementPro Development Team
-- Created:        2025-01-01
-- Last Modified:  2026-01-10
-- Version:        3.0.0
-- ================================================================================
--
-- DESCRIPTION:
--   Production-ready seed data with realistic multi-college hierarchy.
--   Creates 2 universities with complete organizational structure.
--
-- HIERARCHY MODEL:
--   SUPER_ADMIN (Platform)
--   └── UNIVERSITY (College Level)
--       ├── University Admin (ADMIN + SUBTREE)
--       └── INSTITUTE (School/Faculty Level)
--           ├── Institute Admin (ADMIN + SUBTREE)
--           ├── T&P Coordinator (COORDINATOR + SUBTREE)
--           ├── T&P Team Members (COORDINATOR + CHILDREN)
--           └── DEPARTMENT
--               ├── Dept Coordinators (COORDINATOR + SELF)
--               └── Students (STUDENT + SELF)
--
-- PREREQUISITES:
--   - Run schema.sql BEFORE this file
--   - PostgreSQL 14+ with enums created
--
-- ================================================================================
--
-- TEST CREDENTIALS SUMMARY
-- ================================================================================
--
-- PASSWORD FOR ALL USERS: password123
-- BCrypt Hash (cost 12): $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O
--
-- ===== PLATFORM LEVEL =====
-- | superadmin@placementpro.com | SUPER_ADMIN | Platform Owner |
--
-- ===== NIRMA UNIVERSITY (nirmauni.ac.in) =====
-- | admin@nirmauni.ac.in        | ADMIN       | University Admin |
-- | admin.it@nirmauni.ac.in     | ADMIN       | IT Institute Admin |
-- | admin.law@nirmauni.ac.in    | ADMIN       | Law Institute Admin |
-- | tpo.it@nirmauni.ac.in       | COORDINATOR | T&P Head - IT |
-- | tpo.law@nirmauni.ac.in      | COORDINATOR | T&P Head - Law |
-- | coord.cse@nirmauni.ac.in    | COORDINATOR | Dept Coordinator - CSE |
-- | coord.ce@nirmauni.ac.in     | COORDINATOR | Dept Coordinator - CE |
-- | 23BCE001@nirmauni.ac.in     | STUDENT     | CSE Student |
-- | 23BCE002@nirmauni.ac.in     | STUDENT     | CSE Student |
-- | 23CE001@nirmauni.ac.in      | STUDENT     | CE Student |
--
-- ===== PP SAVANI UNIVERSITY (ppsu.ac.in) =====
-- | admin@ppsu.ac.in            | ADMIN       | University Admin |
-- | admin.it@ppsu.ac.in         | ADMIN       | IT School Admin |
-- | admin.mgt@ppsu.ac.in        | ADMIN       | Management School Admin |
-- | tpo.it@ppsu.ac.in           | COORDINATOR | T&P Head - IT |
-- | tpo.mgt@ppsu.ac.in          | COORDINATOR | T&P Head - Management |
-- | coord.cs@ppsu.ac.in         | COORDINATOR | Dept Coordinator - CS |
-- | 24CS001@ppsu.ac.in          | STUDENT     | CS Student |
-- | 24CS002@ppsu.ac.in          | STUDENT     | CS Student |
--
-- ================================================================================


-- ================================================================================
-- SECTION 1: COLLEGES (Tenant Boundaries)
-- ================================================================================

INSERT INTO colleges (
    id, name, code, address, website, contact_email, contact_phone,
    accreditation_code, established_date, is_active, created_at, updated_at
) VALUES
    -- Nirma University - Premier private university in Gujarat
    (1, 'Nirma University', 'NIRMA001',
     'Sarkhej-Gandhinagar Highway, Ahmedabad, Gujarat 382481',
     'https://www.nirmauni.ac.in',
     'admissions@nirmauni.ac.in',
     '+91-2717-241911',
     'NAAC-A++',
     '1994-01-01',
     TRUE, NOW(), NOW()),

    -- PP Savani University - Emerging university in Surat
    (2, 'PP Savani University', 'PPSU001',
     'NH-8, Kosamba, Surat, Gujarat 394125',
     'https://www.ppsu.ac.in',
     'admissions@ppsu.ac.in',
     '+91-2622-227100',
     'NAAC-A',
     '2017-01-01',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    website = EXCLUDED.website,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 2: ORGANIZATION UNITS (Hierarchical Structure)
-- ================================================================================

-- =============================================================================
-- NIRMA UNIVERSITY HIERARCHY
-- =============================================================================
INSERT INTO organization_units (
    id, name, type, code, parent_unit_id, college_id, email_domain, is_root, is_active, created_at, updated_at
) VALUES
    -- University Root (Level 0)
    (1, 'Nirma University', 'UNIVERSITY', 'NU', NULL, 1, 'nirmauni.ac.in', TRUE, TRUE, NOW(), NOW()),

    -- Institutes (Level 1)
    (2, 'Institute of Technology', 'INSTITUTE', 'IT', 1, 1, NULL, FALSE, TRUE, NOW(), NOW()),
    (3, 'Institute of Law', 'INSTITUTE', 'IL', 1, 1, NULL, FALSE, TRUE, NOW(), NOW()),

    -- Departments under Institute of Technology (Level 2)
    (4, 'Department of Computer Science & Engineering', 'DEPARTMENT', 'CSE', 2, 1, NULL, FALSE, TRUE, NOW(), NOW()),
    (5, 'Department of Computer Engineering', 'DEPARTMENT', 'CE', 2, 1, NULL, FALSE, TRUE, NOW(), NOW()),
    (6, 'Department of Mechanical Engineering', 'DEPARTMENT', 'ME', 2, 1, NULL, FALSE, TRUE, NOW(), NOW()),

    -- Departments under Institute of Law (Level 2)
    (7, 'Department of Corporate Law', 'DEPARTMENT', 'CL', 3, 1, NULL, FALSE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- =============================================================================
-- PP SAVANI UNIVERSITY HIERARCHY
-- =============================================================================
INSERT INTO organization_units (
    id, name, type, code, parent_unit_id, college_id, email_domain, is_root, is_active, created_at, updated_at
) VALUES
    -- University Root (Level 0)
    (8, 'PP Savani University', 'UNIVERSITY', 'PPSU', NULL, 2, 'ppsu.ac.in', TRUE, TRUE, NOW(), NOW()),

    -- Schools/Institutes (Level 1)
    (9, 'School of Engineering & Technology', 'INSTITUTE', 'SET', 8, 2, NULL, FALSE, TRUE, NOW(), NOW()),
    (10, 'School of Management', 'INSTITUTE', 'SOM', 8, 2, NULL, FALSE, TRUE, NOW(), NOW()),

    -- Departments under School of Engineering (Level 2)
    (11, 'Department of Computer Science', 'DEPARTMENT', 'CS', 9, 2, NULL, FALSE, TRUE, NOW(), NOW()),
    (12, 'Department of Information Technology', 'DEPARTMENT', 'IT', 9, 2, NULL, FALSE, TRUE, NOW(), NOW()),

    -- Departments under School of Management (Level 2)
    (13, 'Department of MBA', 'DEPARTMENT', 'MBA', 10, 2, NULL, FALSE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 3: USERS
-- ================================================================================
-- Password for ALL users: password123
-- BCrypt Hash (cost 12): $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O
-- ================================================================================

-- 3.1 SUPER ADMIN (Platform Owner)
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES (
    1, 'superadmin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
    'superadmin@placementpro.com',
    'Platform Administrator',
    'SUPER_ADMIN',
    NULL,  -- No college for platform admin
    '+91-9000000001',
    TRUE, NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- =============================================================================
-- 3.2 NIRMA UNIVERSITY USERS
-- =============================================================================

-- University Admin
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES (
    2, 'admin_nirma',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
    'admin@nirmauni.ac.in',
    'Dr. Karsanbhai Patel',
    'ADMIN',
    1,
    '+91-9000000002',
    TRUE, NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- Institute Admins
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- IT Institute Admin
    (3, 'admin_nirma_it',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'admin.it@nirmauni.ac.in',
     'Dr. Amit Shah',
     'ADMIN',
     1,
     '+91-9000000003',
     TRUE, NOW(), NOW()),
    -- Law Institute Admin
    (4, 'admin_nirma_law',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'admin.law@nirmauni.ac.in',
     'Dr. Priya Mehta',
     'ADMIN',
     1,
     '+91-9000000004',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- T&P Coordinators (Institute Level)
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- T&P Head - IT Institute
    (5, 'tpo_nirma_it',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'tpo.it@nirmauni.ac.in',
     'Prof. Sunil Pandey',
     'COORDINATOR',
     1,
     '+91-9000000005',
     TRUE, NOW(), NOW()),
    -- T&P Head - Law Institute
    (6, 'tpo_nirma_law',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'tpo.law@nirmauni.ac.in',
     'Prof. Meera Joshi',
     'COORDINATOR',
     1,
     '+91-9000000006',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- Department Coordinators
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- CSE Department Coordinator
    (7, 'coord_cse_nirma',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'coord.cse@nirmauni.ac.in',
     'Prof. Rajesh Verma',
     'COORDINATOR',
     1,
     '+91-9000000007',
     TRUE, NOW(), NOW()),
    -- CE Department Coordinator
    (8, 'coord_ce_nirma',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'coord.ce@nirmauni.ac.in',
     'Prof. Neha Sharma',
     'COORDINATOR',
     1,
     '+91-9000000008',
     TRUE, NOW(), NOW()),
    -- ME Department Coordinator
    (9, 'coord_me_nirma',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'coord.me@nirmauni.ac.in',
     'Prof. Vikram Singh',
     'COORDINATOR',
     1,
     '+91-9000000009',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- Nirma Students
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- CSE Students
    (10, '23BCE001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '23BCE001@nirmauni.ac.in',
     'Urva Gandhi',
     'STUDENT',
     1,
     '+91-9000000010',
     TRUE, NOW(), NOW()),
    (11, '23BCE002',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '23BCE002@nirmauni.ac.in',
     'Raj Mehta',
     'STUDENT',
     1,
     '+91-9000000011',
     TRUE, NOW(), NOW()),
    -- CE Student
    (12, '23CE001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '23CE001@nirmauni.ac.in',
     'Priya Patel',
     'STUDENT',
     1,
     '+91-9000000012',
     TRUE, NOW(), NOW()),
    -- ME Student
    (13, '23ME001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '23ME001@nirmauni.ac.in',
     'Amit Shah',
     'STUDENT',
     1,
     '+91-9000000013',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- =============================================================================
-- 3.3 PP SAVANI UNIVERSITY USERS
-- =============================================================================

-- University Admin
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES (
    14, 'admin_ppsu',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
    'admin@ppsu.ac.in',
    'Dr. PP Savani',
    'ADMIN',
    2,
    '+91-9000000014',
    TRUE, NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- Institute/School Admins
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- IT School Admin
    (15, 'admin_ppsu_it',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'admin.it@ppsu.ac.in',
     'Dr. Ketan Patel',
     'ADMIN',
     2,
     '+91-9000000015',
     TRUE, NOW(), NOW()),
    -- Management School Admin
    (16, 'admin_ppsu_mgt',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'admin.mgt@ppsu.ac.in',
     'Dr. Sneha Desai',
     'ADMIN',
     2,
     '+91-9000000016',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- T&P Coordinators (School Level)
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- T&P Head - Engineering School
    (17, 'tpo_ppsu_it',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'tpo.it@ppsu.ac.in',
     'Prof. Hardik Shah',
     'COORDINATOR',
     2,
     '+91-9000000017',
     TRUE, NOW(), NOW()),
    -- T&P Head - Management School
    (18, 'tpo_ppsu_mgt',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'tpo.mgt@ppsu.ac.in',
     'Prof. Nisha Jain',
     'COORDINATOR',
     2,
     '+91-9000000018',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- Department Coordinators
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- CS Department Coordinator
    (19, 'coord_cs_ppsu',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'coord.cs@ppsu.ac.in',
     'Prof. Yash Modi',
     'COORDINATOR',
     2,
     '+91-9000000019',
     TRUE, NOW(), NOW()),
    -- IT Department Coordinator
    (20, 'coord_it_ppsu',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     'coord.it@ppsu.ac.in',
     'Prof. Ravi Kumar',
     'COORDINATOR',
     2,
     '+91-9000000020',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;

-- PPSU Students
INSERT INTO users (
    id, username, password_hash, email, full_name, role, college_id, phone_number, is_active, created_at, updated_at
) VALUES
    -- CS Students
    (21, '24CS001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '24CS001@ppsu.ac.in',
     'Harsh Trivedi',
     'STUDENT',
     2,
     '+91-9000000021',
     TRUE, NOW(), NOW()),
    (22, '24CS002',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '24CS002@ppsu.ac.in',
     'Pooja Sharma',
     'STUDENT',
     2,
     '+91-9000000022',
     TRUE, NOW(), NOW()),
    -- IT Student
    (23, '24IT001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '24IT001@ppsu.ac.in',
     'Nikhil Patel',
     'STUDENT',
     2,
     '+91-9000000023',
     TRUE, NOW(), NOW()),
    -- MBA Student
    (24, '24MBA001',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.V5yR3E9MCKCI9O',
     '24MBA001@ppsu.ac.in',
     'Ankita Joshi',
     'STUDENT',
     2,
     '+91-9000000024',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 4: USER ASSIGNMENTS (Role-Based Scope)
-- ================================================================================
-- This defines WHO can do WHAT and WHERE in the system.
-- scope: SUBTREE = all descendants, CHILDREN = direct children, SELF = only this unit
-- ================================================================================

INSERT INTO user_assignments (
    id, user_id, organization_unit_id, role, scope, designation, is_primary, is_active, assigned_at, created_at, updated_at
) VALUES
    -- =================================
    -- NIRMA UNIVERSITY ASSIGNMENTS
    -- =================================

    -- University Admin (full control over Nirma)
    (1, 2, 1, 'ADMIN', 'SUBTREE', 'University Administrator', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- Institute Admins (control over their institutes)
    (2, 3, 2, 'ADMIN', 'SUBTREE', 'Director - Institute of Technology', TRUE, TRUE, NOW(), NOW(), NOW()),
    (3, 4, 3, 'ADMIN', 'SUBTREE', 'Director - Institute of Law', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- T&P Coordinators (placement operations for institute)
    (4, 5, 2, 'COORDINATOR', 'SUBTREE', 'Head - Training & Placement (IT)', TRUE, TRUE, NOW(), NOW(), NOW()),
    (5, 6, 3, 'COORDINATOR', 'SUBTREE', 'Head - Training & Placement (Law)', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- Department Coordinators (self scope - only their department)
    (6, 7, 4, 'COORDINATOR', 'SELF', 'Placement Coordinator - CSE', TRUE, TRUE, NOW(), NOW(), NOW()),
    (7, 8, 5, 'COORDINATOR', 'SELF', 'Placement Coordinator - CE', TRUE, TRUE, NOW(), NOW(), NOW()),
    (8, 9, 6, 'COORDINATOR', 'SELF', 'Placement Coordinator - ME', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- Students (assigned to departments)
    (9, 10, 4, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (10, 11, 4, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (11, 12, 5, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (12, 13, 6, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- =================================
    -- PP SAVANI UNIVERSITY ASSIGNMENTS
    -- =================================

    -- University Admin
    (13, 14, 8, 'ADMIN', 'SUBTREE', 'University Administrator', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- School Admins
    (14, 15, 9, 'ADMIN', 'SUBTREE', 'Dean - School of Engineering', TRUE, TRUE, NOW(), NOW(), NOW()),
    (15, 16, 10, 'ADMIN', 'SUBTREE', 'Dean - School of Management', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- T&P Coordinators
    (16, 17, 9, 'COORDINATOR', 'SUBTREE', 'Head - Training & Placement (Engg)', TRUE, TRUE, NOW(), NOW(), NOW()),
    (17, 18, 10, 'COORDINATOR', 'SUBTREE', 'Head - Training & Placement (Mgmt)', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- Department Coordinators
    (18, 19, 11, 'COORDINATOR', 'SELF', 'Placement Coordinator - CS', TRUE, TRUE, NOW(), NOW(), NOW()),
    (19, 20, 12, 'COORDINATOR', 'SELF', 'Placement Coordinator - IT', TRUE, TRUE, NOW(), NOW(), NOW()),

    -- Students
    (20, 21, 11, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (21, 22, 11, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (22, 23, 12, 'STUDENT', 'SELF', 'B.Tech Student', TRUE, TRUE, NOW(), NOW(), NOW()),
    (23, 24, 13, 'STUDENT', 'SELF', 'MBA Student', TRUE, TRUE, NOW(), NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    role = EXCLUDED.role,
    scope = EXCLUDED.scope,
    designation = EXCLUDED.designation,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 5: STUDENT PROFILES
-- ================================================================================

INSERT INTO student_profiles (
    id, user_id, enrollment_number, department, department_id, program, current_semester, batch_year,
    cgpa, tenth_percentage, twelfth_percentage, active_backlogs, history_backlogs,
    skills, resume_url, linkedin_url, github_url, projects_count, internship_months,
    certifications, career_interests, created_at, updated_at
) VALUES
    -- Nirma Students
    (1, 10, '23BCE001', 'CSE', 4, 'B.Tech', 4, 2023,
     8.9, 92.5, 89.0, 0, 0,
     'Java,Spring Boot,React,PostgreSQL,Docker',
     NULL, 'https://linkedin.com/in/urvagandhi', 'https://github.com/urvagandhi',
     5, 6, 'AWS Certified', '["Backend","Cloud"]',
     NOW(), NOW()),

    (2, 11, '23BCE002', 'CSE', 4, 'B.Tech', 4, 2023,
     8.5, 90.0, 87.0, 0, 0,
     'Python,Django,Machine Learning,TensorFlow',
     NULL, 'https://linkedin.com/in/rajmehta', 'https://github.com/rajmehta',
     4, 3, 'Google Cloud Associate', '["ML","Data Science"]',
     NOW(), NOW()),

    (3, 12, '23CE001', 'CE', 5, 'B.Tech', 4, 2023,
     7.8, 85.0, 82.0, 1, 0,
     'C++,Data Structures,Algorithms,System Design',
     NULL, 'https://linkedin.com/in/priyapatel', 'https://github.com/priyapatel',
     3, 0, NULL, '["Backend","System Design"]',
     NOW(), NOW()),

    (4, 13, '23ME001', 'ME', 6, 'B.Tech', 4, 2023,
     7.5, 80.0, 78.0, 0, 0,
     'AutoCAD,SolidWorks,Simulation',
     NULL, NULL, NULL,
     2, 0, NULL, '["Automotive","Manufacturing"]',
     NOW(), NOW()),

    -- PPSU Students
    (5, 21, '24CS001', 'CS', 11, 'B.Tech', 2, 2024,
     8.2, 88.0, 85.0, 0, 0,
     'Java,Python,React,Node.js',
     NULL, 'https://linkedin.com/in/harshtrivedi', 'https://github.com/harshtrivedi',
     3, 0, NULL, '["Full Stack","Backend"]',
     NOW(), NOW()),

    (6, 22, '24CS002', 'CS', 11, 'B.Tech', 2, 2024,
     7.9, 86.0, 83.0, 0, 0,
     'Python,Data Analysis,SQL',
     NULL, 'https://linkedin.com/in/poojasharma', NULL,
     2, 0, NULL, '["Data Analytics"]',
     NOW(), NOW()),

    (7, 23, '24IT001', 'IT', 12, 'B.Tech', 2, 2024,
     8.0, 87.0, 84.0, 0, 0,
     'JavaScript,React,MongoDB',
     NULL, NULL, 'https://github.com/nikhilpatel',
     2, 0, NULL, '["Frontend","Full Stack"]',
     NOW(), NOW()),

    (8, 24, '24MBA001', 'MBA', 13, 'MBA', 2, 2024,
     8.5, 90.0, 88.0, 0, 0,
     'Marketing,Finance,Analytics',
     NULL, 'https://linkedin.com/in/ankitajoshi', NULL,
     1, 6, 'CFA Level 1', '["Finance","Consulting"]',
     NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    active_backlogs = EXCLUDED.active_backlogs,
    skills = EXCLUDED.skills,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 6: COMPANIES
-- ================================================================================

INSERT INTO companies (
    id, name, industry, website, description, logo_url, location,
    contact_email, contact_phone, is_active, created_at, updated_at
) VALUES
    (1, 'Google', 'Technology', 'https://google.com',
     'Global technology company specializing in search, cloud, and AI.',
     NULL, 'Bangalore, India',
     'campus@google.com', '+91-80-12345678',
     TRUE, NOW(), NOW()),

    (2, 'Microsoft', 'Technology', 'https://microsoft.com',
     'Global leader in software, cloud computing, and personal computing.',
     NULL, 'Hyderabad, India',
     'campus@microsoft.com', '+91-40-12345678',
     TRUE, NOW(), NOW()),

    (3, 'Tata Consultancy Services', 'IT Services', 'https://tcs.com',
     'Global leader in IT services, consulting, and business solutions.',
     NULL, 'Mumbai, India',
     'campus.recruitment@tcs.com', '+91-22-12345678',
     TRUE, NOW(), NOW()),

    (4, 'Infosys', 'IT Services', 'https://infosys.com',
     'Global leader in next-generation digital services and consulting.',
     NULL, 'Bangalore, India',
     'campus@infosys.com', '+91-80-11111111',
     TRUE, NOW(), NOW()),

    (5, 'Reliance Industries', 'Conglomerate', 'https://ril.com',
     'India largest private sector company with diverse business interests.',
     NULL, 'Mumbai, India',
     'careers@ril.com', '+91-22-44447000',
     TRUE, NOW(), NOW()),

    (6, 'Deloitte', 'Consulting', 'https://deloitte.com',
     'Global professional services network providing audit, consulting, and tax services.',
     NULL, 'Mumbai, India',
     'campus@deloitte.com', '+91-22-61854000',
     TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 7: PLACEMENT DRIVES
-- ================================================================================

INSERT INTO placement_drives (
    id, company_id, college_id, title, job_role, description, package_lpa, drive_date, registration_deadline,
    status, min_cgpa, max_backlogs, required_skills, location, is_remote,
    created_at, updated_at
) VALUES
    (1, 1, 1, 'Google Campus Drive 2026', 'Software Engineer',
     'Hiring for SWE roles. Work on products used by billions.',
     32.0, '2026-03-15', '2026-02-28',
     'UPCOMING', 8.0, 0, 'DSA,System Design,Java,Python',
     'Bangalore', FALSE, NOW(), NOW()),

    (2, 2, 1, 'Microsoft IDC Hiring', 'SDE-1',
     'Microsoft India Development Center hiring for Azure and Office teams.',
     28.0, '2026-03-01', '2026-02-15',
     'UPCOMING', 7.5, 0, 'C++,System Programming,Distributed Systems',
     'Hyderabad', FALSE, NOW(), NOW()),

    (3, 3, 2, 'TCS Digital Hiring', 'System Engineer',
     'TCS Digital hiring for digital transformation projects.',
     7.5, '2026-04-01', '2026-03-20',
     'UPCOMING', 6.0, 2, 'Programming,SQL,Communication',
     'Pan India', FALSE, NOW(), NOW()),

    (4, 4, 1, 'Infosys Power Programmer', 'Specialist Programmer',
     'For students with exceptional programming skills.',
     9.5, '2026-03-10', '2026-02-25',
     'UPCOMING', 7.0, 1, 'DSA,Problem Solving,Java',
     'Bangalore,Pune', FALSE, NOW(), NOW()),

    (5, 5, 2, 'Reliance JioGenNext', 'Graduate Engineer Trainee',
     'Fast-track leadership program at Reliance.',
     12.0, '2026-03-20', '2026-03-10',
     'UPCOMING', 7.0, 0, 'Communication,Leadership,Technical Skills',
     'Mumbai', FALSE, NOW(), NOW()),

    (6, 6, 2, 'Deloitte Consulting', 'Business Analyst',
     'Consulting roles for MBA and engineering graduates.',
     15.0, '2026-03-25', '2026-03-15',
     'UPCOMING', 7.5, 0, 'Analytics,Communication,Problem Solving',
     'Mumbai,Delhi', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    status = EXCLUDED.status,
    package_lpa = EXCLUDED.package_lpa,
    updated_at = NOW();

-- ================================================================================
-- SECTION 8: DRIVE ELIGIBLE DEPARTMENTS (Many-to-Many Mapping)
-- ================================================================================

INSERT INTO drive_eligible_departments (drive_id, department_id) VALUES
    -- Drive 1 (Google - Nirma): CSE(4), CE(5)
    (1, 4), (1, 5),

    -- Drive 2 (Microsoft - Nirma): CSE(4), CE(5)
    (2, 4), (2, 5),

    -- Drive 3 (TCS - PPSU): CS(11), IT(12), MBA(13)
    (3, 11), (3, 12), (3, 13),

    -- Drive 4 (Infosys - Nirma): CSE(4), CE(5), ME(6)
    (4, 4), (4, 5), (4, 6),

    -- Drive 5 (Reliance - PPSU): CS(11), IT(12), MBA(13)
    (5, 11), (5, 12), (5, 13),

    -- Drive 6 (Deloitte - PPSU): IT(12), MBA(13)
    (6, 12), (6, 13)
ON CONFLICT DO NOTHING;
;


-- ================================================================================
-- SECTION 9: SAMPLE APPLICATIONS
-- ================================================================================

INSERT INTO applications (
    id, student_id, drive_id, status, applied_at, resume_url, cover_letter, created_at, updated_at
) VALUES
    -- Urva applying to high-tier companies
    (1, 1, 1, 'APPLIED', NOW(), NULL, 'Excited to join Google...', NOW(), NOW()),
    (2, 1, 2, 'SHORTLISTED', NOW() - INTERVAL '5 days', NULL, 'Strong interest in Microsoft...', NOW() - INTERVAL '5 days', NOW()),

    -- Raj applying
    (3, 2, 1, 'APPLIED', NOW(), NULL, NULL, NOW(), NOW()),
    (4, 2, 4, 'APPLIED', NOW(), NULL, NULL, NOW(), NOW()),

    -- PPSU students applying
    (5, 5, 3, 'APPLIED', NOW(), NULL, 'Interested in TCS Digital...', NOW(), NOW()),
    (6, 5, 4, 'APPLIED', NOW(), NULL, NULL, NOW(), NOW()),

    -- MBA student applying to consulting
    (7, 8, 6, 'APPLIED', NOW(), NULL, 'Eager to join Deloitte Consulting...', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
    updated_at = NOW();;


-- ================================================================================
-- SECTION 10: SEQUENCE RESET
-- ================================================================================

SELECT setval('colleges_id_seq', COALESCE((SELECT MAX(id) FROM colleges), 1));;
SELECT setval('organization_units_id_seq', COALESCE((SELECT MAX(id) FROM organization_units), 1));;
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1));;
SELECT setval('user_assignments_id_seq', COALESCE((SELECT MAX(id) FROM user_assignments), 1));;
SELECT setval('student_profiles_id_seq', COALESCE((SELECT MAX(id) FROM student_profiles), 1));;
SELECT setval('companies_id_seq', COALESCE((SELECT MAX(id) FROM companies), 1));;
SELECT setval('placement_drives_id_seq', COALESCE((SELECT MAX(id) FROM placement_drives), 1));;
SELECT setval('applications_id_seq', COALESCE((SELECT MAX(id) FROM applications), 1));;


-- ================================================================================
-- END OF SEED DATA
-- ================================================================================
--
-- QUICK TEST LOGINS:
--   superadmin@placementpro.com / password123 (Platform Admin)
--   admin@nirmauni.ac.in / password123 (Nirma University Admin)
--   admin@ppsu.ac.in / password123 (PPSU University Admin)
--   tpo.it@nirmauni.ac.in / password123 (T&P Coordinator)
--   23BCE001@nirmauni.ac.in / password123 (Student)
--
-- ================================================================================
