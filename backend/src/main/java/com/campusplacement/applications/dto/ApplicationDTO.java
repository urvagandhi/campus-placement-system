package com.campusplacement.applications.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Application entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long driveId;
    private String driveTitle;
    private String companyName;
    private String status;
    private LocalDateTime appliedAt;
    private String notes;
    private LocalDateTime shortlistedAt;
    private LocalDateTime selectedAt;
}
