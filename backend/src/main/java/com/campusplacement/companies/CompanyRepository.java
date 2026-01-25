package com.campusplacement.companies;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Company entity.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    // TODO: Implement advanced scoped filtering for Company Database feature

    List<Company> findByIndustry(String industry);

    List<Company> findByIsActiveTrue();

    Page<Company> findByIsActiveTrue(Pageable pageable);

    List<Company> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    /**
     * Find companies that have conducted drives for a specific college.
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c FROM Company c JOIN c.drives d WHERE d.college.id = :collegeId AND c.isActive = true")
    Page<Company> findByCollegeId(Long collegeId, Pageable pageable);

    /**
     * Find companies that have conducted drives eligible for specific organization
     * units (Institute or Department).
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c FROM Company c JOIN c.drives d JOIN d.eligibleDepartments table WHERE table.id IN :unitIds AND c.isActive = true")
    Page<Company> findByOrganizationUnitIds(
            @org.springframework.data.repository.query.Param("unitIds") java.util.Set<Long> unitIds, Pageable pageable);
}
