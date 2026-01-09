package com.campusplacement.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.campusplacement.common.UserRole;
import com.campusplacement.security.JwtTokenProvider;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for AuthController using MockMvc.
 *
 * <p>
 * Tests the full authentication flow including:
 * <ul>
 * <li>Login with valid/invalid credentials</li>
 * <li>Response format validation</li>
 * <li>HTTP status code correctness</li>
 * <li>Request validation</li>
 * </ul>
 * </p>
 */
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CollegeRepository collegeRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        private College testCollege;
        private User testStudent;
        private User testSuperAdmin;

        private static final String LOGIN_URL = "/api/v1/auth/login";
        private static final String ME_URL = "/api/v1/auth/me";
        private static final String TEST_PASSWORD = "password123";

        @SuppressWarnings("null")
        @BeforeEach
        void setUp() {
                // Clean up existing test data
                userRepository.deleteAll();
                collegeRepository.deleteAll();

                // Create test college
                testCollege = College.builder()
                                .name("Test College")
                                .code("TC001")
                                .isActive(true)
                                .build();
                testCollege = collegeRepository.save(testCollege);

                // Create test student
                testStudent = User.builder()
                                .name("Test Student")
                                .email("student@test.edu")
                                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                                .role(UserRole.STUDENT)
                                .college(testCollege)
                                .isActive(true)
                                .build();
                testStudent = userRepository.save(testStudent);

                // Create super admin (no college)
                testSuperAdmin = User.builder()
                                .name("Super Admin")
                                .email("superadmin@platform.com")
                                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                                .role(UserRole.SUPER_ADMIN)
                                .college(null)
                                .isActive(true)
                                .build();
                testSuperAdmin = userRepository.save(testSuperAdmin);
        }

        // ========== Login Success Tests ==========

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Valid credentials returns 200 and token")
        void login_ValidCredentials_Returns200AndToken() throws Exception {
                String requestBody = """
                                {
                                    "email": "student@test.edu",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.token", notNullValue()))
                                .andExpect(jsonPath("$.data.userId", is(testStudent.getId().intValue())))
                                .andExpect(jsonPath("$.data.role", is("STUDENT")))
                                .andExpect(jsonPath("$.data.collegeId", is(testCollege.getId().intValue())))
                                .andExpect(jsonPath("$.data.redirectUrl", is("/dashboard/student")));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Super admin returns null collegeId")
        void login_SuperAdmin_ReturnsNullCollegeId() throws Exception {
                String requestBody = """
                                {
                                    "email": "superadmin@platform.com",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.role", is("SUPER_ADMIN")))
                                .andExpect(jsonPath("$.data.collegeId", nullValue()))
                                .andExpect(jsonPath("$.data.redirectUrl", is("/dashboard/superadmin")));
        }

        // ========== Login Failure Tests ==========

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Wrong password returns 401")
        void login_WrongPassword_Returns401() throws Exception {
                String requestBody = """
                                {
                                    "email": "student@test.edu",
                                    "password": "wrongpassword"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", containsString("Invalid credentials")));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Unknown email returns 401")
        void login_UnknownEmail_Returns401() throws Exception {
                String requestBody = """
                                {
                                    "email": "unknown@test.edu",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success", is(false)));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Inactive user returns 403")
        void login_InactiveUser_Returns403() throws Exception {
                // Deactivate user
                testStudent.setIsActive(false);
                userRepository.save(testStudent);

                String requestBody = """
                                {
                                    "email": "student@test.edu",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success", is(false)))
                                .andExpect(jsonPath("$.message", containsString("deactivated")));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Inactive college returns appropriate error")
        void login_InactiveCollege_ReturnsError() throws Exception {
                // Deactivate college
                testCollege.setIsActive(false);
                collegeRepository.save(testCollege);

                String requestBody = """
                                {
                                    "email": "student@test.edu",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().is4xxClientError())
                                .andExpect(jsonPath("$.success", is(false)));
        }

        // ========== Validation Tests ==========

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Missing email returns 400")
        void login_MissingEmail_Returns400() throws Exception {
                String requestBody = """
                                {
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Missing password returns 400")
        void login_MissingPassword_Returns400() throws Exception {
                String requestBody = """
                                {
                                    "email": "student@test.edu"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Invalid email format returns 400")
        void login_InvalidEmailFormat_Returns400() throws Exception {
                String requestBody = """
                                {
                                    "email": "not-an-email",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Empty email returns 400")
        void login_EmptyEmail_Returns400() throws Exception {
                String requestBody = """
                                {
                                    "email": "",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        // ========== /me Endpoint Tests ==========

        @SuppressWarnings("null")
        @Test
        @DisplayName("GET /me - Unauthenticated returns 401")
        void me_Unauthenticated_Returns401() throws Exception {
                mockMvc.perform(get(ME_URL)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("GET /me - Authenticated returns user details")
        void me_Authenticated_ReturnsUserDetails() throws Exception {
                // Generate valid token
                String token = jwtTokenProvider.generateToken(testStudent);

                mockMvc.perform(get(ME_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)))
                                .andExpect(jsonPath("$.data.userId", is(testStudent.getId().intValue())))
                                .andExpect(jsonPath("$.data.role", is("STUDENT")));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("GET /me - Invalid token returns 401")
        void me_InvalidToken_Returns401() throws Exception {
                mockMvc.perform(get(ME_URL)
                                .header("Authorization", "Bearer invalid.token.here")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        // ========== Email Normalization Tests ==========

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Email with uppercase is normalized")
        void login_EmailWithUppercase_IsNormalized() throws Exception {
                String requestBody = """
                                {
                                    "email": "STUDENT@TEST.EDU",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("POST /login - Email with whitespace is trimmed")
        void login_EmailWithWhitespace_IsTrimmed() throws Exception {
                String requestBody = """
                                {
                                    "email": "  student@test.edu  ",
                                    "password": "password123"
                                }
                                """;

                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success", is(true)));
        }
}
