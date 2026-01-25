package com.campusplacement.organizations.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.common.ScopeLevel;
import com.campusplacement.organizations.model.UserAssignment;

/**
 * Repository for UserAssignment entity.
 */
@Repository
public interface UserAssignmentRepository extends JpaRepository<UserAssignment, Long> {

        /**
         * Find all assignments for a user.
         */
        List<UserAssignment> findByUserId(Long userId);

        /**
         * Find primary assignment for a user.
         */
        Optional<UserAssignment> findByUserIdAndIsPrimaryTrue(Long userId);

        /**
         * Find all assignments for an organization unit.
         */
        List<UserAssignment> findByOrganizationUnitId(Long organizationUnitId);

        /**
         * Find assignment by user and org unit.
         */
        Optional<UserAssignment> findByUserIdAndOrganizationUnitId(Long userId, Long organizationUnitId);

        /**
         * Find all coordinators for an org unit.
         */
        @Query("SELECT ua FROM UserAssignment ua JOIN ua.user u " +
                        "WHERE ua.organizationUnit.id = :orgUnitId AND u.role = 'COORDINATOR'")
        List<UserAssignment> findCoordinatorsByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

        /**
         * Find all students for an org unit.
         */
        @Query("SELECT ua FROM UserAssignment ua JOIN ua.user u " +
                        "WHERE ua.organizationUnit.id = :orgUnitId AND u.role = 'STUDENT'")
        List<UserAssignment> findStudentsByOrgUnitId(@Param("orgUnitId") Long orgUnitId);

        /**
         * Find assignments by scope level.
         */
        List<UserAssignment> findByScopeLevel(ScopeLevel scopeLevel);

        /**
         * Check if user has assignment in org unit.
         */
        boolean existsByUserIdAndOrganizationUnitId(Long userId, Long organizationUnitId);

        /**
         * Find all assignments for a college (Staff only).
         */
        @Query("SELECT ua FROM UserAssignment ua JOIN FETCH ua.user u JOIN FETCH ua.organizationUnit ou " +
                        "WHERE ou.college.id = :collegeId AND u.role != 'STUDENT' AND u.deletedAt IS NULL")
        List<UserAssignment> findAllStaffByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Count active staff (non-students, not deleted) in a unit.
         */
        @Query("SELECT COUNT(ua) FROM UserAssignment ua JOIN ua.user u " +
                        "WHERE ua.organizationUnit.id = :orgUnitId AND u.role != 'STUDENT' AND u.deletedAt IS NULL")
        long countActiveStaffByUnitId(@Param("orgUnitId") Long orgUnitId);

        /**
         * Find all assignments for a list of organization unit IDs.
         */
        List<UserAssignment> findAllByOrganizationUnitIdIn(List<Long> organizationUnitIds);
}
