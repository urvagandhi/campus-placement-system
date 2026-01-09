package com.campusplacement.users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.common.UserRole;

/**
 * Repository interface for User entity database operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email (globally).
     * Note: For multi-college, use findByEmailAndCollegeId.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by email within a specific college.
     * This is the recommended method for multi-college authentication.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND (u.college.id = :collegeId OR u.college IS NULL)")
    Optional<User> findByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Finds a user by email, including SUPER_ADMIN (college is null).
     * Email comparison is case-insensitive.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.college WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithCollege(@Param("email") String email);

    /**
     * Finds a user by ID with college eagerly fetched.
     * Used by JWT authentication filter to avoid LazyInitializationException.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.college WHERE u.id = :id")
    Optional<User> findByIdWithCollege(@Param("id") Long id);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given email in a specific college.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.college.id = :collegeId")
    boolean existsByEmailAndCollegeId(@Param("email") String email, @Param("collegeId") Long collegeId);

    /**
     * Finds all users with a specific role.
     */
    List<User> findByRole(UserRole role);

    /**
     * Finds all active users.
     */
    List<User> findByIsActiveTrue();

    /**
     * Finds all active users with a specific role.
     */
    List<User> findByRoleAndIsActiveTrue(UserRole role);

    /**
     * Finds all users in a specific college.
     */
    List<User> findByCollegeId(Long collegeId);

    /**
     * Finds all active users in a specific college with a specific role.
     */
    List<User> findByCollegeIdAndRoleAndIsActiveTrue(Long collegeId, UserRole role);
}
