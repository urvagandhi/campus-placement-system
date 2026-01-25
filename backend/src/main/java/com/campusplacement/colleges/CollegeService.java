package com.campusplacement.colleges;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.colleges.dto.CollegeDTO;
import com.campusplacement.colleges.dto.CollegeOnboardingResponse;
import com.campusplacement.colleges.dto.CreateCollegeDTO;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.ScopeLevel;
import com.campusplacement.common.UserRole;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.model.UserAssignment;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;
import com.campusplacement.organizations.repository.UserAssignmentRepository;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing colleges with full onboarding support.
 *
 * <p>
 * When a college is created, this service automatically:
 * <ul>
 * <li>Creates the College record</li>
 * <li>Creates a root Organization Unit (UNIVERSITY type)</li>
 * <li>Creates an ADMIN user account with temporary password</li>
 * <li>Assigns the admin to the root organization unit</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final OrganizationUnitRepository orgUnitRepository;
    private final UserAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    // Character sets for password generation - must include all for policy
    // compliance
    private static final String UPPERCASE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghjkmnpqrstuvwxyz";
    private static final String DIGIT_CHARS = "23456789";
    private static final String SPECIAL_CHARS = "!@#$%&*";
    private static final String ALL_CHARS = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SPECIAL_CHARS;
    private static final int TEMP_PASSWORD_LENGTH = 12;

    // ==================== College Onboarding ====================

    /**
     * Creates a new college with full onboarding.
     * <p>
     * This method creates:
     * <ol>
     * <li>College record</li>
     * <li>Root Organization Unit (UNIVERSITY type)</li>
     * <li>ADMIN user account with temporary password</li>
     * <li>User assignment linking admin to root org unit</li>
     * </ol>
     *
     * @param dto College creation details including admin info
     * @return Onboarding response with credentials
     */
    @Transactional
    public CollegeOnboardingResponse createCollege(CreateCollegeDTO dto) {
        // 1. Validate college code is unique
        if (collegeRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("College code already exists");
        }

        // 2. Validate admin email is unique
        String adminEmail = dto.getAdminEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(adminEmail)) {
            throw new IllegalArgumentException("Admin email already exists: " + adminEmail);
        }

        // 3. Create College
        College college = createCollegeEntity(dto);
        log.info("College created: {} ({})", college.getName(), college.getCode());

        // 4. Create Root Organization Unit (UNIVERSITY)
        OrganizationUnit rootUnit = createRootOrganizationUnit(college, dto);
        log.info("Root organization unit created: {} for college {}", rootUnit.getName(), college.getCode());

        // 5. Generate temporary password
        String tempPassword = generateTemporaryPassword();

        // 6. Create ADMIN user
        User admin = createAdminUser(dto, college, adminEmail, tempPassword);
        log.info("Admin user created: {} for college {}", admin.getEmail(), college.getCode());

        // 7. Assign admin to root org unit
        createAdminAssignment(admin, rootUnit);
        log.info("Admin assigned to root org unit for college {}", college.getCode());

        // 8. Return response with credentials
        return CollegeOnboardingResponse.builder()
                .college(mapToDTO(college))
                .adminEmail(admin.getEmail())
                .temporaryPassword(tempPassword)
                .message("College registered successfully. Admin account created.")
                .build();
    }

    /**
     * Creates the college entity.
     */
    @SuppressWarnings("null")
    private College createCollegeEntity(CreateCollegeDTO dto) {
        College college = College.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase().trim())
                .address(dto.getAddress())
                .website(dto.getWebsite())
                .contactEmail(dto.getContactEmail())
                .adminName(dto.getAdminName())
                .contactPhone(dto.getContactPhone())
                .isActive(true)
                .build();

        return collegeRepository.save(college);
    }

    /**
     * Creates the root organization unit (UNIVERSITY type).
     */
    @SuppressWarnings("null")
    private OrganizationUnit createRootOrganizationUnit(College college, CreateCollegeDTO dto) {
        // Extract email domain from admin email for the org unit
        String adminEmail = dto.getAdminEmail().toLowerCase().trim();
        String emailDomain = adminEmail.substring(adminEmail.indexOf('@') + 1);

        OrganizationUnit rootUnit = OrganizationUnit.builder()
                .college(college)
                .name(dto.getName())
                .code(dto.getCode().toUpperCase().trim())
                .type(OrganizationUnitType.UNIVERSITY)
                .parent(null) // Root has no parent
                .emailDomain(emailDomain)
                .isRoot(true)
                .isActive(true)
                .build();

        return orgUnitRepository.save(rootUnit);
    }

    /**
     * Generates a secure random temporary password that meets password policy.
     * Guarantees at least one uppercase, one lowercase, one digit, and one special
     * character.
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

        // Guarantee at least one of each required character type
        password.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        password.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        password.append(DIGIT_CHARS.charAt(random.nextInt(DIGIT_CHARS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill remaining length with random characters from all sets
        for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the password to avoid predictable pattern
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * Creates the admin user with mustChangePassword=true.
     */
    @SuppressWarnings("null")
    private User createAdminUser(CreateCollegeDTO dto, College college, String email, String tempPassword) {
        User admin = User.builder()
                .name(dto.getAdminName())
                .email(email)
                .username(email) // Use email as username
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(UserRole.ADMIN)
                .college(college)
                .phoneNumber(dto.getAdminPhone())
                .isActive(true)
                .mustChangePassword(true) // Force password change on first login
                .build();

        return userRepository.save(admin);
    }

    /**
     * Creates admin assignment to root organization unit.
     */
    @SuppressWarnings("null")
    private UserAssignment createAdminAssignment(User admin, OrganizationUnit rootUnit) {
        UserAssignment assignment = UserAssignment.builder()
                .user(admin)
                .organizationUnit(rootUnit)
                .designation("University Administrator")
                .scopeLevel(ScopeLevel.SUBTREE) // Admin can manage entire university
                .isPrimary(true)
                .build();

        return assignmentRepository.save(assignment);
    }

    // ==================== DTO Mapping ====================

    private CollegeDTO mapToDTO(College college) {
        // Dynamically fetch the admin user's details from the User table
        AdminDetails adminDetails = resolveAdminDetails(college);

        return CollegeDTO.builder()
                .id(college.getId())
                .name(college.getName())
                .code(college.getCode())
                .address(college.getAddress())
                .website(college.getWebsite())
                .contactEmail(college.getContactEmail())
                .contactPhone(college.getContactPhone())
                .adminName(adminDetails.name())
                .adminEmail(adminDetails.email())
                .adminPhone(adminDetails.phone())
                .isActive(college.getIsActive())
                .build();
    }

    /**
     * Record to hold admin details fetched from User table
     */
    private record AdminDetails(String name, String email, String phone) {
        static AdminDetails empty() {
            return new AdminDetails(null, null, null);
        }

        static AdminDetails fromUser(User user) {
            return new AdminDetails(
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber());
        }
    }

    /**
     * Resolves the admin details for a college by finding the ADMIN user.
     * Prioritizes admin assigned to ROOT org unit, falls back to any ADMIN.
     */
    private AdminDetails resolveAdminDetails(College college) {
        if (college == null || college.getId() == null) {
            return AdminDetails.empty();
        }

        // Get ordered list of admins (root org unit first)
        List<User> admins = userRepository.findAdminsByCollegeIdOrdered(college.getId());

        // If we found admins, return first one (highest priority)
        if (!admins.isEmpty()) {
            return AdminDetails.fromUser(admins.get(0));
        }

        // Fallback to static field if no admin user found
        return new AdminDetails(
                college.getAdminName(),
                null,
                null);
    }

    // ==================== Read Operations ====================

    /**
     * Get college by ID.
     */
    @SuppressWarnings("null")
    public CollegeDTO getCollegeById(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));
        return mapToDTO(college);
    }

    /**
     * Get all colleges (for Super Admin).
     */
    public List<CollegeDTO> getAllColleges() {
        return collegeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Status Management ====================

    /**
     * Update college details (for Admin).
     */
    @SuppressWarnings("null")
    @Transactional
    public CollegeDTO updateCollegeDetails(Long id, com.campusplacement.colleges.dto.UpdateCollegeDetailsDTO dto) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        if (dto.getName() != null)
            college.setName(dto.getName());
        if (dto.getAddress() != null)
            college.setAddress(dto.getAddress());
        if (dto.getWebsite() != null)
            college.setWebsite(dto.getWebsite());
        if (dto.getContactPhone() != null)
            college.setContactPhone(dto.getContactPhone());

        College savedCollege = collegeRepository.save(college);
        return mapToDTO(savedCollege);
    }

    /**
     * Update the active status of a college.
     * When deactivated, users from this college cannot log in.
     */
    @SuppressWarnings("null")
    @Transactional
    public CollegeDTO updateCollegeStatus(Long id, boolean active) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        college.setIsActive(active);
        College savedCollege = collegeRepository.save(college);

        log.info("College status updated: {} ({}) -> isActive: {}",
                savedCollege.getName(), savedCollege.getCode(), active);

        return mapToDTO(savedCollege);
    }

    // ==================== Deletion ====================

    /**
     * Soft-deletes a college and all its users.
     * <p>
     * This method:
     * <ol>
     * <li>Soft-deletes all users in the college</li>
     * <li>Deactivates the college</li>
     * </ol>
     */
    @SuppressWarnings("null")
    @Transactional
    public void deleteCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        // 1. Soft-delete all users in this college
        List<User> users = userRepository.findByCollegeId(id);
        users.forEach(user -> {
            user.softDelete(); // Sets deleted_at timestamp
            user.setIsActive(false);
        });
        userRepository.saveAll(users);

        // 2. Perform soft-delete of the college
        college.softDelete(); // Sets deleted_at and isActive=false
        collegeRepository.save(college);

        log.info("College {} and {} users soft-deleted", college.getCode(), users.size());
    }
}
