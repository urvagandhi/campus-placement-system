package com.campusplacement.organizations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.common.OrganizationUnitType;

/**
 * Repository for OrganizationUnit entity.
 */
@Repository
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long> {

    /**
     * Find all organization units for a college.
     */
    List<OrganizationUnit> findByCollegeId(Long collegeId);

    /**
     * Find the root university for a college.
     */
    Optional<OrganizationUnit> findByCollegeIdAndIsRootTrue(Long collegeId);

    /**
     * Find by college and type.
     */
    List<OrganizationUnit> findByCollegeIdAndType(Long collegeId, OrganizationUnitType type);

    /**
     * Find children of a parent org unit.
     */
    List<OrganizationUnit> findByParentId(Long parentId);

    /**
     * Find by code within a college.
     */
    Optional<OrganizationUnit> findByCollegeIdAndCode(Long collegeId, String code);

    /**
     * Find all active departments for an institute.
     */
    @Query("SELECT o FROM OrganizationUnit o WHERE o.parent.id = :parentId AND o.type = 'DEPARTMENT' AND o.isActive = true")
    List<OrganizationUnit> findActiveDepartmentsByInstituteId(@Param("parentId") Long parentId);

    /**
     * Find all institutes for a university.
     */
    @Query("SELECT o FROM OrganizationUnit o WHERE o.parent.id = :universityId AND o.type = 'INSTITUTE' AND o.isActive = true")
    List<OrganizationUnit> findInstitutesByUniversityId(@Param("universityId") Long universityId);
}
