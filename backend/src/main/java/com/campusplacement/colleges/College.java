package com.campusplacement.colleges;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.campusplacement.common.BaseEntity;
import com.campusplacement.organizations.model.OrganizationUnit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a college in the multi-college system.
 *
 * <p>
 * This is the TENANT BOUNDARY. Each college is an isolated tenant.
 * </p>
 *
 * <p>
 * Key points:
 * </p>
 * <ul>
 * <li>Each user (except SUPER_ADMIN) belongs to exactly one college</li>
 * <li>Organization units form a hierarchy within each college</li>
 * <li>Email domain moved to OrganizationUnit level for flexibility</li>
 * </ul>
 */
@Entity
@Table(name = "colleges")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class College extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String address;
    private String website;
    private String contactEmail;

    @Column(name = "admin_name")
    private String adminName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationship to organization units (the hierarchy)
    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrganizationUnit> organizationUnits = new ArrayList<>();

    // NOTE: emailDomain removed - now stored at OrganizationUnit level

    /**
     * Soft-deletes this college.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }
}
