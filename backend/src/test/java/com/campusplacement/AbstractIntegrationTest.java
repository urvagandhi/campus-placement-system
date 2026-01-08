package com.campusplacement;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers.
 *
 * <p>
 * Provides a shared PostgreSQL container for all integration tests,
 * ensuring real database behavior is tested without mocking.
 * </p>
 *
 * <p>
 * Usage: Extend this class for any test that requires database access.
 * </p>
 *
 * <pre>
 * class MyIntegrationTest extends AbstractIntegrationTest {
 *     // tests here have access to real PostgreSQL
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * Shared PostgreSQL container for all tests.
     * Uses the same PostgreSQL version as production.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_campus_placement")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Dynamically configure Spring datasource to use the container.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
