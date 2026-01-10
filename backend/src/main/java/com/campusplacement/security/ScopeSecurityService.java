package com.campusplacement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.campusplacement.organizations.OrganizationScopeService;

import lombok.RequiredArgsConstructor;

/**
 * Security service for method-level scope access checks.
 *
 * <p>
 * Used in @PreAuthorize SpEL expressions for defense-in-depth security.
 * Example: @PreAuthorize("@scopeSecurityService.canAccessDepartment(#departmentId)")
 * </p>
 *
 * <p>
 * <strong>Why:</strong> Provides an additional security layer at method level,
 * complementing service-level checks.
 * </p>
 */
@Service("scopeSecurityService")
@RequiredArgsConstructor
public class ScopeSecurityService {

    private final OrganizationScopeService scopeService;

    /**
     * Checks if the current authenticated user can access a specific department.
     *
     * @param departmentId Department ID to check
     * @return true if user has access, false otherwise
     */
    public boolean canAccessDepartment(Long departmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Long userId = getCurrentUserId(auth);
        if (userId == null) {
            return false;
        }

        return scopeService.canAccessDepartment(userId, departmentId);
    }

    /**
     * Checks if the current user can access a specific college.
     *
     * @param collegeId College ID to check
     * @return true if user belongs to this college, false otherwise
     */
    public boolean canAccessCollege(Long collegeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Long userId = getCurrentUserId(auth);
        if (userId == null) {
            return false;
        }

        return scopeService.resolveScope(userId).collegeId().equals(collegeId);
    }

    /**
     * Checks if the current user has full college access (ADMIN or SUBTREE at
     * UNIVERSITY).
     *
     * @return true if user has full college access
     */
    public boolean hasFullCollegeAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Long userId = getCurrentUserId(auth);
        if (userId == null) {
            return false;
        }

        return scopeService.resolveScope(userId).hasFullCollegeAccess();
    }

    /**
     * Extracts user ID from authentication principal.
     */
    private Long getCurrentUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }
}
