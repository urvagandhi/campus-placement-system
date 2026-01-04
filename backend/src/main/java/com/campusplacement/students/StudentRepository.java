package com.campusplacement.students;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for StudentProfile entity.
 */
@Repository
public interface StudentRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    Optional<StudentProfile> findByEnrollmentNo(String enrollmentNo);

    List<StudentProfile> findByDepartment(String department);

    List<StudentProfile> findByCgpaGreaterThanEqual(Double minCgpa);

    List<StudentProfile> findByDepartmentAndCgpaGreaterThanEqual(String department, Double minCgpa);

    List<StudentProfile> findByBatchYear(Integer batchYear);

    boolean existsByEnrollmentNo(String enrollmentNo);
}
