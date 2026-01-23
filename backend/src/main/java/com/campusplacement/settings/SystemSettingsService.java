package com.campusplacement.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing system settings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsService {

    private static final Long SETTINGS_ID = 1L;
    private final SystemSettingsRepository repository;

    /**
     * Get current system settings.
     * Creates default settings if none exist.
     */
    /**
     * Get current system settings.
     * Creates default settings if none exist.
     */
    @Transactional
    public SystemSettingsDTO getSettings() {
        @SuppressWarnings("null")
        SystemSettings settings = repository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        return mapToDTO(settings);
    }

    /**
     * Check if maintenance mode is enabled.
     * Efficient check for filters/security.
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public boolean isMaintenanceMode() {
        return repository.findById(SETTINGS_ID)
                .map(settings -> Boolean.TRUE.equals(settings.getMaintenanceMode()))
                .orElse(false);
    }

    /**
     * Check if user registration is enabled.
     * Efficient check for user creation operations.
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public boolean isRegistrationEnabled() {
        return repository.findById(SETTINGS_ID)
                .map(settings -> Boolean.TRUE.equals(settings.getRegistrationEnabled()))
                .orElse(true); // Default to enabled if no settings exist
    }

    /**
     * Update system settings.
     */
    @Transactional
    public SystemSettingsDTO updateSettings(SystemSettingsDTO dto) {
        @SuppressWarnings("null")
        SystemSettings settings = repository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);

        if (dto.getRegistrationEnabled() != null) {
            settings.setRegistrationEnabled(dto.getRegistrationEnabled());
        }
        if (dto.getMaintenanceMode() != null) {
            boolean wasInMaintenance = settings.getMaintenanceMode();
            settings.setMaintenanceMode(dto.getMaintenanceMode());

            if (!wasInMaintenance && dto.getMaintenanceMode()) {
                log.warn("SYSTEM: Maintenance mode ENABLED by super admin");
            } else if (wasInMaintenance && !dto.getMaintenanceMode()) {
                log.info("SYSTEM: Maintenance mode DISABLED by super admin");
            }
        }

        @SuppressWarnings("null")
        SystemSettings saved = repository.saveAndFlush(settings);
        log.info("System settings updated: registration={}, maintenance={}",
                saved.getRegistrationEnabled(), saved.getMaintenanceMode());
        return mapToDTO(saved);
    }

    @SuppressWarnings("null")
    private SystemSettings createDefaultSettings() {
        SystemSettings defaults = SystemSettings.builder()
                .id(SETTINGS_ID)
                .registrationEnabled(true)
                .maintenanceMode(false)
                .appVersion("1.0.0")
                .build();
        return repository.saveAndFlush(defaults);
    }

    private SystemSettingsDTO mapToDTO(SystemSettings settings) {
        return SystemSettingsDTO.builder()
                .registrationEnabled(settings.getRegistrationEnabled())
                .maintenanceMode(settings.getMaintenanceMode())
                .appVersion(settings.getAppVersion())
                .build();
    }
}
