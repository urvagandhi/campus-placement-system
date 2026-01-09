package com.campusplacement.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campusplacement.auth.dto.LoginRequestDTO;
import com.campusplacement.auth.dto.LoginResponseDTO;
import com.campusplacement.auth.dto.RegisterRequestDTO;
import com.campusplacement.auth.exception.AccountDeactivatedException;
import com.campusplacement.auth.exception.AuthenticationException;
import com.campusplacement.auth.exception.CollegeInactiveException;
import com.campusplacement.colleges.College;
import com.campusplacement.common.UserRole;
import com.campusplacement.security.JwtTokenProvider;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private LoginAuditRepository loginAuditRepository;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private HttpServletRequest httpServletRequest;

        @InjectMocks
        private AuthService authService;

        private User testUser;
        private College testCollege;
        private LoginRequestDTO loginRequest;

        @BeforeEach
        void setUp() {
                // Setup test college
                testCollege = College.builder()
                                .name("Test College")
                                .code("TC001")
                                .isActive(true)
                                .build();
                testCollege.setId(1L);

                // Setup test user
                testUser = User.builder()
                                .name("Test Student")
                                .email("student@test.edu")
                                .passwordHash("$2a$12$hashedPassword")
                                .role(UserRole.STUDENT)
                                .college(testCollege)
                                .isActive(true)
                                .build();
                testUser.setId(1L);

                // Setup login request
                loginRequest = LoginRequestDTO.builder()
                                .email("student@test.edu")
                                .password("password123")
                                .build();

                // Common mocks (Using lenient() to avoid UnnecessaryStubbingException in strict
                // mode)
                lenient().when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
                lenient().when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test-Agent");
                lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Login success returns token and redirect URL")
        void testLoginSuccess_ReturnsTokenAndRedirectUrl() {
                // Arrange
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                LoginResponseDTO response = authService.login(loginRequest);

                // Assert
                assertNotNull(response);
                assertEquals("jwt-token-here", response.getToken());
                assertEquals(1L, response.getUserId());
                assertEquals("STUDENT", response.getRole());
                assertEquals(1L, response.getCollegeId());
                assertEquals("/dashboard/student", response.getRedirectUrl());

                // Verify audit was saved
                verify(loginAuditRepository).save(any(LoginAudit.class));
                verify(userRepository).save(testUser);
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Login with invalid password throws AuthenticationException")
        void testLoginWithInvalidPassword_ThrowsAuthenticationException() {
                // Arrange
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash()))
                                .thenReturn(false);

                loginRequest.setPassword("wrongpassword");

                // Act & Assert
                assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
                verify(loginAuditRepository).save(any(LoginAudit.class));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Login with unknown email throws AuthenticationException")
        void testLoginWithUnknownEmail_ThrowsAuthenticationException() {
                // Arrange
                when(userRepository.findByEmailWithCollege("unknown@test.edu"))
                                .thenReturn(Optional.empty());

                loginRequest.setEmail("unknown@test.edu");

                // Act & Assert
                assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
                verify(loginAuditRepository).save(any(LoginAudit.class));
        }

        @Test
        @DisplayName("Login with disabled user throws AccountDeactivatedException")
        void testLoginWithDisabledUser_ThrowsAccountDeactivatedException() {
                // Arrange
                testUser.setIsActive(false);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);

                // Act & Assert
                assertThrows(AccountDeactivatedException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Login with inactive college throws CollegeInactiveException")
        void testLoginWithInactiveCollege_ThrowsCollegeInactiveException() {
                // Arrange
                testCollege.setIsActive(false);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);

                // Act & Assert
                assertThrows(CollegeInactiveException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("SUPER_ADMIN login with null college succeeds")
        void testSuperAdminLogin_NullCollegeId_Success() {
                // Arrange
                User superAdmin = User.builder()
                                .name("Super Admin")
                                .email("superadmin@platform.com")
                                .passwordHash("$2a$12$hashedPassword")
                                .role(UserRole.SUPER_ADMIN)
                                .college(null)
                                .isActive(true)
                                .build();
                superAdmin.setId(99L);

                LoginRequestDTO superAdminRequest = LoginRequestDTO.builder()
                                .email("superadmin@platform.com")
                                .password("password123")
                                .build();

                when(userRepository.findByEmailWithCollege("superadmin@platform.com"))
                                .thenReturn(Optional.of(superAdmin));
                when(passwordEncoder.matches("password123", superAdmin.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(superAdmin))
                                .thenReturn("superadminjwt-token");

                // Act
                LoginResponseDTO response = authService.login(superAdminRequest);

                // Assert
                assertNotNull(response);
                assertEquals("SUPER_ADMIN", response.getRole());
                assertNull(response.getCollegeId());
                assertEquals("/dashboard/superadmin", response.getRedirectUrl());
        }

        @Test
        @DisplayName("Coordinator login redirects to coordinator dashboard")
        void testCoordinatorLogin_ReturnsCorrectRedirectUrl() {
                // Arrange
                testUser.setRole(UserRole.COORDINATOR);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                LoginResponseDTO response = authService.login(loginRequest);

                // Assert
                assertEquals("/dashboard/coordinator", response.getRedirectUrl());
                assertEquals("COORDINATOR", response.getRole());
        }

        @Test
        @DisplayName("Admin login redirects to admin dashboard")
        void testAdminLogin_ReturnsCorrectRedirectUrl() {
                // Arrange
                testUser.setRole(UserRole.ADMIN);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                LoginResponseDTO response = authService.login(loginRequest);

                // Assert
                assertEquals("/dashboard/admin", response.getRedirectUrl());
                assertEquals("ADMIN", response.getRole());
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Successful login updates lastLogin timestamp")
        void testLoginSuccess_UpdatesLastLogin() {
                // Arrange
                assertNull(testUser.getLastLogin()); // Verify no prior login
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                authService.login(loginRequest);

                // Assert
                assertNotNull(testUser.getLastLogin());
                verify(userRepository).save(testUser);
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Successful login creates audit with success=true")
        void testLoginSuccess_CreatesSuccessAudit() {
                // Arrange
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                authService.login(loginRequest);

                // Assert - verify audit was saved with success=true
                verify(loginAuditRepository).save(argThat(audit -> audit.getSuccess() &&
                                audit.getUserId().equals(testUser.getId()) &&
                                audit.getEmail().equals("student@test.edu") &&
                                audit.getEventType() == SecurityAuditEventType.LOGIN));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Failed login with wrong password creates audit with success=false")
        void testLoginFailure_WrongPassword_CreatesFailureAudit() {
                // Arrange
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash()))
                                .thenReturn(false);

                loginRequest.setPassword("wrongpassword");

                // Act & Assert
                assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

                // Verify audit was saved with success=false, correct userId and event type
                verify(loginAuditRepository).save(argThat(audit -> !audit.getSuccess() &&
                                audit.getUserId().equals(testUser.getId()) &&
                                audit.getEmail().equals("student@test.edu") &&
                                audit.getEventType() == SecurityAuditEventType.LOGIN));
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("Failed login with unknown email creates audit with null userId")
        void testLoginFailure_UnknownEmail_CreatesAuditWithNullUserId() {
                // Arrange
                when(userRepository.findByEmailWithCollege("unknown@test.edu"))
                                .thenReturn(Optional.empty());

                loginRequest.setEmail("unknown@test.edu");

                // Act & Assert
                assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

                // Verify audit was saved with null userId (unknown user)
                verify(loginAuditRepository).save(argThat(audit -> !audit.getSuccess() &&
                                audit.getUserId() == null &&
                                audit.getEmail().equals("unknown@test.edu") &&
                                audit.getEventType() == SecurityAuditEventType.LOGIN));
        }

        @Test
        @DisplayName("Login with null isActive throws AccountDeactivatedException")
        void testLoginWithNullIsActive_ThrowsAccountDeactivatedException() {
                // Arrange
                testUser.setIsActive(null);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);

                // Act & Assert
                assertThrows(AccountDeactivatedException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Login normalizes email to lowercase and trims whitespace")
        void testLogin_EmailNormalization_TrimsAndLowercases() {
                // Arrange
                loginRequest.setEmail("  STUDENT@TEST.EDU  ");
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);
                when(jwtTokenProvider.generateToken(testUser))
                                .thenReturn("jwt-token-here");

                // Act
                LoginResponseDTO response = authService.login(loginRequest);

                // Assert - verify the normalized email was used for lookup
                verify(userRepository).findByEmailWithCollege("student@test.edu");
                assertNotNull(response.getToken());
        }

        @Test
        @DisplayName("Login with inactive college isActive null throws CollegeInactiveException")
        void testLoginWithNullCollegeIsActive_ThrowsCollegeInactiveException() {
                // Arrange
                testCollege.setIsActive(null);
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);

                // Act & Assert
                assertThrows(CollegeInactiveException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Non-SUPER_ADMIN without college throws CollegeInactiveException")
        void testLoginNonSuperAdminWithoutCollege_ThrowsCollegeInactiveException() {
                // Arrange
                testUser.setCollege(null); // User without college
                testUser.setRole(UserRole.STUDENT); // Not SUPER_ADMIN
                when(userRepository.findByEmailWithCollege("student@test.edu"))
                                .thenReturn(Optional.of(testUser));
                when(passwordEncoder.matches("password123", testUser.getPasswordHash()))
                                .thenReturn(true);

                // Act & Assert
                assertThrows(CollegeInactiveException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Register with password mismatch throws IllegalArgumentException")
        void testRegister_PasswordMismatch_ThrowsIllegalArgumentException() {
                // Arrange
                RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
                                .name("New User")
                                .email("new@test.edu")
                                .password("password123")
                                .confirmPassword("passwordMismatch")
                                .build();

                // Act & Assert
                Exception exception = assertThrows(IllegalArgumentException.class,
                                () -> authService.register(registerRequest));
                assertEquals("Passwords do not match", exception.getMessage());
        }
}
