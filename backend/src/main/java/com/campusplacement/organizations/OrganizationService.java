package com.campusplacement.organizations;

import com.campusplacement.organizations.dto.CreateOrganizationUnitDTO;
import com.campusplacement.organizations.dto.OrganizationUnitDTO;

public interface OrganizationService {
    OrganizationUnitDTO createOrganizationUnit(CreateOrganizationUnitDTO request);

    /**
     * Get the complete organizational hierarchy for the current user's college.
     * Returns a tree structure: University -> Institutes -> Departments
     * with student and placement counts at the department level.
     */
    OrganizationUnitDTO getHierarchy();

    void deleteOrganizationUnit(Long id);
}
