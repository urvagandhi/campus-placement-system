package com.campusplacement.ai;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.campusplacement.config.properties.AiServiceProperties;

import lombok.RequiredArgsConstructor;

/**
 * Health indicator for the external Python AI Service.
 */
@Component
@RequiredArgsConstructor
public class AiServiceHealthIndicator implements HealthIndicator {

    private final AiServiceProperties aiServiceProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            // Base URL is http://localhost:8000/api/v1
            // Health endpoint is http://localhost:8000/health
            String healthUrl = aiServiceProperties.getBaseUrl().replace("/api/v1", "/health");

            restTemplate.getForObject(healthUrl, String.class);
            return Health.up()
                    .withDetail("service", "Python AI Service")
                    .withDetail("url", healthUrl)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "Python AI Service")
                    .withDetail("error", "Service unreachable: " + e.getMessage())
                    .build();
        }
    }
}
