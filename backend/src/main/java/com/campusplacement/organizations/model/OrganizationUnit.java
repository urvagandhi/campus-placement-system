package com.campusplacement.organizations.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.campusplacement.colleges.College;
import com.campusplacement.common.BaseEntity;
import com.campusplacement.common.OrganizationUnitType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a hierarchical organization unit.
 *
 * <p>
 * Hierarchy structure: UNIVERSITY → INSTITUTE → DEPARTMENT
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Self-referential hierarchy via parent_id</li>
 * <li>is_root flag for fast university root lookup</li>
 * <li>Soft-delete via deleted_at timestamp</li>
 * <li>Hierarchy constraint enforced at database level</li>
 * </ul>
 *
 * <p>
 * <strong>Design Note:</strong> "The system models real-world academic
 * structures
 * using hierarchical organization units (University → Institute → Department).
 * User roles remain intentionally limited, while operational scope and
 * authority
 * are derived from organizational assignments."
 * </p>
 */
@Entity
@Table(name = "organization_units")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false)
    private String name;

    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationUnitType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    private OrganizationUnit parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrganizationUnit> children = new ArrayList<>();

    @Column(name = "email_domain")
    private String emailDomain;

    @Column(name = "is_root")
    @Builder.Default
    private Boolean isRoot = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft-deletes this organization unit.
     * The @SQLRestriction annotation will filter out soft-deleted records.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Checks if this is the university root node.
     */
    public boolean isUniversityRoot() {
        return Boolean.TRUE.equals(isRoot) && type == OrganizationUnitType.UNIVERSITY;
    }
}
