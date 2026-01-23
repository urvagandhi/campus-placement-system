package com.campusplacement.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.campusplacement.common.Constants;

/**
 * Controller for managing system-wide settings.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/settings")
@RequiredArgsConstructor
@Tag(name = "System Settings", description = "Manage system-wide configurations")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get system settings", description = "Retrieve current system settings including maintenance mode status")
    public ResponseEntity<com.campusplacement.common.ApiResponse<SystemSettingsDTO>> getSettings() {
        return ResponseEntity.ok(com.campusplacement.common.ApiResponse.success(settingsService.getSettings()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update system settings", description = "Update system settings. Only SUPER_ADMIN can perform this action.")
    public ResponseEntity<com.campusplacement.common.ApiResponse<SystemSettingsDTO>> updateSettings(
            @RequestBody SystemSettingsDTO settingsDTO) {
        return ResponseEntity
                .ok(com.campusplacement.common.ApiResponse.success(settingsService.updateSettings(settingsDTO)));
    }
}
