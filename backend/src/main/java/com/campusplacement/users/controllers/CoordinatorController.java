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
import com.campusplacement.users.dto.CreateStudentDTO;
import com.campusplacement.users.dto.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for Coordinator-managed operations.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {

    private final UserManagementService userManagementService;

    /**
     * Creates a new student.
     * Only accessible by COORDINATOR.
     */
    @PostMapping("/students")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApiResponse<UserDTO>> createStudent(
            @Valid @RequestBody CreateStudentDTO request) {
        UserDTO user = userManagementService.createStudent(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Student created successfully"));
    }
}
