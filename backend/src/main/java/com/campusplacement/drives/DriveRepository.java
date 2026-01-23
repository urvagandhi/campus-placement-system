package com.campusplacement.drives;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PlacementDrive entity.
 *
 * <p>
 * <strong>Multi-tenancy:</strong> Queries include college_id for tenant
 * isolation.
 * </p>
 */
@Repository
public interface DriveRepository extends JpaRepository<PlacementDrive, Long> {

        List<PlacementDrive> findByCompanyId(Long companyId);

        List<PlacementDrive> findByStatus(String status);

        List<PlacementDrive> findByDriveDateAfter(LocalDate date);

        List<PlacementDrive> findByStatusAndDriveDateAfter(String status, LocalDate date);

        List<PlacementDrive> findByRegistrationDeadlineAfter(LocalDate date);

        // ==================== Scope-Aware Queries ====================

        /**
         * Find all drives within a college.
         * Used for ADMIN role with full college access.
         *
         * @param collegeId The college ID for tenant isolation
         * @return List of all drives in the college
         */
        List<PlacementDrive> findByCollegeId(Long collegeId);

        /**
         * Find upcoming drives within a college.
         * Used for coordinators and students to view relevant drives.
         */
        @Query("SELECT d FROM PlacementDrive d " +
                        "WHERE d.college.id = :collegeId " +
                        "AND d.driveDate > :today " +
                        "ORDER BY d.driveDate ASC")
        List<PlacementDrive> findUpcomingDrivesByCollegeId(
                        @Param("collegeId") Long collegeId,
                        @Param("today") LocalDate today);

        /**
         * Find drives by status within a college.
         */
        @Query("SELECT d FROM PlacementDrive d " +
                        "WHERE d.college.id = :collegeId " +
                        "AND d.status = :status")
        List<PlacementDrive> findByCollegeIdAndStatus(
                        @Param("collegeId") Long collegeId,
                        @Param("status") String status);

        /**
         * Find drives with registration deadline after given date within a college.
         */
        @Query("SELECT d FROM PlacementDrive d " +
                        "WHERE d.college.id = :collegeId " +
                        "AND d.registrationDeadline > :date " +
                        "ORDER BY d.registrationDeadline ASC")
        List<PlacementDrive> findActiveRegistrationDrivesByCollegeId(
                        @Param("collegeId") Long collegeId,
                        @Param("date") LocalDate date);

        /**
         * Count drives within a college (for analytics).
         */
        @Query("SELECT COUNT(d) FROM PlacementDrive d WHERE d.college.id = :collegeId")
        Long countByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Count drives by status within a college.
         */
        @Query("SELECT COUNT(d) FROM PlacementDrive d " +
                        "WHERE d.college.id = :collegeId AND d.status = :status")
        Long countByCollegeIdAndStatus(
                        @Param("collegeId") Long collegeId,
                        @Param("status") String status);

        /**
         * Find active drives within a college.
         * Active = status is OPEN or UPCOMING.
         */
        @Query("SELECT d FROM PlacementDrive d " +
                        "WHERE d.college.id = :collegeId " +
                        "AND d.status IN ('OPEN', 'UPCOMING') " +
                        "ORDER BY d.driveDate ASC")
        List<PlacementDrive> findActiveByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Count distinct colleges that have placement drives.
         * Used for platform-wide analytics.
         */
        @Query("SELECT COUNT(DISTINCT d.college.id) FROM PlacementDrive d")
        long countDistinctColleges();

        /**
         * Find drives eligible for specific departments within a college.
         */
        List<PlacementDrive> findDistinctByEligibleDepartments_IdInAndCollegeId(
                        java.util.Set<Long> departmentIds, Long collegeId);
}
