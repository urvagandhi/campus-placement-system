package com.campusplacement.students;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.users.User;

/**
 * Repository interface for StudentProfile entity.
 *
 * <p>
 * Provides data access methods for student profiles with proper
 * organization hierarchy queries.
 * </p>
 */
@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Long> {

       /**
        * Find profile by associated user.
        */
       Optional<StudentProfile> findByUser(User user);

       /**
        * Find profile by user ID.
        */
       @Query("SELECT sp FROM StudentProfile sp WHERE sp.user.id = :userId")
       Optional<StudentProfile> findByUserId(@Param("userId") Long userId);

       /**
        * Find profile by enrollment number.
        */
       Optional<StudentProfile> findByEnrollmentNo(String enrollmentNo);

       /**
        * Find all profiles in a department.
        */
       List<StudentProfile> findByDepartment(OrganizationUnit department);

       /**
        * Find all profiles in a department by department ID.
        */
       @Query("SELECT sp FROM StudentProfile sp WHERE sp.department.id = :departmentId")
       List<StudentProfile> findByDepartmentId(@Param("departmentId") Long departmentId);

       /**
        * Find students meeting minimum CGPA requirement.
        */
       List<StudentProfile> findByCgpaGreaterThanEqual(Double minCgpa);

       /**
        * Find students in department with minimum CGPA.
        */
       @Query("SELECT sp FROM StudentProfile sp WHERE sp.department.id = :departmentId AND sp.cgpa >= :minCgpa")
       List<StudentProfile> findByDepartmentIdAndCgpaGreaterThanEqual(
                     @Param("departmentId") Long departmentId,
                     @Param("minCgpa") Double minCgpa);

       /**
        * Find students by batch year.
        */
       List<StudentProfile> findByBatchYear(Integer batchYear);

       /**
        * Check if enrollment number exists.
        */
       boolean existsByEnrollmentNo(String enrollmentNo);

       /**
        * Find student profile with full organization hierarchy eagerly loaded.
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "JOIN FETCH sp.user u " +
                     "JOIN FETCH sp.department d " +
                     "LEFT JOIN FETCH d.parent i " +
                     "LEFT JOIN FETCH i.parent c " +
                     "WHERE sp.user.id = :userId")
       Optional<StudentProfile> findByUserIdWithHierarchy(@Param("userId") Long userId);

       /**
        * Find eligible students for a drive based on criteria.
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.cgpa >= :minCgpa " +
                     "AND sp.backlogs <= :maxBacklogs " +
                     "AND sp.department.id IN :departmentIds")
       List<StudentProfile> findEligibleStudents(
                     @Param("minCgpa") Double minCgpa,
                     @Param("maxBacklogs") Integer maxBacklogs,
                     @Param("departmentIds") List<Long> departmentIds);

       // ==================== Scope-Aware Queries ====================

       /**
        * Find all students within a college.
        * Used for ADMIN role with full college access.
        *
        * @param collegeId The college ID for tenant isolation
        * @return List of all student profiles in the college
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.user.college.id = :collegeId")
       List<StudentProfile> findByCollegeId(@Param("collegeId") Long collegeId);

       /**
        * Find students in allowed departments within a college (scope-enforced).
        * Used for COORDINATOR role with limited department access.
        *
        * <p>
        * <strong>Security:</strong> Includes both department and college filters
        * for defense-in-depth tenant isolation.
        * </p>
        *
        * @param departmentIds Set of allowed department IDs
        * @param collegeId     The college ID for tenant isolation
        * @return List of student profiles in allowed departments
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.department.id IN :departmentIds " +
                     "AND sp.user.college.id = :collegeId")
       List<StudentProfile> findByDepartmentIdInAndCollegeId(
                     @Param("departmentIds") java.util.Set<Long> departmentIds,
                     @Param("collegeId") Long collegeId);

       /**
        * Find eligible students with scope enforcement.
        * Combines eligibility criteria with scope constraints.
        *
        * @param minCgpa       Minimum CGPA requirement
        * @param maxBacklogs   Maximum allowed backlogs
        * @param departmentIds Set of allowed department IDs
        * @param collegeId     The college ID for tenant isolation
        * @return List of eligible student profiles within scope
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.cgpa >= :minCgpa " +
                     "AND sp.backlogs <= :maxBacklogs " +
                     "AND sp.department.id IN :departmentIds " +
                     "AND sp.user.college.id = :collegeId")
       List<StudentProfile> findEligibleStudentsScoped(
                     @Param("minCgpa") Double minCgpa,
                     @Param("maxBacklogs") Integer maxBacklogs,
                     @Param("departmentIds") java.util.Set<Long> departmentIds,
                     @Param("collegeId") Long collegeId);

       /**
        * Count students in allowed departments within a college.
        * Useful for analytics and dashboard metrics.
        */
       @Query("SELECT COUNT(sp) FROM StudentProfile sp " +
                     "WHERE sp.department.id IN :departmentIds " +
                     "AND sp.user.college.id = :collegeId")
       Long countByDepartmentIdInAndCollegeId(
                     @Param("departmentIds") java.util.Set<Long> departmentIds,
                     @Param("collegeId") Long collegeId);

       // ==================== Performance Optimized Queries ====================

       /**
        * Count all students in a college.
        * Used by ADMIN and analytics for college-level metrics.
        */
       @Query("SELECT COUNT(sp) FROM StudentProfile sp WHERE sp.user.college.id = :collegeId")
       Long countByCollegeId(@Param("collegeId") Long collegeId);

       /**
        * Find students by college ID with eager loading to prevent N+1 queries.
        * Fetches student profile, user, department, and college in a single query.
        *
        * <p>
        * <strong>Performance:</strong> Use this for bulk operations where you need
        * full student details including organizational hierarchy.
        * </p>
        *
        * @param collegeId The college ID for filtering
        * @return List of student profiles with eagerly loaded relationships
        */
       @Query("SELECT DISTINCT sp FROM StudentProfile sp " +
                     "JOIN FETCH sp.user u " +
                     "JOIN FETCH sp.department d " +
                     "JOIN FETCH u.college c " +
                     "WHERE c.id = :collegeId")
       List<StudentProfile> findByCollegeIdWithEagerLoading(@Param("collegeId") Long collegeId);

       /**
        * Find students in allowed departments with pagination support.
        * Prevents loading thousands of records into memory at once.
        *
        * @param departmentIds Set of allowed department IDs
        * @param collegeId     The college ID for tenant isolation
        * @param pageable      Pagination parameters (page number, size, sort)
        * @return Page of student profiles
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.department.id IN :departmentIds " +
                     "AND sp.user.college.id = :collegeId")
       Page<StudentProfile> findByDepartmentIdInAndCollegeId(
                     @Param("departmentIds") java.util.Set<Long> departmentIds,
                     @Param("collegeId") Long collegeId,
                     Pageable pageable);

       /**
        * Find students by college ID with pagination support.
        *
        * @param collegeId The college ID for filtering
        * @param pageable  Pagination parameters
        * @return Page of student profiles
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.user.college.id = :collegeId")
       Page<StudentProfile> findByCollegeId(
                     @Param("collegeId") Long collegeId,
                     Pageable pageable);

       /**
        * Find eligible students with pagination (scope-enforced).
        * Combines eligibility criteria with scope constraints and pagination.
        *
        * @param minCgpa       Minimum CGPA requirement
        * @param maxBacklogs   Maximum allowed backlogs
        * @param departmentIds Set of allowed department IDs
        * @param collegeId     The college ID for tenant isolation
        * @param pageable      Pagination parameters
        * @return Page of eligible student profiles
        */
       @Query("SELECT sp FROM StudentProfile sp " +
                     "WHERE sp.cgpa >= :minCgpa " +
                     "AND sp.backlogs <= :maxBacklogs " +
                     "AND sp.department.id IN :departmentIds " +
                     "AND sp.user.college.id = :collegeId")
       Page<StudentProfile> findEligibleStudentsScoped(
                     @Param("minCgpa") Double minCgpa,
                     @Param("maxBacklogs") Integer maxBacklogs,
                     @Param("departmentIds") java.util.Set<Long> departmentIds,
                     @Param("collegeId") Long collegeId,
                     Pageable pageable);
}
