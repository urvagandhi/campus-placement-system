package com.campusplacement.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SystemSettings entity.
 */
@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
}
