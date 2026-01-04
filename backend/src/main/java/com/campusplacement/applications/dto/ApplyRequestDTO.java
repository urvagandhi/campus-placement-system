package com.campusplacement.applications.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for applying to a placement drive.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRequestDTO {

    @NotNull(message = "Drive ID is required")
    private Long driveId;

    private String notes;
}
