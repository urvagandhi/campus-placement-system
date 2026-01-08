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
}
