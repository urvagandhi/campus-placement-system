package com.campusplacement.settings;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Custom Health Indicator to reflect Maintenance Mode in /actuator/health.
 * 
 * If System is in Maintenance Mode -> Status: OUT_OF_SERVICE
 * If System is Operational -> Status: UP
 */
@Component
@RequiredArgsConstructor
public class MaintenanceHealthIndicator implements HealthIndicator {

    private final SystemSettingsService systemSettingsService;

    @Override
    public Health health() {
        if (systemSettingsService.isMaintenanceMode()) {
            return Health.outOfService()
                    .withDetail("message", "System is under maintenance")
                    .build();
        }
        return Health.up()
                .withDetail("message", "System is fully operational")
                .build();
    }
}
