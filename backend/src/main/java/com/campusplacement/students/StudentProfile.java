package com.campusplacement.students;

import com.campusplacement.common.BaseEntity;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a student's academic profile.
 *
 * <p>
 * <strong>Data Ownership Model:</strong>
 * </p>
 * <ul>
 * <li>Institution-Owned (immutable by student): enrollmentNo, departmentId,
 * cgpa, backlogs, batchYear, semester</li>
 * <li>Student-Owned (editable): skills, resumeUrl, projectsCount,
 * internshipMonths, certifications, linkedinUrl, githubUrl,
 * careerInterests</li>
 * </ul>
 *
 * <p>
 * Organization hierarchy is derived via department → institute → college joins.
 * Never hard-code college/institute - always derive from department FK.
 * </p>
 */
@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends BaseEntity {

    // ==================== Identity Layer ====================

    /**
     * User associated with this profile.
     * Contains authentication and identity data.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ==================== Academic Truth (Institution-Owned) ====================

    /**
     * Unique enrollment/registration number assigned by institution.
     * Immutable - cannot be changed by student.
     */
    @Column(name = "enrollment_number", nullable = false, unique = true)
    private String enrollmentNo;

    /**
     * Department within the organizational hierarchy.
     * Derived path: Department → Institute → College.
     * Immutable - cannot be changed by student.
     * Note: Nullable per schema (ON DELETE SET NULL).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private OrganizationUnit department;

    /**
     * Cumulative Grade Point Average.
     * Immutable - updated only by institution.
     */
    @Column(nullable = false)
    private Double cgpa;

    /**
     * Number of active backlogs/pending courses.
     * Immutable - updated only by institution.
     */
    @Column(name = "active_backlogs")
    @Builder.Default
    private Integer backlogs = 0;

    /**
     * Admission/graduation batch year.
     * Immutable - cannot be changed by student.
     */
    @Column(name = "batch_year")
    private Integer batchYear;

    /**
     * Current semester (1-8 for UG, 1-4 for PG).
     * Immutable - updated only by institution.
     */
    @Column(name = "current_semester")
    private Integer semester;

    // ==================== Career Layer (Student-Owned) ====================

    /**
     * Technical and soft skills.
     * Stored as comma-separated values for simplicity.
     * Editable by student.
     */
    @Column(columnDefinition = "TEXT")
    private String skills;

    /**
     * URL to uploaded resume (cloud storage).
     * Editable by student.
     */
    @Column(name = "resume_url")
    private String resumeUrl;

    /**
     * Number of completed projects.
     * Editable by student.
     */
    @Column(name = "projects_count")
    @Builder.Default
    private Integer projectsCount = 0;

    /**
     * Total months of internship experience.
     * Editable by student.
     */
    @Column(name = "internship_months")
    @Builder.Default
    private Integer internshipMonths = 0;

    /**
     * Professional certifications earned.
     * Stored as comma-separated values.
     * Editable by student.
     */
    @Column(columnDefinition = "TEXT")
    private String certifications;

    /**
     * LinkedIn profile URL.
     * Editable by student.
     */
    @Column(name = "linkedin_url")
    private String linkedinUrl;

    /**
     * GitHub profile URL.
     * Editable by student.
     */
    @Column(name = "github_url")
    private String githubUrl;

    /**
     * Career interests/aspirations.
     * Stored as JSON array string: ["Backend", "DevOps", "ML"].
     * Editable by student.
     */
    @Column(name = "career_interests", columnDefinition = "TEXT")
    private String careerInterests;

    // ==================== Helper Methods ====================

    /**
     * Gets the user ID for this profile.
     * Used for backward compatibility with code using userId directly.
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}
