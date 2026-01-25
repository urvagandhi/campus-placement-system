package com.campusplacement.users.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.users.UserManagementService;
import com.campusplacement.users.dto.CreateStudentDTO;
import com.campusplacement.users.dto.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.common.exception.UnauthorizedException;
import com.campusplacement.organizations.dto.AcademicEventDTO;
import com.campusplacement.organizations.model.AcademicEvent;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.repository.AcademicEventRepository;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;
import com.campusplacement.organizations.OrganizationScopeService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Coordinator-managed operations.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {

        private final UserManagementService userManagementService;
        private final AcademicEventRepository academicEventRepository;
        private final OrganizationUnitRepository organizationUnitRepository;
        private final OrganizationScopeService scopeService;

        /**
         * Creates a new student.
         * Only accessible by COORDINATOR.
         */
        @PostMapping("/students")
        @PreAuthorize("hasRole('COORDINATOR')")
        public ResponseEntity<ApiResponse<UserDTO>> createStudent(
                        @Valid @RequestBody CreateStudentDTO request) {
                UserDTO user = userManagementService.createStudent(request);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success(user, "Student created successfully"));
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

                @SuppressWarnings("null")
                OrganizationUnit unit = organizationUnitRepository.findById(dto.getOrganizationUnitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Organization Unit not found"));

                if (unit.getCollege() == null || !unit.getCollege().getId().equals(currentUserCollegeId)) {
                        throw new UnauthorizedException(
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
                        @org.springframework.web.bind.annotation.RequestParam List<Long> departmentIds,
                        @org.springframework.web.bind.annotation.RequestParam LocalDate date) {

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
