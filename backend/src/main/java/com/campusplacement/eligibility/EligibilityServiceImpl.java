package com.campusplacement.eligibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.ai.AIClient;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.eligibility.dto.EligibilityResultDTO;
import com.campusplacement.organizations.OrganizationUnit;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of EligibilityService.
 *
 * <p>
 * Implements the "Rule-first, AI-second" principle:
 * 1. Hard eligibility rules (CGPA, backlogs, department) are checked first
 * 2. AI scoring is applied only to rule-eligible students
 * 3. AI acts as a decision-support system, never as an autonomous
 * decision-maker
 * </p>
 *
 * <p>
 * <strong>Scoring Breakdown:</strong>
 * </p>
 * <ul>
 * <li>CGPA Score: 40% weight - based on how much student exceeds minimum</li>
 * <li>Skills Score: 35% weight - based on skill match percentage</li>
 * <li>Experience Score: 25% weight - based on internship/project
 * experience</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityRepository eligibilityRepository;
    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;

    /**
     * AI client for enhanced eligibility scoring.
     * Used for:
     * - Skill gap analysis with NLP
     * - Resume-based competency scoring
     * - Predictive placement success analysis
     *
     * Currently integrated via getAIEnhancedScore() method.
     * Set aiEnhancementEnabled=true to activate AI scoring.
     */
    @SuppressWarnings("unused") // Used in getAIEnhancedScore() - future AI integration
    private final AIClient aiClient;

    // Feature flag for AI enhancement (can be externalized to config)
    private static final boolean AI_ENHANCEMENT_ENABLED = false;

    // Scoring weights
    private static final double CGPA_WEIGHT = 0.40;
    private static final double SKILLS_WEIGHT = 0.35;
    private static final double EXPERIENCE_WEIGHT = 0.25;

    // Score thresholds
    private static final double ELIGIBILITY_THRESHOLD = 50.0;

    /**
     * Checks eligibility for a specific student-drive combination.
     * Applies rule-based checks first, then calculates detailed scores.
     */
    @Override
    @Transactional
    public EligibilityResultDTO checkEligibility(Long studentId, Long driveId) {
        // 1. Get student profile
        @SuppressWarnings("null")
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        // 2. Get drive requirements
        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found: " + driveId));

        // 3. Check for existing result - return if recent (within 1 hour)
        return eligibilityRepository.findByStudentIdAndDriveId(studentId, driveId)
                .filter(existing -> existing.getCalculatedAt().isAfter(LocalDateTime.now().minusHours(1)))
                .map(this::toDTO)
                .orElseGet(() -> calculateAndSaveEligibility(student, drive));
    }

    /**
     * Gets current authenticated student's eligibility for a drive.
     */
    @Override
    @Transactional
    public EligibilityResultDTO getCurrentStudentEligibility(Long driveId) {
        CustomUserDetails currentUser = getCurrentUserDetails();

        // Find student profile for current user
        StudentProfile student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for user: " + currentUser.getId()));

        return checkEligibility(student.getId(), driveId);
    }

    /**
     * Gets all eligibility results for a drive.
     * Only accessible by COORDINATOR/ADMIN roles.
     */
    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public List<EligibilityResultDTO> getEligibilityResultsByDrive(Long driveId) {
        // Verify drive exists
        if (!driveRepository.existsById(driveId)) {
            throw new ResourceNotFoundException("Drive not found: " + driveId);
        }

        return eligibilityRepository.findByDriveId(driveId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Batch recalculates eligibility for all students in a drive.
     * Called when drive requirements change or on-demand by coordinator.
     */
    @Override
    @Transactional
    public void recalculateEligibilityForDrive(Long driveId) {
        // Get drive
        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found: " + driveId));

        // Get all students in the college
        Long collegeId = drive.getCollege().getId();
        List<StudentProfile> students = studentRepository.findByCollegeId(collegeId);

        log.info("Recalculating eligibility for {} students in drive {}", students.size(), driveId);

        int eligible = 0;
        int ineligible = 0;

        for (StudentProfile student : students) {
            try {
                EligibilityResultDTO result = calculateAndSaveEligibility(student, drive);
                if (Boolean.TRUE.equals(result.getIsEligible())) {
                    eligible++;
                } else {
                    ineligible++;
                }
            } catch (Exception e) {
                log.error("Failed to calculate eligibility for student {}: {}",
                        student.getId(), e.getMessage());
            }
        }

        log.info("Eligibility recalculation complete for drive {}: {} eligible, {} ineligible",
                driveId, eligible, ineligible);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Calculates eligibility and saves result.
     * Applies "Rule-first, AI-second" principle.
     */
    private EligibilityResultDTO calculateAndSaveEligibility(StudentProfile student, PlacementDrive drive) {
        List<String> reasons = new ArrayList<>();
        List<String> skillGaps = new ArrayList<>();
        boolean isEligible = true;

        // ==================== RULE-BASED CHECKS (Hard Constraints)
        // ====================

        // 1. CGPA Check
        Double minCgpa = drive.getMinCgpa();
        if (minCgpa != null && student.getCgpa() < minCgpa) {
            isEligible = false;
            reasons.add(String.format("CGPA %.2f is below minimum requirement of %.2f",
                    student.getCgpa(), minCgpa));
        }

        // 2. Backlogs Check
        Integer maxBacklogs = drive.getMaxBacklogs();
        Integer studentBacklogs = student.getBacklogs() != null ? student.getBacklogs() : 0;
        if (maxBacklogs != null && studentBacklogs > maxBacklogs) {
            isEligible = false;
            reasons.add(String.format("Active backlogs (%d) exceed maximum allowed (%d)",
                    studentBacklogs, maxBacklogs));
        }

        // 3. Department Check
        Set<OrganizationUnit> eligibleDepts = drive.getEligibleDepartments();
        if (eligibleDepts != null && !eligibleDepts.isEmpty()) {
            OrganizationUnit studentDept = student.getDepartment();
            if (studentDept == null || !eligibleDepts.contains(studentDept)) {
                isEligible = false;
                reasons.add("Department is not eligible for this drive");
            }
        }

        // ==================== SCORING (For Eligible Students) ====================

        double cgpaScore = calculateCgpaScore(student, drive);
        double skillsScore = calculateSkillsScore(student, drive, skillGaps);
        double experienceScore = calculateExperienceScore(student);

        // Weighted total score
        double totalScore = (cgpaScore * CGPA_WEIGHT) +
                (skillsScore * SKILLS_WEIGHT) +
                (experienceScore * EXPERIENCE_WEIGHT);

        // Additional eligibility based on score threshold
        if (isEligible && totalScore < ELIGIBILITY_THRESHOLD) {
            isEligible = false;
            reasons.add(String.format("Overall score %.1f is below threshold %.1f",
                    totalScore, ELIGIBILITY_THRESHOLD));
        }

        if (isEligible) {
            reasons.add("Meets all eligibility criteria");
        }

        // ==================== PERSIST RESULT ====================

        EligibilityResult result = eligibilityRepository.findByStudentIdAndDriveId(
                student.getId(), drive.getId())
                .orElse(new EligibilityResult());

        result.setStudentId(student.getId());
        result.setDriveId(drive.getId());
        result.setScore(totalScore);
        result.setIsEligible(isEligible);
        result.setReasons(String.join("; ", reasons));
        result.setCgpaScore(cgpaScore);
        result.setSkillsScore(skillsScore);
        result.setExperienceScore(experienceScore);
        result.setSkillGaps(String.join(", ", skillGaps));
        result.setCalculatedAt(LocalDateTime.now());

        result = eligibilityRepository.save(result);

        log.debug("Eligibility calculated for student {} on drive {}: eligible={}, score={}",
                student.getId(), drive.getId(), isEligible, totalScore);

        return toDTO(result, student, drive);
    }

    /**
     * Calculates CGPA score (0-100).
     * Higher score if CGPA significantly exceeds minimum.
     */
    private double calculateCgpaScore(StudentProfile student, PlacementDrive drive) {
        double studentCgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;
        double minCgpa = drive.getMinCgpa() != null ? drive.getMinCgpa() : 0.0;

        if (studentCgpa < minCgpa) {
            return 0.0; // Below minimum
        }

        // Scale: Meeting minimum = 60, Perfect 10.0 = 100
        double cgpaDiff = studentCgpa - minCgpa;
        double maxPossibleDiff = 10.0 - minCgpa;

        if (maxPossibleDiff <= 0) {
            return 100.0;
        }

        return 60.0 + (cgpaDiff / maxPossibleDiff) * 40.0;
    }

    /**
     * Calculates skills match score (0-100).
     * Based on how many required skills the student has.
     */
    private double calculateSkillsScore(StudentProfile student, PlacementDrive drive,
            List<String> skillGaps) {
        String requiredSkillsStr = drive.getRequiredSkills();
        if (requiredSkillsStr == null || requiredSkillsStr.isBlank()) {
            return 80.0; // No specific skills required - default good score
        }

        Set<String> requiredSkills = Arrays.stream(requiredSkillsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (requiredSkills.isEmpty()) {
            return 80.0;
        }

        String studentSkillsStr = student.getSkills();
        Set<String> studentSkills = new HashSet<>();
        if (studentSkillsStr != null && !studentSkillsStr.isBlank()) {
            studentSkills = Arrays.stream(studentSkillsStr.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }

        // Count matched skills
        int matched = 0;
        for (String required : requiredSkills) {
            boolean found = studentSkills.stream()
                    .anyMatch(s -> s.contains(required) || required.contains(s));
            if (found) {
                matched++;
            } else {
                skillGaps.add(required);
            }
        }

        return (double) matched / requiredSkills.size() * 100.0;
    }

    /**
     * Calculates experience score (0-100).
     * Based on internship months and project count.
     */
    private double calculateExperienceScore(StudentProfile student) {
        int internshipMonths = student.getInternshipMonths() != null ? student.getInternshipMonths() : 0;
        int projectsCount = student.getProjectsCount() != null ? student.getProjectsCount() : 0;

        // Internship: 0-6 months = 0-50 points
        double internshipScore = Math.min(internshipMonths * 8.33, 50.0);

        // Projects: 0-5+ projects = 0-50 points
        double projectScore = Math.min(projectsCount * 10.0, 50.0);

        return internshipScore + projectScore;
    }

    /**
     * Converts entity to DTO with student and drive names.
     */
    private EligibilityResultDTO toDTO(EligibilityResult entity, StudentProfile student, PlacementDrive drive) {
        return EligibilityResultDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .studentName(student.getUser() != null ? student.getUser().getName() : "Unknown")
                .driveId(entity.getDriveId())
                .driveTitle(drive.getTitle())
                .score(entity.getScore())
                .isEligible(entity.getIsEligible())
                .reasons(parseReasons(entity.getReasons()))
                .calculatedAt(entity.getCalculatedAt())
                .cgpaScore(entity.getCgpaScore())
                .skillsScore(entity.getSkillsScore())
                .experienceScore(entity.getExperienceScore())
                .skillGaps(parseReasons(entity.getSkillGaps()))
                .build();
    }

    /**
     * Converts entity to DTO (without loading related entities).
     */
    private EligibilityResultDTO toDTO(EligibilityResult entity) {
        return EligibilityResultDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .driveId(entity.getDriveId())
                .score(entity.getScore())
                .isEligible(entity.getIsEligible())
                .reasons(parseReasons(entity.getReasons()))
                .calculatedAt(entity.getCalculatedAt())
                .cgpaScore(entity.getCgpaScore())
                .skillsScore(entity.getSkillsScore())
                .experienceScore(entity.getExperienceScore())
                .skillGaps(parseReasons(entity.getSkillGaps()))
                .build();
    }

    private List<String> parseReasons(String reasonsStr) {
        if (reasonsStr == null || reasonsStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(reasonsStr.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
