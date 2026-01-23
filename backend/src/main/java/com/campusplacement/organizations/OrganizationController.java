package com.campusplacement.organizations;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.organizations.dto.AcademicEventDTO;
import com.campusplacement.organizations.dto.OrganizationUnitDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

        private final OrganizationUnitRepository organizationUnitRepository;
        private final AcademicEventRepository academicEventRepository;
        private final com.campusplacement.students.StudentRepository studentRepository;
        private final com.campusplacement.applications.ApplicationRepository applicationRepository;
        private final OrganizationScopeService scopeService;

        @GetMapping("/departments")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<List<OrganizationUnitDTO>>> getDepartments() {
                Long collegeId = scopeService.getCurrentUserScope().collegeId();
                List<OrganizationUnitDTO> dtos = organizationUnitRepository.findAllDepartmentsByCollegeId(collegeId)
                                .stream()
                                .map(unit -> {
                                        long studentCount = studentRepository.countByDepartmentIdInAndCollegeId(
                                                        java.util.Collections.singleton(unit.getId()), collegeId);
                                        long placementCount = applicationRepository
                                                        .countSelectedByDepartmentIds(
                                                                        java.util.Collections.singleton(unit.getId()));

                                        return OrganizationUnitDTO.builder()
                                                        .id(unit.getId())
                                                        .name(unit.getName())
                                                        .code(unit.getCode())
                                                        .type(unit.getType())
                                                        .parentId(unit.getParent() != null ? unit.getParent().getId()
                                                                        : null)
                                                        .isActive(unit.getIsActive())
                                                        .studentCount((int) studentCount)
                                                        .placementCount((int) placementCount)
                                                        .build();
                                })
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success(dtos));
        }

        @GetMapping("/events")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
        public ResponseEntity<ApiResponse<List<AcademicEventDTO>>> getEvents() {
                Long collegeId = scopeService.getCurrentUserScope().collegeId();
                List<AcademicEventDTO> events = academicEventRepository.findByCollegeId(collegeId)
                                .stream()
                                .map(this::mapToEventDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success(events));
        }

        @PostMapping("/events")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
        public ResponseEntity<ApiResponse<AcademicEventDTO>> createEvent(@RequestBody AcademicEventDTO dto) {
                Long currentUserCollegeId = scopeService.getCurrentUserScope().collegeId();

                // Basic permission check - ensure unit belongs to user's college
                @SuppressWarnings("null")
                OrganizationUnit unit = organizationUnitRepository.findById(dto.getOrganizationUnitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Organization Unit not found"));

                // Validate unit.collegeId == currentUser.collegeId
                if (unit.getCollege() == null || !unit.getCollege().getId().equals(currentUserCollegeId)) {
                        throw new com.campusplacement.common.exception.UnauthorizedException(
                                        "Cannot create event for organization unit outside your college");
                }

                AcademicEvent event = AcademicEvent.builder()
                                .name(dto.getName())
                                .eventType(dto.getEventType())
                                .startDate(dto.getStartDate())
                                .endDate(dto.getEndDate())
                                .description(dto.getDescription())
                                .organizationUnit(unit)
                                .build();

                @SuppressWarnings("null")
                AcademicEvent saved = academicEventRepository.save(event);
                return ResponseEntity.ok(ApiResponse.success(mapToEventDTO(saved)));
        }

        @GetMapping("/conflicts")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
        public ResponseEntity<ApiResponse<List<AcademicEventDTO>>> checkConflicts(
                        @RequestParam List<Long> departmentIds,
                        @RequestParam LocalDate date) {

                List<AcademicEventDTO> conflicts = academicEventRepository.findConflicts(departmentIds, date, date)
                                .stream()
                                .map(this::mapToEventDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(conflicts));
        }

        private AcademicEventDTO mapToEventDTO(AcademicEvent event) {
                return AcademicEventDTO.builder()
                                .id(event.getId())
                                .name(event.getName())
                                .eventType(event.getEventType())
                                .startDate(event.getStartDate())
                                .endDate(event.getEndDate())
                                .description(event.getDescription())
                                .organizationUnitId(event.getOrganizationUnit().getId())
                                .organizationUnitName(event.getOrganizationUnit().getName())
                                .build();
        }
}
