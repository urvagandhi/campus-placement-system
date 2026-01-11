package com.campusplacement.applications;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.campusplacement.applications.dto.ApplicationDTO;
import com.campusplacement.colleges.College;
import com.campusplacement.common.UserRole;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.DriveRepository;
import com.campusplacement.drives.PlacementDrive;
import com.campusplacement.eligibility.DriveEligibilityService;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.organizations.ScopeContext;
import com.campusplacement.security.CustomUserDetails;
import com.campusplacement.students.StudentProfile;
import com.campusplacement.students.StudentRepository;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private DriveRepository driveRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DriveEligibilityService eligibilityService;

    @Mock
    private OrganizationScopeService scopeService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUpSecurity() {
        setAuthentication(1L);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("null")
    @Test
    void updateApplicationStatus_allowsValidTransitionAndSetsTimestamps() {
        ScopeContext scope = new ScopeContext(1L, 10L, UserRole.SUPER_ADMIN, Set.of(), Set.of(), true);
        when(scopeService.resolveScope(1L)).thenReturn(scope);

        PlacementDrive drive = buildDrive(10L);
        when(driveRepository.findById(drive.getId())).thenReturn(Optional.of(drive));

        Application application = buildApplication(ApplicationStatusType.PENDING, drive.getId());
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDTO result = applicationService.updateApplicationStatus(application.getId(),
                ApplicationStatusType.SHORTLISTED);

        assertEquals(ApplicationStatusType.SHORTLISTED.name(), result.getStatus());
        assertNotNull(result.getShortlistedAt());
        verify(applicationRepository).save(any(Application.class));
    }

    @SuppressWarnings("null")
    @Test
    void updateApplicationStatus_rejectsInvalidTransition() {
        ScopeContext scope = new ScopeContext(1L, 10L, UserRole.SUPER_ADMIN, Set.of(), Set.of(), true);
        when(scopeService.resolveScope(1L)).thenReturn(scope);

        PlacementDrive drive = buildDrive(10L);
        when(driveRepository.findById(drive.getId())).thenReturn(Optional.of(drive));

        Application application = buildApplication(ApplicationStatusType.SELECTED, drive.getId());
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThrows(IllegalArgumentException.class, () -> applicationService
                .updateApplicationStatus(application.getId(), ApplicationStatusType.SHORTLISTED));
    }

    @SuppressWarnings("null")
    @Test
    void withdrawApplication_allowsPendingAndPersists() {
        StudentProfile student = StudentProfile.builder().build();
        student.setId(200L);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));

        Application application = buildApplication(ApplicationStatusType.PENDING, 30L);
        application.setStudent(student);
        application.setStudentId(student.getId());
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        applicationService.withdrawApplication(application.getId());

        assertEquals(ApplicationStatusType.WITHDRAWN.name(), application.getStatus());
        verify(applicationRepository).save(application);
    }

    @SuppressWarnings("null")
    @Test
    void withdrawApplication_rejectsOtherStudentsApplication() {
        StudentProfile student = StudentProfile.builder().build();
        student.setId(200L);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(student));

        StudentProfile differentStudent = StudentProfile.builder().build();
        differentStudent.setId(201L);

        Application application = buildApplication(ApplicationStatusType.PENDING, 30L);
        application.setStudent(differentStudent);
        application.setStudentId(differentStudent.getId());
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                applicationService.withdrawApplication(application.getId()));
    }

    @SuppressWarnings("null")
    @Test
    void updateApplicationStatus_throwsWhenDriveMissing() {
        ScopeContext scope = new ScopeContext(1L, 10L, UserRole.SUPER_ADMIN, Set.of(), Set.of(), true);
        when(scopeService.resolveScope(1L)).thenReturn(scope);

        Application application = buildApplication(ApplicationStatusType.PENDING, 99L);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(driveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.updateApplicationStatus(application.getId(), ApplicationStatusType.SHORTLISTED));
    }

    private Application buildApplication(ApplicationStatusType status, Long driveId) {
        Application application = Application.builder()
                .driveId(driveId)
                .status(status.name())
                .build();
        application.setId(100L);
        return application;
    }

    private PlacementDrive buildDrive(Long collegeId) {
        College college = College.builder().name("Test College").code("TC").build();
        college.setId(collegeId);

        PlacementDrive drive = PlacementDrive.builder().college(college).companyId(1L).title("Drive").build();
        drive.setId(50L);
        return drive;
    }

    private void setAuthentication(Long userId) {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getId()).thenReturn(userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
