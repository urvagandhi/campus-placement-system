package com.campusplacement.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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

    // TODO: Add additional query methods as needed
    // - findByEmailAndIsActiveTrue(String email)
    // - findByResetToken(String token)
}
