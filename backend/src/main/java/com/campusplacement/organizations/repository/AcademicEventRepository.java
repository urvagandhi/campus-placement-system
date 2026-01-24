package com.campusplacement.organizations.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campusplacement.organizations.model.AcademicEvent;

@Repository
public interface AcademicEventRepository extends JpaRepository<AcademicEvent, Long> {

    @Query("SELECT e FROM AcademicEvent e JOIN FETCH e.organizationUnit WHERE e.organizationUnit.id IN :unitIds AND e.startDate <= :endDate AND e.endDate >= :startDate")
    List<AcademicEvent> findConflicts(@Param("unitIds") List<Long> unitIds, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<AcademicEvent> findByOrganizationUnitId(Long unitId);

    @Query("SELECT e FROM AcademicEvent e JOIN FETCH e.organizationUnit ou WHERE ou.college.id = :collegeId")
    List<AcademicEvent> findByCollegeId(@Param("collegeId") Long collegeId);
}
