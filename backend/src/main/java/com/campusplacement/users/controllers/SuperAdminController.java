package com.campusplacement.users.controllers;

import java.lang.management.ManagementFactory;
import org.springframework.web.bind.annotation.GetMapping;

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
import com.campusplacement.users.UserRepository; // Direct repo access for simple count
import com.campusplacement.colleges.CollegeRepository; // Direct repo access for simple count
import com.campusplacement.users.dto.CreateAdminDTO;
import com.campusplacement.users.dto.SystemStatsDTO;
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
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final com.campusplacement.settings.SystemSettingsService settingsService;
    private final com.campusplacement.auth.RefreshTokenRepository refreshTokenRepository;

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

    /**
     * Get system-wide statistics.
     * Only accessible by SUPER_ADMIN.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SystemStatsDTO>> getSystemStats() {
        long totalColleges = collegeRepository.count();
        long totalUsers = userRepository.count();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime(); // in milliseconds

        long activeSessions = refreshTokenRepository.countAllActiveSessions();

        SystemStatsDTO stats = SystemStatsDTO.builder()
                .totalColleges(totalColleges)
                .totalUsers(totalUsers)
                .uptimeSeconds(uptime / 1000)
                .activeSessions(activeSessions)
                .maintenanceMode(settingsService.isMaintenanceMode())
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get all colleges.
     * Only accessible by SUPER_ADMIN.
     */
    @GetMapping("/colleges")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<CollegeDTO>>> getAllColleges() {
        java.util.List<CollegeDTO> colleges = collegeService.getAllColleges();
        return ResponseEntity.ok(ApiResponse.success(colleges));
    }

    /**
     * Get current system settings.
     * Only accessible by SUPER_ADMIN.
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.campusplacement.settings.SystemSettingsDTO>> getSettings() {
        com.campusplacement.settings.SystemSettingsDTO settings = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * Update system settings.
     * Only accessible by SUPER_ADMIN.
     */
    @org.springframework.web.bind.annotation.PutMapping("/settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.campusplacement.settings.SystemSettingsDTO>> updateSettings(
            @RequestBody com.campusplacement.settings.SystemSettingsDTO request) {
        com.campusplacement.settings.SystemSettingsDTO settings = settingsService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success(settings, "Settings updated successfully"));
    }

    /**
     * Update college active status (Deactivate/Activate).
     * Only accessible by SUPER_ADMIN.
     */
    @org.springframework.web.bind.annotation.PatchMapping("/colleges/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CollegeDTO>> updateCollegeStatus(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam boolean active) {
        CollegeDTO college = collegeService.updateCollegeStatus(id, active);
        String action = active ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success(college, "College " + action + " successfully"));
    }

    /**
     * Delete a college.
     * Only accessible by SUPER_ADMIN.
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/colleges/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCollege(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.ok(ApiResponse.success(null, "College deleted successfully"));
    }
}
