package com.campusplacement.students.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for full student profile response with organization hierarchy.
 *
 * <p>
 * Includes all profile data plus derived organization path.
 * Used for GET /api/v1/students/me and similar read operations.
 * </p>
 *
 * <p>
 * <strong>Organization Hierarchy:</strong>
 * Derived from department FK via joins: Department → Institute → College.
 * Never hard-coded or stored redundantly.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponseDTO {

    // ==================== Identity ====================

    /**
     * Profile ID.
     */
    private Long id;

    /**
     * Associated user ID.
     */
    private Long userId;

    /**
     * Student's display name.
     */
    private String name;

    /**
     * Student's email address (institution email).
     */
    private String email;

    /**
     * Phone number (if provided).
     */
    private String phoneNumber;

    // ==================== Organization Hierarchy (Read-Only) ====================

    /**
     * College ID for API references.
     */
    private Long collegeId;

    /**
     * College name (derived from hierarchy).
     */
    private String collegeName;

    /**
     * Institute ID for API references.
     */
    private Long instituteId;

    /**
     * Institute name (derived from hierarchy).
     */
    private String instituteName;

    /**
     * Department ID for API references.
     */
    private Long departmentId;

    /**
     * Department name (derived from hierarchy).
     */
    private String departmentName;

    // ==================== Academic Truth (Read-Only) ====================

    /**
     * Enrollment/registration number.
     */
    private String enrollmentNo;

    /**
     * Cumulative GPA.
     */
    private Double cgpa;

    /**
     * Number of backlogs/pending courses.
     */
    private Integer backlogs;

    /**
     * Batch/graduation year.
     */
    private Integer batchYear;

    /**
     * Current semester.
     */
    private String semester;

    // ==================== Career Layer (Editable) ====================

    /**
     * Technical and soft skills.
     */
    private List<String> skills;

    /**
     * URL to uploaded resume.
     */
    private String resumeUrl;

    /**
     * Number of completed projects.
     */
    private Integer projectsCount;

    /**
     * Total months of internship experience.
     */
    private Integer internshipMonths;

    /**
     * Professional certifications.
     */
    private List<String> certifications;

    /**
     * LinkedIn profile URL.
     */
    private String linkedinUrl;

    /**
     * GitHub profile URL.
     */
    private String githubUrl;

    /**
     * Career interests/aspirations.
     */
    private List<String> careerInterests;

    // ==================== Profile Status ====================

    /**
     * Whether the profile is complete enough for placement eligibility.
     * A profile is complete if it has skills and resume uploaded.
     */
    private Boolean isProfileComplete;
}
