package com.campusplacement.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.colleges.CollegeService;
import com.campusplacement.colleges.dto.CollegeDTO;
import com.campusplacement.colleges.dto.CreateCollegeDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.users.UserManagementService;
import com.campusplacement.users.dto.CreateAdminDTO;
import com.campusplacement.users.dto.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for Super Admin operations.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserManagementService userManagementService;
    private final CollegeService collegeService;

    /**
     * Creates a new College Admin.
     * Only accessible by SUPER_ADMIN.
     */
    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createAdmin(
            @Valid @RequestBody CreateAdminDTO request) {
        UserDTO user = userManagementService.createAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Admin created successfully"));
    }

    /**
     * Creates a new College.
     * Only accessible by SUPER_ADMIN.
     */
    @PostMapping("/colleges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeDTO>> createCollege(
            @Valid @RequestBody CreateCollegeDTO request) {
        CollegeDTO college = collegeService.createCollege(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(college, "College created successfully"));
    }
}
