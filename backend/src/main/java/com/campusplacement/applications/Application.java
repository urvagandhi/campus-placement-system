package com.campusplacement.applications;

import java.time.LocalDateTime;

import com.campusplacement.common.BaseEntity;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.students.StudentProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a student's application to a placement drive.
 */
@Entity
@Table(name = "applications", uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(columnNames = { "student_id", "drive_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private StudentProfile student;

    @Column(name = "drive_id", nullable = false)
    private Long driveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", insertable = false, updatable = false)
    private PlacementDrive drive;

    @Column(nullable = false)
    @Builder.Default
    private String status = ApplicationStatusType.PENDING.name();

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "shortlisted_at")
    private LocalDateTime shortlistedAt;

    @Column(name = "selected_at")
    private LocalDateTime selectedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.appliedAt == null) {
            this.appliedAt = LocalDateTime.now();
        }
    }
}
