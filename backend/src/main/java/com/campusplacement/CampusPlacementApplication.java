package com.campusplacement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the AI-Assisted Campus Placement System.
 *
 * <p>
 * This is a modular monolithic application that manages campus placements
 * with AI-assisted decision support for eligibility scoring and skill gap
 * analysis.
 * </p>
 *
 * <p>
 * <strong>Architecture Overview:</strong>
 * </p>
 * <ul>
 * <li>Modular Monolith - Feature-based package structure</li>
 * <li>Controller → Service → Repository pattern</li>
 * <li>Interface-based design with DTOs for data transfer</li>
 * <li>AI service integration via HTTP client (decision support only)</li>
 * </ul>
 *
 * @see <a href="../docs/architecture.md">Architecture Documentation</a>
 * @see <a href="../docs/api-specs.md">API Specifications</a>
 *
 * @author Campus Placement Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class CampusPlacementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusPlacementApplication.class, args);
        System.out.println("\n\n" +
                "==========================================================\n" +
                "   PlacementPro System Started Successfully!\n" +
                "==========================================================\n" +
                "   Application  : Campus Placement System\n" +
                "   Database     : Connected (PostgreSQL - campus_placement)\n" +
                "   AI Service   : Enabled (Decision Support)\n" +
                "   API Docs     : http://localhost:8080/api-docs\n" +
                "   Health       : http://localhost:8080/actuator/health\n" +
                "==========================================================\n");
    }
}
