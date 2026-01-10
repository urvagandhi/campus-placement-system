package com.campusplacement.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.auth.dto.SecurityStatsDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;

import lombok.RequiredArgsConstructor;

/**
 * Controller for security analytics and monitoring.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/security")
@RequiredArgsConstructor
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService securityAnalyticsService;

    /**
     * Gets security dashboard statistics.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SecurityStatsDTO>> getSecurityStats() {
        return ResponseEntity.ok(ApiResponse.success(securityAnalyticsService.getSecurityStats()));
    }

    /**
     * Exports audit logs as CSV.
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<byte[]> exportAuditLogs() {
        String csv = securityAnalyticsService.exportAuditLogsCsv();
        byte[] content = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=security_audit_log.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }
}
