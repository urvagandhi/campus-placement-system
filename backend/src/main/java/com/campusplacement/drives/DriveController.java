package com.campusplacement.drives;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.drives.dto.CreateDriveRequestDTO;
import com.campusplacement.drives.dto.DriveDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for placement drive management.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/drives")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DriveDTO>>> getAllDrives() {
        List<DriveDTO> drives = driveService.getAllDrives();
        return ResponseEntity.ok(ApiResponse.success(drives));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriveDTO>> getDriveById(@PathVariable Long id) {
        DriveDTO drive = driveService.getDriveById(id);
        return ResponseEntity.ok(ApiResponse.success(drive));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<DriveDTO>>> getUpcomingDrives() {
        List<DriveDTO> drives = driveService.getUpcomingDrives();
        return ResponseEntity.ok(ApiResponse.success(drives));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<DriveDTO>>> getDrivesByCompany(
            @PathVariable Long companyId) {
        List<DriveDTO> drives = driveService.getDrivesByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success(drives));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DriveDTO>> createDrive(
            @Valid @RequestBody CreateDriveRequestDTO request) {
        DriveDTO created = driveService.createDrive(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Drive created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DriveDTO>> updateDrive(
            @PathVariable Long id,
            @Valid @RequestBody DriveDTO driveDTO) {
        DriveDTO updated = driveService.updateDrive(id, driveDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Drive updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN')")
    public ResponseEntity<ApiResponse<DriveDTO>> updateDriveStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        DriveDTO updated = driveService.updateDriveStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Drive deleted successfully"));
    }
}
