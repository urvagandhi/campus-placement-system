package com.campusplacement.students;

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
 * Entity representing a student's academic profile.
 */
@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "enrollment_no", nullable = false, unique = true)
    private String enrollmentNo;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private Double cgpa;

    @Column(columnDefinition = "TEXT")
    private String skills; // Comma-separated skills

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "batch_year")
    private Integer batchYear;

    @Column
    private String semester;

    @Column(name = "projects_count")
    @Builder.Default
    private Integer projectsCount = 0;

    @Column(name = "internship_months")
    @Builder.Default
    private Integer internshipMonths = 0;

    @Column(columnDefinition = "TEXT")
    private String certifications; // Comma-separated certifications

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    // TODO: Add relationship to User
    // @OneToOne
    // @JoinColumn(name = "user_id", referencedColumnName = "id")
    // private User user;
}
