package com.campusplacement.drives;

import java.util.List;

import com.campusplacement.drives.dto.CreateDriveRequestDTO;
import com.campusplacement.drives.dto.DriveDTO;

/**
 * Service interface for placement drive operations.
 *
 * <p>
 * Defines the contract for drive management business logic.
 * This interface pattern enables:
 * </p>
 * <ul>
 * <li>Clean UML Class Diagrams with interface dependencies</li>
 * <li>Easy mocking for unit tests</li>
 * <li>Dependency Inversion principle demonstration</li>
 * </ul>
 *
 * @see DriveServiceImpl for the implementation
 */
public interface DriveService {

    /**
     * Retrieves all placement drives.
     */
    List<DriveDTO> getAllDrives();

    /**
     * Retrieves a drive by ID.
     */
    DriveDTO getDriveById(Long id);

    /**
     * Retrieves upcoming drives.
     */
    List<DriveDTO> getUpcomingDrives();

    /**
     * Retrieves drives by company.
     */
    List<DriveDTO> getDrivesByCompany(Long companyId);

    /**
     * Creates a new placement drive.
     */
    DriveDTO createDrive(CreateDriveRequestDTO request);

    /**
     * Updates an existing drive.
     */
    DriveDTO updateDrive(Long id, DriveDTO driveDTO);

    /**
     * Updates drive status.
     */
    DriveDTO updateDriveStatus(Long id, String status);

    /**
     * Deletes a drive.
     */
    void deleteDrive(Long id);
}
