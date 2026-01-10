package com.campusplacement.students;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.students.dto.StudentProfileDTO;
import com.campusplacement.students.dto.StudentProfileResponseDTO;
import com.campusplacement.students.dto.StudentProfileUpdateDTO;
import com.campusplacement.students.dto.StudentSkillsDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for student profile management.
 *
 * <p>
 * <strong>Security Model:</strong>
 * </p>
 * <ul>
 * <li>Students can only access/update their own profile</li>
 * <li>Students can only update career-layer fields</li>
 * <li>Academic fields are read-only for students</li>
 * <li>Coordinators and Admins have elevated access</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ==================== Student Self-Service Endpoints ====================

    /**
     * Get current student's profile with full organization hierarchy.
     *
     * <p>
     * Returns the authenticated student's complete profile including:
     * <ul>
     * <li>Identity information (name, email)</li>
     * <li>Organization hierarchy (College → Institute → Department)</li>
     * <li>Academic data (read-only)</li>
     * <li>Career data (editable)</li>
     * </ul>
     * </p>
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponseDTO>> getMyProfile() {
        StudentProfileResponseDTO profile = studentService.getCurrentStudentProfile();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Update current student's career-layer profile fields.
     *
     * <p>
     * <strong>Security:</strong> Only career-layer fields can be updated:
     * skills, resumeUrl, projectsCount, internshipMonths, certifications,
     * linkedinUrl, githubUrl, careerInterests.
     * </p>
     *
     * <p>
     * Academic fields (cgpa, enrollmentNo, department, backlogs, batchYear,
     * semester)
     * are IGNORED even if present in the request.
     * </p>
     */
    @PatchMapping("/me/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponseDTO>> updateMyProfile(
            @Valid @RequestBody StudentProfileUpdateDTO updateDTO) {
        StudentProfileResponseDTO updated = studentService.updateCareerProfile(updateDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    // ==================== Admin/Coordinator Endpoints ====================

    /**
     * Get all student profiles.
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getAllStudents() {
        List<StudentProfileDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get student profile by ID.
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> getStudentById(@PathVariable Long id) {
        StudentProfileDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    /**
     * Create or update student profile (admin action).
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN for full field updates.
     * Access: STUDENT for career-layer updates only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> createOrUpdateProfile(
            @Valid @RequestBody StudentProfileDTO profileDTO) {
        StudentProfileDTO saved = studentService.createOrUpdateProfile(profileDTO);
        return ResponseEntity.ok(ApiResponse.success(saved, "Profile saved successfully"));
    }

    /**
     * Update student skills.
     * Access: STUDENT (own profile only), COORDINATOR, ADMIN.
     */
    @PatchMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('STUDENT', 'COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> updateSkills(
            @PathVariable Long id,
            @Valid @RequestBody StudentSkillsDTO skillsDTO) {
        StudentProfileDTO updated = studentService.updateSkills(id, skillsDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Skills updated successfully"));
    }

    /**
     * Get students by department.
     * Access: COORDINATOR, ADMIN, SUPER_ADMIN only.
     *
     * <p>
     * <strong>Defense-in-Depth:</strong> Method-level @PreAuthorize checks
     * department access
     * before service layer validation.
     * </p>
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN') and " +
            "@scopeSecurityService.canAccessDepartment(#departmentId)")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getStudentsByDepartment(
            @PathVariable Long departmentId) {
        List<StudentProfileDTO> students = studentService.getStudentsByDepartmentId(departmentId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get eligible students for a drive.
     * Access: COORDINATOR, ADMIN only.
     */
    @GetMapping("/eligible/{driveId}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getEligibleStudents(
            @PathVariable Long driveId) {
        List<StudentProfileDTO> students = studentService.getEligibleStudentsForDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
