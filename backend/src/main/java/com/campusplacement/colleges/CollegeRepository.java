package com.campusplacement.colleges;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for College entity operations.
 */
@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    /**
     * Find a college by its unique code.
     *
     * @param code the college code
     * @return Optional containing the college if found
     */
    Optional<College> findByCode(String code);

    /**
     * Check if a college with the given code exists.
     *
     * @param code the college code
     * @return true if exists
     */
    boolean existsByCode(String code);
}
