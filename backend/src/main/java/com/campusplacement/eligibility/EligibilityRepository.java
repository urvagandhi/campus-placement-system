package com.campusplacement.eligibility;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for EligibilityResult entity.
 */
@Repository
public interface EligibilityRepository extends JpaRepository<EligibilityResult, Long> {

    Optional<EligibilityResult> findByStudentIdAndDriveId(Long studentId, Long driveId);

    List<EligibilityResult> findByDriveId(Long driveId);

    List<EligibilityResult> findByStudentId(Long studentId);

    List<EligibilityResult> findByDriveIdAndIsEligibleTrue(Long driveId);

    void deleteByDriveId(Long driveId);
}
