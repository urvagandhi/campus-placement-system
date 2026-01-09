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
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpirationMs", 86400000L);

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

    @Test
    @DisplayName("Generate token contains version claim")
    void testGenerateToken_ContainsVersionClaim() {
        String token = jwtTokenProvider.generateToken(testUser);
        Integer version = jwtTokenProvider.getTokenVersion(token);

        assertNotNull(version);
        assertEquals(1, version.intValue());
    }

    @Test
    @DisplayName("Get current token version returns 1")
    void testGetCurrentTokenVersion_Returns1() {
        assertEquals(1, jwtTokenProvider.getCurrentTokenVersion());
    }

    @Test
    @DisplayName("Get expiration returns configured value")
    void testGetAccessExpirationMs_ReturnsConfiguredValue() {
        assertEquals(86400000L, jwtTokenProvider.getAccessExpirationMs());
    }

    @Test
    @DisplayName("Validate expired token returns false")
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Create a provider with very short expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret",
                "ThisIsAVerySecureSecretKeyForJWTTokenGenerationMinimum256BitsLong");
        ReflectionTestUtils.setField(shortLivedProvider, "accessExpirationMs", 1L); // 1ms expiration

        String token = shortLivedProvider.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(50); // Wait 50ms to ensure expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    @DisplayName("Get org unit ID from token when present")
    void testGetOrgUnitIdFromToken_WhenPresent() {
        Long orgUnitId = 42L;
        String token = jwtTokenProvider.generateToken(testUser, orgUnitId);

        assertEquals(orgUnitId, jwtTokenProvider.getOrgUnitIdFromToken(token));
    }

    @Test
    @DisplayName("Get org unit ID from token when absent returns null")
    void testGetOrgUnitIdFromToken_WhenAbsent_ReturnsNull() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertNull(jwtTokenProvider.getOrgUnitIdFromToken(token));
    }

    @Test
    @DisplayName("Generate token with org unit includes oid claim")
    void testGenerateTokenWithOrgUnit_IncludesOidClaim() {
        Long orgUnitId = 100L;
        String token = jwtTokenProvider.generateToken(testUser, orgUnitId);

        // Verify all claims are present
        assertEquals("test@college.edu", jwtTokenProvider.getEmailFromToken(token));
        assertEquals(10L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(5L, jwtTokenProvider.getCollegeIdFromToken(token));
        assertEquals(100L, jwtTokenProvider.getOrgUnitIdFromToken(token));
    }

    @Test
    @DisplayName("Validate empty token returns false")
    void testValidateToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("Validate null-like token returns false")
    void testValidateToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt.token.at.all"));
    }

    @Test
    @DisplayName("Get email from token returns correct email")
    void testGetEmailFromToken_ReturnsCorrectEmail() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertEquals("test@college.edu", jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("Get college ID from token returns correct ID")
    void testGetCollegeIdFromToken_ReturnsCorrectId() {
        String token = jwtTokenProvider.generateToken(testUser);
        assertEquals(5L, jwtTokenProvider.getCollegeIdFromToken(token));
    }
}
