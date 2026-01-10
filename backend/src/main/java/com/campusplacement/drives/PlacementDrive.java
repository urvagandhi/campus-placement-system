package com.campusplacement.drives;

import java.time.LocalDate;

import com.campusplacement.colleges.College;
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
 * Entity representing a placement drive conducted by a company.
 *
 * <p>
 * <strong>Multi-tenancy:</strong> Each drive belongs to a college (tenant).
 * Scope enforcement ensures coordinators only see drives within their scope.
 * </p>
 */
@Entity
@Table(name = "placement_drives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDrive extends BaseEntity {

    /**
     * College (tenant) this drive belongs to.
     * Required for scope enforcement.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private com.campusplacement.companies.Company company;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "job_role")
    private String jobRole;

    @Column(name = "package_lpa")
    private Double packageLpa;

    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(nullable = false)
    @Builder.Default
    private String status = "UPCOMING"; // UPCOMING, ONGOING, COMPLETED, CANCELLED

    // Eligibility Criteria
    @Column(name = "min_cgpa")
    private Double minCgpa;

    @jakarta.persistence.ManyToMany(fetch = FetchType.LAZY)
    @jakarta.persistence.JoinTable(name = "drive_eligible_departments", joinColumns = @JoinColumn(name = "drive_id"), inverseJoinColumns = @JoinColumn(name = "department_id"))
    @Builder.Default
    private java.util.Set<com.campusplacement.organizations.OrganizationUnit> eligibleDepartments = new java.util.HashSet<>();

    @Column(name = "required_skills")
    private String requiredSkills; // Comma-separated

    @Column(name = "max_backlogs")
    @Builder.Default
    private Integer maxBacklogs = 0;

    @Column
    private String location;

    @Column(name = "is_remote")
    @Builder.Default
    private Boolean isRemote = false;

    // TODO: Add relationship to Company
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "company_id", insertable = false, updatable = false)
    // private Company company;
}
