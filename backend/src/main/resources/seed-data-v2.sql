-- ================================================================================
-- PlacementPro Seed Data
-- ================================================================================
-- Generated from Java Entity classes on 2026-01-21
-- Matches schema.sql exactly
-- ================================================================================
-- PASSWORD FOR ALL USERS: password123
-- BCrypt Hash (cost 12): $2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2
-- ================================================================================


-- ================================================================================
-- 1. COLLEGES (Tenant Boundaries)
-- ================================================================================
INSERT INTO colleges (id, name, code, address, website, contact_email, is_active, created_at)
VALUES
    (1, 'Nirma University', 'NIRMA001', 'Sarkhej-Gandhinagar Highway, Ahmedabad, Gujarat 382481', 'https://www.nirmauni.ac.in', 'admissions@nirmauni.ac.in', TRUE, NOW()),
    (2, 'PP Savani University', 'PPSU001', 'NH-8, Kosamba, Surat, Gujarat 394125', 'https://www.ppsu.ac.in', 'admissions@ppsu.ac.in', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 2. ORGANIZATION UNITS (Hierarchical Structure)
-- ================================================================================
-- NIRMA UNIVERSITY HIERARCHY
INSERT INTO organization_units (id, college_id, name, code, type, parent_unit_id, email_domain, is_root, is_active, created_at)
VALUES
    -- University Root
    (1, 1, 'Nirma University', 'NU', 'UNIVERSITY', NULL, 'nirmauni.ac.in', TRUE, TRUE, NOW()),
    -- Institutes
    (2, 1, 'Institute of Technology', 'IT', 'INSTITUTE', 1, NULL, FALSE, TRUE, NOW()),
    (3, 1, 'Institute of Law', 'IL', 'INSTITUTE', 1, NULL, FALSE, TRUE, NOW()),
    -- Departments - IT
    (4, 1, 'Computer Science & Engineering', 'CSE', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    (5, 1, 'Computer Engineering', 'CE', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    (6, 1, 'Mechanical Engineering', 'ME', 'DEPARTMENT', 2, NULL, FALSE, TRUE, NOW()),
    -- Departments - Law
    (7, 1, 'Corporate Law', 'CL', 'DEPARTMENT', 3, NULL, FALSE, TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- PP SAVANI UNIVERSITY HIERARCHY
INSERT INTO organization_units (id, college_id, name, code, type, parent_unit_id, email_domain, is_root, is_active, created_at)
VALUES
    -- University Root
    (8, 2, 'PP Savani University', 'PPSU', 'UNIVERSITY', NULL, 'ppsu.ac.in', TRUE, TRUE, NOW()),
    -- Schools/Institutes
    (9, 2, 'School of Engineering & Technology', 'SET', 'INSTITUTE', 8, NULL, FALSE, TRUE, NOW()),
    (10, 2, 'School of Management', 'SOM', 'INSTITUTE', 8, NULL, FALSE, TRUE, NOW()),
    -- Departments - Engineering
    (11, 2, 'Computer Science', 'CS', 'DEPARTMENT', 9, NULL, FALSE, TRUE, NOW()),
    (12, 2, 'Information Technology', 'IT', 'DEPARTMENT', 9, NULL, FALSE, TRUE, NOW()),
    -- Departments - Management
    (13, 2, 'MBA', 'MBA', 'DEPARTMENT', 10, NULL, FALSE, TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 3. USERS
-- ================================================================================
-- SUPER ADMIN (Platform Owner)
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    (1, 'superadmin', 'Platform Administrator', 'superadmin@placementpro.com', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'SUPER_ADMIN', NULL, '+91-9000000001', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- NIRMA UNIVERSITY USERS
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    -- University Admin
    (2, 'admin_nirma', 'Dr. Karsanbhai Patel', 'admin@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 1, '+91-9000000002', TRUE, NOW()),
    -- Institute Admins
    (3, 'admin_nirma_it', 'Dr. Amit Shah', 'admin.it@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 1, '+91-9000000003', TRUE, NOW()),
    (4, 'admin_nirma_law', 'Dr. Priya Mehta', 'admin.law@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 1, '+91-9000000004', TRUE, NOW()),
    -- T&P Coordinators
    (5, 'tpo_nirma_it', 'Prof. Sunil Pandey', 'tpo.it@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 1, '+91-9000000005', TRUE, NOW()),
    (6, 'tpo_nirma_law', 'Prof. Meera Joshi', 'tpo.law@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 1, '+91-9000000006', TRUE, NOW()),
    -- Department Coordinators
    (7, 'coord_cse_nirma', 'Prof. Rajesh Verma', 'coord.cse@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 1, '+91-9000000007', TRUE, NOW()),
    (8, 'coord_ce_nirma', 'Prof. Neha Sharma', 'coord.ce@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 1, '+91-9000000008', TRUE, NOW()),
    (9, 'coord_me_nirma', 'Prof. Vikram Singh', 'coord.me@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 1, '+91-9000000009', TRUE, NOW()),
    -- Students
    (10, '23BCE001', 'Urva Gandhi', '23BCE001@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 1, '+91-9000000010', TRUE, NOW()),
    (11, '23BCE002', 'Raj Mehta', '23BCE002@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 1, '+91-9000000011', TRUE, NOW()),
    (12, '23CE001', 'Priya Patel', '23CE001@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 1, '+91-9000000012', TRUE, NOW()),
    (13, '23ME001', 'Amit Shah', '23ME001@nirmauni.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 1, '+91-9000000013', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- PP SAVANI UNIVERSITY USERS
INSERT INTO users (id, username, full_name, email, password_hash, role, college_id, phone_number, is_active, created_at)
VALUES
    -- University Admin
    (14, 'admin_ppsu', 'Dr. PP Savani', 'admin@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 2, '+91-9000000014', TRUE, NOW()),
    -- School Admins
    (15, 'admin_ppsu_it', 'Dr. Ketan Patel', 'admin.it@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 2, '+91-9000000015', TRUE, NOW()),
    (16, 'admin_ppsu_mgt', 'Dr. Sneha Desai', 'admin.mgt@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'ADMIN', 2, '+91-9000000016', TRUE, NOW()),
    -- T&P Coordinators
    (17, 'tpo_ppsu_it', 'Prof. Hardik Shah', 'tpo.it@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 2, '+91-9000000017', TRUE, NOW()),
    (18, 'tpo_ppsu_mgt', 'Prof. Nisha Jain', 'tpo.mgt@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 2, '+91-9000000018', TRUE, NOW()),
    -- Department Coordinators
    (19, 'coord_cs_ppsu', 'Prof. Yash Modi', 'coord.cs@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 2, '+91-9000000019', TRUE, NOW()),
    (20, 'coord_it_ppsu', 'Prof. Ravi Kumar', 'coord.it@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'COORDINATOR', 2, '+91-9000000020', TRUE, NOW()),
    -- Students
    (21, '24CS001', 'Harsh Trivedi', '24CS001@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 2, '+91-9000000021', TRUE, NOW()),
    (22, '24CS002', 'Pooja Sharma', '24CS002@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 2, '+91-9000000022', TRUE, NOW()),
    (23, '24IT001', 'Nikhil Patel', '24IT001@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 2, '+91-9000000023', TRUE, NOW()),
    (24, '24MBA001', 'Ankita Joshi', '24MBA001@ppsu.ac.in', '$2b$12$N9ciIVb1Cyxp.I6HQr3vuefOjhL66Y.7MrMv65zCzvSiuUydw/y.2', 'STUDENT', 2, '+91-9000000024', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 4. USER ASSIGNMENTS (Role-Based Scope)
-- ================================================================================
INSERT INTO user_assignments (id, user_id, organization_unit_id, designation, scope, is_primary, created_at)
VALUES
    -- NIRMA UNIVERSITY
    (1, 2, 1, 'University Administrator', 'SUBTREE', TRUE, NOW()),
    (2, 3, 2, 'Director - Institute of Technology', 'SUBTREE', TRUE, NOW()),
    (3, 4, 3, 'Director - Institute of Law', 'SUBTREE', TRUE, NOW()),
    (4, 5, 2, 'Head - Training & Placement (IT)', 'SUBTREE', TRUE, NOW()),
    (5, 6, 3, 'Head - Training & Placement (Law)', 'SUBTREE', TRUE, NOW()),
    (6, 7, 4, 'Placement Coordinator - CSE', 'SELF', TRUE, NOW()),
    (7, 8, 5, 'Placement Coordinator - CE', 'SELF', TRUE, NOW()),
    (8, 9, 6, 'Placement Coordinator - ME', 'SELF', TRUE, NOW()),
    (9, 10, 4, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (10, 11, 4, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (11, 12, 5, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (12, 13, 6, 'B.Tech Student', 'SELF', TRUE, NOW()),
    -- PP SAVANI UNIVERSITY
    (13, 14, 8, 'University Administrator', 'SUBTREE', TRUE, NOW()),
    (14, 15, 9, 'Dean - School of Engineering', 'SUBTREE', TRUE, NOW()),
    (15, 16, 10, 'Dean - School of Management', 'SUBTREE', TRUE, NOW()),
    (16, 17, 9, 'Head - Training & Placement (Engg)', 'SUBTREE', TRUE, NOW()),
    (17, 18, 10, 'Head - Training & Placement (Mgmt)', 'SUBTREE', TRUE, NOW()),
    (18, 19, 11, 'Placement Coordinator - CS', 'SELF', TRUE, NOW()),
    (19, 20, 12, 'Placement Coordinator - IT', 'SELF', TRUE, NOW()),
    (20, 21, 11, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (21, 22, 11, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (22, 23, 12, 'B.Tech Student', 'SELF', TRUE, NOW()),
    (23, 24, 13, 'MBA Student', 'SELF', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 5. STUDENT PROFILES
-- ================================================================================
INSERT INTO student_profiles (id, user_id, enrollment_number, department_id, cgpa, active_backlogs, batch_year, current_semester, skills, linkedin_url, github_url, projects_count, internship_months, career_interests, created_at)
VALUES
    -- NIRMA Students
    (1, 10, '23BCE001', 4, 8.9, 0, 2023, 4, 'Java,Spring Boot,React,PostgreSQL,Docker', 'https://linkedin.com/in/urvagandhi', 'https://github.com/urvagandhi', 5, 6, '["Backend","Cloud"]', NOW()),
    (2, 11, '23BCE002', 4, 8.5, 0, 2023, 4, 'Python,Django,Machine Learning,TensorFlow', 'https://linkedin.com/in/rajmehta', 'https://github.com/rajmehta', 4, 3, '["ML","Data Science"]', NOW()),
    (3, 12, '23CE001', 5, 7.8, 1, 2023, 4, 'C++,Data Structures,Algorithms,System Design', 'https://linkedin.com/in/priyapatel', 'https://github.com/priyapatel', 3, 0, '["Backend","System Design"]', NOW()),
    (4, 13, '23ME001', 6, 7.5, 0, 2023, 4, 'AutoCAD,SolidWorks,Simulation', NULL, NULL, 2, 0, '["Automotive","Manufacturing"]', NOW()),
    -- PPSU Students
    (5, 21, '24CS001', 11, 8.2, 0, 2024, 2, 'Java,Python,React,Node.js', 'https://linkedin.com/in/harshtrivedi', 'https://github.com/harshtrivedi', 3, 0, '["Full Stack","Backend"]', NOW()),
    (6, 22, '24CS002', 11, 7.9, 0, 2024, 2, 'Python,Data Analysis,SQL', 'https://linkedin.com/in/poojasharma', NULL, 2, 0, '["Data Analytics"]', NOW()),
    (7, 23, '24IT001', 12, 8.0, 0, 2024, 2, 'JavaScript,React,MongoDB', NULL, 'https://github.com/nikhilpatel', 2, 0, '["Frontend","Full Stack"]', NOW()),
    (8, 24, '24MBA001', 13, 8.5, 0, 2024, 2, 'Marketing,Finance,Analytics', 'https://linkedin.com/in/ankitajoshi', NULL, 1, 6, '["Finance","Consulting"]', NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 6. COMPANIES
-- ================================================================================
INSERT INTO companies (id, name, industry, website, description, location, contact_email, contact_phone, is_active, created_at)
VALUES
    (1, 'Google', 'Technology', 'https://google.com', 'Global technology company specializing in search, cloud, and AI.', 'Bangalore, India', 'campus@google.com', '+91-80-12345678', TRUE, NOW()),
    (2, 'Microsoft', 'Technology', 'https://microsoft.com', 'Global leader in software, cloud computing, and personal computing.', 'Hyderabad, India', 'campus@microsoft.com', '+91-40-12345678', TRUE, NOW()),
    (3, 'Tata Consultancy Services', 'IT Services', 'https://tcs.com', 'Global leader in IT services, consulting, and business solutions.', 'Mumbai, India', 'campus.recruitment@tcs.com', '+91-22-12345678', TRUE, NOW()),
    (4, 'Infosys', 'IT Services', 'https://infosys.com', 'Global leader in next-generation digital services and consulting.', 'Bangalore, India', 'campus@infosys.com', '+91-80-11111111', TRUE, NOW()),
    (5, 'Reliance Industries', 'Conglomerate', 'https://ril.com', 'India largest private sector company with diverse business interests.', 'Mumbai, India', 'careers@ril.com', '+91-22-44447000', TRUE, NOW()),
    (6, 'Deloitte', 'Consulting', 'https://deloitte.com', 'Global professional services network providing audit, consulting, and tax services.', 'Mumbai, India', 'campus@deloitte.com', '+91-22-61854000', TRUE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 7. PLACEMENT DRIVES
-- ================================================================================
INSERT INTO placement_drives (id, college_id, company_id, title, description, job_role, package_lpa, drive_date, registration_deadline, status, min_cgpa, max_backlogs, required_skills, location, is_remote, created_at)
VALUES
    (1, 1, 1, 'Google Campus Drive 2026', 'Hiring for SWE roles. Work on products used by billions.', 'Software Engineer', 32.0, '2026-03-15', '2026-02-28', 'UPCOMING', 8.0, 0, 'DSA,System Design,Java,Python', 'Bangalore', FALSE, NOW()),
    (2, 1, 2, 'Microsoft IDC Hiring', 'Microsoft India Development Center hiring for Azure and Office teams.', 'SDE-1', 28.0, '2026-03-01', '2026-02-15', 'UPCOMING', 7.5, 0, 'C++,System Programming,Distributed Systems', 'Hyderabad', FALSE, NOW()),
    (3, 2, 3, 'TCS Digital Hiring', 'TCS Digital hiring for digital transformation projects.', 'System Engineer', 7.5, '2026-04-01', '2026-03-20', 'UPCOMING', 6.0, 2, 'Programming,SQL,Communication', 'Pan India', FALSE, NOW()),
    (4, 1, 4, 'Infosys Power Programmer', 'For students with exceptional programming skills.', 'Specialist Programmer', 9.5, '2026-03-10', '2026-02-25', 'UPCOMING', 7.0, 1, 'DSA,Problem Solving,Java', 'Bangalore,Pune', FALSE, NOW()),
    (5, 2, 5, 'Reliance JioGenNext', 'Fast-track leadership program at Reliance.', 'Graduate Engineer Trainee', 12.0, '2026-03-20', '2026-03-10', 'UPCOMING', 7.0, 0, 'Communication,Leadership,Technical Skills', 'Mumbai', FALSE, NOW()),
    (6, 2, 6, 'Deloitte Consulting', 'Consulting roles for MBA and engineering graduates.', 'Business Analyst', 15.0, '2026-03-25', '2026-03-15', 'UPCOMING', 7.5, 0, 'Analytics,Communication,Problem Solving', 'Mumbai,Delhi', FALSE, NOW())
ON CONFLICT (id) DO NOTHING;


-- ================================================================================
-- 8. DRIVE ELIGIBLE DEPARTMENTS
-- ================================================================================
INSERT INTO drive_eligible_departments (drive_id, department_id)
VALUES
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


-- ================================================================================
-- 9. SAMPLE APPLICATIONS
-- ================================================================================
INSERT INTO applications (id, student_id, drive_id, status, applied_at, cover_letter, created_at)
VALUES
    (1, 1, 1, 'PENDING', NOW(), 'Excited to join Google...', NOW()),
    (2, 1, 2, 'SHORTLISTED', NOW() - INTERVAL '5 days', 'Strong interest in Microsoft...', NOW() - INTERVAL '5 days'),
    (3, 2, 1, 'PENDING', NOW(), NULL, NOW()),
    (4, 2, 4, 'PENDING', NOW(), NULL, NOW()),
    (5, 5, 3, 'PENDING', NOW(), 'Interested in TCS Digital...', NOW()),
    (6, 5, 5, 'PENDING', NOW(), NULL, NOW()),
    (7, 8, 6, 'PENDING', NOW(), 'Eager to join Deloitte Consulting...', NOW())
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
-- END OF SEED DATA
-- ================================================================================
-- 
-- QUICK TEST LOGINS (Password: password123):
-- 
-- | Email                           | Role        | College              |
-- |---------------------------------|-------------|----------------------|
-- | superadmin@placementpro.com     | SUPER_ADMIN | Platform             |
-- | admin@nirmauni.ac.in            | ADMIN       | Nirma University     |
-- | admin@ppsu.ac.in                | ADMIN       | PP Savani University |
-- | tpo.it@nirmauni.ac.in           | COORDINATOR | Nirma University     |
-- | coord.cse@nirmauni.ac.in        | COORDINATOR | Nirma University     |
-- | 23BCE001@nirmauni.ac.in         | STUDENT     | Nirma University     |
-- | 24CS001@ppsu.ac.in              | STUDENT     | PP Savani University |
-- 
-- ================================================================================
