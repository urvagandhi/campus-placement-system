package com.campusplacement.students;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.AbstractIntegrationTest;
import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.UserRole;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.organizations.OrganizationUnitRepository;
import com.campusplacement.security.JwtTokenProvider;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for Student role security enforcement.
 *
 * <p>
 * Tests verify:
 * <ul>
 * <li>Students cannot modify academic fields</li>
 * <li>Students cannot access other profiles</li>
 * <li>Students cannot create drives</li>
 * <li>Career profile updates work correctly</li>
 * </ul>
 * </p>
 */
@AutoConfigureMockMvc
@Transactional
class StudentSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private OrganizationUnitRepository orgUnitRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // @Autowired
    // private ObjectMapper objectMapper;

    private College testCollege;
    private OrganizationUnit department;
    private User studentUser1;
    private User studentUser2;
    private User coordinatorUser;
    private StudentProfile studentProfile1;
    private String student1Token;
    private String student2Token;
    private String coordinatorToken;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        // Clean up
        studentRepository.deleteAll();
        userRepository.deleteAll();
        orgUnitRepository.deleteAll();
        collegeRepository.deleteAll();

        // Create test college
        testCollege = College.builder()
                .name("Student Security Test College")
                .code("SST001")
                .isActive(true)
                .build();
        testCollege = collegeRepository.save(testCollege);

        // Create organization hierarchy
        OrganizationUnit university = OrganizationUnit.builder()
                .college(testCollege)
                .name("Test University")
                .type(OrganizationUnitType.UNIVERSITY)
                .isRoot(true)
                .isActive(true)
                .build();
        university = orgUnitRepository.save(university);

        OrganizationUnit institute = OrganizationUnit.builder()
                .college(testCollege)
                .name("Test Institute")
                .type(OrganizationUnitType.INSTITUTE)
                .parent(university)
                .isActive(true)
                .build();
        institute = orgUnitRepository.save(institute);

        department = OrganizationUnit.builder()
                .college(testCollege)
                .name("Computer Science")
                .type(OrganizationUnitType.DEPARTMENT)
                .parent(institute)
                .isActive(true)
                .build();
        department = orgUnitRepository.save(department);

        // Create users
        studentUser1 = createUser("student1@test.edu", UserRole.STUDENT);
        studentUser2 = createUser("student2@test.edu", UserRole.STUDENT);
        coordinatorUser = createUser("coordinator@test.edu", UserRole.COORDINATOR);

        // Create student profile
        studentProfile1 = StudentProfile.builder()
                .user(studentUser1)
                .enrollmentNo("STU001")
                .department(department)
                .cgpa(8.5)
                .backlogs(0)
                .batchYear(2024)
                .semester("7")
                .skills("Java,Python")
                .build();
        studentProfile1 = studentRepository.save(studentProfile1);

        // Generate tokens
        student1Token = jwtTokenProvider.generateToken(studentUser1);
        student2Token = jwtTokenProvider.generateToken(studentUser2);
        coordinatorToken = jwtTokenProvider.generateToken(coordinatorUser);
    }

    @SuppressWarnings("null")
    private User createUser(String email, UserRole role) {
        User user = User.builder()
                .name(role.name() + " User")
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .role(role)
                .college(testCollege)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    // ========== Student Cannot Create Drives ==========

    @Nested
    @DisplayName("Drive Creation Restrictions")
    class DriveCreationTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot create drives - returns 403")
        void student_cannotCreateDrives() throws Exception {
            String driveJson = """
                    {
                        "title": "Test Drive",
                        "companyId": 1,
                        "driveDate": "2024-12-01"
                    }
                    """;

            mockMvc.perform(post("/api/v1/drives")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(driveJson))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== Profile Access Controls ==========

    @Nested
    @DisplayName("Profile Access Controls")
    class ProfileAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student can access own profile")
        void student_canAccessOwnProfile() throws Exception {
            mockMvc.perform(get("/api/v1/students/me")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.enrollmentNo").value("STU001"));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot access other student's profile by ID")
        void student_cannotAccessOtherProfiles() throws Exception {
            mockMvc.perform(get("/api/v1/students/" + studentProfile1.getId())
                    .header("Authorization", "Bearer " + student2Token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot list all students")
        void student_cannotListAllStudents() throws Exception {
            mockMvc.perform(get("/api/v1/students")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Coordinator can list all students")
        void coordinator_canListAllStudents() throws Exception {
            mockMvc.perform(get("/api/v1/students")
                    .header("Authorization", "Bearer " + coordinatorToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ========== Career Profile Updates ==========

    @Nested
    @DisplayName("Career Profile Updates")
    class CareerProfileUpdateTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student can update career fields")
        void student_canUpdateCareerFields() throws Exception {
            String updateJson = """
                    {
                        "skills": ["Java", "Spring Boot", "React"],
                        "resumeUrl": "https://example.com/resume.pdf",
                        "projectsCount": 5,
                        "linkedinUrl": "https://linkedin.com/in/testuser"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/students/me/profile")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.projectsCount").value(5));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Academic fields are ignored in student updates")
        void student_academicFieldsIgnored() throws Exception {
            // Try to update CGPA (should be ignored)
            String updateJson = """
                    {
                        "skills": ["Updated Skills"],
                        "cgpa": 10.0,
                        "enrollmentNo": "HACKED001",
                        "backlogs": 99
                    }
                    """;

            mockMvc.perform(patch("/api/v1/students/me/profile")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andExpect(status().isOk());

            // Verify CGPA unchanged
            mockMvc.perform(get("/api/v1/students/me")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cgpa").value(8.5)) // Original value
                    .andExpect(jsonPath("$.data.enrollmentNo").value("STU001")) // Original value
                    .andExpect(jsonPath("$.data.backlogs").value(0)); // Original value
        }
    }

    // ========== Admin Access Controls ==========

    @Nested
    @DisplayName("Admin Access Controls")
    class AdminAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot access admin endpoints")
        void student_cannotAccessAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + student1Token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
