package com.campusplacement.users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.SQLRestriction;

import com.campusplacement.colleges.College;
import com.campusplacement.common.BaseEntity;
import com.campusplacement.common.UserRole;
import com.campusplacement.organizations.UserAssignment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user in the system.
 *
 * <p>
 * AUTH + IDENTITY ONLY. No department/designation stored here.
 * </p>
 *
 * <p>
 * Supports RBAC with exactly 4 roles:
 * </p>
 * <ul>
 * <li>STUDENT - End user, applies to drives</li>
 * <li>COORDINATOR - ALL placement actors (TPO, faculty coordinators, corporate
 * relations)</li>
 * <li>ADMIN - College governance</li>
 * <li>SUPER_ADMIN - Platform owner (college_id is NULL)</li>
 * </ul>
 *
 * <p>
 * <strong>Key design notes:</strong>
 * </p>
 * <ul>
 * <li>Roles define WHAT a user can do</li>
 * <li>Organizational hierarchy defines WHERE they can do it (via
 * UserAssignment)</li>
 * <li>Faculty coordinators and T&P staff are NOT roles — they are
 * assignments</li>
 * </ul>
 *
 * <p>
 * Multi-college support: Email is unique per college (not globally unique).
 * </p>
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "email", "college_id" })
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    /**
     * Login username (globally unique).
     * Used as the primary identifier for authentication.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Display name (e.g., "Dr. Amit Patel").
     * Maps to 'full_name' column in the database.
     */
    @Column(name = "full_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    /**
     * BCrypt hashed password (12 rounds).
     * Renamed from 'password' to 'passwordHash' for clarity.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * College association.
     * NULL for SUPER_ADMIN users who manage the entire platform.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    /**
     * Soft-delete timestamp. When set, user is considered deleted.
     * The @SQLRestriction annotation filters out soft-deleted records
     * automatically.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Flag indicating the user must change their password on next login.
     * Set to TRUE when:
     * - Account is created by TPO/Coordinator (first-login flow)
     * - Admin forces password reset
     * Set to FALSE after successful password change.
     */
    @Column(name = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    /**
     * User's organizational assignments.
     * Defines WHERE they can operate and with what scope.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAssignment> assignments = new ArrayList<>();

    /**
     * Gets the primary assignment for this user.
     * Used to determine the main organizational context.
     */
    public Optional<UserAssignment> getPrimaryAssignment() {
        return assignments.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                .findFirst();
    }

    /**
     * Soft-deletes this user.
     * The @SQLRestriction annotation will filter out soft-deleted records.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Gets the password hash (for backward compatibility with existing code).
     *
     * @deprecated Use getPasswordHash() instead
     */
    @Deprecated
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Sets the password hash (for backward compatibility with existing code).
     *
     * @deprecated Use setPasswordHash() instead
     */
    @Deprecated
    public void setPassword(String password) {
        this.passwordHash = password;
    }
}
