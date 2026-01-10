package com.campusplacement.resume;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ParsedResumeData entity.
 */
@Repository
public interface ParsedResumeRepository extends JpaRepository<ParsedResumeData, Long> {

    /**
     * Finds parsed resume data by student ID.
     *
     * @param studentId Student profile ID
     * @return Parsed data if exists
     */
    Optional<ParsedResumeData> findByStudentId(Long studentId);

    /**
     * Checks if parsed data exists for a student.
     *
     * @param studentId Student profile ID
     * @return true if exists
     */
    boolean existsByStudentId(Long studentId);

    /**
     * Deletes parsed resume data by student ID.
     *
     * @param studentId Student profile ID
     */
    @Modifying
    @Query("DELETE FROM ParsedResumeData p WHERE p.studentId = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
