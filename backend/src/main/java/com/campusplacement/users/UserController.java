package com.campusplacement.users;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.UserRole;
import com.campusplacement.common.exception.ResourceNotFoundException;
import com.campusplacement.common.exception.UnauthorizedException;
import com.campusplacement.organizations.OrganizationScopeService;
import com.campusplacement.users.dto.UserDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OrganizationScopeService scopeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 100) Pageable pageable) {

        Long collegeId = scopeService.getCurrentUserScope().collegeId();

        Page<User> usersPage;
        if (search != null && !search.isEmpty()) {
            usersPage = userRepository.searchByCollegeId(collegeId, search, pageable);
        } else {
            usersPage = userRepository.findByCollegeId(collegeId, pageable);
        }

        List<UserDTO> userDTOs = usersPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(userDTOs));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Long collegeId = scopeService.getCurrentUserScope().collegeId();
        User user = userRepository.findByIdWithCollege(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getCollege().getId().equals(collegeId)) {
            throw new UnauthorizedException("Cannot access user from another college");
        }

        boolean isActive = "Active".equalsIgnoreCase(status);
        user.setIsActive(isActive);
        user = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(mapToDTO(user)));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {

        Long collegeId = scopeService.getCurrentUserScope().collegeId();
        User user = userRepository.findByIdWithCollege(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getCollege().getId().equals(collegeId)) {
            throw new UnauthorizedException("Cannot access user from another college");
        }

        try {
            UserRole newRole = UserRole.valueOf(role.toUpperCase());
            user.setRole(newRole);
            user = userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success(mapToDTO(user)));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                .collegeName(user.getCollege() != null ? user.getCollege().getName() : null)
                .isActive(user.getIsActive())
                .status(user.getIsActive() ? "Active" : "Inactive") // Explicit status string
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
