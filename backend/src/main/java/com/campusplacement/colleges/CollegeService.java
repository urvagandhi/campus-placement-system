package com.campusplacement.colleges;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.colleges.dto.CollegeDTO;
import com.campusplacement.colleges.dto.CreateCollegeDTO;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing colleges.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    // TODO: Re-enable notification service after fixing notification system
    // private final com.campusplacement.notifications.NotificationService
    // notificationService;

    @SuppressWarnings("null")
    @Transactional
    public CollegeDTO createCollege(CreateCollegeDTO dto) {
        if (collegeRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("College code already exists");
        }

        College college = College.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .address(dto.getAddress())
                .website(dto.getWebsite())
                .contactEmail(dto.getContactEmail())
                .adminName(dto.getAdminName())
                .contactPhone(dto.getContactPhone())
                .isActive(true)
                .build();

        College savedCollege = collegeRepository.save(college);
        log.info("College created: {} ({})", savedCollege.getName(), savedCollege.getCode());

        // Notify System
        // notificationService.createSystemNotification(
        // "INFO",
        // "New College Registered",
        // "A new college '" + savedCollege.getName() + "' (" + savedCollege.getCode()
        // + ") has been registered on the platform.",
        // null);

        return mapToDTO(savedCollege);
    }

    private CollegeDTO mapToDTO(College college) {
        // Dynamically fetch the admin user's name instead of using static field
        String adminName = resolveAdminName(college);

        return CollegeDTO.builder()
                .id(college.getId())
                .name(college.getName())
                .code(college.getCode())
                .address(college.getAddress())
                .website(college.getWebsite())
                .contactEmail(college.getContactEmail())
                .adminName(adminName)
                .contactPhone(college.getContactPhone())
                .isActive(college.getIsActive())
                .build();
    }

    /**
     * Resolves the admin name for a college by finding the ADMIN user assigned
     * to the ROOT organization unit (university level).
     * Falls back to the static adminName field if no primary admin is found.
     *
     * @param college The college to resolve admin name for
     * @return The primary admin user's name, or the static field value, or null
     */
    private String resolveAdminName(College college) {
        if (college == null || college.getId() == null) {
            return null;
        }

        // Find the admin assigned to the ROOT organization unit (university level)
        return userRepository.findPrimaryAdminByCollegeId(college.getId())
                .map(User::getName)
                .orElseGet(college::getAdminName); // Fallback to static field
    }

    /**
     * Get all colleges (for Super Admin).
     */
    public java.util.List<CollegeDTO> getAllColleges() {
        return collegeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Update the active status of a college.
     *
     * @param id     College ID
     * @param active New active status
     * @return Updated CollegeDTO
     */
    @SuppressWarnings("null")
    @Transactional
    public CollegeDTO updateCollegeStatus(Long id, boolean active) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new com.campusplacement.common.exception.ResourceNotFoundException(
                        "College not found with id: " + id));

        college.setIsActive(active);
        College savedCollege = collegeRepository.save(college);
        log.info("College status updated: {} ({}) -> isActive: {}", savedCollege.getName(), savedCollege.getCode(),
                active);

        // Notify System
        // notificationService.createSystemNotification(
        // "WARNING",
        // "College Status Updated",
        // "College '" + savedCollege.getName() + "' has been " + (active ?
        // "activated" : "deactivated") + ".",
        // null);

        return mapToDTO(savedCollege);
    }

    /**
     * Delete a college.
     *
     * @param id College ID
     */
    @SuppressWarnings("null")
    @Transactional
    public void deleteCollege(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new com.campusplacement.common.exception.ResourceNotFoundException(
                        "College not found with id: " + id));

        collegeRepository.delete(college);
        log.info("College deleted: {} ({})", college.getName(), college.getCode());

        // Notify System
        // notificationService.createSystemNotification(
        // "SECURITY",
        // "College Deleted",
        // "College '" + college.getName() + "' (" + college.getCode() + ") has been
        // deleted from the platform.",
        // null);
    }
}
