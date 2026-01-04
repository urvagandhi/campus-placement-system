package com.campusplacement.users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity database operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role.
     */
    List<User> findByRole(String role);

    /**
     * Finds all active users.
     */
    List<User> findByIsActiveTrue();

    /**
     * Finds all active users with a specific role.
     */
    List<User> findByRoleAndIsActiveTrue(String role);
}
