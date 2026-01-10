package com.campusplacement.organizations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusplacement.colleges.College;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.ScopeLevel;
import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

/**
 * Unit tests for OrganizationScopeService.
 *
 * <p>
 * Tests the core scope resolution algorithm including:
 * </p>
 * <ul>
 * <li>Role-based access (SUPER_ADMIN, ADMIN, COORDINATOR, STUDENT)</li>
 * <li>Scope level expansion (SELF, CHILDREN, SUBTREE)</li>
 * <li>College guard validation</li>
 * <li>Request-scoped caching</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OrganizationScopeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAssignmentRepository assignmentRepository;

    @Mock
    private OrganizationUnitRepository orgUnitRepository;

    @Mock
    private ScopeContextHolder scopeContextHolder;

    @InjectMocks
    private OrganizationScopeService scopeService;

    // Test data
    private static final Long USER_ID = 1L;
    private static final Long COLLEGE_ID = 100L;
    private static final Long UNIVERSITY_ID = 1000L;
    private static final Long INSTITUTE_ID = 2000L;
    private static final Long DEPT_CS_ID = 3001L;
    private static final Long DEPT_ECE_ID = 3002L;

    private College testCollege;
    private User testUser;
    private OrganizationUnit university;
    private OrganizationUnit institute;
    private OrganizationUnit deptCS;
    private OrganizationUnit deptECE;

    @BeforeEach
    void setUp() {
        // Setup test college
        testCollege = College.builder()
                .name("Test College")
                .code("TC001")
                .build();
        // Use reflection or setter to set ID since it's auto-generated
        setEntityId(testCollege, COLLEGE_ID);

        // Setup test user (default: COORDINATOR)
        testUser = User.builder()
                .email("coordinator@test.edu")
                .role(UserRole.COORDINATOR)
                .college(testCollege)
                .build();
        setEntityId(testUser, USER_ID);

        // Setup organization hierarchy
        university = createOrgUnit(UNIVERSITY_ID, "Test University", OrganizationUnitType.UNIVERSITY, null);
        institute = createOrgUnit(INSTITUTE_ID, "Engineering Institute", OrganizationUnitType.INSTITUTE, university);
        deptCS = createOrgUnit(DEPT_CS_ID, "Computer Science", OrganizationUnitType.DEPARTMENT, institute);
        deptECE = createOrgUnit(DEPT_ECE_ID, "Electronics", OrganizationUnitType.DEPARTMENT, institute);
    }

    // ==================== SUPER_ADMIN Tests ====================

    @Nested
    @DisplayName("SUPER_ADMIN Scope Tests")
    class SuperAdminTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("SUPER_ADMIN should have unrestricted access")
        void superAdmin_shouldHaveUnrestrictedAccess() {
            // Given
            testUser.setRole(UserRole.SUPER_ADMIN);
            testUser.setCollege(null); // SUPER_ADMIN has no college
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(scopeContextHolder.get()).thenReturn(Optional.empty());

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then
            assertThat(scope.isSuperAdmin()).isTrue();
            assertThat(scope.collegeId()).isNull();
            assertThat(scope.hasFullCollegeAccess()).isFalse(); // SUPER_ADMIN uses different path
        }
    }

    // ==================== ADMIN Tests ====================

    @Nested
    @DisplayName("ADMIN Scope Tests")
    class AdminTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("ADMIN should have full college access")
        void admin_shouldHaveFullCollegeAccess() {
            // Given
            testUser.setRole(UserRole.ADMIN);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(assignmentRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(scopeContextHolder.get()).thenReturn(Optional.empty());

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then
            assertThat(scope.isAdmin()).isTrue();
            assertThat(scope.collegeId()).isEqualTo(COLLEGE_ID);
            assertThat(scope.hasFullCollegeAccess()).isTrue();
        }
    }

    // ==================== COORDINATOR Tests ====================

    @Nested
    @DisplayName("COORDINATOR Scope Tests")
    class CoordinatorTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("COORDINATOR with SELF scope at department should only see that department")
        void coordinator_selfScopeAtDepartment_shouldOnlySeeOwnDepartment() {
            // Given
            UserAssignment assignment = createAssignment(testUser, deptCS, ScopeLevel.SELF);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(assignmentRepository.findByUserId(USER_ID)).thenReturn(List.of(assignment));
            when(scopeContextHolder.get()).thenReturn(Optional.empty());
            when(orgUnitRepository.findById(DEPT_CS_ID)).thenReturn(Optional.of(deptCS));

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then
            assertThat(scope.isCoordinator()).isTrue();
            assertThat(scope.allowedDepartmentIds()).containsExactly(DEPT_CS_ID);
            assertThat(scope.hasScopedAccess()).isTrue();
            assertThat(scope.hasFullCollegeAccess()).isFalse();
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("COORDINATOR with SUBTREE at university should have full college access")
        void coordinator_subtreeAtUniversity_shouldHaveFullCollegeAccess() {
            // Given
            UserAssignment assignment = createAssignment(testUser, university, ScopeLevel.SUBTREE);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(assignmentRepository.findByUserId(USER_ID)).thenReturn(List.of(assignment));
            when(scopeContextHolder.get()).thenReturn(Optional.empty());
            when(orgUnitRepository.findSubtreeIds(UNIVERSITY_ID, COLLEGE_ID))
                    .thenReturn(List.of(UNIVERSITY_ID, INSTITUTE_ID, DEPT_CS_ID, DEPT_ECE_ID));
            when(orgUnitRepository.findById(UNIVERSITY_ID)).thenReturn(Optional.of(university));
            when(orgUnitRepository.findById(INSTITUTE_ID)).thenReturn(Optional.of(institute));
            when(orgUnitRepository.findById(DEPT_CS_ID)).thenReturn(Optional.of(deptCS));
            when(orgUnitRepository.findById(DEPT_ECE_ID)).thenReturn(Optional.of(deptECE));

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then
            assertThat(scope.isUniversityScope()).isTrue();
            assertThat(scope.hasFullCollegeAccess()).isTrue();
            assertThat(scope.allowedDepartmentIds()).containsExactlyInAnyOrder(DEPT_CS_ID, DEPT_ECE_ID);
        }

        @SuppressWarnings("null")
        @Test
        @DisplayName("COORDINATOR with CHILDREN at institute should see direct child departments")
        void coordinator_childrenAtInstitute_shouldSeeChildDepartments() {
            // Given
            UserAssignment assignment = createAssignment(testUser, institute, ScopeLevel.CHILDREN);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(assignmentRepository.findByUserId(USER_ID)).thenReturn(List.of(assignment));
            when(scopeContextHolder.get()).thenReturn(Optional.empty());
            when(orgUnitRepository.findByParentId(INSTITUTE_ID)).thenReturn(List.of(deptCS, deptECE));
            when(orgUnitRepository.findById(INSTITUTE_ID)).thenReturn(Optional.of(institute));
            when(orgUnitRepository.findById(DEPT_CS_ID)).thenReturn(Optional.of(deptCS));
            when(orgUnitRepository.findById(DEPT_ECE_ID)).thenReturn(Optional.of(deptECE));

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then
            assertThat(scope.allowedDepartmentIds()).containsExactlyInAnyOrder(DEPT_CS_ID, DEPT_ECE_ID);
            assertThat(scope.hasFullCollegeAccess()).isFalse();
        }
    }

    // ==================== College Guard Tests ====================

    @Nested
    @DisplayName("College Guard Tests")
    class CollegeGuardTests {

        @SuppressWarnings("null")
        @Test
        @DisplayName("Cross-college assignment should be skipped with security log")
        void crossCollegeAssignment_shouldBeSkipped() {
            // Given
            College otherCollege = College.builder().name("Other College").code("OC001").build();
            setEntityId(otherCollege, 999L);

            OrganizationUnit otherDept = createOrgUnit(9999L, "Other Dept", OrganizationUnitType.DEPARTMENT, null);
            otherDept.setCollege(otherCollege);

            UserAssignment badAssignment = createAssignment(testUser, otherDept, ScopeLevel.SELF);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(assignmentRepository.findByUserId(USER_ID)).thenReturn(List.of(badAssignment));
            when(scopeContextHolder.get()).thenReturn(Optional.empty());

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then - cross-college assignment should be ignored, returning empty scope
            assertThat(scope.allowedDepartmentIds()).isEmpty();
        }
    }

    // ==================== Caching Tests ====================

    @Nested
    @DisplayName("Request-Scoped Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Cached scope should be returned without recomputation")
        void cachedScope_shouldBeReturnedWithoutRecomputation() {
            // Given
            ScopeContext cachedContext = new ScopeContext(
                    USER_ID, COLLEGE_ID, UserRole.COORDINATOR,
                    Set.of(DEPT_CS_ID), Set.of(DEPT_CS_ID), false);
            when(scopeContextHolder.get()).thenReturn(Optional.of(cachedContext));

            // When
            ScopeContext scope = scopeService.resolveScope(USER_ID);

            // Then - should return cached, userRepository should NOT be called
            assertThat(scope).isSameAs(cachedContext);
        }
    }

    // ==================== Helper Methods ====================

    private OrganizationUnit createOrgUnit(Long id, String name, OrganizationUnitType type, OrganizationUnit parent) {
        OrganizationUnit orgUnit = OrganizationUnit.builder()
                .name(name)
                .type(type)
                .college(testCollege)
                .parent(parent)
                .isActive(true)
                .isRoot(type == OrganizationUnitType.UNIVERSITY)
                .build();
        setEntityId(orgUnit, id);
        return orgUnit;
    }

    private UserAssignment createAssignment(User user, OrganizationUnit orgUnit, ScopeLevel scopeLevel) {
        return UserAssignment.builder()
                .user(user)
                .organizationUnit(orgUnit)
                .scopeLevel(scopeLevel)
                .isPrimary(true)
                .build();
    }

    private void setEntityId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException e) {
            // Try direct class
            try {
                java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set ID", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }
}
