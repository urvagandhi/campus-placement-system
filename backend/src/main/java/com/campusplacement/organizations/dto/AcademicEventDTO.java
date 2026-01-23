package com.campusplacement.organizations.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicEventDTO {
    private Long id;
    private String name;
    private String eventType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long organizationUnitId;
    private String organizationUnitName;
    private String description;
}
