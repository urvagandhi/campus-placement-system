package com.campusplacement.users;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusplacement.common.ApiResponse;
import com.campusplacement.common.Constants;
import com.campusplacement.common.PagedResponse;
import com.campusplacement.users.dto.UserDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for user management endpoints.
 *
 * <p>
 * Provides CRUD operations for users. Access controlled by role.
 * </p>
 *
 * <p>
 * <strong>Pagination:</strong>
 * </p>
 * <ul>
 * <li>Default page size: 20</li>
 * <li>Maximum page size: 100</li>
 * <li>Sorted by createdAt descending by default</li>
 * </ul>
 */
@RestController
@RequestMapping(Constants.API_VERSION + "/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD operations")
public class UserController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserService userService;

    /**
     * Retrieves users with pagination, search, and filtering.
     * Only accessible by ADMIN and SUPER_ADMIN roles.
     *
     * @param page   Page number (0-indexed, default 0)
     * @param size   Page size (default 20, max 100)
     * @param sort   Sort field (default: createdAt)
     * @param order  Sort order: asc or desc (default: desc)
     * @param search Search term for name or email
     * @param role   Filter by role (STUDENT, COORDINATOR, ADMIN)
     * @return Paginated list of users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all users (paginated)", description = "Retrieves users with pagination. Admins see only their college's users.")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sort,

            @Parameter(description = "Sort order: asc or desc") @RequestParam(defaultValue = "desc") String order,

            @Parameter(description = "Search by name or email") @RequestParam(required = false) String search,

            @Parameter(description = "Filter by role") @RequestParam(required = false) String role) {

        // Validate and cap page size
        int validSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        // Create pageable with sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, validSize, Sort.by(direction, sort));

        PagedResponse<UserDTO> users = userService.getAllUsersPaginated(pageable, search, role);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Retrieves a user by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @userService.isCurrentUser(#id)")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Updates a user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @userService.isCurrentUser(#id)")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated successfully"));
    }

    /**
     * Deletes a user (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete user (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    /**
     * Changes user role.
     * Only accessible by SUPER_ADMIN.
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Change user role")
    public ResponseEntity<ApiResponse<UserDTO>> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        UserDTO updated = userService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(updated, "Role updated successfully"));
    }
}
