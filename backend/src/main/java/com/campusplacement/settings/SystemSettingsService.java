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
    // TODO: Re-enable notification service after fixing notification system
    // private final com.campusplacement.notifications.NotificationService
    // notificationService;

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
                // Notify System
                // notificationService.createSystemNotification(
                // "SYSTEM",
                // "Maintenance Mode Enabled",
                // "System is now in maintenance mode. Access may be restricted.",
                // null);
            } else if (wasInMaintenance && !dto.getMaintenanceMode()) {
                log.info("SYSTEM: Maintenance mode DISABLED by super admin");
                // Notify System
                // notificationService.createSystemNotification(
                // "SYSTEM",
                // "Maintenance Mode Disabled",
                // "System maintenance mode has been disabled. The platform is now fully
                // accessible.",
                // null);
            }
        }

        if (dto.getMaintenanceMode() != null && dto.getMaintenanceMode() && !settings.getMaintenanceMode()) { // Check
                                                                                                              // correctly
                                                                                                              // if was
                                                                                                              // disabled
            // Just handled in logic above, wait, logic above is:
            // wasInMaintenance (from DB) -> set new value
            // logic: if (!wasInMaintenance && dto.getMaintenanceMode()) -> ENABLED
            // if (wasInMaintenance && !dto.getMaintenanceMode()) -> DISABLED
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
