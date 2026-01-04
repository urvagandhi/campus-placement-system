package com.campusplacement.eligibility;

import java.util.List;

import org.springframework.stereotype.Service;

import com.campusplacement.ai.AIClient;
import com.campusplacement.eligibility.dto.EligibilityResultDTO;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of EligibilityService.
 *
 * <p>
 * Integrates with the Python AI service for eligibility scoring.
 * The AI module acts strictly as a decision-support system.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityRepository eligibilityRepository;
    private final AIClient aiClient;

    @Override
    public EligibilityResultDTO checkEligibility(Long studentId, Long driveId) {
        // TODO: Implement
        // 1. Get student profile
        // 2. Get drive requirements
        // 3. Call AI service for scoring
        // 4. Save and return result
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EligibilityResultDTO getCurrentStudentEligibility(Long driveId) {
        // TODO: Get current user's eligibility for drive
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<EligibilityResultDTO> getEligibilityResultsByDrive(Long driveId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void recalculateEligibilityForDrive(Long driveId) {
        // TODO: Batch recalculate for all students
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
