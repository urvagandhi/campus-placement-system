package com.campusplacement.resume;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.ai.dto.ResumeParseResponseDTO;
import com.campusplacement.common.ApiResponse;
import com.campusplacement.resume.dto.ResumeParseRequest;
import com.campusplacement.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for resume parsing operations.
 *
 * <p>
 * <strong>Access Control:</strong>
 * </p>
 * <ul>
 * <li>STUDENT: Can parse own resume only</li>
 * <li>COORDINATOR/ADMIN: Cannot parse student resumes directly</li>
 * </ul>
 *
 * <p>
 * Resume parsing is student-owned career data, aligned with the
 * student data ownership model.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resume Parsing", description = "AI-assisted resume parsing and skill extraction")
@SecurityRequirement(name = "bearerAuth")
public class ResumeParsingController {

    private final ResumeParsingService resumeParsingService;

    /**
     * Parse the current student's resume.
     *
     * <p>
     * Extracts skills, experience level, and other structured data
     * from resume text. The parsed data is stored as derived metadata
     * and never overwrites the original resume.
     * </p>
     *
     * @param request Resume text content to parse
     * @return Parsed resume data with explanation
     */
    @PostMapping("/parse")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Parse own resume", description = "Extracts skills and experience from resume text. Students can only parse their own resume.")
    public ResponseEntity<ApiResponse<ResumeParseResponseDTO>> parseOwnResume(
            @Valid @RequestBody ResumeParseRequest request) {

        Long currentUserId = getCurrentUserId();
        log.info("Resume parse request from user {}", currentUserId);

        ResumeParseResponseDTO result = resumeParsingService.parseResume(currentUserId, request.getResumeText());

        return ResponseEntity.ok(ApiResponse.success(result, "Resume parsed successfully"));
    }

    /**
     * Get previously parsed resume data for the current student.
     *
     * @return Previously parsed data if available
     */
    @GetMapping("/parsed")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get parsed resume data", description = "Retrieves previously extracted skills and experience from student's resume.")
    public ResponseEntity<ApiResponse<ParsedResumeData>> getParsedResume() {
        Long currentUserId = getCurrentUserId();

        ParsedResumeData parsed = resumeParsingService.getParsedResumeForStudent(currentUserId);

        if (parsed == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No parsed resume data found"));
        }

        return ResponseEntity.ok(ApiResponse.success(parsed, "Parsed resume data retrieved"));
    }

    /**
     * Invalidate cached parsed resume data.
     * Call this when uploading a new resume.
     *
     * @return Success response
     */
    @PostMapping("/invalidate")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Invalidate parsed resume cache", description = "Clears cached parsed data. Call this after uploading a new resume.")
    public ResponseEntity<ApiResponse<Void>> invalidateCache() {
        Long currentUserId = getCurrentUserId();

        resumeParsingService.invalidateParsedResume(currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "Resume cache invalidated"));
    }

    // ==================== Private Helpers ====================

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new AccessDeniedException("User not authenticated");
    }
}
