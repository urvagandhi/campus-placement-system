package com.campusplacement.eligibility;

import java.util.List;

import com.campusplacement.eligibility.dto.EligibilityResultDTO;

/**
 * Service interface for eligibility operations.
 * Integrates with AI service for score calculation.
 */
public interface EligibilityService {

    EligibilityResultDTO checkEligibility(Long studentId, Long driveId);

    EligibilityResultDTO getCurrentStudentEligibility(Long driveId);

    List<EligibilityResultDTO> getEligibilityResultsByDrive(Long driveId);

    void recalculateEligibilityForDrive(Long driveId);
}
