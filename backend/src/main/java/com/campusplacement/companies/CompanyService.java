package com.campusplacement.companies;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusplacement.common.PagedResponse;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.companies.dto.CompanyDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for company operations.
 *
 * <p>
 * Manages company/recruiter data including CRUD operations,
 * search, and industry filtering.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Companies are global entities, not tenant-scoped.
 * All colleges can associate with any company via placement drives.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    // ==================== Read Operations ====================

    /**
     * Retrieves all active companies with pagination.
     *
     * @param pageable Pagination parameters
     * @return Paginated list of companies
     */
    @Transactional(readOnly = true)
    public PagedResponse<CompanyDTO> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findByIsActiveTrue(pageable);
        return PagedResponse.of(companies, companies.map(this::toDTO).getContent());
    }

    /**
     * Retrieves all active companies as a list (legacy method).
     *
     * @return List of all active companies
     * @deprecated Use getAllCompanies(Pageable) for new implementations
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a company by ID.
     *
     * @param id Company ID
     * @return Company DTO
     * @throws ResourceNotFoundException if company not found
     */
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyById(Long id) {
        @SuppressWarnings("null")
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        return toDTO(company);
    }

    /**
     * Searches companies by name (case-insensitive partial match).
     *
     * @param name Search term
     * @return Matching companies
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> searchCompaniesByName(String name) {
        return companyRepository.findByNameContainingIgnoreCase(name).stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves companies by industry.
     *
     * @param industry Industry name
     * @return Companies in that industry
     */
    @Transactional(readOnly = true)
    public List<CompanyDTO> getCompaniesByIndustry(String industry) {
        return companyRepository.findByIndustry(industry).stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== Write Operations ====================

    /**
     * Creates a new company.
     *
     * @param companyDTO Company data
     * @return Created company DTO
     * @throws IllegalArgumentException if company name already exists
     */
    @SuppressWarnings("null")
    @Transactional
    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        // Validate unique name
        if (companyRepository.existsByName(companyDTO.getName())) {
            throw new IllegalArgumentException("Company with name '" + companyDTO.getName() + "' already exists");
        }

        Company company = Company.builder()
                .name(companyDTO.getName())
                .industry(companyDTO.getIndustry())
                .website(companyDTO.getWebsite())
                .description(companyDTO.getDescription())
                .logoUrl(companyDTO.getLogoUrl())
                .location(companyDTO.getLocation())
                .contactEmail(companyDTO.getContactEmail())
                .contactPhone(companyDTO.getContactPhone())
                .isActive(true)
                .build();

        company = companyRepository.save(company);
        log.info("Created company: {} (ID: {})", company.getName(), company.getId());

        return toDTO(company);
    }

    /**
     * Updates an existing company.
     *
     * @param id         Company ID
     * @param companyDTO Updated company data
     * @return Updated company DTO
     * @throws ResourceNotFoundException if company not found
     * @throws IllegalArgumentException  if new name already exists
     */
    @Transactional
    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO) {
        @SuppressWarnings("null")
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        // Check for name conflicts (if name is being changed)
        if (!company.getName().equals(companyDTO.getName()) &&
                companyRepository.existsByName(companyDTO.getName())) {
            throw new IllegalArgumentException("Company with name '" + companyDTO.getName() + "' already exists");
        }

        // Update fields
        if (companyDTO.getName() != null) {
            company.setName(companyDTO.getName());
        }
        if (companyDTO.getIndustry() != null) {
            company.setIndustry(companyDTO.getIndustry());
        }
        if (companyDTO.getWebsite() != null) {
            company.setWebsite(companyDTO.getWebsite());
        }
        if (companyDTO.getDescription() != null) {
            company.setDescription(companyDTO.getDescription());
        }
        if (companyDTO.getLogoUrl() != null) {
            company.setLogoUrl(companyDTO.getLogoUrl());
        }
        if (companyDTO.getLocation() != null) {
            company.setLocation(companyDTO.getLocation());
        }
        if (companyDTO.getContactEmail() != null) {
            company.setContactEmail(companyDTO.getContactEmail());
        }
        if (companyDTO.getContactPhone() != null) {
            company.setContactPhone(companyDTO.getContactPhone());
        }
        // Note: isActive is managed separately via activate/deactivate

        company = companyRepository.save(company);
        log.info("Updated company: {} (ID: {})", company.getName(), company.getId());

        return toDTO(company);
    }

    /**
     * Soft-deletes a company (sets isActive to false).
     *
     * @param id Company ID
     * @throws ResourceNotFoundException if company not found
     */
    @Transactional
    public void deleteCompany(Long id) {
        @SuppressWarnings("null")
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        company.setIsActive(false);
        companyRepository.save(company);

        log.info("Soft-deleted company: {} (ID: {})", company.getName(), company.getId());
    }

    /**
     * Reactivates a soft-deleted company.
     *
     * @param id Company ID
     * @return Reactivated company DTO
     * @throws ResourceNotFoundException if company not found
     */
    @Transactional
    public CompanyDTO reactivateCompany(Long id) {
        @SuppressWarnings("null")
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));

        company.setIsActive(true);
        company = companyRepository.save(company);

        log.info("Reactivated company: {} (ID: {})", company.getName(), company.getId());

        return toDTO(company);
    }

    // ==================== Helper Methods ====================

    /**
     * Converts entity to DTO.
     */
    private CompanyDTO toDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .description(company.getDescription())
                .logoUrl(company.getLogoUrl())
                .location(company.getLocation())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .isActive(company.getIsActive())
                .build();
    }
}
