package com.campusplacement.companies;

import java.util.ArrayList;
import java.util.List;

import com.campusplacement.common.BaseEntity;
import com.campusplacement.drives.PlacementDrive;

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
 * Entity representing a company/recruiter.
 *
 * <p>
 * Companies are global entities (not tenant-scoped).
 * Multiple colleges can associate with the same company via placement drives.
 * </p>
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String industry;

    @Column
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column
    private String location;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * All placement drives conducted by this company across all colleges.
     * This is a read-only view for analytics and reporting purposes.
     */
    @OneToMany(mappedBy = "company")
    @Builder.Default
    private List<PlacementDrive> drives = new ArrayList<>();

    // ==================== Helper Methods ====================

    /**
     * Gets the count of drives for this company.
     * Useful for displaying company stats without loading all drives.
     */
    public int getDriveCount() {
        return drives != null ? drives.size() : 0;
    }
}
