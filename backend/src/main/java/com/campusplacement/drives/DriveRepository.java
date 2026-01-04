package com.campusplacement.drives;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PlacementDrive entity.
 */
@Repository
public interface DriveRepository extends JpaRepository<PlacementDrive, Long> {

    List<PlacementDrive> findByCompanyId(Long companyId);

    List<PlacementDrive> findByStatus(String status);

    List<PlacementDrive> findByDriveDateAfter(LocalDate date);

    List<PlacementDrive> findByStatusAndDriveDateAfter(String status, LocalDate date);

    List<PlacementDrive> findByRegistrationDeadlineAfter(LocalDate date);

    List<PlacementDrive> findByEligibleDepartmentsContaining(String department);
}
