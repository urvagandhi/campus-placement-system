package com.campusplacement.auth;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {
    List<SecurityAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<SecurityAlert> findByUserIdOrderByCreatedAtDesc(Long userId);
}
