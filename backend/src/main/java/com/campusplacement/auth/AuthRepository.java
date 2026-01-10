package com.campusplacement.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.users.User;

/**
 * Repository interface for authentication-related database operations.
 *
 * <p>
 * Provides methods for user lookup during authentication.
 * </p>
 */
@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email to check
     * @return true if a user exists with this email
     */
    boolean existsByEmail(String email);

    // ==================== Active User Queries ====================

    /**
     * Finds an active user by email.
     * Primary method for login - only returns users who are not deactivated.
     *
     * @param email the email to search for
     * @return Optional containing the active user if found
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Finds an active user by username.
     * Alternative login method using username.
     *
     * @param username the username to search for
     * @return Optional containing the active user if found
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);

    // ==================== Multi-College Auth Queries ====================

    /**
     * Finds an active user by email within a specific college.
     * Used for multi-tenant authentication where email is unique per college.
     *
     * @param email     the email to search for
     * @param collegeId the college context
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.college " +
            "WHERE LOWER(u.email) = LOWER(:email) " +
            "AND (u.college.id = :collegeId OR u.college IS NULL) " +
            "AND u.isActive = true")
    Optional<User> findActiveByEmailAndCollegeId(
            @Param("email") String email,
            @Param("collegeId") Long collegeId);

    /**
     * Finds an active user by email with college eagerly loaded.
     * Includes SUPER_ADMIN users who have no college.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.college " +
            "WHERE LOWER(u.email) = LOWER(:email) AND u.isActive = true")
    Optional<User> findActiveByEmailWithCollege(@Param("email") String email);

    // ==================== Account Management Queries ====================

    /**
     * Updates the last login timestamp for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId);

    /**
     * Checks if an active user exists with the given email.
     * Used for registration validation.
     *
     * @param email the email to check
     * @return true if an active user exists with this email
     */
    boolean existsByEmailAndIsActiveTrue(String email);

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return true if the username exists
     */
    boolean existsByUsername(String username);
}
