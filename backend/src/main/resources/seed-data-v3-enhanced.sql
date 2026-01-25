-- ================================================================================
-- PlacementPro Complete Standalone Mock Data V3
-- ================================================================================
-- This is a STANDALONE script - run directly after schema.sql
-- Do NOT run seed-data-v2.sql - this script includes everything
-- ================================================================================
-- PASSWORD FOR ALL USERS: Password@123
-- BCrypt Hash (cost 12): $2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK
-- ================================================================================


-- ================================================================================
-- 0. RESET PASSWORDS FOR ANY EXISTING USERS (fixes old password hashes)
-- ================================================================================
UPDATE users SET password_hash = '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK' WHERE password_hash IS NOT NULL;

-- ================================================================================
-- 1. COLLEGES (Tenant Boundaries)
-- ================================================================================
INSERT INTO colleges (id, name, code, address, website, contact_email, is_active, created_at)
VALUES
    (1, 'Nirma University', 'NIRMA001', 'Sarkhej-Gandhinagar Highway, Ahmedabad, Gujarat 382481', 'https://www.nirmauni.ac.in', 'admissions@nirmauni.ac.in', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 2. ORGANIZATION UNITS (Complete Hierarchical Structure)
-- ================================================================================
INSERT INTO organization_units (id, college_id, name, code, type, parent_unit_id, email_domain, is_root, is_active, created_at)
VALUES
    -- University Root
    (1, 1, 'Nirma University', 'NU', 'UNIVERSITY', NULL, 'nirmauni.ac.in', TRUE, TRUE, NOW()),
    
    -- Institute of Technology
    (2, 1, 'Institute of Technology', 'IT', 'INSTITUTE', 1, NULL, FALSE, TRUE, NOW()),
    -- Institute of Law
    (3, 1, 'Institute of Law', 'IL', 'INSTITUTE', 1, NULL, FALSE, TRUE, NOW()),
    -- Institute of Science
    (4, 1, 'Institute of Science', 'ISNU', 'INSTITUTE', 1, NULL, FALSE, TRUE, NOW()),
    
    -- Departments under Institute of Technology
    (10, 1, 'Computer Science & Engineering', 'CSE', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    (11, 1, 'Computer Engineering', 'CE', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    (12, 1, 'Mechanical Engineering', 'ME', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    (13, 1, 'Electrical Engineering', 'EE', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    
    -- Departments under Institute of Law
    (20, 1, 'Corporate Law', 'CL', 'DEPARTMENT', 3, NULL, FALSE, TRUE, NOW()),
    (21, 1, 'Criminal Law', 'CRIM', 'DEPARTMENT', 3, NULL, FALSE, TRUE, NOW()),
    
    -- Departments under Institute of Science
    (30, 1, 'Physics', 'PHY', 'DEPARTMENT', 4, NULL, FALSE, TRUE, NOW()),
    (31, 1, 'Chemistry', 'CHEM', 'DEPARTMENT', 4, NULL, FALSE, TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 3. USERS (All Roles)
-- ================================================================================
-- SUPER ADMIN
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    (1, 'superadmin', 'Platform Administrator', 'superadmin@placementpro.com', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'SUPER_ADMIN', NULL, '+919000000001', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- UNIVERSITY ADMIN (ROOT - sees all)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    (2, 'admin_nirma', 'Dr. Karsanbhai Patel', 'admin@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919000000002', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- INSTITUTE ADMINS (3 - one per institute)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    (10, 'admin_nirma_it', 'Dr. Rajesh Patel', 'rajesh.patel@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919100000001', TRUE, NOW()),
    (11, 'admin_nirma_law', 'Dr. Priya Mehta', 'priya.mehta@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919100000002', TRUE, NOW()),
    (12, 'admin_nirma_science', 'Dr. Ramesh Kumar', 'ramesh.kumar@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919100000003', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- T&P COORDINATORS (3 - one per institute)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    (20, 'tpo_nirma_it', 'Prof. Sunil Pandey', 'tpo.it@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919200000001', TRUE, NOW()),
    (21, 'tpo_nirma_law', 'Prof. Meera Joshi', 'tpo.law@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919200000002', TRUE, NOW()),
    (22, 'tpo_nirma_science', 'Prof. Anjali Desai', 'tpo.science@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919200000003', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- DEPARTMENT ADMINS (2 per institute = 6 total)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    -- IT Institute dept admins
    (30, 'admin_cse', 'Dr. Amit Shah', 'admin.cse@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000001', TRUE, NOW()),
    (31, 'admin_ce', 'Dr. Neha Sharma', 'admin.ce@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000002', TRUE, NOW()),
    -- Law Institute dept admins
    (32, 'admin_corplaw', 'Dr. Vikram Singh', 'admin.corplaw@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000003', TRUE, NOW()),
    (33, 'admin_crimlaw', 'Dr. Kavita Rao', 'admin.crimlaw@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000004', TRUE, NOW()),
    -- Science Institute dept admins
    (34, 'admin_phy', 'Dr. Manoj Trivedi', 'admin.phy@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000005', TRUE, NOW()),
    (35, 'admin_chem', 'Dr. Deepa Nair', 'admin.chem@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'ADMIN', 1, '+919300000006', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- DEPARTMENT COORDINATORS (multiple per department)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    -- CSE Coordinators
    (40, 'coord_cse_1', 'Prof. Rajesh Verma', 'coord.cse@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000001', TRUE, NOW()),
    (41, 'coord_cse_2', 'Prof. Anita Patel', 'coord2.cse@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000002', TRUE, NOW()),
    -- CE Coordinator
    (42, 'coord_ce_1', 'Prof. Harish Modi', 'coord.ce@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000003', TRUE, NOW()),
    -- ME Coordinator
    (43, 'coord_me_1', 'Prof. Suresh Nair', 'coord.me@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000004', TRUE, NOW()),
    -- EE Coordinator
    (44, 'coord_ee_1', 'Prof. Kiran Shah', 'coord.ee@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000005', TRUE, NOW()),
    -- Law Coordinators
    (45, 'coord_cl_1', 'Prof. Rajan Desai', 'coord.corplaw@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000006', TRUE, NOW()),
    (46, 'coord_crim_1', 'Prof. Sunita Yadav', 'coord.crimlaw@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000007', TRUE, NOW()),
    -- Science Coordinators
    (47, 'coord_phy_1', 'Prof. Vivek Jain', 'coord.phy@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000008', TRUE, NOW()),
    (48, 'coord_chem_1', 'Prof. Rekha Bhatt', 'coord.chem@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'COORDINATOR', 1, '+919400000009', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- STUDENTS (Many students across batches 2022, 2023, 2024)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    -- CSE Students - Batch 2022 (Final year, some placed)
    (100, '22BCE001', 'Aarav Patel', '22BCE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000001', TRUE, NOW()),
    (101, '22BCE002', 'Ananya Sharma', '22BCE002@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000002', TRUE, NOW()),
    (102, '22BCE003', 'Vihaan Kumar', '22BCE003@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000003', TRUE, NOW()),
    (103, '22BCE004', 'Ishita Desai', '22BCE004@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000004', TRUE, NOW()),
    (104, '22BCE005', 'Arjun Singh', '22BCE005@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000005', TRUE, NOW()),
    
    -- CSE Students - Batch 2023
    (110, '23BCE001', 'Urva Gandhi', '23BCE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000010', TRUE, NOW()),
    (111, '23BCE002', 'Raj Mehta', '23BCE002@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000011', TRUE, NOW()),
    (112, '23BCE003', 'Diya Patel', '23BCE003@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000012', TRUE, NOW()),
    (113, '23BCE004', 'Rohan Shah', '23BCE004@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000013', TRUE, NOW()),
    
    -- CSE Students - Batch 2024
    (120, '24BCE001', 'Vivaan Modi', '24BCE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000020', TRUE, NOW()),
    (121, '24BCE002', 'Aisha Khan', '24BCE002@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000021', TRUE, NOW()),
    
    -- CE Students
    (130, '22CE001', 'Rahul Joshi', '22CE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000030', TRUE, NOW()),
    (131, '23CE001', 'Priya Patel', '23CE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000031', TRUE, NOW()),
    (132, '24CE001', 'Kunal Rao', '24CE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000032', TRUE, NOW()),
    
    -- ME Students
    (140, '22ME001', 'Kartik Bhatt', '22ME001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000040', TRUE, NOW()),
    (141, '23ME001', 'Amit Shah', '23ME001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000041', TRUE, NOW()),
    (142, '24ME001', 'Simran Kaur', '24ME001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000042', TRUE, NOW()),
    
    -- EE Students
    (150, '22EE001', 'Harsh Yadav', '22EE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000050', TRUE, NOW()),
    (151, '23EE001', 'Prachi Modi', '23EE001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000051', TRUE, NOW()),
    
    -- Law Students
    (160, '22LAW001', 'Aditi Kapoor', '22LAW001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000060', TRUE, NOW()),
    (161, '23LAW001', 'Karan Mehta', '23LAW001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000061', TRUE, NOW()),
    
    -- Science Students
    (170, '23PHY001', 'Arnav Desai', '23PHY001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000070', TRUE, NOW()),
    (171, '23CHEM001', 'Yash Sharma', '23CHEM001@nirmauni.ac.in', '$2a$12$IvcMKf1o8Sgw.04wp/PNH.xMO72BJVjOV/qtD/VztR2Irw7iEz4ZK', 'STUDENT', 1, '+919500000071', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 4. USER ASSIGNMENTS (Scope Hierarchy)
-- ================================================================================
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope, is_primary, created_at)
VALUES
    -- University Admin (ROOT - SUBTREE on University = sees all)
    (1, 2, 1, 'Vice Chancellor', 'SUBTREE', TRUE, NOW()),
    
    -- Institute Admins (SUBTREE on their Institute)
    (10, 10, 2, 'Director - Institute of Technology', 'SUBTREE', TRUE, NOW()),
    (11, 11, 3, 'Director - Institute of Law', 'SUBTREE', TRUE, NOW()),
    (12, 12, 4, 'Director - Institute of Science', 'SUBTREE', TRUE, NOW()),
    
    -- T&P Coordinators (SUBTREE on their Institute)
    (20, 20, 2, 'Head - Training & Placement (IT)', 'SUBTREE', TRUE, NOW()),
    (21, 21, 3, 'Head - Training & Placement (Law)', 'SUBTREE', TRUE, NOW()),
    (22, 22, 4, 'Head - Training & Placement (Science)', 'SUBTREE', TRUE, NOW()),
    
    -- Department Admins (SUBTREE on their Department)
    (30, 30, 10, 'Head - CSE Department', 'SUBTREE', TRUE, NOW()),
    (31, 31, 11, 'Head - CE Department', 'SUBTREE', TRUE, NOW()),
    (32, 32, 20, 'Head - Corporate Law Department', 'SUBTREE', TRUE, NOW()),
    (33, 33, 21, 'Head - Criminal Law Department', 'SUBTREE', TRUE, NOW()),
    (34, 34, 30, 'Head - Physics Department', 'SUBTREE', TRUE, NOW()),
    (35, 35, 31, 'Head - Chemistry Department', 'SUBTREE', TRUE, NOW()),
    
    -- Department Coordinators (SELF on their Department)
    (40, 40, 10, 'Placement Coordinator - CSE', 'SELF', TRUE, NOW()),
    (41, 41, 10, 'Assistant Coordinator - CSE', 'SELF', TRUE, NOW()),
    (42, 42, 11, 'Placement Coordinator - CE', 'SELF', TRUE, NOW()),
    (43, 43, 12, 'Placement Coordinator - ME', 'SELF', TRUE, NOW()),
    (44, 44, 13, 'Placement Coordinator - EE', 'SELF', TRUE, NOW()),
    (45, 45, 20, 'Placement Coordinator - Corp Law', 'SELF', TRUE, NOW()),
    (46, 46, 21, 'Placement Coordinator - Crim Law', 'SELF', TRUE, NOW()),
    (47, 47, 30, 'Placement Coordinator - Physics', 'SELF', TRUE, NOW()),
    (48, 48, 31, 'Placement Coordinator - Chemistry', 'SELF', TRUE, NOW()),
    
    -- Student Assignments (SELF on their Department)
    -- CSE 2022
    (100, 100, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (101, 101, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (102, 102, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (103, 103, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (104, 104, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- CSE 2023
    (110, 110, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (111, 111, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (112, 112, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (113, 113, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- CSE 2024
    (120, 120, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (121, 121, 10, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- CE
    (130, 130, 11, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (131, 131, 11, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (132, 132, 11, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- ME
    (140, 140, 12, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (141, 141, 12, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (142, 142, 12, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- EE
    (150, 150, 13, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (151, 151, 13, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- Law
    (160, 160, 20, 'LLB Student', 'SELF', TRUE, NOW()),
    (161, 161, 20, 'LLB Student', 'SELF', TRUE, NOW()),
    -- Science
    (170, 170, 30, 'M.Sc Student', 'SELF', TRUE, NOW()),
    (171, 171, 31, 'M.Sc Student', 'SELF', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 5. STUDENT PROFILES
-- ================================================================================
INSERT INTO student_profiles (id, user_id, enrollment_number, department_id, cgpa, active_backlogs, batch_year, current_semester, skills, linkedin_url, github_url, projects_count, internship_months, career_interests, created_at)
VALUES
    -- CSE 2022 (Placed/Unplaced mix)
    (100, 100, '22BCE001', 10, 8.7, 0, 2022, 8, 'Java,Spring Boot,React,AWS', 'https://linkedin.com/in/aaravpatel', 'https://github.com/aaravpatel', 7, 12, '["Backend","Cloud"]', NOW()),
    (101, 101, '22BCE002', 10, 9.2, 0, 2022, 8, 'Python,ML,TensorFlow,PyTorch', 'https://linkedin.com/in/ananyasharma', 'https://github.com/ananyasharma', 6, 6, '["ML","AI"]', NOW()),
    (102, 102, '22BCE003', 10, 7.8, 0, 2022, 8, 'JavaScript,Node.js,MongoDB', 'https://linkedin.com/in/vihaankumar', 'https://github.com/vihaankumar', 5, 3, '["Full Stack"]', NOW()),
    (103, 103, '22BCE004', 10, 8.3, 0, 2022, 8, 'Java,Spring,Microservices', NULL, 'https://github.com/ishitadesai', 4, 3, '["Backend"]', NOW()),
    (104, 104, '22BCE005', 10, 7.2, 1, 2022, 8, 'Python,Django', NULL, NULL, 3, 0, '["Backend"]', NOW()),
    
    -- CSE 2023
    (110, 110, '23BCE001', 10, 8.9, 0, 2023, 6, 'Java,Spring Boot,React,PostgreSQL,Docker', 'https://linkedin.com/in/urvagandhi', 'https://github.com/urvagandhi', 5, 6, '["Backend","Cloud"]', NOW()),
    (111, 111, '23BCE002', 10, 8.5, 0, 2023, 6, 'Python,Django,Machine Learning', 'https://linkedin.com/in/rajmehta', 'https://github.com/rajmehta', 4, 3, '["ML","Data Science"]', NOW()),
    (112, 112, '23BCE003', 10, 8.5, 0, 2023, 6, 'Java,Spring Boot,React', 'https://linkedin.com/in/diyapatel', 'https://github.com/diyapatel', 3, 0, '["Full Stack"]', NOW()),
    (113, 113, '23BCE004', 10, 8.1, 0, 2023, 6, 'Python,Data Analysis,SQL', NULL, NULL, 2, 0, '["Data Analytics"]', NOW()),
    
    -- CSE 2024
    (120, 120, '24BCE001', 10, 8.8, 0, 2024, 4, 'C++,Python,DSA', NULL, 'https://github.com/vivaanmodi', 2, 0, '["Backend"]', NOW()),
    (121, 121, '24BCE002', 10, 9.0, 0, 2024, 4, 'Python,ML Basics', 'https://linkedin.com/in/aishakhan', 'https://github.com/aishakhan', 1, 0, '["AI/ML"]', NOW()),
    
    -- CE
    (130, 130, '22CE001', 11, 8.0, 0, 2022, 8, 'C++,Java,System Design,Networks', 'https://linkedin.com/in/rahuljoshi', 'https://github.com/rahuljoshi', 5, 6, '["Systems","Backend"]', NOW()),
    (131, 131, '23CE001', 11, 7.8, 1, 2023, 6, 'C++,Data Structures,Algorithms', NULL, NULL, 3, 0, '["Backend"]', NOW()),
    (132, 132, '24CE001', 11, 8.5, 0, 2024, 4, 'C,C++,Data Structures', NULL, NULL, 1, 0, '["Embedded"]', NOW()),
    
    -- ME
    (140, 140, '22ME001', 12, 7.8, 0, 2022, 8, 'AutoCAD,SolidWorks,MATLAB', NULL, NULL, 4, 6, '["Automotive"]', NOW()),
    (141, 141, '23ME001', 12, 7.5, 0, 2023, 6, 'SolidWorks,Simulation', NULL, NULL, 2, 0, '["Manufacturing"]', NOW()),
    (142, 142, '24ME001', 12, 7.5, 0, 2024, 4, 'CAD,Drawing', NULL, NULL, 1, 0, '["Core Mechanical"]', NOW()),
    
    -- EE
    (150, 150, '22EE001', 13, 8.2, 0, 2022, 8, 'VLSI,Embedded C,PCB Design', NULL, 'https://github.com/harshyadav', 4, 3, '["Embedded","VLSI"]', NOW()),
    (151, 151, '23EE001', 13, 7.9, 0, 2023, 6, 'Power Systems,MATLAB', NULL, NULL, 2, 0, '["Power"]', NOW()),
    
    -- Law
    (160, 160, '22LAW001', 20, 8.5, 0, 2022, 10, 'Corporate Law,Contracts,Litigation', 'https://linkedin.com/in/aditikapoor', NULL, 2, 6, '["Corporate Law"]', NOW()),
    (161, 161, '23LAW001', 20, 8.0, 0, 2023, 8, 'Legal Research,Drafting', NULL, NULL, 1, 0, '["Litigation"]', NOW()),
    
    -- Science
    (170, 170, '23PHY001', 30, 8.8, 0, 2023, 4, 'Python,MATLAB,Data Analysis', NULL, NULL, 2, 0, '["Research","Data Science"]', NOW()),
    (171, 171, '23CHEM001', 31, 8.3, 0, 2023, 4, 'Lab Skills,Analytical Chemistry', NULL, NULL, 2, 0, '["Research","Pharma"]', NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 6. COMPANIES (Active and Inactive)
-- ================================================================================
INSERT INTO companies (id, name, industry, website, description, location, contact_email, contact_phone, is_active, created_at)
VALUES
    -- Active Companies - Technology
    (1, 'Google', 'Technology', 'https://google.com', 'Global technology company specializing in search, cloud, and AI.', 'Bangalore, India', 'campus@google.com', '+9180-12345678', TRUE, NOW()),
    (2, 'Microsoft', 'Technology', 'https://microsoft.com', 'Global leader in software, cloud computing.', 'Hyderabad, India', 'campus@microsoft.com', '+9140-12345678', TRUE, NOW()),
    (3, 'TCS', 'IT Services', 'https://tcs.com', 'Global leader in IT services and consulting.', 'Mumbai, India', 'campus@tcs.com', '+9122-12345678', TRUE, NOW()),
    (4, 'Infosys', 'IT Services', 'https://infosys.com', 'Digital services and consulting.', 'Bangalore, India', 'campus@infosys.com', '+9180-11111111', TRUE, NOW()),
    (5, 'Amazon', 'E-Commerce & Tech', 'https://amazon.com', 'Global e-commerce and cloud computing.', 'Bangalore, India', 'campus@amazon.com', '+9180-22222222', TRUE, NOW()),
    (6, 'Wipro', 'IT Services', 'https://wipro.com', 'Global IT services company.', 'Bangalore, India', 'campus@wipro.com', '+9180-33333333', TRUE, NOW()),
    (7, 'Flipkart', 'E-Commerce', 'https://flipkart.com', 'Leading e-commerce marketplace in India.', 'Bangalore, India', 'campus@flipkart.com', '+9180-44444444', TRUE, NOW()),
    (8, 'Goldman Sachs', 'Finance', 'https://goldmansachs.com', 'Global investment banking.', 'Bangalore, India', 'campus@gs.com', '+9180-55555555', TRUE, NOW()),
    (9, 'L&T', 'Engineering', 'https://larsentoubro.com', 'Engineering and construction.', 'Mumbai, India', 'campus@lnt.com', '+9122-66666666', TRUE, NOW()),
    (10, 'Maruti Suzuki', 'Automotive', 'https://marutisuzuki.com', 'India largest automobile manufacturer.', 'Gurugram, India', 'campus@maruti.co.in', '+91124-77777777', TRUE, NOW()),
    
    -- Law Firms for Institute of Law
    (13, 'Cyril Amarchand Mangaldas', 'Law Firm', 'https://cyrilshroff.com', 'India largest law firm.', 'Mumbai, India', 'campus@cyrilshroff.com', '+9122-24963535', TRUE, NOW()),
    (14, 'AZB & Partners', 'Law Firm', 'https://azbpartners.com', 'Leading full-service law firm.', 'Mumbai, India', 'campus@azbpartners.com', '+9122-66396880', TRUE, NOW()),
    (15, 'Khaitan & Co', 'Law Firm', 'https://khaitanco.com', 'Full-service law firm with 100+ year history.', 'Mumbai, India', 'campus@khaitanco.com', '+9122-66365000', TRUE, NOW()),
    (16, 'Trilegal', 'Law Firm', 'https://trilegal.com', 'Premier Indian law firm.', 'Mumbai, India', 'campus@trilegal.com', '+9122-43437777', TRUE, NOW()),
    
    -- Research/Pharma Companies for Institute of Science
    (17, 'Dr. Reddys Laboratories', 'Pharmaceutical', 'https://drreddys.com', 'Multinational pharmaceutical company.', 'Hyderabad, India', 'campus@drreddys.com', '+9140-49002900', TRUE, NOW()),
    (18, 'Sun Pharma', 'Pharmaceutical', 'https://sunpharma.com', 'Fifth-largest specialty generic pharmaceutical company.', 'Mumbai, India', 'campus@sunpharma.com', '+9122-43243424', TRUE, NOW()),
    (19, 'ISRO', 'Research', 'https://isro.gov.in', 'Indian Space Research Organisation.', 'Bangalore, India', 'campus@isro.gov.in', '+9180-22172323', TRUE, NOW()),
    (20, 'DRDO', 'Research', 'https://drdo.gov.in', 'Defence Research and Development Organisation.', 'Delhi, India', 'campus@drdo.gov.in', '+9111-23007010', TRUE, NOW()),
    
    -- Inactive Companies (past recruiters)
    (11, 'Startup XYZ (Closed)', 'Startup', 'https://startupxyz.com', 'Early stage startup - no longer recruiting.', 'Pune, India', 'hr@startupxyz.com', '+9120-88888888', FALSE, NOW()),
    (12, 'OldTech Solutions', 'IT Services', 'https://oldtech.com', 'Legacy IT company - reduced hiring.', 'Chennai, India', 'hr@oldtech.com', '+9144-99999999', FALSE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 7. PLACEMENT DRIVES (Across 3 Academic Years)
-- ================================================================================
-- AY 2023-24 (July 2023 - June 2024) - COMPLETED
INSERT INTO placement_drives (id, college_id, company_id, title, description, job_role, package_lpa, drive_date, registration_deadline, status, min_cgpa, max_backlogs, required_skills, location, is_remote, created_at)
VALUES
    (10, 1, 3, 'TCS Campus Hiring 2023', 'TCS mass hiring.', 'System Engineer', 7.0, '2023-09-15', '2023-09-01', 'COMPLETED', 6.0, 2, 'Programming,SQL', 'Pan India', FALSE, '2023-08-01'),
    (11, 1, 4, 'Infosys Hiring 2023', 'Infosys campus drive.', 'Systems Engineer', 6.5, '2023-10-01', '2023-09-20', 'COMPLETED', 6.0, 2, 'Java,SQL', 'Mysore', FALSE, '2023-08-15'),
    (12, 1, 5, 'Amazon SDE 2023', 'Amazon SDE roles.', 'SDE-1', 26.0, '2024-01-20', '2024-01-05', 'COMPLETED', 7.5, 0, 'DSA,Java,Python', 'Bangalore', FALSE, '2023-11-01'),
    (13, 1, 9, 'L&T GET 2023', 'L&T engineering hiring.', 'GET', 8.0, '2024-02-15', '2024-02-01', 'COMPLETED', 6.5, 1, 'AutoCAD,Technical', 'Mumbai', FALSE, '2023-12-01'),
    (14, 1, 10, 'Maruti Campus 2023', 'Maruti ME hiring.', 'Graduate Engineer', 9.0, '2024-03-05', '2024-02-20', 'COMPLETED', 7.0, 0, 'AutoCAD,SolidWorks', 'Gurugram', FALSE, '2024-01-01'),
    -- Law Drives 2023-24
    (15, 1, 13, 'CAM Associate Program 2023', 'Cyril Amarchand law associate program.', 'Associate', 18.0, '2023-11-15', '2023-11-01', 'COMPLETED', 7.5, 0, 'Corporate Law,Research', 'Mumbai', FALSE, '2023-10-01'),
    (16, 1, 14, 'AZB Law Trainee 2023', 'AZB & Partners trainee program.', 'Law Trainee', 15.0, '2024-01-10', '2023-12-20', 'COMPLETED', 7.0, 0, 'Litigation,Drafting', 'Mumbai', FALSE, '2023-11-01'),
    -- Science Drives 2023-24
    (17, 1, 17, 'Dr Reddys Research 2023', 'Research scientist program.', 'Research Scientist', 10.0, '2024-02-01', '2024-01-15', 'COMPLETED', 7.5, 0, 'Chemistry,Lab Skills', 'Hyderabad', FALSE, '2023-12-01'),
    (18, 1, 19, 'ISRO Scientist 2023', 'ISRO scientist recruitment.', 'Scientist/Engineer', 12.0, '2024-03-01', '2024-02-15', 'COMPLETED', 8.0, 0, 'Physics,Mathematics', 'Bangalore', FALSE, '2024-01-01')
ON CONFLICT (id) DO NOTHING;

-- AY 2024-25 (July 2024 - June 2025) - COMPLETED
INSERT INTO placement_drives (id, college_id, company_id, title, description, job_role, package_lpa, drive_date, registration_deadline, status, min_cgpa, max_backlogs, required_skills, location, is_remote, created_at)
VALUES
    (20, 1, 3, 'TCS Digital 2024', 'TCS Digital hiring.', 'Digital Specialist', 9.0, '2024-08-20', '2024-08-05', 'COMPLETED', 7.0, 0, 'Programming,DSA', 'Pan India', FALSE, '2024-07-01'),
    (21, 1, 2, 'Microsoft Engage 2024', 'Microsoft hiring.', 'SDE Intern', 20.0, '2024-09-15', '2024-08-30', 'COMPLETED', 8.0, 0, 'DSA,C++', 'Hyderabad', FALSE, '2024-07-15'),
    (22, 1, 5, 'Amazon WOW 2024', 'Amazon hiring.', 'SDE-1', 28.0, '2024-10-10', '2024-09-25', 'COMPLETED', 7.5, 0, 'DSA,Java', 'Bangalore', FALSE, '2024-08-01'),
    (23, 1, 7, 'Flipkart GRiD 5.0', 'Flipkart hiring.', 'SDE-1', 25.0, '2024-11-15', '2024-10-30', 'COMPLETED', 7.5, 0, 'Problem Solving', 'Bangalore', FALSE, '2024-09-01'),
    (24, 1, 8, 'Goldman Sachs 2024', 'GS tech hiring.', 'Analyst', 30.0, '2024-12-01', '2024-11-15', 'COMPLETED', 8.0, 0, 'DSA,Finance', 'Bangalore', FALSE, '2024-10-01'),
    -- Law Drives 2024-25
    (25, 1, 15, 'Khaitan Legal Associate 2024', 'Khaitan associate program.', 'Legal Associate', 16.0, '2024-10-20', '2024-10-05', 'COMPLETED', 7.5, 0, 'Corporate Law', 'Mumbai', FALSE, '2024-09-01'),
    (26, 1, 16, 'Trilegal Trainee 2024', 'Trilegal trainee hiring.', 'Legal Trainee', 14.0, '2024-12-10', '2024-11-25', 'COMPLETED', 7.0, 0, 'Legal Research', 'Mumbai', FALSE, '2024-11-01'),
    -- Science Drives 2024-25
    (27, 1, 18, 'Sun Pharma R&D 2024', 'Research and development roles.', 'Research Associate', 11.0, '2024-11-20', '2024-11-05', 'COMPLETED', 7.5, 0, 'Chemistry,Research', 'Mumbai', FALSE, '2024-10-01'),
    (28, 1, 20, 'DRDO Scientist 2024', 'Defence research scientist.', 'Scientist B', 13.0, '2025-01-15', '2024-12-30', 'COMPLETED', 8.0, 0, 'Physics,Engineering', 'Delhi', FALSE, '2024-12-01')
ON CONFLICT (id) DO NOTHING;

-- AY 2025-26 (July 2025 - June 2026) - CURRENT YEAR (OPEN/UPCOMING)
INSERT INTO placement_drives (id, college_id, company_id, title, description, job_role, package_lpa, drive_date, registration_deadline, status, min_cgpa, max_backlogs, required_skills, location, is_remote, created_at)
VALUES
    (30, 1, 1, 'Google Summer 2026', 'Google SWE hiring.', 'Software Engineer', 35.0, '2026-03-15', '2026-02-28', 'UPCOMING', 8.0, 0, 'DSA,System Design,Java', 'Bangalore', FALSE, '2026-01-01'),
    (31, 1, 2, 'Microsoft FTE 2026', 'Microsoft full-time.', 'SDE-1', 30.0, '2026-03-01', '2026-02-15', 'OPEN', 7.5, 0, 'C++,System Programming', 'Hyderabad', FALSE, '2025-12-01'),
    (32, 1, 5, 'Amazon SDET 2026', 'Amazon SDET roles.', 'SDET', 28.0, '2026-03-20', '2026-03-05', 'OPEN', 7.0, 0, 'Testing,Java', 'Bangalore', FALSE, '2026-01-10'),
    (33, 1, 7, 'Flipkart GRiD 6.0', 'Flipkart competition.', 'SDE-1', 27.0, '2026-04-10', '2026-03-25', 'UPCOMING', 7.5, 0, 'DSA', 'Bangalore', FALSE, '2026-02-01'),
    (34, 1, 9, 'L&T Build India 2026', 'L&T engineering.', 'GET', 9.5, '2026-03-25', '2026-03-10', 'OPEN', 6.5, 1, 'Engineering', 'Pan India', FALSE, '2026-01-15'),
    -- Law Drives 2025-26
    (35, 1, 13, 'CAM Corporate Practice 2026', 'CAM associate program.', 'Associate', 20.0, '2026-02-20', '2026-02-05', 'OPEN', 7.5, 0, 'Contracts,M&A', 'Mumbai', FALSE, '2026-01-01'),
    (36, 1, 14, 'AZB Disputes 2026', 'Disputes practice hiring.', 'Litigation Associate', 17.0, '2026-03-15', '2026-03-01', 'UPCOMING', 7.0, 0, 'Litigation', 'Mumbai', FALSE, '2026-02-01'),
    -- Science Drives 2025-26
    (37, 1, 19, 'ISRO Space Program 2026', 'Scientist recruitment.', 'Scientist/Engineer', 14.0, '2026-04-01', '2026-03-15', 'UPCOMING', 8.0, 0, 'Physics,Aerospace', 'Bangalore', FALSE, '2026-02-01'),
    (38, 1, 17, 'Dr Reddys Pharma 2026', 'Pharma research roles.', 'Research Scientist', 12.0, '2026-03-10', '2026-02-25', 'OPEN', 7.5, 0, 'Chemistry,Pharma', 'Hyderabad', FALSE, '2026-01-15')
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 8. DRIVE ELIGIBLE DEPARTMENTS
-- ================================================================================
INSERT INTO drive_eligible_departments (drive_id, department_id)
VALUES
    -- AY 2023-24 Engineering
    (10, 10), (10, 11), (10, 12), (10, 13),  -- TCS: All engineering
    (11, 10), (11, 11),                       -- Infosys: CSE, CE
    (12, 10), (12, 11),                       -- Amazon: CSE, CE
    (13, 12), (13, 13),                       -- L&T: ME, EE
    (14, 12),                                  -- Maruti: ME
    -- AY 2023-24 Law
    (15, 20), (15, 21),                       -- CAM: Corporate Law, Criminal Law
    (16, 20), (16, 21),                       -- AZB: Corporate Law, Criminal Law
    -- AY 2023-24 Science
    (17, 31),                                  -- Dr Reddys: Chemistry
    (18, 30),                                  -- ISRO: Physics
    
    -- AY 2024-25 Engineering
    (20, 10), (20, 11),                       -- TCS Digital
    (21, 10), (21, 11),                       -- Microsoft
    (22, 10), (22, 11),                       -- Amazon
    (23, 10), (23, 11),                       -- Flipkart
    (24, 10), (24, 11),                       -- Goldman Sachs
    -- AY 2024-25 Law
    (25, 20), (25, 21),                       -- Khaitan: Both law depts
    (26, 20), (26, 21),                       -- Trilegal: Both law depts
    -- AY 2024-25 Science
    (27, 31),                                  -- Sun Pharma: Chemistry
    (28, 30),                                  -- DRDO: Physics
    
    -- AY 2025-26 Engineering
    (30, 10), (30, 11),                       -- Google
    (31, 10), (31, 11),                       -- Microsoft
    (32, 10), (32, 11),                       -- Amazon SDET
    (33, 10), (33, 11),                       -- Flipkart
    (34, 12), (34, 13),                       -- L&T: ME, EE
    -- AY 2025-26 Law
    (35, 20), (35, 21),                       -- CAM: Both law
    (36, 20), (36, 21),                       -- AZB: Both law
    -- AY 2025-26 Science
    (37, 30),                                  -- ISRO: Physics
    (38, 31)                                   -- Dr Reddys: Chemistry
ON CONFLICT DO NOTHING;


-- ================================================================================
-- 9. APPLICATIONS (Various statuses across years)
-- ================================================================================
INSERT INTO applications (id, student_id, drive_id, status, applied_at, cover_letter, created_at)
VALUES
    -- AY 2023-24 Engineering (SELECTED = Placed)
    (10, 100, 10, 'SELECTED', '2023-09-10', 'TCS application...', '2023-09-10'),     -- Aarav CSE - TCS (PLACED)
    (11, 101, 12, 'SELECTED', '2024-01-10', 'Amazon SDE...', '2024-01-10'),          -- Ananya CSE - Amazon (PLACED)
    (12, 102, 11, 'SELECTED', '2023-09-25', 'Infosys app...', '2023-09-25'),         -- Vihaan CSE - Infosys (PLACED)
    (13, 130, 12, 'SELECTED', '2024-01-08', 'Amazon application...', '2024-01-08'),  -- Rahul CE - Amazon (PLACED)
    (14, 140, 14, 'SELECTED', '2024-02-22', 'Maruti dream...', '2024-02-22'),        -- Kartik ME - Maruti (PLACED)
    (15, 150, 13, 'SELECTED', '2024-02-05', 'L&T role...', '2024-02-05'),            -- Harsh EE - L&T (PLACED)
    (16, 103, 10, 'REJECTED', '2023-09-11', 'TCS app...', '2023-09-11'),             -- Ishita - Rejected
    (17, 104, 11, 'WITHDRAWN', '2023-09-22', 'Infosys...', '2023-09-22'),            -- Arjun - Withdrew
    
    -- AY 2023-24 Law (PLACED)
    (40, 160, 15, 'SELECTED', '2023-11-10', 'CAM associate...', '2023-11-10'),       -- Aditi Law - CAM (PLACED)
    
    -- AY 2023-24 Science (PLACED)
    (41, 171, 17, 'SELECTED', '2024-01-20', 'Dr Reddys research...', '2024-01-20'),  -- Yash Chem - Dr Reddys (PLACED)
    (42, 170, 18, 'SELECTED', '2024-02-20', 'ISRO scientist...', '2024-02-20'),      -- Arnav Physics - ISRO (PLACED)
    
    -- AY 2024-25 Engineering
    (20, 100, 21, 'SELECTED', '2024-08-25', 'Microsoft...', '2024-08-25'),           -- Aarav - Microsoft (2nd offer)
    (21, 103, 20, 'SELECTED', '2024-08-12', 'TCS Digital...', '2024-08-12'),         -- Ishita - TCS Digital (PLACED)
    (22, 130, 23, 'SELECTED', '2024-10-25', 'Flipkart...', '2024-10-25'),            -- Rahul - Flipkart (2nd offer)
    (23, 101, 24, 'SHORTLISTED', '2024-11-20', 'GS app...', '2024-11-20'),           -- Ananya - GS interview stage
    
    -- AY 2024-25 Law (PLACED)
    (43, 161, 25, 'SELECTED', '2024-10-15', 'Khaitan legal...', '2024-10-15'),       -- Karan Law - Khaitan (PLACED)
    
    -- AY 2025-26 (Current - PENDING/SHORTLISTED)
    -- Engineering
    (30, 110, 30, 'PENDING', '2026-01-20', 'Google SWE...', '2026-01-20'),           -- Urva CSE - Google
    (31, 110, 31, 'SHORTLISTED', '2026-01-18', 'Microsoft FTE...', '2026-01-18'),    -- Urva CSE - Microsoft shortlisted
    (32, 111, 30, 'PENDING', '2026-01-21', 'Google app...', '2026-01-21'),           -- Raj CSE - Google
    (33, 112, 31, 'PENDING', '2026-01-19', 'Microsoft...', '2026-01-19'),            -- Diya CSE - Microsoft
    (34, 113, 32, 'PENDING', '2026-01-22', 'Amazon SDET...', '2026-01-22'),          -- Rohan CSE - Amazon
    (35, 131, 32, 'PENDING', '2026-01-23', 'Amazon testing...', '2026-01-23'),       -- Priya CE - Amazon
    (36, 141, 34, 'PENDING', '2026-01-24', 'L&T engineering...', '2026-01-24'),      -- Amit ME - L&T
    (37, 151, 34, 'PENDING', '2026-01-25', 'L&T role...', '2026-01-25'),             -- Prachi EE - L&T
    -- Law
    (44, 160, 35, 'SHORTLISTED', '2026-01-22', 'CAM practice...', '2026-01-22'),     -- Aditi Law - CAM (2nd offer attempt)
    (45, 161, 36, 'PENDING', '2026-02-10', 'AZB disputes...', '2026-02-10'),         -- Karan Law - AZB
    -- Science
    (46, 170, 37, 'PENDING', '2026-02-05', 'ISRO space...', '2026-02-05'),           -- Arnav Physics - ISRO
    (47, 171, 38, 'PENDING', '2026-02-01', 'Dr Reddys pharma...', '2026-02-01')      -- Yash Chem - Dr Reddys
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 10. SEQUENCE RESETS
-- ================================================================================
SELECT setval('colleges_id_seq', COALESCE((SELECT MAX(id) FROM colleges), 1));
SELECT setval('organization_units_id_seq', COALESCE((SELECT MAX(id) FROM organization_units), 1));
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval('user_assignments_id_seq', COALESCE((SELECT MAX(id) FROM user_assignments), 1));
SELECT setval('student_profiles_id_seq', COALESCE((SELECT MAX(id) FROM student_profiles), 1));
SELECT setval('companies_id_seq', COALESCE((SELECT MAX(id) FROM companies), 1));
SELECT setval('placement_drives_id_seq', COALESCE((SELECT MAX(id) FROM placement_drives), 1));
SELECT setval('applications_id_seq', COALESCE((SELECT MAX(id) FROM applications), 1));


-- ================================================================================
-- END - SUMMARY AND TEST LOGINS
-- ================================================================================
-- 
-- DATA SUMMARY:
-- ================================================================================
-- 1 University Admin (ROOT - sees all)
-- 3 Institute Admins (sees their institute + departments)
-- 3 T&P Coordinators (institute-level)
-- 6 Department Admins (department-level)  
-- 9 Department Coordinators
-- 24 Students across CSE, CE, ME, EE, Law, Science (batches 2022, 2023, 2024)
-- 12 Companies (10 active, 2 inactive)
-- 14 Placement Drives (AY 2023-24: 5, AY 2024-25: 5, AY 2025-26: 4)
-- ~37 Applications (SELECTED, SHORTLISTED, PENDING, REJECTED, WITHDRAWN)
-- 
-- PLACED STUDENTS: 6 (in AY 2023-24 and 2024-25)
-- PENDING PLACEMENTS: ~8 (in AY 2025-26)
-- 
-- TEST LOGINS (Password: Password@123):
-- ================================================================================
-- | Email                           | Role        | Scope                        |
-- |---------------------------------|-------------|------------------------------|
-- | admin@nirmauni.ac.in            | ADMIN       | University (sees all)        |
-- | rajesh.patel@nirmauni.ac.in     | ADMIN       | Institute of Technology      |
-- | priya.mehta@nirmauni.ac.in      | ADMIN       | Institute of Law             |
-- | ramesh.kumar@nirmauni.ac.in     | ADMIN       | Institute of Science         |
-- | admin.cse@nirmauni.ac.in        | ADMIN       | CSE Department Only          |
-- | tpo.it@nirmauni.ac.in           | COORDINATOR | Institute of Technology      |
-- | coord.cse@nirmauni.ac.in        | COORDINATOR | CSE Department Only          |
-- | 23BCE001@nirmauni.ac.in         | STUDENT     | Own profile                  |
-- | 22BCE001@nirmauni.ac.in         | STUDENT     | Own profile (Placed)         |
-- ================================================================================
