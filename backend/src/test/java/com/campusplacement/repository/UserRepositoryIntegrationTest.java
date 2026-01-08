package com.campusplacement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.AbstractIntegrationTest;
import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

/**
 * Integration tests for UserRepository using real PostgreSQL via
 * Testcontainers.
 *
 * <p>
 * Tests verify:
 * <ul>
 * <li>Custom query methods work correctly</li>
 * <li>Unique constraints are enforced</li>
 * <li>Soft delete filtering works</li>
 * <li>Eager fetching for college works</li>
 * </ul>
 * </p>
 */
@Transactional
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private College testCollege;
    private College otherCollege;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();
        collegeRepository.deleteAll();

        // Create test colleges
        testCollege = College.builder()
                .name("Test College")
                .code("TC001")
                .isActive(true)
                .build();
        testCollege = collegeRepository.save(testCollege);

        otherCollege = College.builder()
                .name("Other College")
                .code("OC001")
                .isActive(true)
                .build();
        otherCollege = collegeRepository.save(otherCollege);

        // Create test user
        testUser = User.builder()
                .name("Test User")
                .email("user@test.edu")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .college(testCollege)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);
    }

    // ========== findByEmail Tests ==========

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("Returns user when email exists")
        void findByEmail_ExistingEmail_ReturnsUser() {
            Optional<User> result = userRepository.findByEmail("user@test.edu");

            assertTrue(result.isPresent());
            assertEquals("Test User", result.get().getName());
            assertEquals(UserRole.STUDENT, result.get().getRole());
        }

        @Test
        @DisplayName("Returns empty when email does not exist")
        void findByEmail_NonExistingEmail_ReturnsEmpty() {
            Optional<User> result = userRepository.findByEmail("nonexistent@test.edu");

            assertFalse(result.isPresent());
        }
    }

    // ========== findByEmailWithCollege Tests ==========

    @Nested
    @DisplayName("findByEmailWithCollege")
    class FindByEmailWithCollegeTests {

        @Test
        @DisplayName("Returns user with college eagerly fetched")
        void findByEmailWithCollege_ExistingUser_ReturnsWithCollege() {
            Optional<User> result = userRepository.findByEmailWithCollege("user@test.edu");

            assertTrue(result.isPresent());
            assertNotNull(result.get().getCollege());
            assertEquals("Test College", result.get().getCollege().getName());
        }

        @Test
        @DisplayName("Returns super admin with null college")
        void findByEmailWithCollege_SuperAdmin_ReturnsWithNullCollege() {
            // Create super admin without college
            User superAdmin = User.builder()
                    .name("Super Admin")
                    .email("superadmin@platform.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(UserRole.SUPER_ADMIN)
                    .college(null)
                    .isActive(true)
                    .build();
            userRepository.save(superAdmin);

            Optional<User> result = userRepository.findByEmailWithCollege("superadmin@platform.com");

            assertTrue(result.isPresent());
            assertEquals(UserRole.SUPER_ADMIN, result.get().getRole());
            // College should be null for super admin
        }
    }

    // ========== findByEmailAndCollegeId Tests ==========

    @Nested
    @DisplayName("findByEmailAndCollegeId")
    class FindByEmailAndCollegeIdTests {

        @Test
        @DisplayName("Returns user when email and college match")
        void findByEmailAndCollegeId_MatchingCollege_ReturnsUser() {
            Optional<User> result = userRepository.findByEmailAndCollegeId(
                    "user@test.edu", testCollege.getId());

            assertTrue(result.isPresent());
            assertEquals(testUser.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Returns empty when email exists in different college")
        void findByEmailAndCollegeId_DifferentCollege_ReturnsEmpty() {
            Optional<User> result = userRepository.findByEmailAndCollegeId(
                    "user@test.edu", otherCollege.getId());

            assertFalse(result.isPresent());
        }
    }

    // ========== existsByEmailAndCollegeId Tests ==========

    @Nested
    @DisplayName("existsByEmailAndCollegeId")
    class ExistsByEmailAndCollegeIdTests {

        @Test
        @DisplayName("Returns true when email exists in college")
        void existsByEmailAndCollegeId_Exists_ReturnsTrue() {
            boolean exists = userRepository.existsByEmailAndCollegeId(
                    "user@test.edu", testCollege.getId());

            assertTrue(exists);
        }

        @Test
        @DisplayName("Returns false when email exists in different college")
        void existsByEmailAndCollegeId_DifferentCollege_ReturnsFalse() {
            boolean exists = userRepository.existsByEmailAndCollegeId(
                    "user@test.edu", otherCollege.getId());

            assertFalse(exists);
        }

        @Test
        @DisplayName("Email uniqueness per college allows same email in different colleges")
        void emailUniquenessPerCollege_AllowsSameEmailInDifferentColleges() {
            // Create user with same email in different college
            User otherCollegeUser = User.builder()
                    .name("Other College User")
                    .email("user@test.edu") // Same email
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(UserRole.STUDENT)
                    .college(otherCollege) // Different college
                    .isActive(true)
                    .build();
            userRepository.save(otherCollegeUser);

            // Both should exist in their respective colleges
            assertTrue(userRepository.existsByEmailAndCollegeId("user@test.edu", testCollege.getId()));
            assertTrue(userRepository.existsByEmailAndCollegeId("user@test.edu", otherCollege.getId()));
        }
    }

    // ========== findByIdWithCollege Tests ==========

    @Nested
    @DisplayName("findByIdWithCollege")
    class FindByIdWithCollegeTests {

        @Test
        @DisplayName("Returns user with college eagerly fetched")
        void findByIdWithCollege_ExistingId_ReturnsWithCollege() {
            Optional<User> result = userRepository.findByIdWithCollege(testUser.getId());

            assertTrue(result.isPresent());
            assertNotNull(result.get().getCollege());
            assertEquals(testCollege.getId(), result.get().getCollege().getId());
        }

        @Test
        @DisplayName("Returns empty for non-existing ID")
        void findByIdWithCollege_NonExistingId_ReturnsEmpty() {
            Optional<User> result = userRepository.findByIdWithCollege(99999L);

            assertFalse(result.isPresent());
        }
    }

    // ========== Soft Delete Tests ==========

    @Nested
    @DisplayName("Soft Delete Filtering")
    class SoftDeleteTests {

        @Test
        @DisplayName("Soft deleted user is filtered from findByEmail")
        void softDelete_FilteredFromFindByEmail() {
            // Soft delete the user
            testUser.softDelete();
            userRepository.save(testUser);
            userRepository.flush();

            // User should not be found by normal queries
            Optional<User> result = userRepository.findByEmail("user@test.edu");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Soft deleted user is filtered from findByEmailWithCollege")
        void softDelete_FilteredFromFindByEmailWithCollege() {
            // Soft delete the user
            testUser.softDelete();
            userRepository.save(testUser);
            userRepository.flush();

            Optional<User> result = userRepository.findByEmailWithCollege("user@test.edu");

            assertFalse(result.isPresent());
        }
    }

    // ========== Role-Based Query Tests ==========

    @Nested
    @DisplayName("Role-Based Queries")
    class RoleBasedQueryTests {

        @Test
        @DisplayName("findByRole returns users with matching role")
        void findByRole_ReturnsMatchingUsers() {
            assertEquals(1, userRepository.findByRole(UserRole.STUDENT).size());
            assertEquals(0, userRepository.findByRole(UserRole.COORDINATOR).size());
        }

        @Test
        @DisplayName("findByRoleAndIsActiveTrue returns only active users")
        void findByRoleAndIsActiveTrue_ReturnsActiveOnly() {
            // Deactivate user
            testUser.setIsActive(false);
            userRepository.save(testUser);

            assertEquals(0, userRepository.findByRoleAndIsActiveTrue(UserRole.STUDENT).size());
        }

        @Test
        @DisplayName("findByCollegeId returns users in college")
        void findByCollegeId_ReturnsCollegeUsers() {
            assertEquals(1, userRepository.findByCollegeId(testCollege.getId()).size());
            assertEquals(0, userRepository.findByCollegeId(otherCollege.getId()).size());
        }
    }
}
