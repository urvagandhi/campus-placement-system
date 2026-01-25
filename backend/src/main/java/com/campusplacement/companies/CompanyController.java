package com.campusplacement.companies;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.common.PagedResponse;
import com.campusplacement.companies.dto.CompanyDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for company management.
 *
 * <p>
 * Companies are global entities accessible to all colleges.
 * Write operations are restricted to TPO/ADMIN/SUPER_ADMIN roles.
 * </p>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Get all companies with pagination.
     *
     * @param pageable Pagination parameters (page, size, sort)
     * @return Paginated list of active companies
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CompanyDTO>>> getAllCompanies(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        // TODO: Implement Company Database fully with scoped access
        throw new UnsupportedOperationException(
                "Company Database feature is currently under development (Coming Soon)");
        // PagedResponse<CompanyDTO> companies =
        // companyService.getAllCompanies(pageable);
        // return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Get a company by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompanyById(@PathVariable Long id) {
        CompanyDTO company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    /**
     * Search companies by name.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> searchCompanies(
            @RequestParam String name) {
        List<CompanyDTO> companies = companyService.searchCompaniesByName(name);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Get companies by industry.
     */
    @GetMapping("/industry/{industry}")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getCompaniesByIndustry(
            @PathVariable String industry) {
        List<CompanyDTO> companies = companyService.getCompaniesByIndustry(industry);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Create a new company.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO created = companyService.createCompany(companyDTO);
        return ResponseEntity.ok(ApiResponse.success(created, "Company created successfully"));
    }

    /**
     * Update an existing company.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO updated = companyService.updateCompany(id, companyDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Company updated successfully"));
    }

    /**
     * Soft-delete a company.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Company deleted successfully"));
    }

    /**
     * Reactivate a soft-deleted company.
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> reactivateCompany(@PathVariable Long id) {
        CompanyDTO reactivated = companyService.reactivateCompany(id);
        return ResponseEntity.ok(ApiResponse.success(reactivated, "Company reactivated successfully"));
    }
}
