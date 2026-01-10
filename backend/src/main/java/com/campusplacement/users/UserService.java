package com.campusplacement.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.common.PagedResponse;
import com.campusplacement.common.UserRole;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.users.dto.UserDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for user management operations.
 *
 * <p>
 * <strong>Scope Enforcement:</strong>
 * </p>
 * <ul>
 * <li>SUPER_ADMIN: Can access all users</li>
 * <li>ADMIN: Can access users within their college only</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    // ==================== Read Operations ====================

    /**
     * Retrieves all users with pagination (legacy - returns list).
     *
     * @return list of user DTOs
     * @deprecated Use getAllUsersPaginated for new implementations
     */
    @Deprecated
    @Transactional(readOnly = true)
    public java.util.List<UserDTO> getAllUsers() {
        CustomUserDetails currentUser = getCurrentUserDetails();
        Page<User> users;

        if (UserRole.SUPER_ADMIN.equals(currentUser.getRole())) {
            users = userRepository.findByIsActiveTrue(Pageable.unpaged());
        } else {
            Long collegeId = currentUser.getCollegeId();
            if (collegeId == null) {
                throw new AccessDeniedException("User has no college association");
            }
            users = userRepository.findByCollegeIdAndIsActiveTrue(collegeId, Pageable.unpaged());
        }

        return users.map(this::toDTO).getContent();
    }

    /**
     * Retrieves all users with pagination and scope enforcement.
     *
     * @param pageable Pagination parameters
     * @param search   Optional search term (name or email)
     * @param role     Optional role filter
     * @return Paginated response of user DTOs
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserDTO> getAllUsersPaginated(Pageable pageable, String search, String role) {
        CustomUserDetails currentUser = getCurrentUserDetails();
        Page<User> users;

        if (UserRole.SUPER_ADMIN.equals(currentUser.getRole())) {
            // SUPER_ADMIN: Access all active users
            users = userRepository.findByIsActiveTrue(pageable);
        } else {
            // ADMIN: Access only users within their college
            Long collegeId = currentUser.getCollegeId();
            if (collegeId == null) {
                throw new AccessDeniedException("User has no college association");
            }

            if (search != null && !search.isBlank()) {
                users = userRepository.searchByCollegeId(collegeId, search.trim(), pageable);
            } else if (role != null && !role.isBlank()) {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                users = userRepository.findByCollegeIdAndRole(collegeId, userRole, pageable);
            } else {
                users = userRepository.findByCollegeIdAndIsActiveTrue(collegeId, pageable);
            }
        }

        return PagedResponse.of(users, users.map(this::toDTO).getContent());
    }

    /**
     * Retrieves a user by ID with scope enforcement.
     *
     * @param id user ID
     * @return user DTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdWithCollege(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        validateUserAccess(user);

        return toDTO(user);
    }

    // ==================== Write Operations ====================

    /**
     * Updates a user with scope enforcement.
     *
     * @param id      user ID
     * @param userDTO updated user data
     * @return updated user DTO
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findByIdWithCollege(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        validateUserAccess(user);

        // Update allowed fields only
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userDTO.getProfileImageUrl());
        }
        // Note: Email and role changes require separate privileged operations

        @SuppressWarnings("null")
        User saved = userRepository.save(user);
        log.info("User {} updated by {}", id, getCurrentUserDetails().getId());

        return toDTO(saved);
    }

    /**
     * Soft deletes a user.
     * Only SUPER_ADMIN can delete users.
     *
     * @param id user ID
     */
    @Transactional
    public void deleteUser(Long id) {
        @SuppressWarnings("null")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent self-deletion
        if (getCurrentUserDetails().getId().equals(id)) {
            throw new IllegalStateException("Cannot delete your own account");
        }

        user.softDelete();
        userRepository.save(user);

        log.info("User {} soft-deleted by {}", id, getCurrentUserDetails().getId());
    }

    /**
     * Changes a user's role.
     * Only SUPER_ADMIN can change roles.
     *
     * @param id   user ID
     * @param role new role
     * @return updated user DTO
     */
    @Transactional
    public UserDTO changeUserRole(Long id, String role) {
        @SuppressWarnings("null")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Prevent changing own role
        if (getCurrentUserDetails().getId().equals(id)) {
            throw new IllegalStateException("Cannot change your own role");
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);

        log.info("User {} role changed to {} by {}", id, newRole, getCurrentUserDetails().getId());

        return toDTO(saved);
    }

    // ==================== Authorization Helpers ====================

    /**
     * Checks if the given ID matches the current authenticated user.
     *
     * @param userId user ID to check
     * @return true if current user matches the ID
     */
    public boolean isCurrentUser(Long userId) {
        CustomUserDetails currentUser = getCurrentUserDetails();
        return currentUser != null && currentUser.getId().equals(userId);
    }

    // ==================== Private Helpers ====================

    private CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    private void validateUserAccess(User user) {
        CustomUserDetails currentUser = getCurrentUserDetails();

        // SUPER_ADMIN can access anyone
        if (UserRole.SUPER_ADMIN.equals(currentUser.getRole())) {
            return;
        }

        // Users can access themselves
        if (currentUser.getId().equals(user.getId())) {
            return;
        }

        // ADMIN can access users in their college
        Long currentCollegeId = currentUser.getCollegeId();
        Long userCollegeId = user.getCollege() != null ? user.getCollege().getId() : null;

        if (currentCollegeId != null && currentCollegeId.equals(userCollegeId)) {
            return;
        }

        throw new AccessDeniedException("No access to this user");
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .isActive(user.getIsActive())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
