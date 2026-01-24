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
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.model.UserAssignment;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;
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
    private final com.campusplacement.settings.SystemSettingsService systemSettingsService;

    /**
     * Checks if user registration is currently enabled.
     * Only applies to COORDINATOR and STUDENT creation.
     * SUPER_ADMIN operations are always allowed.
     */
    private void checkRegistrationEnabled() {
        if (!systemSettingsService.isRegistrationEnabled()) {
            throw new IllegalStateException("User registration is currently disabled by system administrator");
        }
    }

    /**
     * Creates a new student user.
     * MUST be called by COORDINATOR.
     */
    @Transactional
    public UserDTO createStudent(CreateStudentDTO dto) {
        // Check if registration is enabled
        checkRegistrationEnabled();

        CustomUserDetails currentUser = getCurrentUser();
        enforceRole(currentUser, UserRole.COORDINATOR);

        // Student belongs to coordinator's college
        College college = validateAndGetCollege(currentUser.getCollegeId());

        // Validate coordinator's scope if assigning student to an org unit
        if (dto.getOrganizationUnitId() != null) {
            validateCoordinatorScope(currentUser, dto.getOrganizationUnitId());
        }

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
     * Validates that the coordinator has authority to create users in the target
     * org unit.
     *
     * <p>
     * <strong>Scope Enforcement Rules:</strong>
     * </p>
     * <ul>
     * <li>If coordinator has no specific assignment → can create anywhere in
     * college</li>
     * <li>If coordinator has COLLEGE scope → can create anywhere in college</li>
     * <li>If coordinator has INSTITUTE scope → can create in their institute
     * subtree</li>
     * <li>If coordinator has DEPARTMENT scope → can only create in their
     * department</li>
     * </ul>
     *
     * @param coordinator     The current coordinator user details
     * @param targetOrgUnitId The org unit where the new user will be assigned
     * @throws AccessDeniedException if coordinator lacks scope for target org unit
     */
    @SuppressWarnings("null")
    private void validateCoordinatorScope(CustomUserDetails coordinator, Long targetOrgUnitId) {
        Long coordinatorOrgUnitId = coordinator.getOrgUnitId();

        // If coordinator has no specific org unit assignment, they have college-wide
        // scope
        if (coordinatorOrgUnitId == null) {
            log.debug("Coordinator {} has college-wide scope", coordinator.getUsername());
            return;
        }

        // If target is the same as coordinator's org unit, always allowed
        if (coordinatorOrgUnitId.equals(targetOrgUnitId)) {
            return;
        }

        // Get the coordinator's org unit to check scope level
        OrganizationUnit coordinatorOrgUnit = organizationUnitRepository.findById(coordinatorOrgUnitId)
                .orElseThrow(() -> new AccessDeniedException("Coordinator's organization unit not found"));

        // Get target org unit
        OrganizationUnit targetOrgUnit = organizationUnitRepository.findById(targetOrgUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Target organization unit not found"));

        // Verify both are in the same college
        if (!coordinatorOrgUnit.getCollege().getId().equals(targetOrgUnit.getCollege().getId())) {
            throw new AccessDeniedException("Cannot create users in a different college");
        }

        // Check if target is within coordinator's subtree
        boolean isInSubtree = isOrgUnitInSubtree(coordinatorOrgUnit, targetOrgUnit);

        if (!isInSubtree) {
            log.warn("Coordinator {} (orgUnit: {}) attempted to create user in orgUnit {} which is outside their scope",
                    coordinator.getUsername(), coordinatorOrgUnitId, targetOrgUnitId);
            throw new AccessDeniedException(
                    "You can only create users within your organizational scope. " +
                            "Target department is not in your hierarchy.");
        }

        log.debug("Coordinator {} scope validated for target org unit {}",
                coordinator.getUsername(), targetOrgUnitId);
    }

    /**
     * Checks if target org unit is within the subtree rooted at parent org unit.
     * Uses parent traversal: target is in subtree if walking up its parent chain
     * reaches the parent.
     *
     * @param parent The root of the subtree (coordinator's org unit)
     * @param target The org unit to check
     * @return true if target is in parent's subtree (or is parent itself)
     */
    private boolean isOrgUnitInSubtree(OrganizationUnit parent, OrganizationUnit target) {
        // Same unit - always in subtree
        if (parent.getId().equals(target.getId())) {
            return true;
        }

        // Walk up the parent chain from target
        OrganizationUnit current = target.getParent();
        int maxDepth = 10; // Prevent infinite loops in case of data corruption
        int depth = 0;

        while (current != null && depth < maxDepth) {
            if (current.getId().equals(parent.getId())) {
                return true; // Found parent in the chain
            }
            current = current.getParent();
            depth++;
        }

        return false; // Parent not found in chain
    }

    /**
     * Creates a new coordinator (TPO) user.
     * MUST be called by ADMIN.
     */
    @Transactional
    public UserDTO createCoordinator(CreateCoordinatorDTO dto) {
        // Check if registration is enabled
        checkRegistrationEnabled();

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

    /**
     * Updates the current user's profile.
     */
    @Transactional
    public UserDTO updateProfile(com.campusplacement.users.dto.ProfileUpdateDTO dto) {
        CustomUserDetails currentUserDetails = getCurrentUser();
        @SuppressWarnings("null")
        User user = userRepository.findById(currentUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());

        User savedUser = userRepository.save(user);
        log.info("User {} updated their profile", user.getEmail());

        return mapToDTO(savedUser);
    }

    /**
     * Changes the current user's password.
     */
    @Transactional
    public void changePassword(com.campusplacement.users.dto.ChangePasswordDTO dto) {
        CustomUserDetails currentUserDetails = getCurrentUser();
        @SuppressWarnings("null")
        User user = userRepository.findById(currentUserDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed their password", user.getEmail());
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
