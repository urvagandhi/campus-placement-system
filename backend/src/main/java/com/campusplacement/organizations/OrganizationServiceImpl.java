package com.campusplacement.organizations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.applications.ApplicationRepository;
import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.organizations.dto.CreateOrganizationUnitDTO;
import com.campusplacement.organizations.dto.OrganizationUnitDTO;
import com.campusplacement.organizations.model.OrganizationUnit;
import com.campusplacement.organizations.repository.OrganizationUnitRepository;
import com.campusplacement.students.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

        private final OrganizationUnitRepository organizationUnitRepository;
        private final CollegeRepository collegeRepository;
        private final OrganizationScopeService scopeService;
        private final StudentRepository studentRepository;
        private final ApplicationRepository applicationRepository;
        private final com.campusplacement.organizations.repository.UserAssignmentRepository userAssignmentRepository;
        private final com.campusplacement.users.UserRepository userRepository;

        @SuppressWarnings("null")
        @Override
        @Transactional
        public OrganizationUnitDTO createOrganizationUnit(CreateOrganizationUnitDTO request) {
                Long collegeId = scopeService.getCurrentUserScope().collegeId();

                College college = collegeRepository.findById(collegeId)
                                .orElseThrow(() -> new ResourceNotFoundException("College not found"));

                OrganizationUnit parent = null;
                if (request.getParentUnitId() != null) {
                        parent = organizationUnitRepository.findById(request.getParentUnitId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Parent unit not found"));

                        // Validate parent belongs to same college
                        if (!parent.getCollege().getId().equals(collegeId)) {
                                throw new IllegalArgumentException("Parent unit must belong to the same college");
                        }
                } else if (request.getType() == OrganizationUnitType.INSTITUTE) {
                        // Auto-assign to Root University if no parent specified for Institute
                        parent = organizationUnitRepository.findByCollegeIdAndIsRootTrue(collegeId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Root organization unit not found for college"));
                }

                OrganizationUnit unit = OrganizationUnit.builder()
                                .name(request.getName())
                                .code(request.getCode())
                                .type(request.getType())
                                .college(college)
                                .parent(parent)
                                .isActive(true)
                                .isRoot(false)
                                .build();

                OrganizationUnit savedUnit = organizationUnitRepository.save(unit);

                return OrganizationUnitDTO.builder()
                                .id(savedUnit.getId())
                                .name(savedUnit.getName())
                                .code(savedUnit.getCode())
                                .type(savedUnit.getType())
                                .parentId(savedUnit.getParent() != null ? savedUnit.getParent().getId() : null)
                                .isActive(savedUnit.getIsActive())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrganizationUnitDTO getHierarchy() {
                com.campusplacement.organizations.model.ScopeContext scope = scopeService.getCurrentUserScope();
                Long collegeId = scope.collegeId();

                OrganizationUnit root;

                if (scope.isUniversityScope()) {
                        // University Admin / Super Admin gets the full tree
                        root = organizationUnitRepository.findByCollegeIdAndIsRootTrue(collegeId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Root organization unit not found for college"));
                } else {
                        // Restricted scope (Director / HOD)
                        // Find the effective root: the unit whose parent is NOT in the allowed set
                        java.util.Set<Long> allowedIds = scope.allowedOrgUnitIds();

                        // We need to examine the allowed units to find the top-most one
                        @SuppressWarnings("null")
                        List<OrganizationUnit> allowedUnits = organizationUnitRepository.findAllById(allowedIds);

                        root = allowedUnits.stream()
                                        .filter(u -> u.getParent() == null
                                                        || !allowedIds.contains(u.getParent().getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No accessible organization units found"));
                }

                // Fetch all units for the college (we'll filter in buildHierarchyDTO logic)
                // Optimization: For restricted scope, we could only fetch the subtree, but
                // keeping it simple for now
                // as buildHierarchyDTO performs filtering.
                List<OrganizationUnit> allUnits = organizationUnitRepository.findByCollegeId(collegeId);

                // Fetch all staff assignments for the college
                List<com.campusplacement.organizations.model.UserAssignment> allStaffAssignments = userAssignmentRepository
                                .findAllStaffByCollegeId(collegeId);

                // Build the tree starting from the determined root
                // Note: We reuse buildHierarchyDTO but need to ensure it strictly follows the
                // subtree of 'root'
                return buildHierarchyDTO(root, allUnits, allStaffAssignments, collegeId);
        }

        /**
         * Recursively build hierarchical DTO from entity.
         */
        private OrganizationUnitDTO buildHierarchyDTO(OrganizationUnit unit,
                        List<OrganizationUnit> allUnits,
                        List<com.campusplacement.organizations.model.UserAssignment> allStaffAssignments,
                        Long collegeId) {

                // Filter children from the in-memory list
                List<OrganizationUnit> children = allUnits.stream()
                                .filter(u -> u.getParent() != null && u.getParent().getId().equals(unit.getId()))
                                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                                .collect(Collectors.toList());

                // Calculate stats for departments
                Integer studentCount = null;
                Integer placementCount = null;
                if (unit.getType() == OrganizationUnitType.DEPARTMENT) {
                        long studentCountLong = studentRepository.countByDepartmentIdInAndCollegeId(
                                        Collections.singleton(unit.getId()), collegeId);
                        long placementCountLong = applicationRepository.countSelectedByDepartmentIds(
                                        Collections.singleton(unit.getId()));
                        studentCount = Long.valueOf(studentCountLong).intValue();
                        placementCount = Long.valueOf(placementCountLong).intValue();
                }

                // Recursively build children DTOs
                List<OrganizationUnitDTO> childDTOs = children.stream()
                                .map(child -> buildHierarchyDTO(child, allUnits, allStaffAssignments, collegeId))
                                .collect(Collectors.toList());

                // Aggregate counts for Institutes from their children (Departments)
                if (unit.getType() == OrganizationUnitType.INSTITUTE) {
                        studentCount = childDTOs.stream()
                                        .mapToInt(child -> child.getStudentCount() != null ? child.getStudentCount()
                                                        : 0)
                                        .sum();
                        placementCount = childDTOs.stream()
                                        .mapToInt(child -> child.getPlacementCount() != null ? child.getPlacementCount()
                                                        : 0)
                                        .sum();
                }

                // Filter staff for this unit
                List<com.campusplacement.organizations.dto.OrganizationStaffDTO> staff = allStaffAssignments.stream()
                                .filter(ua -> ua.getOrganizationUnit().getId().equals(unit.getId()))
                                .map(ua -> com.campusplacement.organizations.dto.OrganizationStaffDTO.builder()
                                                .userId(ua.getUser().getId())
                                                .name(ua.getUser().getName())
                                                .email(ua.getUser().getEmail())
                                                .role(ua.getUser().getRole().name())
                                                .designation(ua.getDesignation())
                                                .scopeLevel(ua.getScopeLevel())
                                                .scopeLevel(ua.getScopeLevel())
                                                .isPrimary(ua.getIsPrimary())
                                                .userIsActive(ua.getUser().getIsActive())
                                                .phoneNumber(ua.getUser().getPhoneNumber())
                                                .build())
                                .collect(Collectors.toList());

                return OrganizationUnitDTO.builder()
                                .id(unit.getId())
                                .name(unit.getName())
                                .code(unit.getCode())
                                .type(unit.getType())
                                .parentId(unit.getParent() != null ? unit.getParent().getId() : null)
                                .isActive(unit.getIsActive())
                                .studentCount(studentCount)
                                .placementCount(placementCount)
                                .children(childDTOs.isEmpty() ? null : childDTOs)
                                .staff(staff)
                                .build();
        }

        @Override
        @Transactional
        public void deleteOrganizationUnit(Long id) {
                if (id == null) {
                        throw new IllegalArgumentException("Organization Unit ID cannot be null");
                }
                com.campusplacement.organizations.model.ScopeContext scope = scopeService.getCurrentUserScope();
                Long collegeId = scope.collegeId();

                OrganizationUnit unit = organizationUnitRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Organization Unit not found"));

                // Validate college ownership
                if (!unit.getCollege().getId().equals(collegeId)) {
                        throw new ResourceNotFoundException("Organization Unit not found");
                }

                // Prevent deleting Root University
                if (Boolean.TRUE.equals(unit.getIsRoot())) {
                        throw new IllegalArgumentException("Cannot delete the root University organization unit");
                }

                // 1. Get all unit IDs in the subtree (PostgreSQL CTE)
                List<Long> subtreeIds = organizationUnitRepository.findSubtreeIds(id, collegeId);

                // 2. Find all unique users assigned to any unit in the subtree
                // This includes both staff (COORDINATOR, ADMIN) and STUDENTS
                List<com.campusplacement.organizations.model.UserAssignment> assignments = userAssignmentRepository
                                .findAllByOrganizationUnitIdIn(subtreeIds);

                java.util.Set<com.campusplacement.users.User> usersToDelete = assignments.stream()
                                .map(com.campusplacement.organizations.model.UserAssignment::getUser)
                                .collect(Collectors.toSet());

                // 3. Soft-delete all associated users
                // JPA will handle soft-deletion visibility via @SQLRestriction("deleted_at IS
                // NULL")
                usersToDelete.forEach(com.campusplacement.users.User::softDelete);
                userRepository.saveAll(usersToDelete);

                // 4. Soft-delete all organization units in the subtree
                @SuppressWarnings("null")
                List<OrganizationUnit> unitsToDelete = organizationUnitRepository.findAllById(subtreeIds);
                unitsToDelete.forEach(OrganizationUnit::softDelete);
                organizationUnitRepository.saveAll(unitsToDelete);
        }
}
