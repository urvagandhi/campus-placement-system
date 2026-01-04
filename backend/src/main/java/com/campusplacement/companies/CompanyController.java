package com.campusplacement.companies;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.companies.dto.CompanyDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for company management.
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getAllCompanies() {
        List<CompanyDTO> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDTO>> getCompanyById(@PathVariable Long id) {
        CompanyDTO company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO created = companyService.createCompany(companyDTO);
        return ResponseEntity.ok(ApiResponse.success(created, "Company created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TPO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyDTO>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyDTO companyDTO) {
        CompanyDTO updated = companyService.updateCompany(id, companyDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Company updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Company deleted successfully"));
    }

    @GetMapping("/industry/{industry}")
    public ResponseEntity<ApiResponse<List<CompanyDTO>>> getCompaniesByIndustry(
            @PathVariable String industry) {
        List<CompanyDTO> companies = companyService.getCompaniesByIndustry(industry);
        return ResponseEntity.ok(ApiResponse.success(companies));
    }
}
