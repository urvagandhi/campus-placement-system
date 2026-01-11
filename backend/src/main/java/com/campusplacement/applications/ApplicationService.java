package com.campusplacement.applications;

import java.util.List;

import com.campusplacement.applications.dto.ApplicationDTO;
import com.campusplacement.applications.dto.ApplyRequestDTO;

/**
 * Service interface for application operations.
 */
public interface ApplicationService {

    List<ApplicationDTO> getAllApplications();

    List<ApplicationDTO> getCurrentStudentApplications();

    List<ApplicationDTO> getApplicationsByDrive(Long driveId);

    ApplicationDTO applyToDrive(ApplyRequestDTO request);

    ApplicationDTO updateApplicationStatus(Long id, ApplicationStatusType status);

    void withdrawApplication(Long id);
}
