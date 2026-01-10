package com.campusplacement.applications;

import java.time.LocalDateTime;

import com.campusplacement.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "drive_id", nullable = false)
    private Long driveId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SHORTLISTED, REJECTED, SELECTED, WITHDRAWN

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "resume_url")
    private String resumeSnapshotUrl;

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
