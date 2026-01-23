package com.campusplacement.colleges;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.colleges.dto.CollegeDTO;
import com.campusplacement.colleges.dto.CreateCollegeDTO;

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
                .isActive(true)
                .build();

        College savedCollege = collegeRepository.save(college);
        log.info("College created: {} ({})", savedCollege.getName(), savedCollege.getCode());

        return mapToDTO(savedCollege);
    }

    private CollegeDTO mapToDTO(College college) {
        return CollegeDTO.builder()
                .id(college.getId())
                .name(college.getName())
                .code(college.getCode())
                .address(college.getAddress())
                .website(college.getWebsite())
                .contactEmail(college.getContactEmail())
                .isActive(college.getIsActive())
                .build();
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
    }
}
