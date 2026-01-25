package com.campusplacement.organizations;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;

import com.campusplacement.organizations.dto.OrganizationUnitDTO;

import com.campusplacement.organizations.repository.OrganizationUnitRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

        private final OrganizationUnitRepository organizationUnitRepository;

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

        /**
         * Delete an Organization Unit.
         */
        @org.springframework.web.bind.annotation.DeleteMapping("/units/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteOrganizationUnit(
                        @org.springframework.web.bind.annotation.PathVariable Long id) {
                organizationService.deleteOrganizationUnit(id);
                return ResponseEntity.ok(ApiResponse.success(null, "Organization unit deleted successfully"));
        }

}
