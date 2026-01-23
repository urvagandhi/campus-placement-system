package com.campusplacement.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for system settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {

    private Boolean registrationEnabled;
    private Boolean maintenanceMode;
    private String appVersion;
}
