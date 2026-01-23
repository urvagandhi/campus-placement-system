package com.campusplacement.common;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.campusplacement.drives.DriveRepository;
import com.campusplacement.users.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Health indicator for core application modules.
 * Verifies that key database tables are accessible.
 */
@Component
@RequiredArgsConstructor
public class ModuleHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;
    private final DriveRepository driveRepository;

    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            long driveCount = driveRepository.count();

            return Health.up()
                    .withDetail("Users Module", "UP (Count: " + userCount + ")")
                    .withDetail("Drives Module", "UP (Count: " + driveCount + ")")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("Module Status", "Database Access Failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
