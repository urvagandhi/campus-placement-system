package com.campusplacement.users;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.ScopeLevel;
import com.campusplacement.common.UserRole;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.organizations.OrganizationUnitRepository;
import com.campusplacement.organizations.UserAssignment;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.users.dto.CreateAdminDTO;
import com.campusplacement.users.dto.CreateCoordinatorDTO;
import com.campusplacement.users.dto.CreateStudentDTO;
import com.campusplacement.users.dto.UserDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for user management operations.
 *
 * <p>
 * Enforces strict role-based creation rules:
 * </p>
 * <ul>
 * <li>COORDINATOR can create STUDENT (Same college only)</li>
 * <li>ADMIN can create COORDINATOR (Same college only)</li>
 * <li>SUPER_ADMIN can create ADMIN (Any college)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new student user.
     * MUST be called by COORDINATOR.
     */
    @Transactional
    public UserDTO createStudent(CreateStudentDTO dto) {
        CustomUserDetails currentUser = getCurrentUser();
        enforceRole(currentUser, UserRole.COORDINATOR);

        // Student belongs to coordinator's college
        College college = validateAndGetCollege(currentUser.getCollegeId());

        return createUserInternal(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword(),
                UserRole.STUDENT,
                college,
                dto.getOrganizationUnitId(),
                dto.getPhoneNumber(),
                currentUser.getUsername());
    }

    /**
     * Creates a new coordinator (TPO) user.
     * MUST be called by ADMIN.
     */
    @Transactional
    public UserDTO createCoordinator(CreateCoordinatorDTO dto) {
        CustomUserDetails currentUser = getCurrentUser();
        enforceRole(currentUser, UserRole.ADMIN);

        // Coordinator belongs to admin's college
        College college = validateAndGetCollege(currentUser.getCollegeId());

        return createUserInternal(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword(),
                UserRole.COORDINATOR,
                college,
                null,
                dto.getPhoneNumber(),
                currentUser.getUsername());
    }

    /**
     * Creates a new admin user.
     * MUST be called by SUPER_ADMIN.
     */
    @Transactional
    public UserDTO createAdmin(CreateAdminDTO dto) {
        CustomUserDetails currentUser = getCurrentUser();
        enforceRole(currentUser, UserRole.SUPER_ADMIN);

        // Admin assigned to specific college
        College college = validateAndGetCollege(dto.getCollegeId());

        return createUserInternal(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword(),
                UserRole.ADMIN,
                college,
                null,
                dto.getPhoneNumber(),
                currentUser.getUsername());
    }

    /**
     * Enforces that the current user has the expected role.
     * Service-level security check.
     */
    private void enforceRole(CustomUserDetails user, UserRole expected) {
        if (user.getRole() != expected) {
            log.warn("Access denied: User {} with role {} attempted to act as {}",
                    user.getUsername(), user.getRole(), expected);
            throw new AccessDeniedException("Invalid registration authority. Expected: " + expected);
        }
    }

    @SuppressWarnings("null")
    private UserDTO createUserInternal(
            String name,
            String email,
            String password,
            UserRole role,
            College college,
            Long organizationUnitId,
            String phoneNumber,
            String creator) {

        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.existsByEmailAndCollegeId(normalizedEmail, college.getId())) {
            throw new IllegalArgumentException("Email is already registered in this college");
        }

        User user = User.builder()
                .name(name.trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .college(college)
                .phoneNumber(phoneNumber)
                .isActive(true)
                .build();

        // Handle Organization Unit Assignment if provided
        if (organizationUnitId != null) {
            OrganizationUnit orgUnit = organizationUnitRepository.findById(organizationUnitId)
                    .orElseThrow(() -> new IllegalArgumentException("Organization Unit not found"));

            // Verify Org Unit belongs to the same college
            if (!orgUnit.getCollege().getId().equals(college.getId())) {
                throw new IllegalArgumentException("Organization Unit does not belong to the user's college");
            }

            UserAssignment assignment = UserAssignment.builder()
                    .user(user)
                    .organizationUnit(orgUnit)
                    .designation(role.name()) // Use role name as default designation
                    .scopeLevel(ScopeLevel.SELF) // Default scope
                    .isPrimary(true)
                    .build();

            user.getAssignments().add(assignment);
        }

        User savedUser = userRepository.save(user);
        log.info("User {} with role {} created by {} in college {}",
                normalizedEmail, role, creator, college.getName());

        return mapToDTO(savedUser);
    }

    private College validateAndGetCollege(Long collegeId) {
        if (collegeId == null) {
            throw new IllegalArgumentException("College ID is required");
        }

        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new IllegalArgumentException("College not found"));

        if (college.getIsActive() == null || !college.getIsActive()) {
            throw new IllegalArgumentException("Cannot create users in an inactive college");
        }

        return college;
    }

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    private UserDTO mapToDTO(User user) {
        Long orgUnitId = null;
        Optional<UserAssignment> primaryAssignment = user.getPrimaryAssignment();
        if (primaryAssignment.isPresent()) {
            orgUnitId = primaryAssignment.get().getOrganizationUnit().getId();
        }

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .organizationUnitId(orgUnitId)
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
