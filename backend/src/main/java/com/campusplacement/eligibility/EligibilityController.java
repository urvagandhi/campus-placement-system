package com.campusplacement.eligibility;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.eligibility.dto.EligibilityResultDTO;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for eligibility operations.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    /**
     * Check eligibility for a student and drive.
     * Calls AI service for calculation.
     */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<EligibilityResultDTO>> checkEligibility(
            @RequestParam Long studentId,
            @RequestParam Long driveId) {
        EligibilityResultDTO result = eligibilityService.checkEligibility(studentId, driveId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get eligibility result for current student.
     */
    @GetMapping("/my/{driveId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EligibilityResultDTO>> getMyEligibility(
            @PathVariable Long driveId) {
        EligibilityResultDTO result = eligibilityService.getCurrentStudentEligibility(driveId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get all eligibility results for a drive.
     */
    @GetMapping("/drive/{driveId}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EligibilityResultDTO>>> getEligibilityByDrive(
            @PathVariable Long driveId) {
        List<EligibilityResultDTO> results = eligibilityService.getEligibilityResultsByDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Recalculate eligibility for all students for a drive.
     */
    @PostMapping("/recalculate/{driveId}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> recalculateEligibility(
            @PathVariable Long driveId) {
        eligibilityService.recalculateEligibilityForDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success("Eligibility recalculation initiated"));
    }
}
