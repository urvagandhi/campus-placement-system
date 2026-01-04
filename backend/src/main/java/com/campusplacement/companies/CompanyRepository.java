package com.campusplacement.companies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Company entity.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByIndustry(String industry);

    List<Company> findByIsActiveTrue();

    List<Company> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);
}
