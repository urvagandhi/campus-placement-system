package com.campusplacement.applications;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Application entity.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

        List<Application> findByStudentId(Long studentId);

        List<Application> findByDriveId(Long driveId);

        List<Application> findByStatus(String status);

        Optional<Application> findByStudentIdAndDriveId(Long studentId, Long driveId);

        boolean existsByStudentIdAndDriveId(Long studentId, Long driveId);

        List<Application> findByDriveIdAndStatus(Long driveId, String status);

        // ==================== Scope-Aware Queries ====================

        /**
         * Find applications by drive ID, filtered by student department.
         * Used by Department Coordinators to see applicants from their department for a
         * specific drive.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.driveId = :driveId " +
                        "AND a.student.department.id IN :departmentIds")
        List<Application> findByDriveIdAndStudentDepartmentIdIn(
                        @Param("driveId") Long driveId,
                        @Param("departmentIds") Set<Long> departmentIds);

        /**
         * Find all applications from specific departments.
         * Used by Department Coordinators to track their students' applications.
         */
        @Query("SELECT a FROM Application a WHERE a.student.department.id IN :departmentIds")
        List<Application> findByStudentDepartmentIdIn(@Param("departmentIds") Set<Long> departmentIds);

        /**
         * Find all applications for a drive within a college (College Guard).
         * Ensures accessing users can only see applications for their college's drives.
         */
        @Query("SELECT a FROM Application a JOIN PlacementDrive d ON a.driveId = d.id " +
                        "WHERE a.driveId = :driveId AND d.college.id = :collegeId")
        List<Application> findByDriveIdAndCollegeId(
                        @Param("driveId") Long driveId,
                        @Param("collegeId") Long collegeId);

        /**
         * Find applications by student departments with college guard.
         * Used by coordinators to see applications from their allowed departments.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.department.id IN :departmentIds " +
                        "AND a.student.user.college.id = :collegeId")
        List<Application> findByStudentDepartmentIdInAndCollegeId(
                        @Param("departmentIds") Set<Long> departmentIds,
                        @Param("collegeId") Long collegeId);

        /**
         * Find applications by college ID for full college access.
         */
        @Query("SELECT a FROM Application a WHERE a.student.user.college.id = :collegeId")
        List<Application> findByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Count selected applications by department IDs.
         * Used for analytics and dashboard KPIs.
         */
        @Query("SELECT COUNT(a) FROM Application a " +
                        "WHERE a.status = 'SELECTED' " +
                        "AND a.student.department.id IN :departmentIds")
        long countSelectedByDepartmentIds(@Param("departmentIds") Set<Long> departmentIds);

        /**
         * Find applications by college and year for analytics.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.user.college.id = :collegeId " +
                        "AND YEAR(a.appliedAt) = :year")
        List<Application> findByCollegeIdAndYear(
                        @Param("collegeId") Long collegeId,
                        @Param("year") Integer year);

        /**
         * Find applications by department IDs and year for scoped analytics.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.department.id IN :departmentIds " +
                        "AND YEAR(a.appliedAt) = :year")
        List<Application> findByDepartmentIdsAndYear(
                        @Param("departmentIds") Set<Long> departmentIds,
                        @Param("year") Integer year);

        // ==================== Analytics Count Queries ====================

        /**
         * Count applications by drive ID.
         */
        long countByDriveId(Long driveId);

        /**
         * Count applications by drive ID and status.
         */
        @Query("SELECT COUNT(a) FROM Application a WHERE a.driveId = :driveId AND a.status = :status")
        long countByDriveIdAndStatus(@Param("driveId") Long driveId, @Param("status") String status);

        /**
         * Count placed students by college ID.
         */
        @Query("SELECT COUNT(DISTINCT a.studentId) FROM Application a " +
                        "JOIN PlacementDrive d ON a.driveId = d.id " +
                        "WHERE d.college.id = :collegeId AND a.status = 'SELECTED'")
        long countPlacedStudentsByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Count total placed students across all colleges.
         */
        @Query("SELECT COUNT(DISTINCT a.studentId) FROM Application a WHERE a.status = 'SELECTED'")
        long countPlacedStudents();

        /**
         * Calculate average package by college ID.
         */
        @Query("SELECT AVG(d.packageLpa) FROM Application a " +
                        "JOIN PlacementDrive d ON a.driveId = d.id " +
                        "WHERE d.college.id = :collegeId AND a.status = 'SELECTED' " +
                        "AND d.packageLpa IS NOT NULL")
        Double calculateAveragePackageByCollegeId(@Param("collegeId") Long collegeId);

        /**
         * Get top hiring company names by selection count.
         */
        @Query(value = "SELECT c.name FROM applications a " +
                        "JOIN placement_drives d ON a.drive_id = d.id " +
                        "JOIN companies c ON d.company_id = c.id " +
                        "WHERE a.status = 'SELECTED' " +
                        "GROUP BY c.id, c.name " +
                        "ORDER BY COUNT(a.id) DESC " +
                        "LIMIT :limit", nativeQuery = true)
        List<String> findTopHiringCompanyNames(@Param("limit") int limit);

        // ==================== Academic Year Queries ====================

        /**
         * Find applications in a college within an academic year date range.
         * Academic year is July 1 to June 30.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.user.college.id = :collegeId " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        List<Application> findByCollegeIdAndDateRange(
                        @Param("collegeId") Long collegeId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * Find applications by department IDs within an academic year date range.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.department.id IN :departmentIds " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        List<Application> findByDepartmentIdsAndDateRange(
                        @Param("departmentIds") java.util.Set<Long> departmentIds,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // ==================== Institute-Level Queries ====================

        /**
         * Count students placed from departments under an institute within date range.
         * Used for institute-level analytics.
         */
        @Query("SELECT COUNT(DISTINCT a.studentId) FROM Application a " +
                        "WHERE a.status = 'SELECTED' " +
                        "AND a.student.department.parent.id = :instituteId " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        Long countPlacedByInstituteIdAndDateRange(
                        @Param("instituteId") Long instituteId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * Find all applications from an institute's departments within date range.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.department.parent.id = :instituteId " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        List<Application> findByInstituteIdAndDateRange(
                        @Param("instituteId") Long instituteId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * Calculate average package for selected students from an institute.
         */
        @Query("SELECT AVG(d.packageLpa) FROM Application a " +
                        "JOIN PlacementDrive d ON a.driveId = d.id " +
                        "WHERE a.status = 'SELECTED' " +
                        "AND a.student.department.parent.id = :instituteId " +
                        "AND d.packageLpa IS NOT NULL " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        Double calculateAveragePackageByInstituteIdAndDateRange(
                        @Param("instituteId") Long instituteId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        /**
         * Find all applications from a single department within date range.
         */
        @Query("SELECT a FROM Application a " +
                        "WHERE a.student.department.id = :departmentId " +
                        "AND a.appliedAt >= :startDate AND a.appliedAt < :endDate")
        List<Application> findByDepartmentIdAndDateRange(
                        @Param("departmentId") Long departmentId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);
}
