package com.campusplacement.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Cross-Origin Resource Sharing (CORS) configuration.
 *
 * <p>
 * Enables secure communication between the Next.js frontend
 * and the Spring Boot backend.
 * </p>
 *
 * <p>
 * <strong>Allowed Origins:</strong>
 * </p>
 * <ul>
 * <li>http://localhost:3000 - Next.js development server</li>
 * <li>TODO: Add production frontend URL</li>
 * </ul>
 */
@Configuration
public class CorsConfig {

    /**
     * Configures CORS settings for the application.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // Next.js development
                "http://127.0.0.1:3000" // Alternative localhost
        // TODO: Add production frontend URL
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        // Exposed headers (accessible to frontend)
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Total-Count"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
