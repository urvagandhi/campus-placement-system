package com.campusplacement.colleges.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new College.
 * Includes admin account details for auto-creation during onboarding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollegeDTO {

    @NotBlank(message = "College Name is required")
    @Size(min = 2, max = 100, message = "College Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "College Code is required")
    @Size(min = 2, max = 20, message = "College Code must be between 2 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-\\s]+$", message = "College Code can contain letters, numbers, spaces and hyphens")
    private String code;

    private String address;
    private String website;

    /**
     * Institutional contact email (for general inquiries, not login).
     */
    private String contactEmail;

    @jakarta.validation.constraints.Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format (e.g., +919876543210)")
    private String contactPhone;

    // ==================== Admin Account Fields ====================

    /**
     * Full name of the college admin.
     */
    @NotBlank(message = "Admin Name is required")
    private String adminName;

    /**
     * Admin login email (REQUIRED for account creation).
     */
    @NotBlank(message = "Admin Email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    /**
     * Admin phone number (optional).
     */
    @jakarta.validation.constraints.Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Admin phone must be in E.164 format")
    private String adminPhone;
}
