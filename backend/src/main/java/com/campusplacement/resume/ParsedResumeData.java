package com.campusplacement.resume;

import java.time.LocalDateTime;

import com.campusplacement.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity storing AI-extracted resume metadata.
 *
 * <p>
 * <strong>Important:</strong> This is DERIVED data from AI parsing.
 * It never overwrites student-owned resume content.
 * </p>
 *
 * <p>
 * <strong>Data Ownership:</strong>
 * </p>
 * <ul>
 * <li>Original resume: Student-owned (stored in StudentProfile.resumeUrl)</li>
 * <li>Parsed metadata: System-derived (this entity)</li>
 * </ul>
 */
@Entity
@Table(name = "parsed_resume_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedResumeData extends BaseEntity {

    /**
     * Student profile ID this parsed data belongs to.
     */
    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    /**
     * Extracted skills as comma-separated values.
     * Normalized format: "Java,Spring Boot,SQL,Docker"
     */
    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    /**
     * Detected experience level.
     * Values: FRESHER, BEGINNER, INTERMEDIATE, ADVANCED
     */
    @Column(name = "experience_level")
    private String experienceLevel;

    /**
     * AI confidence score for the extraction (0.0 to 1.0).
     */
    @Column(name = "confidence_score")
    private Double confidenceScore;

    /**
     * Extracted project keywords (comma-separated).
     */
    @Column(name = "project_keywords", columnDefinition = "TEXT")
    private String projectKeywords;

    /**
     * Extracted education signals (comma-separated).
     * Example: "B.Tech CSE,IIT Delhi,First Class"
     */
    @Column(name = "education_signals", columnDefinition = "TEXT")
    private String educationSignals;

    /**
     * When this data was parsed.
     */
    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;

    /**
     * AI model version used for parsing.
     * Used for cache invalidation when model changes.
     */
    @Column(name = "model_version")
    @Builder.Default
    private String modelVersion = "v1.0.0";
}
