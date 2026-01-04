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
import com.campusplacement.students.dto.StudentSkillsDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for student profile management.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * Get all student profiles.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getAllStudents() {
        List<StudentProfileDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get student profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> getStudentById(@PathVariable Long id) {
        StudentProfileDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    /**
     * Get current student's profile.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> getMyProfile() {
        StudentProfileDTO profile = studentService.getCurrentStudentProfile();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Create or update student profile.
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> createOrUpdateProfile(
            @Valid @RequestBody StudentProfileDTO profileDTO) {
        StudentProfileDTO saved = studentService.createOrUpdateProfile(profileDTO);
        return ResponseEntity.ok(ApiResponse.success(saved, "Profile saved successfully"));
    }

    /**
     * Update student skills.
     */
    @PatchMapping("/{id}/skills")
    @PreAuthorize("hasRole('STUDENT') or hasAnyRole('TPO', 'ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileDTO>> updateSkills(
            @PathVariable Long id,
            @Valid @RequestBody StudentSkillsDTO skillsDTO) {
        StudentProfileDTO updated = studentService.updateSkills(id, skillsDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Skills updated successfully"));
    }

    /**
     * Get students by department.
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getStudentsByDepartment(
            @PathVariable String department) {
        List<StudentProfileDTO> students = studentService.getStudentsByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * Get eligible students for a drive.
     */
    @GetMapping("/eligible/{driveId}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentProfileDTO>>> getEligibleStudents(
            @PathVariable Long driveId) {
        List<StudentProfileDTO> students = studentService.getEligibleStudentsForDrive(driveId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
