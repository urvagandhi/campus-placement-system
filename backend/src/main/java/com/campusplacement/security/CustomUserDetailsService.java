package com.campusplacement.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Custom UserDetailsService implementation for Spring Security authentication.
 *
 * <p>
 * Loads user details from the database for authentication purposes.
 * Uses eager fetching for college to avoid lazy loading issues.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithCollege(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return new CustomUserDetails(user);
    }

    /**
     * Loads user by ID for token-based authentication.
     *
     * @param userId the user ID
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findByIdWithCollege(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));

        return new CustomUserDetails(user);
    }
}
