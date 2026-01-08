package com.campusplacement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.campusplacement.security.CustomUserDetailsService;
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
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for REST API
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configure(http))

                // Stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - only login is public
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()

                        // Role-based protection
                        .requestMatchers("/api/v1/superadmin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/coordinator/**").hasAnyRole("COORDINATOR", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/student/**")
                        .hasAnyRole("STUDENT", "COORDINATOR", "ADMIN", "SUPER_ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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
