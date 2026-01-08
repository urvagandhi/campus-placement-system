-- ============================================
-- PlacementPro - Seed Data v2.0
-- Realistic Nirma University Hierarchy
-- ============================================

-- Run AFTER schema-redesign.sql

-- ============================================
-- 1. COLLEGES
-- ============================================
INSERT INTO colleges (id, name, code, is_active, created_at) VALUES
(1, 'Nirma University', 'NIRMA001', true, NOW());

SELECT setval('colleges_id_seq', (SELECT MAX(id) FROM colleges));

-- ============================================
-- 2. ORGANIZATION UNITS (Hierarchy)
-- ============================================

-- University Level (Root) - is_root = true
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(1, 1, 'Nirma University', 'NU', 'UNIVERSITY', NULL, 'nirmauni.ac.in', true, true, NOW());

-- Institute Level (Children of University)
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(2, 1, 'Institute of Technology', 'IT', 'INSTITUTE', 1, NULL, false, true, NOW()),
(3, 1, 'Institute of Law', 'IL', 'INSTITUTE', 1, NULL, false, true, NOW()),
(4, 1, 'Institute of Management', 'IM', 'INSTITUTE', 1, NULL, false, true, NOW()),
(5, 1, 'Institute of Pharmacy', 'IP', 'INSTITUTE', 1, NULL, false, true, NOW());

-- Department Level (Children of Institute of Technology)
INSERT INTO organization_units (id, college_id, name, code, type, parent_id, email_domain, is_root, is_active, created_at) VALUES
(6, 1, 'Computer Science & Engineering', 'CSE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(7, 1, 'Computer Engineering', 'CE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(8, 1, 'Mechanical Engineering', 'ME', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(9, 1, 'Electronics & Communication', 'ECE', 'DEPARTMENT', 2, NULL, false, true, NOW()),
(10, 1, 'Electrical Engineering', 'EE', 'DEPARTMENT', 2, NULL, false, true, NOW());

SELECT setval('organization_units_id_seq', (SELECT MAX(id) FROM organization_units));

-- ============================================
-- 3. USERS
-- Password for ALL users: password123
-- BCrypt (12 rounds): $2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6
-- ============================================

INSERT INTO users (id, name, email, password_hash, role, college_id, is_active, created_at) VALUES
-- Super Admin (Platform Owner - no college)
(1, 'Super Admin', 'super.admin@placement.pro',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'SUPER_ADMIN', NULL, true, NOW()),

-- Admin (University Level)
(2, 'Karsan Patel', 'admin@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'ADMIN', 1, true, NOW()),

-- Coordinators (TPO, Faculty Coordinators, Corporate Relations)
(3, 'Sunil Pandi', 'tpo@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW()),

(4, 'Dr. Jai Verma', 'jai.verma@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW()),

(5, 'Priya Sharma', 'priya.sharma@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW()),

(6, 'Dr. Meera Patel', 'meera.patel@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'COORDINATOR', 1, true, NOW()),

-- Students (CSE Department)
(7, 'Urva Gandhi', '23BCE078@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW()),

(8, 'Raj Mehta', '23BCE079@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW()),

(9, 'Priya Singh', '23BCE080@nirmauni.ac.in',
   '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
   'STUDENT', 1, true, NOW()),

-- Students (CE Department)
(10, 'Amit Patel', '23CE001@nirmauni.ac.in',
    '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
    'STUDENT', 1, true, NOW()),

(11, 'Neha Shah', '23CE002@nirmauni.ac.in',
    '$2b$12$ZMXRwQszhYtRM5wzgr8jdOhmH1AEhKAAosYmmsAeWMqgtg38g.6V6',
    'STUDENT', 1, true, NOW());

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- ============================================
-- 4. USER ASSIGNMENTS (Who Works Where)
-- ============================================

INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope_level, is_primary, created_at) VALUES
-- Admin: University level, SUBTREE scope (can manage everything)
(1, 2, 1, 'University Administrator', 'SUBTREE', true, NOW()),

-- TPO: Institute of Technology, SUBTREE scope (manages all IT departments)
(2, 3, 2, 'Head - Training & Placement', 'SUBTREE', true, NOW()),

-- Faculty Coordinator CSE: Department level, SELF scope (only CSE)
(3, 4, 6, 'Faculty Placement Coordinator', 'SELF', true, NOW()),

-- Faculty Coordinator CE: Department level, SELF scope (only CE)
(4, 6, 7, 'Faculty Placement Coordinator', 'SELF', true, NOW()),

-- Corporate Relations: Institute level, CHILDREN scope (direct children only)
(5, 5, 2, 'Corporate Relations Executive', 'CHILDREN', true, NOW()),

-- Students: Department level assignments
(6, 7, 6, 'Student', 'SELF', true, NOW()),
(7, 8, 6, 'Student', 'SELF', true, NOW()),
(8, 9, 6, 'Student', 'SELF', true, NOW()),
(9, 10, 7, 'Student', 'SELF', true, NOW()),
(10, 11, 7, 'Student', 'SELF', true, NOW());

SELECT setval('user_assignments_id_seq', (SELECT MAX(id) FROM user_assignments));

-- ============================================
-- 5. TEST LOGIN CREDENTIALS SUMMARY
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
