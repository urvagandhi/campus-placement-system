package com.campusplacement.applications;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
