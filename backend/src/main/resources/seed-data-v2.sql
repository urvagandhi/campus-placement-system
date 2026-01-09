-- ============================================
-- PlacementPro - Seed Data v2.0 (Idempotent)
-- Realistic Nirma University Hierarchy
-- ============================================

-- Run AFTER schema.sql
-- This script is IDEMPOTENT - safe to run multiple times

-- ============================================
-- 1. COLLEGES
-- ============================================
INSERT INTO colleges (id, name, code, is_active, created_at) VALUES
(1, 'Nirma University', 'NIRMA001', true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    is_active = EXCLUDED.is_active;

SELECT setval('colleges_id_seq', GREATEST((SELECT MAX(id) FROM colleges), 1));

-- ============================================
-- 2. ORGANIZATION UNITS (Hierarchy)
-- ============================================

-- University Level (Root) - is_root = true
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(1, 1, 'Nirma University', 'NU', 'UNIVERSITY', NULL, 'nirmauni.ac.in', true, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    email_domain = EXCLUDED.email_domain,
    is_active = EXCLUDED.is_active;

-- Institute Level (Children of University)
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(2, 1, 'Institute of Technology', 'IT', 'INSTITUTE', 1, NULL, false, true, NOW()),
(3, 1, 'Institute of Law', 'IL', 'INSTITUTE', 1, NULL, false, true, NOW()),
(4, 1, 'Institute of Management', 'IM', 'INSTITUTE', 1, NULL, false, true, NOW()),
(5, 1, 'Institute of Pharmacy', 'IP', 'INSTITUTE', 1, NULL, false, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    is_active = EXCLUDED.is_active;

-- Department Level (Children of Institute of Technology)
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(6, 1, 'Computer Science & Engineering', 'CSE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(7, 1, 'Computer Engineering', 'CE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(8, 1, 'Mechanical Engineering', 'ME', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(9, 1, 'Electronics & Communication', 'ECE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(10, 1, 'Electrical Engineering', 'EE', 'DEPARTMENT', 2, NULL, false, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    code = EXCLUDED.code,
    is_active = EXCLUDED.is_active;

SELECT setval('organization_units_id_seq', GREATEST((SELECT MAX(id) FROM organization_units), 1));

-- ============================================
-- 3. USERS
-- Password for ALL users: password123
-- BCrypt (12 rounds): $2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6
-- ============================================

-- Super Admin (Platform Owner - no college)
INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(1, 'Super Admin', 'super.admin@placement.pro',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'SUPER_ADMIN', NULL, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

-- Admin (University Level)
INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(2, 'Karsan Patel', 'admin@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'ADMIN', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

-- Coordinators (TPO, Faculty Coordinators, Corporate Relations)
INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(3, 'Sunil Pandi', 'tpo@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(4, 'Dr. Jai Verma', 'jai.verma@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(5, 'Priya Sharma', 'priya.sharma@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(6, 'Dr. Meera Patel', 'meera.patel@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

-- Students (CSE Department)
INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(7, 'Urva Gandhi', '23BCE078@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(8, 'Raj Mehta', '23BCE079@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(9, 'Priya Singh', '23BCE080@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

-- Students (CE Department)
INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(10, 'Amit Patel', '23CE001@nirmauni.ac.in',
    '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
    'STUDENT', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
(11, 'Neha Shah', '23CE002@nirmauni.ac.in',
    '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
    'STUDENT', 1, true, NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    is_active = EXCLUDED.is_active;

SELECT setval('users_id_seq', GREATEST((SELECT MAX(id) FROM users), 1));

-- ============================================
-- 4. USER ASSIGNMENTS (Who Works Where)
-- ============================================

-- Admin: University level, SUBTREE scope (can manage everything)
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(1, 2, 1, 'University Administrator', 'SUBTREE', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

-- TPO: Institute of Technology, SUBTREE scope (manages all IT departments)
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(2, 3, 2, 'Head - Training & Placement', 'SUBTREE', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

-- Faculty Coordinator CSE: Department level, SELF scope (only CSE)
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(3, 4, 6, 'Faculty Placement Coordinator', 'SELF', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

-- Faculty Coordinator CE: Department level, SELF scope (only CE)
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(4, 6, 7, 'Faculty Placement Coordinator', 'SELF', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

-- Corporate Relations: Institute level, CHILDREN scope (direct children only)
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(5, 5, 2, 'Corporate Relations Executive', 'CHILDREN', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

-- Students: Department level assignments
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
(6, 7, 6, 'Student', 'SELF', true, NOW()),
(7, 8, 6, 'Student', 'SELF', true, NOW()),
(8, 9, 6, 'Student', 'SELF', true, NOW()),
(9, 10, 7, 'Student', 'SELF', true, NOW()),
(10, 11, 7, 'Student', 'SELF', true, NOW())
ON CONFLICT (id) DO UPDATE SET designation = EXCLUDED.designation;

SELECT setval('user_assignments_id_seq', GREATEST((SELECT MAX(id) FROM user_assignments), 1));

-- ============================================
-- 5. STUDENT PROFILES
-- ============================================

-- CSE Students (department_id = 6)
INSERT INTO student_profiles (id, user_id, enrollment_no, department_id, cgpa, backlogs, batch_year, semester, skills, resume_url, projects_count, internship_months, created_at) VALUES
(1, 7, '23BCE078', 6, 8.5, 0, 2023, '4', 'Java,Spring Boot,React,PostgreSQL', NULL, 3, 2, NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    semester = EXCLUDED.semester,
    skills = EXCLUDED.skills;

INSERT INTO student_profiles (id, user_id, enrollment_no, department_id, cgpa, backlogs, batch_year, semester, skills, resume_url, projects_count, internship_months, created_at) VALUES
(2, 8, '23BCE079', 6, 8.2, 0, 2023, '4', 'Python,Django,Machine Learning', NULL, 2, 0, NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    semester = EXCLUDED.semester,
    skills = EXCLUDED.skills;

INSERT INTO student_profiles (id, user_id, enrollment_no, department_id, cgpa, backlogs, batch_year, semester, skills, resume_url, projects_count, internship_months, created_at) VALUES
(3, 9, '23BCE080', 6, 7.8, 1, 2023, '4', 'JavaScript,Node.js,MongoDB', NULL, 1, 0, NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    semester = EXCLUDED.semester,
    skills = EXCLUDED.skills;

-- CE Students (department_id = 7)
INSERT INTO student_profiles (id, user_id, enrollment_no, department_id, cgpa, backlogs, batch_year, semester, skills, resume_url, projects_count, internship_months, created_at) VALUES
(4, 10, '23CE001', 7, 7.5, 0, 2023, '4', 'C++,Data Structures,Algorithms', NULL, 2, 0, NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    semester = EXCLUDED.semester,
    skills = EXCLUDED.skills;

INSERT INTO student_profiles (id, user_id, enrollment_no, department_id, cgpa, backlogs, batch_year, semester, skills, resume_url, projects_count, internship_months, created_at) VALUES
(5, 11, '23CE002', 7, 8.8, 0, 2023, '4', 'Java,Android,Kotlin', NULL, 4, 3, NOW())
ON CONFLICT (id) DO UPDATE SET
    cgpa = EXCLUDED.cgpa,
    semester = EXCLUDED.semester,
    skills = EXCLUDED.skills;

SELECT setval('student_profiles_id_seq', GREATEST((SELECT MAX(id) FROM student_profiles), 1));

-- ============================================
-- 6. TEST LOGIN CREDENTIALS SUMMARY
-- ============================================
/*
+----------------------------------+-------------+------------------+------------------------+
| Email                            | Role        | Org Unit         | Scope                  |
+----------------------------------+-------------+------------------+------------------------+
| super.admin@placement.pro        | SUPER_ADMIN | (Platform)       | ALL                    |
+----------------------------------+-------------+------------------+------------------------+
| admin@nirmauni.ac.in             | ADMIN       | Nirma University | SUBTREE (all)          |
+----------------------------------+-------------+------------------+------------------------+
| tpo@nirmauni.ac.in               | COORDINATOR | Inst. of Tech    | SUBTREE (all IT depts) |
| jai.verma@nirmauni.ac.in         | COORDINATOR | CSE Dept         | SELF (CSE only)        |
| priya.sharma@nirmauni.ac.in      | COORDINATOR | Inst. of Tech    | CHILDREN (direct)      |
| meera.patel@nirmauni.ac.in       | COORDINATOR | CE Dept          | SELF (CE only)         |
+----------------------------------+-------------+------------------+------------------------+
| 23BCE078@nirmauni.ac.in          | STUDENT     | CSE Dept         | SELF                   |
| 23BCE079@nirmauni.ac.in          | STUDENT     | CSE Dept         | SELF                   |
| 23BCE080@nirmauni.ac.in          | STUDENT     | CSE Dept         | SELF                   |
| 23CE001@nirmauni.ac.in           | STUDENT     | CE Dept          | SELF                   |
| 23CE002@nirmauni.ac.in           | STUDENT     | CE Dept          | SELF                   |
+----------------------------------+-------------+------------------+------------------------+

Password for ALL users: password123

HIERARCHY:
Nirma University (UNIVERSITY) [is_root=true]
 └── Institute of Technology (INSTITUTE)
       ├── CSE (DEPARTMENT)
       ├── CE (DEPARTMENT)
       ├── ME (DEPARTMENT)
       ├── ECE (DEPARTMENT)
       └── EE (DEPARTMENT)
 └── Institute of Law (INSTITUTE)
 └── Institute of Management (INSTITUTE)
 └── Institute of Pharmacy (INSTITUTE)
*/
