package com.campusplacement.insights;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.applications.ApplicationRepository;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.insights.dto.AggregatedInsight;
import com.campusplacement.insights.dto.SkillTrendInsight;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for AI-generated insights.
 *
 * <p>
 * <strong>Aggregation Rules:</strong>
 * </p>
 * <ul>
 * <li>STUDENT: Self-only insights</li>
 * <li>COORDINATOR: Aggregated stats (no PII)</li>
 * <li>ADMIN: College-wide aggregates</li>
 * <li>SUPER_ADMIN: Platform-wide aggregates</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AIInsightsService {

    private static final Logger log = LoggerFactory.getLogger(AIInsightsService.class);

    private final OrganizationScopeService scopeService;
    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;

    // ==================== Student Insights ====================

    /**
     * Gets personalized insights for the current student.
     *
     * @return Student-specific career insights
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public AIInsightsController.StudentInsights getStudentInsights() {
        Long userId = getCurrentUserId();

        StudentProfile student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Analyze student's skills
        List<String> studentSkills = parseSkills(student.getSkills());

        // Get common required skills from active drives
        ScopeContext scope = scopeService.resolveScope(userId);
        List<PlacementDrive> activeDrives = driveRepository.findActiveByCollegeId(scope.collegeId());

        Map<String, Integer> requiredSkillsCount = new HashMap<>();
        for (PlacementDrive drive : activeDrives) {
            List<String> driveSkills = parseSkills(drive.getRequiredSkills());
            for (String skill : driveSkills) {
                requiredSkillsCount.merge(skill.toLowerCase(), 1, Integer::sum);
            }
        }

        // Find strong skills (student has + in demand)
        List<String> strongSkills = studentSkills.stream()
                .filter(s -> requiredSkillsCount.containsKey(s.toLowerCase()))
                .limit(5)
                .collect(Collectors.toList());

        // Find skills to improve (in demand but student lacks)
        List<String> skillsToImprove = requiredSkillsCount.entrySet().stream()
                .filter(e -> e.getValue() >= 2) // Required by at least 2 drives
                .filter(e -> studentSkills.stream().noneMatch(s -> s.equalsIgnoreCase(e.getKey())))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Generate recommendations
        List<String> certifications = new ArrayList<>();
        if (skillsToImprove.contains("aws") || skillsToImprove.contains("cloud")) {
            certifications.add("AWS Cloud Practitioner");
        }
        if (skillsToImprove.contains("java") || skillsToImprove.contains("spring")) {
            certifications.add("Spring Professional Certification");
        }

        // Calculate readiness score
        double readiness = strongSkills.isEmpty() ? 50.0 : Math.min(100.0, 60.0 + (strongSkills.size() * 8.0));

        return new AIInsightsController.StudentInsights(
                readiness,
                strongSkills,
                skillsToImprove,
                certifications,
                generateCareerAdvice(strongSkills, skillsToImprove));
    }

    // ==================== Coordinator Insights ====================

    /**
     * Gets aggregated drive insights for coordinator.
     * No student-level PII exposed.
     *
     * @return Aggregated drive insights
     */
    @Transactional(readOnly = true)
    public List<AggregatedInsight> getAggregatedDriveInsights() {
        log.debug("Fetching aggregated drive insights");
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        List<PlacementDrive> drives = driveRepository.findActiveByCollegeId(scope.collegeId());
        List<AggregatedInsight> insights = new ArrayList<>();

        for (PlacementDrive drive : drives) {
            long totalApps = applicationRepository.countByDriveId(drive.getId());
            long shortlisted = applicationRepository.countByDriveIdAndStatus(drive.getId(), "SHORTLISTED");
            long selected = applicationRepository.countByDriveIdAndStatus(drive.getId(), "SELECTED");

            Double selectionRate = totalApps > 0 ? (selected * 100.0 / totalApps) : 0.0;

            insights.add(AggregatedInsight.builder()
                    .driveId(drive.getId())
                    .driveTitle(drive.getTitle())
                    .totalApplications((int) totalApps)
                    .shortlistedCount((int) shortlisted)
                    .selectedCount((int) selected)
                    .selectionRate(selectionRate)
                    .insightSummary(generateDriveInsightSummary(totalApps, selected, drive.getTitle()))
                    .build());
        }

        return insights;
    }

    /**
     * Gets skill gap trends for coordinator.
     *
     * @return Skill gap analysis
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<SkillTrendInsight> getSkillGapTrends() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        // Count skills in student profiles
        List<StudentProfile> students = studentRepository.findByCollegeId(scope.collegeId());
        Map<String, Integer> studentSkillCount = new HashMap<>();
        for (StudentProfile student : students) {
            for (String skill : parseSkills(student.getSkills())) {
                studentSkillCount.merge(skill.toLowerCase(), 1, Integer::sum);
            }
        }

        // Count skills required by drives
        List<PlacementDrive> drives = driveRepository.findActiveByCollegeId(scope.collegeId());
        Map<String, Integer> driveSkillCount = new HashMap<>();
        for (PlacementDrive drive : drives) {
            for (String skill : parseSkills(drive.getRequiredSkills())) {
                driveSkillCount.merge(skill.toLowerCase(), 1, Integer::sum);
            }
        }

        // Calculate gaps
        List<SkillTrendInsight> trends = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : driveSkillCount.entrySet()) {
            String skill = entry.getKey();
            int drivesRequiring = entry.getValue();
            int studentsWithSkill = studentSkillCount.getOrDefault(skill, 0);

            double gapRatio = drivesRequiring > 0
                    ? (drivesRequiring - studentsWithSkill) / (double) drivesRequiring
                    : 0.0;

            String priority = gapRatio > 0.5 ? "HIGH" : (gapRatio > 0.2 ? "MEDIUM" : "LOW");

            trends.add(SkillTrendInsight.builder()
                    .skill(skill)
                    .studentsWithSkill(studentsWithSkill)
                    .drivesRequiring(drivesRequiring)
                    .gapRatio(gapRatio)
                    .priority(priority)
                    .recommendation(generateSkillRecommendation(skill, priority))
                    .build());
        }

        // Sort by gap ratio descending
        trends.sort((a, b) -> Double.compare(b.getGapRatio(), a.getGapRatio()));

        return trends.stream().limit(10).collect(Collectors.toList());
    }

    // ==================== Admin Insights ====================

    /**
     * Gets college-wide placement insights.
     *
     * @return College-wide statistics
     */
    @Transactional(readOnly = true)
    public AIInsightsController.CollegeInsights getCollegeInsights() {
        Long userId = getCurrentUserId();
        ScopeContext scope = scopeService.resolveScope(userId);

        long totalStudents = studentRepository.countByCollegeId(scope.collegeId());
        long placedCount = applicationRepository.countPlacedStudentsByCollegeId(scope.collegeId());
        double placementRate = totalStudents > 0 ? (placedCount * 100.0 / totalStudents) : 0.0;

        // Get top skills in demand
        List<SkillTrendInsight> trends = getSkillGapTrends();
        List<String> topSkillsInDemand = trends.stream()
                .limit(5)
                .map(SkillTrendInsight::getSkill)
                .collect(Collectors.toList());

        List<String> mostCommonSkillGaps = trends.stream()
                .filter(t -> "HIGH".equals(t.getPriority()))
                .limit(5)
                .map(SkillTrendInsight::getSkill)
                .collect(Collectors.toList());

        // Calculate average package from placed students
        Double avgPackage = applicationRepository.calculateAveragePackageByCollegeId(scope.collegeId());
        if (avgPackage == null)
            avgPackage = 0.0;

        log.debug("College insights: {} students, {} placed, {:.2f}% rate, {:.2f} avg package",
                totalStudents, placedCount, placementRate, avgPackage);

        return new AIInsightsController.CollegeInsights(
                (int) totalStudents,
                (int) placedCount,
                placementRate,
                avgPackage,
                topSkillsInDemand,
                mostCommonSkillGaps);
    }

    // ==================== Super Admin Insights ====================

    /**
     * Gets platform-wide insights.
     *
     * @return Platform-wide statistics
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public AIInsightsController.PlatformInsights getPlatformInsights() {
        log.info("Generating platform-wide insights for SUPER_ADMIN");

        // Platform-wide metrics
        long totalStudents = studentRepository.count();
        long totalDrives = driveRepository.count();
        long placedStudents = applicationRepository.countPlacedStudents();

        // Calculate overall placement rate
        double overallPlacementRate = totalStudents > 0
                ? (placedStudents * 100.0 / totalStudents)
                : 0.0;

        // Get top hiring companies (by number of selections)
        List<String> topHiringCompanies = applicationRepository.findTopHiringCompanyNames(5);

        // Get most in-demand skills across all drives
        List<PlacementDrive> allDrives = driveRepository.findAll();
        Map<String, Integer> skillDemand = new HashMap<>();
        for (PlacementDrive drive : allDrives) {
            for (String skill : parseSkills(drive.getRequiredSkills())) {
                skillDemand.merge(skill.toLowerCase(), 1, Integer::sum);
            }
        }
        List<String> topSkillsDemanded = skillDemand.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Count unique colleges (distinct from drives)
        long totalColleges = driveRepository.countDistinctColleges();

        log.info("Platform insights: {} colleges, {} students, {} drives, {:.2f}% placement rate",
                totalColleges, totalStudents, totalDrives, overallPlacementRate);

        return new AIInsightsController.PlatformInsights(
                (int) totalColleges,
                (int) totalStudents,
                (int) totalDrives,
                overallPlacementRate,
                topHiringCompanies,
                topSkillsDemanded);
    }

    // ==================== Private Helpers ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }

    private List<String> parseSkills(String skillsString) {
        if (skillsString == null || skillsString.isBlank()) {
            return List.of();
        }
        return Arrays.stream(skillsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String generateCareerAdvice(List<String> strongSkills, List<String> skillsToImprove) {
        if (strongSkills.isEmpty()) {
            return "Focus on building foundational skills in your target domain.";
        }
        if (skillsToImprove.isEmpty()) {
            return "Excellent profile! Consider advanced certifications to stand out.";
        }
        return String.format("Your %s skills are strong. Consider improving %s to increase your opportunities.",
                String.join(", ", strongSkills.subList(0, Math.min(2, strongSkills.size()))),
                String.join(", ", skillsToImprove.subList(0, Math.min(2, skillsToImprove.size()))));
    }

    private String generateDriveInsightSummary(long totalApps, long selected, String driveTitle) {
        if (totalApps == 0) {
            return "No applications received yet for " + driveTitle;
        }
        double rate = selected * 100.0 / totalApps;
        if (rate > 20) {
            return driveTitle + " has a strong selection rate.";
        } else if (rate > 5) {
            return driveTitle + " has a competitive selection rate.";
        } else {
            return driveTitle + " is highly competitive.";
        }
    }

    private String generateSkillRecommendation(String skill, String priority) {
        return switch (priority) {
            case "HIGH" -> "Consider organizing " + skill + " training sessions.";
            case "MEDIUM" -> "Encourage students to learn " + skill + " through online courses.";
            default -> skill + " is well represented in student profiles.";
        };
    }
}
