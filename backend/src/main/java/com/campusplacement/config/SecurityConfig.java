package com.campusplacement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.campusplacement.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security configuration for JWT-based authentication.
 *
 * <p>
 * Features:
 * <ul>
 * <li>Stateless session management (JWT-based)</li>
 * <li>CSRF disabled for REST API</li>
 * <li>Role-based endpoint protection</li>
 * <li>BCrypt password encoding</li>
 * </ul>
 * </p>
 *
 * <p>
 * Endpoint Protection Rules:
 * <ul>
 * <li>/api/v1/auth/login - Permit all</li>
 * <li>/api/v1/auth/logout - Authenticated</li>
 * <li>/api/v1/auth/me - Authenticated</li>
 * <li>/api/v1/users/students - COORDINATOR, ADMIN, SUPER_ADMIN</li>
 * <li>/api/v1/users/coordinators - ADMIN, SUPER_ADMIN</li>
 * <li>/api/v1/users/admins - SUPER_ADMIN only</li>
 * <li>/api/v1/admin/** - ADMIN, SUPER_ADMIN only</li>
 * <li>/api/v1/superadmin/** - SUPER_ADMIN only</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final com.campusplacement.security.CustomAccessDeniedHandler customAccessDeniedHandler;
        private final com.campusplacement.security.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF for REST API
                                .csrf(csrf -> csrf.disable())

                                // Explicitly disable HTTP Basic and Form Login to prevent browser popups
                                .httpBasic(basic -> basic.disable())
                                .formLogin(form -> form.disable())

                                // Enable CORS
                                .cors(Customizer.withDefaults())

                                // Stateless session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Allow CORS preflight requests
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                // Public endpoints - login, refresh, and logout are public to handle
                                                // session
                                                // cleanup
                                                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh",
                                                                "/api/v1/auth/logout")
                                                .permitAll()
                                                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/health", "/actuator/**").permitAll()

                                                // Role-based protection
                                                .requestMatchers("/api/v1/superadmin/**").hasRole("SUPER_ADMIN")
                                                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                                                .requestMatchers("/api/v1/coordinator/**")
                                                .hasAnyRole("COORDINATOR", "ADMIN", "SUPER_ADMIN")
                                                .requestMatchers("/api/v1/student/**")
                                                .hasAnyRole("STUDENT", "COORDINATOR", "ADMIN", "SUPER_ADMIN")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Add JWT filter before UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // Add Security Headers
                                .headers(headers -> headers
                                                // Content Security Policy
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self'; " +
                                                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
                                                                                +
                                                                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                                                                                +
                                                                                "img-src 'self' data: https://*.tile.openstreetmap.org https://*.tile.osm.org; "
                                                                                +
                                                                                "font-src 'self' https://fonts.gstatic.com data:; "
                                                                                +
                                                                                "connect-src 'self' http://localhost:8080 http://127.0.0.1:8080 http://localhost:3000; "
                                                                                +
                                                                                "frame-ancestors 'none'; "))
                                                // X-Frame-Options: DENY
                                                .frameOptions(frame -> frame.deny())
                                                // X-Content-Type-Options: nosniff
                                                .contentTypeOptions(Customizer.withDefaults())
                                                // Referrer-Policy: strict-origin-when-cross-origin
                                                .referrerPolicy(referrer -> referrer.policy(
                                                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                                                // Strict-Transport-Security: max-age=31536000; includeSubDomains
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)))

                                // Exception handling
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDeniedHandler));

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }
}
