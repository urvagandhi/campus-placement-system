package com.campusplacement.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resume parsing from the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseRequest {

    /**
     * Raw text content extracted from the resume file.
     * The frontend is responsible for extracting text from PDF/DOC.
     */
    @NotBlank(message = "Resume text is required")
    @Size(min = 50, max = 100000, message = "Resume text must be between 50 and 100000 characters")
    private String resumeText;
}
