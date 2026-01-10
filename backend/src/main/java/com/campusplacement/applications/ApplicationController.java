package com.campusplacement.applications;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.applications.dto.ApplicationDTO;
import com.campusplacement.applications.dto.ApplyRequestDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for application management.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Get all applications (scope-enforced).
     *
     * <p>
     * <strong>Scope Enforcement:</strong>
     * <ul>
     * <li>SUPER_ADMIN: All applications across all colleges</li>
     * <li>ADMIN: All applications in their college</li>
     * <li>COORDINATOR: Only applications from students in their scope</li>
     * </ul>
     * </p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getAllApplications() {
        List<ApplicationDTO> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getMyApplications() {
        List<ApplicationDTO> applications = applicationService.getCurrentStudentApplications();
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    /**
     * Get applications for a specific drive (scope-enforced).
     *
     * <p>
     * <strong>Defense-in-Depth:</strong> Method-level check validates drive access,
     * service layer filters by allowed departments.
     * </p>
     */
    @GetMapping("/drive/{driveId}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApplicationDTO>>> getApplicationsByDrive(
            @PathVariable Long driveId) {
        List<ApplicationDTO> applications = applicationService.getApplicationsByDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ApplicationDTO>> applyToDrive(
            @Valid @RequestBody ApplyRequestDTO request) {
        ApplicationDTO application = applicationService.applyToDrive(request);
        return ResponseEntity.ok(ApiResponse.success(application, "Applied successfully"));
    }

    /**
     * Update application status.
     *
     * <p>
     * <strong>Security:</strong> Only COORDINATOR and ADMIN can update application
     * status.
     * Service layer validates scope access to the student's department.
     * </p>
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ApplicationDTO updated = applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Status updated"));
    }

    @DeleteMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(@PathVariable Long id) {
        applicationService.withdrawApplication(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Application withdrawn"));
    }
}
