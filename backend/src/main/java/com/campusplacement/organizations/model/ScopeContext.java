package com.campusplacement.organizations.model;

import java.util.Set;

import com.campusplacement.common.UserRole;
import com.campusplacement.organizations.ScopeContextHolder;

/**
 * Immutable context object containing resolved scope data for a user.
 *
 * <p>
 * This record is the single source of truth for scope-based access control.
 * It is computed once per request and cached in {@link ScopeContextHolder}.
 * </p>
 *
 * <p>
 * <strong>Design Principles:</strong>
 * </p>
 * <ul>
 * <li>Immutable - cannot be modified after creation</li>
 * <li>Request-scoped - computed fresh each request</li>
 * <li>Derived from database - never stored in JWT</li>
 * </ul>
 *
 * @param userId               The user's ID
 * @param collegeId            The user's college (tenant) ID
 * @param role                 The user's system role
 * @param allowedDepartmentIds Set of department IDs the user can access
 * @param allowedOrgUnitIds    Set of all org unit IDs the user can access
 * @param isUniversityScope    True if user has SUBTREE scope at UNIVERSITY
 *                             level
 */
public record ScopeContext(
        Long userId,
        Long collegeId,
        UserRole role,
        Set<Long> allowedDepartmentIds,
        Set<Long> allowedOrgUnitIds,
        boolean isUniversityScope) {

    // ==================== Role Helpers (Derived, Not Structural)
    // ====================

    /**
     * Checks if user is platform owner (no restrictions).
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    /**
     * Checks if user is college administrator.
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Checks if user is placement coordinator.
     */
    public boolean isCoordinator() {
        return role == UserRole.COORDINATOR;
    }

    /**
     * Checks if user is a student.
     */
    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }

    // ==================== Access Level Helpers ====================

    /**
     * Checks if user has full college access.
     * This applies to:
     * <ul>
     * <li>ADMIN role users</li>
     * <li>COORDINATOR with SUBTREE scope at UNIVERSITY level</li>
     * </ul>
     */
    public boolean hasFullCollegeAccess() {
        return isAdmin() || isUniversityScope;
    }

    /**
     * Checks if user has scoped (department-limited) access.
     * This applies to COORDINATOR users with org unit assignments.
     */
    public boolean hasScopedAccess() {
        return isCoordinator() && !allowedDepartmentIds.isEmpty();
    }

    /**
     * Checks if user can access a specific department.
     *
     * @param departmentId The department ID to check
     * @return true if user has access to this department
     */
    public boolean canAccessDepartment(Long departmentId) {
        if (isSuperAdmin()) {
            return true;
        }
        if (hasFullCollegeAccess()) {
            return true; // College-level access includes all departments
        }
        return allowedDepartmentIds.contains(departmentId);
    }
}
