package com.campusplacement.students.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student-editable profile fields only.
 *
 * <p>
 * <strong>Security Note:</strong> This DTO intentionally excludes all
 * institution-owned fields (enrollmentNo, departmentId, cgpa, backlogs,
 * batchYear, semester). Students can ONLY update career-layer fields.
 * </p>
 *
 * <p>
 * Any attempt to include academic fields in this DTO will be:
 * <ul>
 * <li>Ignored at the service layer</li>
 * <li>Logged for security audit</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileUpdateDTO {

    /**
     * Technical and soft skills.
     * Maximum 50 skills allowed.
     */
    @Size(max = 50, message = "Maximum 50 skills allowed")
    private List<String> skills;

    /**
     * URL to uploaded resume.
     * Must be a valid URL format.
     */
    @Pattern(regexp = "^(https?://.*)?$", message = "Resume URL must be a valid URL")
    private String resumeUrl;

    /**
     * Number of completed projects.
     * Must be non-negative.
     */
    @Min(value = 0, message = "Project count cannot be negative")
    private Integer projectsCount;

    /**
     * Total months of internship experience.
     * Must be non-negative.
     */
    @Min(value = 0, message = "Internship months cannot be negative")
    private Integer internshipMonths;

    /**
     * Professional certifications earned.
     * Maximum 30 certifications allowed.
     */
    @Size(max = 30, message = "Maximum 30 certifications allowed")
    private List<String> certifications;

    /**
     * LinkedIn profile URL.
     */
    @Pattern(regexp = "^(https?://(www\\.)?linkedin\\.com/.*)?$", 
             message = "LinkedIn URL must be a valid LinkedIn profile URL")
    private String linkedinUrl;

    /**
     * GitHub profile URL.
     */
    @Pattern(regexp = "^(https?://(www\\.)?github\\.com/.*)?$", 
             message = "GitHub URL must be a valid GitHub profile URL")
    private String githubUrl;

    /**
     * Career interests/aspirations.
     * Example: ["Backend Development", "DevOps", "Machine Learning"]
     * Maximum 10 interests allowed.
     */
    @Size(max = 10, message = "Maximum 10 career interests allowed")
    private List<String> careerInterests;
}
