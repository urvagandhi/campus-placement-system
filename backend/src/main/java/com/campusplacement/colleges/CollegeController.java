package com.campusplacement.colleges;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.colleges.dto.CollegeDTO;
import com.campusplacement.colleges.dto.UpdateCollegeDetailsDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.organizations.OrganizationScopeService;

import lombok.RequiredArgsConstructor;

/**
 * Controller for College related operations accessible by authorized users.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;
    private final OrganizationScopeService scopeService;

    /**
     * Get details of the current user's college.
     * Accessible by ADMIN and COORDINATOR.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<CollegeDTO>> getMyCollege() {
        Long collegeId = scopeService.getCurrentUserScope().collegeId();
        CollegeDTO college = collegeService.getCollegeById(collegeId);
        return ResponseEntity.ok(ApiResponse.success(college));
    }

    /**
     * Update details of the current user's college.
     * Only accessible by ADMIN.
     */
    @PutMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CollegeDTO>> updateMyCollege(@RequestBody UpdateCollegeDetailsDTO request) {
        Long collegeId = scopeService.getCurrentUserScope().collegeId();
        CollegeDTO updatedCollege = collegeService.updateCollegeDetails(collegeId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedCollege, "College details updated successfully"));
    }
}
