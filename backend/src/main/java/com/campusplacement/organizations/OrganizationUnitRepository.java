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
   * Find all organization units by type across all colleges.
   * Used by SUPER_ADMIN for analytics.
   */
  List<OrganizationUnit> findByType(OrganizationUnitType type);

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

  // ==================== Recursive Subtree Queries (CTE) ====================

  /**
   * Recursively get all descendant org unit IDs within a college.
   * Uses PostgreSQL CTE (Common Table Expression) for efficient tree traversal.
   *
   * <p>
   * <strong>Security:</strong> Includes college_id filter in both base and
   * recursive
   * parts to ensure absolute tenant isolation.
   * </p>
   *
   * @param rootId    The root org unit ID to start traversal from
   * @param collegeId The college ID for tenant isolation
   * @return List of all org unit IDs in the subtree (including root)
   */
  @Query(value = """
      WITH RECURSIVE subtree AS (
          SELECT id, parent_unit_id, type, college_id
          FROM organization_units
          WHERE id = :rootId
            AND college_id = :collegeId
            AND deleted_at IS NULL

          UNION ALL

          SELECT o.id, o.parent_unit_id, o.type, o.college_id
          FROM organization_units o
          INNER JOIN subtree s ON o.parent_unit_id = s.id
          WHERE o.college_id = :collegeId
            AND o.deleted_at IS NULL
      )
      SELECT id FROM subtree
      """, nativeQuery = true)
  List<Long> findSubtreeIds(@Param("rootId") Long rootId, @Param("collegeId") Long collegeId);

  /**
   * Get all department IDs in a subtree within a college.
   *
   * <p>
   * Useful for scope resolution - returns only leaf-level department nodes.
   * </p>
   *
   * @param rootId    The root org unit ID to start traversal from
   * @param collegeId The college ID for tenant isolation
   * @return List of department IDs in the subtree
   */
  @Query(value = """
      WITH RECURSIVE subtree AS (
          SELECT id, parent_unit_id, type, college_id
          FROM organization_units
          WHERE id = :rootId
            AND college_id = :collegeId
            AND deleted_at IS NULL

          UNION ALL

          SELECT o.id, o.parent_unit_id, o.type, o.college_id
          FROM organization_units o
          INNER JOIN subtree s ON o.parent_unit_id = s.id
          WHERE o.college_id = :collegeId
            AND o.deleted_at IS NULL
      )
      SELECT id FROM subtree WHERE type = 'DEPARTMENT'
      """, nativeQuery = true)
  List<Long> findDepartmentIdsInSubtree(@Param("rootId") Long rootId, @Param("collegeId") Long collegeId);

  /**
   * Find all departments within a college.
   */
  @Query("SELECT o FROM OrganizationUnit o WHERE o.college.id = :collegeId AND o.type = 'DEPARTMENT' AND o.isActive = true AND o.deletedAt IS NULL")
  List<OrganizationUnit> findAllDepartmentsByCollegeId(@Param("collegeId") Long collegeId);

  // ==================== Ancestor Traversal Query (Opposite Direction)
  // ====================

  /**
   * Recursively get all ancestor org unit IDs (from child to root) within a
   * college.
   * Uses PostgreSQL CTE for upward tree traversal.
   *
   * <p>
   * <strong>Use Case:</strong> Find the parent hierarchy of a department or
   * institute.
   * Example: Department CSE → Institute of Technology → Nirma University
   * </p>
   *
   * <p>
   * <strong>Security:</strong> Includes college_id filter to ensure tenant
   * isolation
   * during upward traversal.
   * </p>
   *
   * @param childId   The org unit ID to start traversal from (e.g., department
   *                  ID)
   * @param collegeId The college ID for tenant isolation
   * @return List of ancestor org unit IDs (including the child), ordered from
   *         child to root
   */
  @Query(value = """
      WITH RECURSIVE ancestors AS (
          SELECT id, parent_unit_id, type, college_id, name, 1 as level
          FROM organization_units
          WHERE id = :childId
            AND college_id = :collegeId
            AND deleted_at IS NULL

          UNION ALL

          SELECT o.id, o.parent_unit_id, o.type, o.college_id, o.name, a.level + 1
          FROM organization_units o
          INNER JOIN ancestors a ON o.id = a.parent_unit_id
          WHERE o.college_id = :collegeId
            AND o.deleted_at IS NULL
      )
      SELECT id FROM ancestors ORDER BY level ASC
      """, nativeQuery = true)
  List<Long> findAncestorIds(@Param("childId") Long childId, @Param("collegeId") Long collegeId);
}
