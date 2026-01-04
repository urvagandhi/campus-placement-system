package com.campusplacement.users;

import com.campusplacement.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user in the system.
 *
 * <p>
 * Supports RBAC with the following roles:
 * </p>
 * <ul>
 * <li>STUDENT - Student user</li>
 * <li>TPO - Training and Placement Officer</li>
 * <li>ADMIN - College administrator</li>
 * <li>SUPER_ADMIN - Global administrator</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // TODO: Add relationships
    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    // private StudentProfile studentProfile;
}
