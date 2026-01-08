package com.campusplacement.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.users.UserManagementService;
import com.campusplacement.users.dto.CreateCoordinatorDTO;
import com.campusplacement.users.dto.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for Admin-managed operations.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagementService userManagementService;

    /**
     * Creates a new Coordinator (TPO).
     * Only accessible by ADMIN.
     */
    @PostMapping("/coordinators")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createCoordinator(
            @Valid @RequestBody CreateCoordinatorDTO request) {
        UserDTO user = userManagementService.createCoordinator(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Coordinator created successfully"));
    }
}
