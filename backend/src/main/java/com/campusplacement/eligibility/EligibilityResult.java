package com.campusplacement.eligibility;

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
 * Entity storing eligibility calculation results from the AI service.
 *
 * <p>
 * The AI module acts strictly as a decision-support system and does not
 * autonomously make placement decisions.
 * </p>
 */
@Entity
@Table(name = "eligibility_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityResult extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "drive_id", nullable = false)
    private Long driveId;

    @Column(nullable = false)
    private Double score;

    @Column(name = "is_eligible", nullable = false)
    private Boolean isEligible;

    @Column(columnDefinition = "TEXT")
    private String reasons; // JSON or comma-separated reasons

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "cgpa_score")
    private Double cgpaScore;

    @Column(name = "skills_score")
    private Double skillsScore;

    @Column(name = "experience_score")
    private Double experienceScore;

    @Column(columnDefinition = "TEXT")
    private String skillGaps; // Skills the student is missing
}
