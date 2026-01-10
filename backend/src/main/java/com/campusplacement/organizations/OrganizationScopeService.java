package com.campusplacement.organizations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.ScopeLevel;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * Service for resolving organization-based access scope.
 *
 * <p>
 * This is the <strong>single source of truth</strong> for scope resolution.
 * All data access queries should use this service to determine allowed
 * departments.
 * </p>
 *
 * <p>
 * <strong>Algorithm:</strong>
 * </p>
 *
 * <pre>
 * 0. COLLEGE GUARD (Mandatory - Defense in Depth)
 *    a. Fetch User with college_id
 *    b. For each assignment, verify: organizationUnit.college_id == user.college_id
 *    c. If mismatch: LOG SECURITY ALERT + SKIP assignment
 *
 * 1. Fetch all active UserAssignments for userId
 * 2. For each VALID assignment:
 *    a. Get the assigned OrganizationUnit
 *    b. Based on scopeLevel:
 *       - SELF: Add only this unit
 *       - CHILDREN: Add this unit + direct children
 *       - SUBTREE: Add this unit + recursive descendants
 * 3. Filter to only DEPARTMENT type units
 * 4. Return Set of department IDs (all guaranteed same college)
 * </pre>
 *
 * <p>
 * <strong>Design Principles:</strong>
 * </p>
 * <ul>
 * <li>Scope is resolved from database at runtime, never stored in JWT</li>
 * <li>Request-scoped caching via {@link ScopeContextHolder}</li>
 * <li>Defense-in-depth with explicit college validation</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OrganizationScopeService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationScopeService.class);
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private final UserRepository userRepository;
    private final UserAssignmentRepository assignmentRepository;
    private final OrganizationUnitRepository orgUnitRepository;
    private final ScopeContextHolder scopeContextHolder;
    private final MeterRegistry meterRegistry;

    // ==================== Public API ====================

    /**
     * Resolves the complete scope context for a user.
     * Results are cached per-request via {@link ScopeContextHolder}.
     *
     * <p>
     * <strong>Performance Monitoring:</strong>
     * </p>
     * <ul>
     * <li>Tracks scope resolution time via Micrometer metrics</li>
     * <li>Tags metrics with cache hit/miss status</li>
     * <li>Alerts if resolution exceeds performance thresholds</li>
     * </ul>
     *
     * @param userId User ID to resolve scope for
     * @return ScopeContext containing all access information
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public ScopeContext resolveScope(Long userId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean cached = false;

        try {
            // Check request-scoped cache first
            Optional<ScopeContext> cachedContext = scopeContextHolder.get();
            if (cachedContext.isPresent() && cachedContext.get().userId().equals(userId)) {
                log.debug("Using cached ScopeContext for userId: {}", userId);
                cached = true;
                return cachedContext.get();
            }

            // Compute scope
            ScopeContext context = computeScope(userId);
            scopeContextHolder.set(context);

            log.debug("Resolved scope for userId {}: {} departments, universityScope={}",
                    userId, context.allowedDepartmentIds().size(), context.isUniversityScope());

            return context;
        } finally {
            // Record metrics with cache status
            sample.stop(Timer.builder("scope.resolution.time")
                    .tag("cached", String.valueOf(cached))
                    .tag("result", cached ? "hit" : "miss")
                    .description("Time taken to resolve organization scope for a user")
                    .register(meterRegistry));

            // Increment counter
            meterRegistry.counter("scope.resolution.count",
                    "cached", String.valueOf(cached)).increment();
        }
    }

    /**
     * Gets all department IDs the user can access.
     *
     * <p>
     * <strong>Optional Caching:</strong>
     * </p>
     * <p>
     * This method uses Spring Cache abstraction for cross-request caching
     * when performance requirements exceed request-scoped caching benefits.
     * Cache key includes userId to ensure proper isolation.
     * </p>
     *
     * <p>
     * <strong>Cache Configuration:</strong>
     * </p>
     * <ul>
     * <li>Cache Name: "allowedDepartments"</li>
     * <li>TTL: Configure via application properties (default: 5 minutes)</li>
     * <li>Eviction: Automatic on user assignment changes</li>
     * </ul>
     *
     * @param userId User ID
     * @return Set of allowed department IDs
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "allowedDepartments", key = "#userId", unless = "#result.isEmpty()")
    public Set<Long> getAllowedDepartmentIds(Long userId) {
        return resolveScope(userId).allowedDepartmentIds();
    }

    /**
     * Gets all organization unit IDs the user can access.
     *
     * @param userId User ID
     * @return Set of allowed org unit IDs (all types)
     */
    @Transactional(readOnly = true)
    public Set<Long> getAllowedOrgUnitIds(Long userId) {
        return resolveScope(userId).allowedOrgUnitIds();
    }

    /**
     * Checks if user can access a specific department.
     *
     * @param userId       User ID
     * @param departmentId Department ID to check
     * @return true if access is allowed
     */
    @Transactional(readOnly = true)
    public boolean canAccessDepartment(Long userId, Long departmentId) {
        return resolveScope(userId).canAccessDepartment(departmentId);
    }

    // ==================== Core Algorithm ====================

    @SuppressWarnings("null")
    private ScopeContext computeScope(Long userId) {
        // Step 0a: Fetch user with college_id
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Long collegeId = user.getCollege() != null ? user.getCollege().getId() : null;
        UserRole role = user.getRole();

        // SUPER_ADMIN: No scope restrictions
        if (role == UserRole.SUPER_ADMIN) {
            return new ScopeContext(
                    userId,
                    null, // SUPER_ADMIN has no college restriction
                    role,
                    Collections.emptySet(),
                    Collections.emptySet(),
                    true // Effectively has all access
            );
        }

        // Validate college exists for non-SUPER_ADMIN
        if (collegeId == null) {
            securityLog.warn("SECURITY: Non-SUPER_ADMIN user {} has no college_id", userId);
            return emptyScope(userId, null, role);
        }

        // Step 1: Fetch all active assignments for user
        List<UserAssignment> assignments = assignmentRepository.findByUserId(userId);
        if (assignments.isEmpty()) {
            log.debug("User {} has no assignments, returning empty scope", userId);
            return emptyScope(userId, collegeId, role);
        }

        Set<Long> allowedDepartmentIds = new HashSet<>();
        Set<Long> allowedOrgUnitIds = new HashSet<>();
        boolean isUniversityScope = false;

        // Step 2: Process each assignment with college guard
        for (UserAssignment assignment : assignments) {
            OrganizationUnit orgUnit = assignment.getOrganizationUnit();

            // Step 0b: College Guard - verify assignment is within user's college
            if (!isValidAssignment(assignment, user, collegeId)) {
                continue; // Skip invalid assignments
            }

            // Check if assignment is not active (date-based)
            if (!assignment.isActive()) {
                log.debug("Skipping inactive assignment {} for user {}", assignment.getId(), userId);
                continue;
            }

            ScopeLevel scopeLevel = assignment.getScopeLevel();
            if (scopeLevel == null) {
                scopeLevel = ScopeLevel.SUBTREE; // Default
            }

            // Check for university-level SUBTREE scope
            if (orgUnit.getType() == OrganizationUnitType.UNIVERSITY &&
                    scopeLevel == ScopeLevel.SUBTREE) {
                isUniversityScope = true;
            }

            // Expand scope based on level
            Set<Long> expandedIds = expandScope(orgUnit, scopeLevel, collegeId);
            allowedOrgUnitIds.addAll(expandedIds);

            // Filter to only departments
            for (Long orgUnitId : expandedIds) {
                Optional<OrganizationUnit> ou = orgUnitRepository.findById(orgUnitId);
                if (ou.isPresent() && ou.get().getType() == OrganizationUnitType.DEPARTMENT) {
                    allowedDepartmentIds.add(orgUnitId);
                }
            }
        }

        return new ScopeContext(
                userId,
                collegeId,
                role,
                Collections.unmodifiableSet(allowedDepartmentIds),
                Collections.unmodifiableSet(allowedOrgUnitIds),
                isUniversityScope);
    }

    /**
     * Step 0b: College Guard - validates assignment is within user's college.
     */
    private boolean isValidAssignment(UserAssignment assignment, User user, Long userCollegeId) {
        OrganizationUnit orgUnit = assignment.getOrganizationUnit();

        if (orgUnit == null) {
            securityLog.warn("SECURITY: Assignment {} has null organizationUnit for user {}",
                    assignment.getId(), user.getId());
            return false;
        }

        Long assignmentCollegeId = orgUnit.getCollege() != null ? orgUnit.getCollege().getId() : null;

        if (!userCollegeId.equals(assignmentCollegeId)) {
            securityLog.error("SECURITY ALERT: Cross-college assignment detected! " +
                    "User {} (college={}) has assignment to orgUnit {} (college={})",
                    user.getId(), userCollegeId, orgUnit.getId(), assignmentCollegeId);
            return false;
        }

        return true;
    }

    /**
     * Expands scope based on scope level.
     *
     * <p>
     * <strong>Defense-in-Depth Strategy:</strong>
     * </p>
     * <p>
     * This method implements multiple layers of college validation to prevent
     * cross-tenant data leakage:
     * </p>
     *
     * <ul>
     * <li><strong>Layer 1 (Database):</strong> CTE queries include college_id
     * filter</li>
     * <li><strong>Layer 2 (Application):</strong> Explicit college validation in
     * CHILDREN case</li>
     * <li><strong>Layer 3 (Upstream):</strong> College guard validates assignments
     * before this method</li>
     * </ul>
     *
     * <p>
     * <strong>Why findSubtreeIds Already Filters by College (Line 275):</strong>
     * </p>
     * <blockquote>
     * The {@link OrganizationUnitRepository#findSubtreeIds(Long, Long)} method uses
     * a recursive CTE that includes {@code college_id = :collegeId} in BOTH the
     * base case
     * and recursive part. This ensures:
     * <ol>
     * <li>The starting node is validated against collegeId</li>
     * <li>Every descendant traversal is limited to same college</li>
     * <li>Even if parent_unit_id points across colleges (database corruption),
     * the recursive join will fail</li>
     * </ol>
     *
     * This is CRITICAL for multi-tenant security. Without college filtering in the
     * CTE:
     * <ul>
     * <li>A malicious admin could create cross-college parent links</li>
     * <li>Scope traversal could leak data across tenant boundaries</li>
     * <li>Single point of failure in college isolation</li>
     * </ul>
     * </blockquote>
     *
     * <p>
     * <strong>Example:</strong>
     * </p>
     * 
     * <pre>
     * User: coordinator@college1.edu
     * Assignment: Institute A (college_id=1), scope=SUBTREE
     *
     * Without college filter in CTE:
     *   Could traverse to departments in college 2 if parent_unit_id is corrupted
     *
     * With college filter in CTE:
     *   Guaranteed to only return org units where college_id = 1
     * </pre>
     *
     * @param orgUnit    The organization unit to expand from
     * @param scopeLevel The scope level (SELF, CHILDREN, SUBTREE)
     * @param collegeId  The college ID for validation (defense-in-depth)
     * @return Set of organization unit IDs accessible under this scope
     *
     * @see OrganizationUnitRepository#findSubtreeIds(Long, Long)
     * @see <a href="https://owasp.org/www-community/Defense_in_Depth">OWASP Defense
     *      in Depth</a>
     */
    private Set<Long> expandScope(OrganizationUnit orgUnit, ScopeLevel scopeLevel, Long collegeId) {
        Set<Long> result = new HashSet<>();
        Long orgUnitId = orgUnit.getId();

        switch (scopeLevel) {
            case SELF:
                // Only the assigned unit
                result.add(orgUnitId);
                break;

            case CHILDREN:
                // Assigned unit + direct children
                result.add(orgUnitId);
                List<OrganizationUnit> children = orgUnitRepository.findByParentId(orgUnitId);
                for (OrganizationUnit child : children) {
                    // Verify child is in same college (defense in depth)
                    // This is redundant with DB constraints but provides application-level safety
                    if (child.getCollege() != null && collegeId.equals(child.getCollege().getId())) {
                        result.add(child.getId());
                    }
                }
                break;

            case SUBTREE:
                // Assigned unit + all descendants (recursive)
                // NOTE: findSubtreeIds() already filters by collegeId in the CTE query.
                // See method JavaDoc above for detailed explanation of why this is critical
                // for multi-tenant security and defense-in-depth.
                List<Long> subtreeIds = orgUnitRepository.findSubtreeIds(orgUnitId, collegeId);
                result.addAll(subtreeIds);
                break;
        }

        return result;
    }

    /**
     * Creates an empty scope context (fail-secure default).
     */
    private ScopeContext emptyScope(Long userId, Long collegeId, UserRole role) {
        return new ScopeContext(
                userId,
                collegeId,
                role,
                Collections.emptySet(),
                Collections.emptySet(),
                false);
    }
}
