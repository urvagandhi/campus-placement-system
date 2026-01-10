package com.campusplacement.drives;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.drives.dto.CreateDriveRequestDTO;
import com.campusplacement.drives.dto.DriveDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriveServiceImpl implements DriveService {

    private final DriveRepository driveRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getAllDrives() {
        return driveRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DriveDTO getDriveById(Long id) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        return mapToDTO(drive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getUpcomingDrives() {
        return driveRepository.findByDriveDateAfter(LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriveDTO> getDrivesByCompany(Long companyId) {
        return driveRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DriveDTO createDrive(CreateDriveRequestDTO request) {
        PlacementDrive drive = new PlacementDrive();
        drive.setCompanyId(request.getCompanyId());
        drive.setTitle(request.getTitle());
        drive.setJobRole(request.getJobRole());
        drive.setDescription(request.getDescription());
        drive.setPackageLpa(request.getPackageLpa());
        drive.setLocation(request.getLocation());
        drive.setIsRemote(request.getIsRemote());

        drive.setMinCgpa(request.getMinCgpa());
        drive.setMaxBacklogs(request.getMaxBacklogs());

        if (request.getEligibleDepartments() != null) {
            drive.setEligibleDepartments(String.join(",", request.getEligibleDepartments()));
        }
        if (request.getRequiredSkills() != null) {
            drive.setRequiredSkills(String.join(",", request.getRequiredSkills()));
        }

        drive.setRegistrationDeadline(request.getRegistrationDeadline());
        drive.setDriveDate(request.getDriveDate());
        drive.setStatus("SCHEDULED");
        drive.setCreatedAt(java.time.LocalDateTime.now());
        drive.setUpdatedAt(java.time.LocalDateTime.now());

        PlacementDrive saved = driveRepository.save(drive);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DriveDTO updateDrive(Long id, DriveDTO driveDTO) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        if (driveDTO.getTitle() != null)
            drive.setTitle(driveDTO.getTitle());
        if (driveDTO.getJobRole() != null)
            drive.setJobRole(driveDTO.getJobRole());
        if (driveDTO.getDescription() != null)
            drive.setDescription(driveDTO.getDescription());
        if (driveDTO.getPackageLpa() != null)
            drive.setPackageLpa(driveDTO.getPackageLpa());
        if (driveDTO.getLocation() != null)
            drive.setLocation(driveDTO.getLocation());
        if (driveDTO.getIsRemote() != null)
            drive.setIsRemote(driveDTO.getIsRemote());

        if (driveDTO.getMinCgpa() != null)
            drive.setMinCgpa(driveDTO.getMinCgpa());
        if (driveDTO.getMaxBacklogs() != null)
            drive.setMaxBacklogs(driveDTO.getMaxBacklogs());

        if (driveDTO.getEligibleDepartments() != null) {
            drive.setEligibleDepartments(String.join(",", driveDTO.getEligibleDepartments()));
        }
        if (driveDTO.getRequiredSkills() != null) {
            drive.setRequiredSkills(String.join(",", driveDTO.getRequiredSkills()));
        }

        if (driveDTO.getRegistrationDeadline() != null)
            drive.setRegistrationDeadline(driveDTO.getRegistrationDeadline());
        if (driveDTO.getDriveDate() != null)
            drive.setDriveDate(driveDTO.getDriveDate());

        drive.setUpdatedAt(java.time.LocalDateTime.now());

        PlacementDrive updated = driveRepository.save(drive);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public DriveDTO updateDriveStatus(Long id, String status) {
        PlacementDrive drive = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        drive.setStatus(status);
        drive.setUpdatedAt(java.time.LocalDateTime.now());
        return mapToDTO(driveRepository.save(drive));
    }

    @Override
    @Transactional
    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Drive not found");
        }
        driveRepository.deleteById(id);
    }

    private DriveDTO mapToDTO(PlacementDrive drive) {
        return DriveDTO.builder()
                .id(drive.getId())
                .companyId(drive.getCompanyId())
                .title(drive.getTitle())
                .jobRole(drive.getJobRole())
                .description(drive.getDescription())
                .packageLpa(drive.getPackageLpa())
                .location(drive.getLocation())
                .isRemote(drive.getIsRemote())
                .minCgpa(drive.getMinCgpa())
                .maxBacklogs(drive.getMaxBacklogs())
                .eligibleDepartments(drive.getEligibleDepartments() != null
                        ? java.util.Arrays.asList(drive.getEligibleDepartments().split(","))
                        : java.util.Collections.emptyList())
                .requiredSkills(drive.getRequiredSkills() != null
                        ? java.util.Arrays.asList(drive.getRequiredSkills().split(","))
                        : java.util.Collections.emptyList())
                .registrationDeadline(drive.getRegistrationDeadline())
                .driveDate(drive.getDriveDate())
                .status(drive.getStatus())
                .build();
    }
}
