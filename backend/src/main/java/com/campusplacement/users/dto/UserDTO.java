package com.campusplacement.users.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Long collegeId;
    private String collegeName;
    private Long organizationUnitId;
    private Boolean isActive;
    private String phoneNumber;
    private String profileImageUrl;
    private String status; // Active, Inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
