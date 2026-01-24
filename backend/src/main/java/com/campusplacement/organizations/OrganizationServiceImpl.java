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
                    .orElseThrow(() -> new ResourceNotFoundException("Root organization unit not found for college"));
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
        Long collegeId = scopeService.getCurrentUserScope().collegeId();

        // Find the root university for this college
        OrganizationUnit root = organizationUnitRepository.findByCollegeIdAndIsRootTrue(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Root organization unit not found for college"));

        // Fetch all units for the college to build hierarchy in memory (avoids N+1)
        List<OrganizationUnit> allUnits = organizationUnitRepository.findByCollegeId(collegeId);

        // Build the tree starting from root
        return buildHierarchyDTO(root, allUnits, collegeId);
    }

    /**
     * Recursively build hierarchical DTO from entity.
     */
    private OrganizationUnitDTO buildHierarchyDTO(OrganizationUnit unit, List<OrganizationUnit> allUnits,
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
                .map(child -> buildHierarchyDTO(child, allUnits, collegeId))
                .collect(Collectors.toList());

        // Aggregate counts for Institutes from their children (Departments)
        if (unit.getType() == OrganizationUnitType.INSTITUTE) {
            studentCount = childDTOs.stream()
                    .mapToInt(child -> child.getStudentCount() != null ? child.getStudentCount() : 0)
                    .sum();
            placementCount = childDTOs.stream()
                    .mapToInt(child -> child.getPlacementCount() != null ? child.getPlacementCount() : 0)
                    .sum();
        }

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
                .build();
    }
}
