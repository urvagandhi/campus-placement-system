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
import com.campusplacement.organizations.model.AcademicEvent;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.repository.AcademicEventRepository;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;

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
        private final OrganizationService organizationService; // Inject new service

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

        @GetMapping("/institutes")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
        public ResponseEntity<ApiResponse<List<OrganizationUnitDTO>>> getInstitutes() {
                Long collegeId = scopeService.getCurrentUserScope().collegeId();
                List<OrganizationUnitDTO> dtos = organizationUnitRepository
                                .findByCollegeIdAndType(collegeId,
                                                com.campusplacement.common.OrganizationUnitType.INSTITUTE)
                                .stream()
                                .map(unit -> OrganizationUnitDTO.builder()
                                                .id(unit.getId())
                                                .name(unit.getName())
                                                .code(unit.getCode())
                                                .type(unit.getType())
                                                .parentId(unit.getParent() != null ? unit.getParent().getId() : null)
                                                .isActive(unit.getIsActive())
                                                .build())
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success(dtos));
        }

        /**
         * Get the complete organizational hierarchy for the current user's college.
         * Returns a nested tree structure: University -> Institutes -> Departments
         * with student and placement counts at the department level.
         */
        @GetMapping("/hierarchy")
        @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
        public ResponseEntity<ApiResponse<OrganizationUnitDTO>> getHierarchy() {
                OrganizationUnitDTO hierarchy = organizationService.getHierarchy();
                return ResponseEntity.ok(ApiResponse.success(hierarchy));
        }

        /**
         * Create a new Organization Unit (Department/Institute).
         */
        @PostMapping("/units")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<OrganizationUnitDTO>> createOrganizationUnit(
                        @RequestBody @jakarta.validation.Valid com.campusplacement.organizations.dto.CreateOrganizationUnitDTO request) {
                OrganizationUnitDTO created = organizationService.createOrganizationUnit(request);
                return ResponseEntity.ok(ApiResponse.success(created, "Department created successfully"));
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
