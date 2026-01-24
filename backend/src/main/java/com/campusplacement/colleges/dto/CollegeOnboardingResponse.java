package com.campusplacement.colleges.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for college onboarding.
 * Contains college details and admin credentials (returned only on creation).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeOnboardingResponse {

    /**
     * Created college details.
     */
    private CollegeDTO college;

    /**
     * Admin login email.
     */
    private String adminEmail;

    /**
     * Temporary password (only returned on creation, not stored in plain text).
     * Super Admin should share this with the college admin.
     */
    private String temporaryPassword;

    /**
     * Success/status message.
     */
    private String message;
}
