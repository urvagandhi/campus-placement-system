package com.campusplacement.organizations.model;

import java.time.LocalDate;

import org.hibernate.annotations.SQLRestriction;

import com.campusplacement.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an academic event (e.g., Exam, Holiday, Tech Fest).
 * Used for detecting scheduling conflicts with placement drives.
 */
@Entity
@Table(name = "academic_events")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_unit_id", nullable = false)
    private OrganizationUnit organizationUnit;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_type", nullable = false)
    private String eventType; // EXAM, HOLIDAY, EVENT

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
}
