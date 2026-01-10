package com.campusplacement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

/**
 * Seeds test data for development and E2E testing.
 *
 * <p>
 * Only runs when the "dev" or "test" profile is active.
 * </p>
 *
 * <p>
 * Test users created:
 * </p>
 * <ul>
 * <li>student@test.edu / password123 (STUDENT)</li>
 * <li>coordinator@test.edu / password123 (COORDINATOR)</li>
 * <li>admin@test.edu / password123 (ADMIN)</li>
 * <li>superadmin@platform.com / password123 (SUPER_ADMIN)</li>
 * </ul>
 */
@Configuration
@Profile({ "dev", "test" })
public class TestDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(TestDataSeeder.class);
    private static final String TEST_PASSWORD = "password123";

    /**
     * DISABLED: Seeding is now handled by seed-data-v2.sql via Spring SQL init.
     * The SQL file contains comprehensive multi-college hierarchy data.
     *
     * To re-enable for E2E tests only, uncomment the @Bean annotation.
     */
    @SuppressWarnings("null")
    // @Bean // DISABLED - Using seed-data-v2.sql instead
    CommandLineRunner seedTestData(
            CollegeRepository collegeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Checking if test data needs to be seeded...");

            // Check if seed data from seed-data-v2.sql already exists
            // If superadmin exists, the SQL seed data has already been loaded
            if (userRepository.findByEmail("superadmin@placementpro.com").isPresent()) {
                log.info("Seed data already exists (found superadmin@placementpro.com), skipping TestDataSeeder.");
                return;
            }

            log.info("Seeding test data for E2E tests...");

            // Create test college
            College testCollege = collegeRepository.findByCode("TU001")
                    .orElseGet(() -> {
                        College college = College.builder()
                                .name("Test University")
                                .code("TU001")
                                .isActive(true)
                                .build();
                        return collegeRepository.save(college);
                    });

            String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);

            // Create Student
            createUserIfNotExists(userRepository, User.builder()
                    .name("Test Student")
                    .email("student@test.edu")
                    .passwordHash(encodedPassword)
                    .role(UserRole.STUDENT)
                    .college(testCollege)
                    .isActive(true)
                    .build());

            // Create Coordinator
            createUserIfNotExists(userRepository, User.builder()
                    .name("Test Coordinator")
                    .email("coordinator@test.edu")
                    .passwordHash(encodedPassword)
                    .role(UserRole.COORDINATOR)
                    .college(testCollege)
                    .isActive(true)
                    .build());

            // Create Admin
            createUserIfNotExists(userRepository, User.builder()
                    .name("Test Admin")
                    .email("admin@test.edu")
                    .passwordHash(encodedPassword)
                    .role(UserRole.ADMIN)
                    .college(testCollege)
                    .isActive(true)
                    .build());

            // Create Super Admin (no college)
            createUserIfNotExists(userRepository, User.builder()
                    .name("Test Super Admin")
                    .email("superadmin@platform.com")
                    .passwordHash(encodedPassword)
                    .role(UserRole.SUPER_ADMIN)
                    .college(null)
                    .isActive(true)
                    .build());

            log.info("Test data seeded successfully!");
            log.info("Test users created:");
            log.info("  - student@test.edu / password123 (STUDENT)");
            log.info("  - coordinator@test.edu / password123 (COORDINATOR)");
            log.info("  - admin@test.edu / password123 (ADMIN)");
            log.info("  - superadmin@platform.com / password123 (SUPER_ADMIN)");
        };
    }

    private void createUserIfNotExists(UserRepository userRepository, User user) {
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            userRepository.save(user);
            log.debug("Created user: {}", user.getEmail());
        }
    }
}
