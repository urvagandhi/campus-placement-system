package com.campusplacement.resume;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.ai.AIOrchestrationService;
import com.campusplacement.ai.AIResultPersistence;
import com.campusplacement.ai.dto.ResumeParseResponseDTO;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.model.ScopeContext;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for resume parsing operations.
 *
 * <p>
 * Orchestrates resume parsing, stores parsed metadata,
 * and integrates with the AI service.
 * </p>
 *
 * <p>
 * <strong>Data Ownership:</strong> Parsed resume data is stored
 * as derived metadata. The original resume is never modified.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ResumeParsingService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParsingService.class);

    private final AIOrchestrationService aiOrchestrationService;
    private final AIResultPersistence aiResultPersistence;
    private final OrganizationScopeService scopeService;
    private final StudentRepository studentRepository;
    private final ParsedResumeRepository parsedResumeRepository;

    /**
     * Parses a resume and stores the extracted data.
     *
     * @param userId     User ID of the student
     * @param resumeText Raw text content of the resume
     * @return Parsed resume data with explanation
     */
    @Transactional
    public ResumeParseResponseDTO parseResume(Long userId, String resumeText) {
        log.debug("Parsing resume for user {}", userId);

        // Get student profile
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Parse via AI orchestration
        ResumeParseResponseDTO result = aiOrchestrationService.parseResume(student.getId(), resumeText);

        if (result.isSuccess()) {
            // Store parsed data
            saveParsedResume(student, result);
        }

        return result;
    }

    /**
     * Gets previously parsed resume data for a student.
     *
     * @param userId User ID of the student
     * @return Parsed resume data or null if not found
     */
    @Transactional(readOnly = true)
    public ParsedResumeData getParsedResumeForStudent(Long userId) {
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return parsedResumeRepository.findByStudentId(student.getId()).orElse(null);
    }

    /**
     * Invalidates cached parsed resume for a student.
     *
     * @param userId User ID of the student
     */
    @Transactional
    public void invalidateParsedResume(Long userId) {
        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        ScopeContext scope = scopeService.resolveScope(userId);

        // Invalidate AI cache
        aiResultPersistence.invalidateResumeParse(scope.collegeId(), student.getId());

        // Delete stored parsed data
        parsedResumeRepository.deleteByStudentId(student.getId());

        log.info("Invalidated parsed resume for student {}", student.getId());
    }

    // ==================== Private Helpers ====================

    private void saveParsedResume(StudentProfile student, ResumeParseResponseDTO result) {
        // Check for existing record
        Optional<ParsedResumeData> existing = parsedResumeRepository.findByStudentId(student.getId());

        ParsedResumeData entity;
        if (existing.isPresent()) {
            entity = existing.get();
        } else {
            entity = new ParsedResumeData();
            entity.setStudentId(student.getId());
        }

        // Update fields
        entity.setExtractedSkills(String.join(",", result.getSkills()));
        entity.setExperienceLevel(result.getExperienceLevel());
        entity.setConfidenceScore(result.getConfidenceScore());
        entity.setProjectKeywords(result.getProjectKeywords() != null
                ? String.join(",", result.getProjectKeywords())
                : null);
        entity.setEducationSignals(result.getEducationSignals() != null
                ? String.join(",", result.getEducationSignals())
                : null);
        entity.setParsedAt(LocalDateTime.now());

        parsedResumeRepository.save(entity);

        log.debug("Saved parsed resume data for student {}", student.getId());
    }
}
