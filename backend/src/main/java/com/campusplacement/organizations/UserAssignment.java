package com.campusplacement.organizations;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.campusplacement.common.ScopeLevel;
import com.campusplacement.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user's assignment to an organization unit.
 *
 * <p>
 * This is the REAL AUTHORITY MODEL. Same role can have different scopes:
 * </p>
 * <ul>
 * <li>TPO at Institute level with SUBTREE scope → manages all departments</li>
 * <li>Faculty Coordinator at Department level with SELF scope → manages only
 * that department</li>
 * </ul>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>scope_level defines operational reach (SELF, CHILDREN, SUBTREE)</li>
 * <li>designation is a human-readable title</li>
 * <li>is_primary marks the main assignment for users with multiple
 * assignments</li>
 * <li>start_date/end_date for time-bounded assignments</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> Faculty coordinators and T&P staff are NOT roles —
 * they are assignments.
 * </p>
 */
@Entity
@Table(name = "user_assignments", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "organization_unit_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_unit_id", nullable = false)
    private OrganizationUnit organizationUnit;

    /**
     * Human-readable designation (e.g., "Faculty Coordinator", "TPO", "Student").
     */
    private String designation;

    /**
     * Defines the operational reach of this assignment.
     * <ul>
     * <li>SELF - Can only act on assigned unit</li>
     * <li>CHILDREN - Can act on direct children</li>
     * <li>SUBTREE - Can act on entire subtree</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope")
    @Builder.Default
    private ScopeLevel scopeLevel = ScopeLevel.SUBTREE;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = true;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

    /**
     * Checks if this assignment is currently active.
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return (startDate == null || !startDate.isAfter(today)) &&
                (endDate == null || !endDate.isBefore(today));
    }
}
