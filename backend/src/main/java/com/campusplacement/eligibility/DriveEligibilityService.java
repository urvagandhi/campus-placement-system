package com.campusplacement.eligibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.eligibility.dto.EligibilityCheckResult;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.model.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for checking student eligibility for placement drives.
 *
 * <p>
 * <strong>Backend-Only Eligibility:</strong> All eligibility checks are
 * performed
 * server-side. Frontend must never make eligibility decisions.
 * </p>
 *
 * <p>
 * <strong>Eligibility Criteria:</strong>
 * </p>
 * <ul>
 * <li>CGPA >= drive.minCgpa</li>
 * <li>backlogs <= drive.maxBacklogs</li>
 * <li>department in drive.eligibleDepartments</li>
 * <li>student.college == drive.college</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DriveEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(DriveEligibilityService.class);

    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;
    private final OrganizationScopeService scopeService;
    private final EligibilityRepository eligibilityRepository;

    /**
     * Check if a student is eligible for a drive.
     * Uses cached results if available (within last 24 hours).
     *
     * @param studentId Student profile ID
     * @param driveId   Placement drive ID
     * @return EligibilityCheckResult with isEligible flag and reasons list
     */
    @Transactional(readOnly = true)
    public EligibilityCheckResult checkEligibility(Long studentId, Long driveId) {
        // Check cache first
        Optional<EligibilityResult> cachedResult = eligibilityRepository.findByStudentIdAndDriveId(studentId, driveId);

        if (cachedResult.isPresent()) {
            EligibilityResult cached = cachedResult.get();

            // Use cache if less than 24 hours old
            if (cached.getCalculatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
                log.debug("Using cached eligibility result for student {} and drive {}", studentId, driveId);
                return convertToCheckResult(cached);
            }
        }

        // Calculate fresh result
        @SuppressWarnings("null")
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        EligibilityCheckResult result = evaluateEligibility(student, drive);

        // Cache the result
        cacheEligibilityResult(studentId, driveId, result);

        return result;
    }

    /**
     * Check if the current authenticated student is eligible for a drive.
     *
     * @param driveId Placement drive ID
     * @return EligibilityCheckResult with isEligible flag and reasons list
     */
    @Transactional(readOnly = true)
    public EligibilityCheckResult checkCurrentStudentEligibility(Long driveId) {
        Long userId = getCurrentUserId();

        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        return evaluateEligibility(student, drive);
    }

    /**
     * Get all eligible students for a drive (scope-enforced).
     * Only returns students within the coordinator's scope.
     *
     * @param driveId       Placement drive ID
     * @param coordinatorId User ID of the coordinator requesting
     * @return List of eligible student profiles within scope
     */
    @Transactional(readOnly = true)
    public List<StudentProfile> getEligibleStudents(Long driveId, Long coordinatorId) {
        ScopeContext scope = scopeService.resolveScope(coordinatorId);

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate coordinator has access to drive's college
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                throw new AccessDeniedException("No access to this drive");
            }
        }

        // Get students based on scope
        List<StudentProfile> studentsInScope;
        if (scope.isSuperAdmin() || scope.hasFullCollegeAccess()) {
            studentsInScope = studentRepository.findByCollegeId(
                    drive.getCollege() != null ? drive.getCollege().getId() : scope.collegeId());
        } else if (scope.hasScopedAccess()) {
            studentsInScope = studentRepository.findByDepartmentIdInAndCollegeId(
                    scope.allowedDepartmentIds(),
                    scope.collegeId());
        } else {
            return Collections.emptyList();
        }

        // Filter by eligibility
        return studentsInScope.stream()
                .filter(student -> evaluateEligibility(student, drive).isEligible())
                .collect(Collectors.toList());
    }

    /**
     * Get all eligible students for a drive using scoped query.
     * More efficient than filtering in memory.
     *
     * @param driveId Placement drive ID
     * @return List of eligible student profiles
     */
    @Transactional(readOnly = true)
    public List<StudentProfile> getEligibleStudentsOptimized(Long driveId) {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        @SuppressWarnings("null")
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        // Validate access
        if (!scope.isSuperAdmin()) {
            Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;
            if (!scope.collegeId().equals(driveCollegeId)) {
                throw new AccessDeniedException("No access to this drive");
            }
        }

        Double minCgpa = drive.getMinCgpa() != null ? drive.getMinCgpa() : 0.0;
        Integer maxBacklogs = drive.getMaxBacklogs() != null ? drive.getMaxBacklogs() : Integer.MAX_VALUE;
        Set<Long> allowedDepts = scope.hasFullCollegeAccess() || scope.isSuperAdmin()
                ? null // All departments
                : scope.allowedDepartmentIds();

        // If specific departments required
        if (allowedDepts != null && !allowedDepts.isEmpty()) {
            return studentRepository.findEligibleStudentsScoped(
                    minCgpa,
                    maxBacklogs,
                    allowedDepts,
                    scope.collegeId());
        }

        // Full college access - use drive's eligible departments if specified
        if (drive.getEligibleDepartments() != null && !drive.getEligibleDepartments().isEmpty()) {
            Set<Long> eligibleDeptIds = drive.getEligibleDepartments().stream()
                    .map(OrganizationUnit::getId)
                    .collect(Collectors.toSet());

            if (!eligibleDeptIds.isEmpty()) {
                return studentRepository.findEligibleStudentsScoped(
                        minCgpa,
                        maxBacklogs,
                        eligibleDeptIds,
                        scope.collegeId() != null ? scope.collegeId()
                                : (drive.getCollege() != null ? drive.getCollege().getId() : null));
            }
        }

        // Fallback: get all eligible by CGPA and backlogs
        return studentRepository.findByCollegeId(
                scope.collegeId() != null ? scope.collegeId()
                        : (drive.getCollege() != null ? drive.getCollege().getId() : null))
                .stream()
                .filter(s -> s.getCgpa() != null && s.getCgpa() >= minCgpa)
                .filter(s -> s.getBacklogs() == null || s.getBacklogs() <= maxBacklogs)
                .collect(Collectors.toList());
    }

    /**
     * Validate application before creation.
     * Throws exception if ineligible.
     *
     * @param studentId Student profile ID
     * @param driveId   Placement drive ID
     * @throws IllegalStateException if student is not eligible
     */
    @Transactional(readOnly = true)
    public void validateApplication(Long studentId, Long driveId) {
        EligibilityCheckResult result = checkEligibility(studentId, driveId);

        if (!result.isEligible()) {
            String reasons = String.join("; ", result.reasons());
            throw new IllegalStateException("Student is not eligible for this drive: " + reasons);
        }
    }

    // ==================== Private Methods ====================

    private EligibilityCheckResult evaluateEligibility(StudentProfile student, PlacementDrive drive) {
        List<String> reasons = new ArrayList<>();
        boolean isEligible = true;

        // 1. College match
        Long studentCollegeId = student.getUser() != null && student.getUser().getCollege() != null
                ? student.getUser().getCollege().getId()
                : null;
        Long driveCollegeId = drive.getCollege() != null ? drive.getCollege().getId() : null;

        if (studentCollegeId == null || !studentCollegeId.equals(driveCollegeId)) {
            isEligible = false;
            reasons.add("Student and drive are not in the same college");
        }

        // 2. CGPA check
        if (drive.getMinCgpa() != null) {
            Double studentCgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;
            if (studentCgpa < drive.getMinCgpa()) {
                isEligible = false;
                reasons.add(String.format("CGPA %.2f is below minimum %.2f", studentCgpa, drive.getMinCgpa()));
            }
        }

        // 3. Backlogs check
        if (drive.getMaxBacklogs() != null) {
            Integer studentBacklogs = student.getBacklogs() != null ? student.getBacklogs() : 0;
            if (studentBacklogs > drive.getMaxBacklogs()) {
                isEligible = false;
                reasons.add(String.format("Backlogs %d exceeds maximum %d", studentBacklogs, drive.getMaxBacklogs()));
            }
        }

        // 4. Department check
        Set<OrganizationUnit> eligibleDepts = drive.getEligibleDepartments();
        if (eligibleDepts != null && !eligibleDepts.isEmpty()) {
            OrganizationUnit studentDept = student.getDepartment();

            if (studentDept == null) {
                isEligible = false;
                reasons.add("Student has no department assigned");
            } else {
                boolean deptMatch = eligibleDepts.stream()
                        .anyMatch(d -> d.getId().equals(studentDept.getId()));
                if (!deptMatch) {
                    isEligible = false;
                    reasons.add("Department '" + studentDept.getName() + "' is not eligible for this drive");
                }
            }
        }

        if (isEligible) {
            reasons.add("All eligibility criteria met");
        }

        log.debug("Eligibility check for student {} on drive {}: eligible={}, reasons={}",
                student.getId(), drive.getId(), isEligible, reasons);

        double totalScore = calculateEligibilityScore(student, drive);

        // Calculate component scores for transparency
        double cgpaScore = calculateCgpaScore(student, drive);
        double skillsScore = calculateSkillsScore(student, drive);

        return EligibilityCheckResult.builder()
                .isEligible(isEligible)
                .reasons(reasons)
                .score(totalScore)
                .cgpaScore(cgpaScore)
                .skillsScore(skillsScore)
                .experienceScore(0.0) // Placeholder for future implementation
                .skillGaps(calculateSkillGaps(student, drive))
                .build();
    }

    private double calculateEligibilityScore(StudentProfile student, PlacementDrive drive) {
        double score = 0.0;

        // CGPA contribution (40%)
        if (student.getCgpa() != null) {
            score += (student.getCgpa() / 10.0) * 40;
        }

        // No backlogs bonus (20%)
        if (student.getBacklogs() == null || student.getBacklogs() == 0) {
            score += 20;
        }

        // Skills match (40%) - with fuzzy logic using Levenshtein distance
        if (drive.getRequiredSkills() != null && student.getSkills() != null) {
            List<String> requiredSkills = Arrays.asList(drive.getRequiredSkills().toLowerCase().split(","));
            List<String> studentSkills = Arrays.asList(student.getSkills().toLowerCase().split(","));

            double matchScore = 0.0;
            for (String reqSkill : requiredSkills) {
                String reqTrimmed = reqSkill.trim();
                double bestMatch = 0.0;

                for (String stuSkill : studentSkills) {
                    String stuTrimmed = stuSkill.trim();

                    // Exact match
                    if (stuTrimmed.equals(reqTrimmed)) {
                        bestMatch = 1.0;
                        break;
                    }

                    // Contains match
                    if (stuTrimmed.contains(reqTrimmed) || reqTrimmed.contains(stuTrimmed)) {
                        bestMatch = Math.max(bestMatch, 0.8);
                        continue;
                    }

                    // Fuzzy match using Levenshtein distance
                    double similarity = calculateSimilarity(reqTrimmed, stuTrimmed);
                    if (similarity >= 0.75) { // 75% similarity threshold
                        bestMatch = Math.max(bestMatch, similarity);
                    }
                }

                matchScore += bestMatch;
            }

            if (!requiredSkills.isEmpty()) {
                score += (matchScore / requiredSkills.size()) * 40;
            } else {
                score += 40; // No required skills = full score
            }
        } else {
            score += 20; // Partial score if no skills data
        }

        return Math.min(100.0, Math.max(0.0, score));
    }

    /**
     * Calculate CGPA component score.
     */
    private double calculateCgpaScore(StudentProfile student, PlacementDrive drive) {
        if (drive.getMinCgpa() == null) {
            return 40.0; // Max CGPA score when no requirement
        }

        Double studentCgpa = student.getCgpa() != null ? student.getCgpa() : 0.0;

        if (studentCgpa < drive.getMinCgpa()) {
            return 0.0;
        }

        // Score based on how much above minimum
        double excess = studentCgpa - drive.getMinCgpa();
        double maxPossible = 10.0 - drive.getMinCgpa(); // Assuming 10.0 CGPA scale

        if (maxPossible <= 0) {
            return 40.0;
        }

        return 40.0 * Math.min(1.0, excess / maxPossible);
    }

    /**
     * Calculate skills component score.
     */
    private double calculateSkillsScore(StudentProfile student, PlacementDrive drive) {
        if (drive.getRequiredSkills() == null || student.getSkills() == null) {
            return 20.0; // Partial score if no skills data
        }

        List<String> requiredSkills = Arrays.asList(drive.getRequiredSkills().toLowerCase().split(","));
        List<String> studentSkills = Arrays.asList(student.getSkills().toLowerCase().split(","));

        double matchScore = 0.0;
        for (String reqSkill : requiredSkills) {
            String reqTrimmed = reqSkill.trim();
            double bestMatch = 0.0;

            for (String stuSkill : studentSkills) {
                String stuTrimmed = stuSkill.trim();

                if (stuTrimmed.equals(reqTrimmed)) {
                    bestMatch = 1.0;
                    break;
                }

                if (stuTrimmed.contains(reqTrimmed) || reqTrimmed.contains(stuTrimmed)) {
                    bestMatch = Math.max(bestMatch, 0.8);
                    continue;
                }

                double similarity = calculateSimilarity(reqTrimmed, stuTrimmed);
                if (similarity >= 0.75) {
                    bestMatch = Math.max(bestMatch, similarity);
                }
            }

            matchScore += bestMatch;
        }

        if (!requiredSkills.isEmpty()) {
            return (matchScore / requiredSkills.size()) * 40.0;
        }

        return 40.0;
    }

    /**
     * Calculate skill gaps for student.
     */
    private String calculateSkillGaps(StudentProfile student, PlacementDrive drive) {
        if (drive.getRequiredSkills() == null || student.getSkills() == null) {
            return null;
        }

        List<String> requiredSkills = Arrays.asList(drive.getRequiredSkills().toLowerCase().split(","));
        List<String> studentSkills = Arrays.asList(student.getSkills().toLowerCase().split(","));
        List<String> gaps = new ArrayList<>();

        for (String reqSkill : requiredSkills) {
            String reqTrimmed = reqSkill.trim();
            boolean hasSkill = false;

            for (String stuSkill : studentSkills) {
                String stuTrimmed = stuSkill.trim();

                if (stuTrimmed.equals(reqTrimmed) ||
                        stuTrimmed.contains(reqTrimmed) ||
                        reqTrimmed.contains(stuTrimmed)) {
                    hasSkill = true;
                    break;
                }

                double similarity = calculateSimilarity(reqTrimmed, stuTrimmed);
                if (similarity >= 0.75) {
                    hasSkill = true;
                    break;
                }
            }

            if (!hasSkill) {
                gaps.add(reqTrimmed);
            }
        }

        return gaps.isEmpty() ? null : String.join(", ", gaps);
    }

    /**
     * Calculate similarity between two strings using Levenshtein distance.
     *
     * @param s1 first string
     * @param s2 second string
     * @return similarity score between 0.0 and 1.0
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }

        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Calculate Levenshtein distance between two strings.
     *
     * @param s1 first string
     * @param s2 second string
     * @return minimum number of single-character edits required
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1, // deletion
                                dp[i][j - 1] + 1 // insertion
                        ),
                        dp[i - 1][j - 1] + cost // substitution
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Convert cached EligibilityResult to EligibilityCheckResult.
     */
    private EligibilityCheckResult convertToCheckResult(EligibilityResult cached) {
        List<String> reasons = new ArrayList<>();
        if (cached.getReasons() != null && !cached.getReasons().isEmpty()) {
            reasons = Arrays.asList(cached.getReasons().split(","));
        }

        return EligibilityCheckResult.builder()
                .isEligible(cached.getIsEligible())
                .score(cached.getScore())
                .reasons(reasons)
                .cgpaScore(cached.getCgpaScore())
                .skillsScore(cached.getSkillsScore())
                .experienceScore(cached.getExperienceScore())
                .skillGaps(cached.getSkillGaps())
                .build();
    }

    /**
     * Cache eligibility result for future lookups.
     */
    @Transactional
    private void cacheEligibilityResult(Long studentId, Long driveId, EligibilityCheckResult result) {
        try {
            // Check if result already exists
            Optional<EligibilityResult> existing = eligibilityRepository.findByStudentIdAndDriveId(studentId, driveId);

            EligibilityResult entity;
            if (existing.isPresent()) {
                entity = existing.get();
            } else {
                entity = new EligibilityResult();
                entity.setStudentId(studentId);
                entity.setDriveId(driveId);
            }

            // Update values
            entity.setIsEligible(result.isEligible());
            entity.setScore(result.score());
            entity.setReasons(result.reasons() != null ? String.join(",", result.reasons()) : null);
            entity.setCgpaScore(result.cgpaScore());
            entity.setSkillsScore(result.skillsScore());
            entity.setExperienceScore(result.experienceScore());
            entity.setSkillGaps(result.skillGaps());
            entity.setCalculatedAt(LocalDateTime.now());

            eligibilityRepository.save(entity);

            log.debug("Cached eligibility result for student {} and drive {}", studentId, driveId);
        } catch (Exception e) {
            // Don't fail the request if caching fails
            log.warn("Failed to cache eligibility result: {}", e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
