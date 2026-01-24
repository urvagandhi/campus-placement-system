package com.campusplacement.users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // ==================== Paginated Queries ====================

        /**
         * Finds all active users with pagination.
         * Used by SUPER_ADMIN to view all users across colleges.
         *
         * @param pageable Pagination parameters
         * @return Page of active users
         */
        Page<User> findByIsActiveTrue(Pageable pageable);

        /**
         * Finds all users in a specific college with pagination.
         * Used by ADMIN to view users within their college.
         *
         * @param collegeId College ID for scope filtering
         * @param pageable  Pagination parameters
         * @return Page of users in the college
         */
        Page<User> findByCollegeId(Long collegeId, Pageable pageable);

        /**
         * Finds active users in a college with pagination.
         *
         * @param collegeId College ID for scope filtering
         * @param pageable  Pagination parameters
         * @return Page of active users in the college
         */
        Page<User> findByCollegeIdAndIsActiveTrue(Long collegeId, Pageable pageable);

        /**
         * Finds users by role within a college with pagination.
         *
         * @param collegeId College ID for scope filtering
         * @param role      User role filter
         * @param pageable  Pagination parameters
         * @return Page of users matching criteria
         */
        Page<User> findByCollegeIdAndRole(Long collegeId, UserRole role, Pageable pageable);

        /**
         * Searches users by name or email within a college.
         * Supports partial matching for autocomplete/search functionality.
         *
         * @param collegeId College ID for scope filtering
         * @param search    Search term (matches name or email)
         * @param pageable  Pagination parameters
         * @return Page of matching users
         */
        @Query("SELECT u FROM User u WHERE u.college.id = :collegeId " +
                        "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<User> searchByCollegeId(
                        @Param("collegeId") Long collegeId,
                        @Param("search") String search,
                        Pageable pageable);

        /**
         * Counts users by college ID.
         *
         * @param collegeId College ID
         * @return User count in the college
         */
        long countByCollegeId(Long collegeId);

        /**
         * Counts active users by college ID.
         *
         * @param collegeId College ID
         * @return Active user count in the college
         */
        long countByCollegeIdAndIsActiveTrue(Long collegeId);

        /**
         * Finds ADMIN users for a college, prioritizing those assigned to root org
         * unit.
         * Returns a list since there may be multiple admins.
         *
         * @param collegeId College ID
         * @return List of admin users, ordered by priority (root org unit first)
         */
        @Query("SELECT u FROM User u " +
                        "LEFT JOIN UserAssignment a ON a.user.id = u.id " +
                        "LEFT JOIN OrganizationUnit ou ON a.organizationUnit.id = ou.id AND ou.isRoot = true " +
                        "WHERE u.college.id = :collegeId " +
                        "AND u.role = com.campusplacement.common.UserRole.ADMIN " +
                        "AND u.isActive = true " +
                        "ORDER BY CASE WHEN ou.id IS NOT NULL THEN 0 ELSE 1 END, u.id ASC")
        List<User> findAdminsByCollegeIdOrdered(@Param("collegeId") Long collegeId);

        /**
         * Simple fallback: Find any active ADMIN user for a college.
         * Used when the complex query fails.
         */
        Optional<User> findFirstByCollegeIdAndRoleAndIsActiveTrue(Long collegeId,
                        com.campusplacement.common.UserRole role);
}
