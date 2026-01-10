package com.campusplacement.organizations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.colleges.College;
import com.campusplacement.colleges.CollegeRepository;
import com.campusplacement.common.OrganizationUnitType;
import com.campusplacement.common.ScopeLevel;
import com.campusplacement.common.UserRole;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;
import com.campusplacement.students.StudentService;
import com.campusplacement.users.User;
import com.campusplacement.users.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScopeEnforcementIntegrationTest {

        @Autowired
        private StudentService studentService;

        @Autowired
        private CollegeRepository collegeRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OrganizationUnitRepository orgUnitRepository;

        @Autowired
        private UserAssignmentRepository assignmentRepository;

        @Autowired
        private StudentRepository studentRepository;

        private College college;
        private OrganizationUnit cseDept;
        private OrganizationUnit eceDept;
        private User coordinator;

        @SuppressWarnings("null")
        @BeforeEach
        void setUp() {
                // 1. Setup College
                college = collegeRepository.save(College.builder()
                                .name("Test College")
                                .code("TEST")
                                .address("Main St")
                                .contactEmail("info@test.edu")
                                .build());

                // 2. Setup Departments
                cseDept = orgUnitRepository.save(OrganizationUnit.builder()
                                .name("CSE")
                                .code("CSE")
                                .type(OrganizationUnitType.DEPARTMENT)
                                .college(college)
                                .build());

                eceDept = orgUnitRepository.save(OrganizationUnit.builder()
                                .name("ECE")
                                .code("ECE")
                                .type(OrganizationUnitType.DEPARTMENT)
                                .college(college)
                                .build());

                // 3. Setup Coordinator (Assigned to CSE only)
                coordinator = userRepository.save(User.builder()
                                .username("cse_coord")
                                .name("CSE Coordinator")
                                .email("coord@test.edu")
                                .passwordHash("pass")
                                .role(UserRole.COORDINATOR)
                                .college(college)
                                .build());

                assignmentRepository.save(UserAssignment.builder()
                                .user(coordinator)
                                .organizationUnit(cseDept)
                                .scopeLevel(ScopeLevel.SELF) // Basic assignment to CSE Dept
                                .build());

                // 4. Setup Students
                User studentUserCSE = userRepository.save(User.builder()
                                .username("student_cse")
                                .name("CSE Student")
                                .email("student_cse@test.edu")
                                .passwordHash("pass")
                                .role(UserRole.STUDENT)
                                .college(college)
                                .build());

                StudentProfile profileCSE = new StudentProfile();
                profileCSE.setUser(studentUserCSE);
                profileCSE.setDepartment(cseDept);
                profileCSE.setEnrollmentNo("RN1");
                profileCSE.setCgpa(8.5);
                studentRepository.save(profileCSE);

                @SuppressWarnings("null")
                User studentUserECE = userRepository.save(User.builder()
                                .username("student_ece")
                                .name("ECE Student")
                                .email("student_ece@test.edu")
                                .passwordHash("pass")
                                .role(UserRole.STUDENT)
                                .college(college)
                                .build());

                StudentProfile profileECE = new StudentProfile();
                profileECE.setUser(studentUserECE);
                profileECE.setDepartment(eceDept);
                profileECE.setEnrollmentNo("RN2");
                profileECE.setCgpa(8.5);
                studentRepository.save(profileECE);
        }

        private void mockLogin(User user) {
                CustomUserDetails principal = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("Coordinator should only see students in their assigned department")
        void testCoordinatorScopeEnforcement() {
                mockLogin(coordinator);

                // Act: Request all students
                var students = studentService.getAllStudents();

                // Assert: Access is filtered by scope
                assertThat(students).hasSize(1);
                assertThat(students.get(0).getEnrollmentNo()).isEqualTo("RN1");
        }

        @Test
        @DisplayName("Coordinator accessing their own department explicitly should succeed")
        void testAccessOwnDepartment() {
                mockLogin(coordinator);

                // Act: Request students for CSE dept using String ID
                var students = studentService.getStudentsByDepartment(cseDept.getId().toString());

                // Assert
                assertThat(students).isNotEmpty();
                assertThat(students.get(0).getEnrollmentNo()).isEqualTo("RN1");
        }

        @Test
        @DisplayName("Coordinator accessing unassigned department should throw AccessDenied")
        void testAccessUnassignedDepartment_IdSpoofing() {
                mockLogin(coordinator);

                // Act & Assert: Request students for ECE dept (Out of scope)
                // This simulates "ID Spoofing" where a user changes deptId in URL
                assertThatThrownBy(() -> studentService.getStudentsByDepartment(eceDept.getId().toString()))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("access");
        }

        @Test
        @DisplayName("Admin should access all departments in college")
        void testAdminAccess() {
                // Setup Admin
                @SuppressWarnings("null")
                User admin = userRepository.save(User.builder()
                                .username("admin")
                                .name("Admin User")
                                .email("admin@test.edu")
                                .passwordHash("pass")
                                .role(UserRole.ADMIN)
                                .college(college)
                                .build());

                mockLogin(admin);

                // Act
                var students = studentService.getAllStudents();

                // Assert: Should see both CSE and ECE students
                assertThat(students).hasSize(2);
        }
}
