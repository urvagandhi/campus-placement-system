package com.campusplacement.students;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.organizations.OrganizationUnit;
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
}

