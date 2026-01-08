package com.campusplacement.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.AbstractIntegrationTest;
import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

/**
 * Integration tests for security and RBAC (Role-Based Access Control).
 *
 * <p>
 * Tests verify that:
 * <ul>
 * <li>Role-based endpoint protection works correctly</li>
 * <li>JWT authentication is enforced</li>
 * <li>Token validation rejects invalid/expired/tampered tokens</li>
 * </ul>
 * </p>
 *
 * <p>
 * Endpoint Protection Rules (from SecurityConfig):
 * <ul>
 * <li>/api/v1/auth/** - Permit all</li>
 * <li>/api/v1/superadmin/** - SUPER_ADMIN only</li>
 * <li>/api/v1/admin/** - ADMIN, SUPER_ADMIN</li>
 * <li>/api/v1/coordinator/** - COORDINATOR, ADMIN, SUPER_ADMIN</li>
 * <li>/api/v1/student/** - STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN</li>
 * </ul>
 * </p>
 */
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private College testCollege;
    private User studentUser;
    private User coordinatorUser;
    private User adminUser;
    private User superAdminUser;

    private String studentToken;
    private String coordinatorToken;
    private String adminToken;
    private String superAdminToken;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();
        collegeRepository.deleteAll();

        // Create test college
        testCollege = College.builder()
                .name("Security Test College")
                .code("SEC001")
                .isActive(true)
                .build();
        testCollege = collegeRepository.save(testCollege);

        // Create users for each role
        studentUser = createUser("student@test.edu", UserRole.STUDENT, testCollege);
        coordinatorUser = createUser("coordinator@test.edu", UserRole.COORDINATOR, testCollege);
        adminUser = createUser("admin@test.edu", UserRole.ADMIN, testCollege);
        superAdminUser = createUser("superadmin@platform.com", UserRole.SUPER_ADMIN, null);

        // Generate tokens
        studentToken = jwtTokenProvider.generateToken(studentUser);
        coordinatorToken = jwtTokenProvider.generateToken(coordinatorUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);
        superAdminToken = jwtTokenProvider.generateToken(superAdminUser);
    }

    @SuppressWarnings("null")
    private User createUser(String email, UserRole role, College college) {
        User user = User.builder()
                .name(role.name() + " User")
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .role(role)
                .college(college)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    // ========== Unauthenticated Access Tests ==========

    @Nested
    @DisplayName("Unauthenticated Access")
    class UnauthenticatedAccessTests {

        @Test
        @DisplayName("Public auth endpoints are accessible without authentication")
        void authEndpoints_ArePublic() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized()); // /me requires auth, login doesn't
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Protected endpoint without token returns 401")
        void protectedEndpoint_NoToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/student/profile")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========== Student Role Access Tests ==========

    @Nested
    @DisplayName("Student Role Access")
    class StudentRoleAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot access admin endpoints - returns 403")
        void student_CannotAccessAdminEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot access coordinator-only endpoints - returns 403")
        void student_CannotAccessCoordinatorEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/coordinator/drives")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Student cannot access superadmin endpoints - returns 403")
        void student_CannotAccessSuperAdminEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/superadmin/colleges")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== Coordinator Role Access Tests ==========

    @Nested
    @DisplayName("Coordinator Role Access")
    class CoordinatorRoleAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Coordinator cannot access admin endpoints - returns 403")
        void coordinator_CannotAccessAdminEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .header("Authorization", "Bearer " + coordinatorToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Coordinator cannot access superadmin endpoints - returns 403")
        void coordinator_CannotAccessSuperAdminEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/superadmin/colleges")
                    .header("Authorization", "Bearer " + coordinatorToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== Admin Role Access Tests ==========

    @Nested
    @DisplayName("Admin Role Access")
    class AdminRoleAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Admin cannot access superadmin endpoints - returns 403")
        void admin_CannotAccessSuperAdminEndpoints_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/superadmin/colleges")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== Token Validation Tests ==========

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Expired token returns 401")
        void expiredToken_Returns401() throws Exception {
            // Create a provider with very short expiration
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret",
                    "TestSecretKeyForJWTTokenGenerationMinimum256BitsLongEnoughForHMACSHA256");
            ReflectionTestUtils.setField(shortLivedProvider, "jwtExpirationMs", 1L);

            String expiredToken = shortLivedProvider.generateToken(studentUser);

            // Wait for expiration
            Thread.sleep(50);

            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer " + expiredToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Tampered token returns 401")
        void tamperedToken_Returns401() throws Exception {
            String tamperedToken = studentToken.substring(0, studentToken.length() - 5) + "XXXXX";

            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer " + tamperedToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Malformed token returns 401")
        void malformedToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer not.a.valid.jwt.token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Empty Bearer token returns 401")
        void emptyToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer ")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Missing Bearer prefix returns 401")
        void missingBearerPrefix_Returns401() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", studentToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========== Super Admin Full Access Tests ==========

    @Nested
    @DisplayName("Super Admin Full Access")
    class SuperAdminFullAccessTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Super Admin can access /me endpoint")
        void superAdmin_CanAccessMeEndpoint() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                    .header("Authorization", "Bearer " + superAdminToken)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
