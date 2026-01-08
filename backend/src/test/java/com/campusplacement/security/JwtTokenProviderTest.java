package com.campusplacement.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.campusplacement.colleges.College;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Set properties via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "ThisIsAVerySecureSecretKeyForJWTTokenGenerationMinimum256BitsLong");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);

        // Setup test college
        College testCollege = College.builder()
                .name("Test College")
                .code("TC001")
                .build();
        testCollege.setId(5L);

        // Setup test user
        testUser = User.builder()
                .name("Test User")
                .email("test@college.edu")
                .role(UserRole.COORDINATOR)
                .college(testCollege)
                .build();
        testUser.setId(10L);
    }

    @Test
    @DisplayName("Generate token contains all required claims")
    void testGenerateToken_ContainsAllClaims() {
        // Act
        String token = jwtTokenProvider.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify claims can be extracted
        assertEquals("test@college.edu", jwtTokenProvider.getEmailFromToken(token));
        assertEquals(10L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(UserRole.COORDINATOR, jwtTokenProvider.getRoleFromToken(token));
        assertEquals(5L, jwtTokenProvider.getCollegeIdFromToken(token));
    }

    @Test
    @DisplayName("Validate token returns true for valid token")
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Validate token returns false for invalid token")
    void testValidateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("Validate token returns false for tampered token")
    void testValidateToken_TamperedToken_ReturnsFalse() {
        String token = jwtTokenProvider.generateToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Get user ID from token returns correct ID")
    void testGetUserIdFromToken_ReturnsCorrectId() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertEquals(10L, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Super admin token has null college ID")
    void testSuperAdminToken_NullCollegeId() {
        User superAdmin = User.builder()
                .email("admin@platform.com")
                .role(UserRole.SUPER_ADMIN)
                .college(null)
                .build();
        superAdmin.setId(1L);

        String token = jwtTokenProvider.generateToken(superAdmin);

        assertNull(jwtTokenProvider.getCollegeIdFromToken(token));
        assertEquals(UserRole.SUPER_ADMIN, jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Get role from token returns correct role")
    void testGetRoleFromToken_ReturnsCorrectRole() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertEquals(UserRole.COORDINATOR, jwtTokenProvider.getRoleFromToken(token));
    }
}
