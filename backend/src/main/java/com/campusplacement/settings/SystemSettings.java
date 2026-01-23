package com.campusplacement.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for system-wide settings.
 * Uses a single-row pattern with a fixed ID.
 */
@Entity
@Table(name = "system_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    @Id
    @Column(name = "id")
    @lombok.Builder.Default
    private Long id = 1L; // Single row pattern

    @Column(name = "registration_enabled")
    @lombok.Builder.Default
    private Boolean registrationEnabled = true;

    @Column(name = "maintenance_mode")
    @lombok.Builder.Default
    private Boolean maintenanceMode = false;

    @Column(name = "app_version")
    @lombok.Builder.Default
    private String appVersion = "1.0.0";
}
