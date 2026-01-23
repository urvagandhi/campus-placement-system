package com.campusplacement.users.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.users.UserManagementService;
import com.campusplacement.users.dto.ChangePasswordDTO;
import com.campusplacement.users.dto.ProfileUpdateDTO;
import com.campusplacement.users.dto.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for user profile operations.
 * Accessible by all authenticated users.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserManagementService userManagementService;

    @PutMapping("/details")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody ProfileUpdateDTO request) {
        UserDTO user = userManagementService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        userManagementService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
