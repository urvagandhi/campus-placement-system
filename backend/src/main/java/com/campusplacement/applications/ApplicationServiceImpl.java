package com.campusplacement.applications;

import java.util.List;

import org.springframework.stereotype.Service;

import com.campusplacement.applications.dto.ApplicationDTO;
import com.campusplacement.applications.dto.ApplyRequestDTO;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of ApplicationService.
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    public List<ApplicationDTO> getAllApplications() {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ApplicationDTO> getCurrentStudentApplications() {
        // TODO: Get current user, find their applications
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ApplicationDTO> getApplicationsByDrive(Long driveId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ApplicationDTO applyToDrive(ApplyRequestDTO request) {
        // TODO: Validate eligibility before applying
        // 1. Check if already applied
        // 2. Check if eligible (call eligibility service)
        // 3. Create application
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ApplicationDTO updateApplicationStatus(Long id, String status) {
        // TODO: Validate status transition
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void withdrawApplication(Long id) {
        // TODO: Verify ownership and withdraw
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
