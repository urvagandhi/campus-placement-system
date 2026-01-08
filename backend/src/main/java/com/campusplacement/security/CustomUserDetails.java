package com.campusplacement.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.campusplacement.common.UserRole;
import com.campusplacement.users.User;

import lombok.Getter;

/**
 * Custom UserDetails implementation with college and org unit awareness.
 *
 * <p>
 * Extends Spring Security's UserDetails to include:
 * </p>
 * <ul>
 * <li>User ID for database operations</li>
 * <li>College ID for multi-tenancy</li>
 * <li>Org Unit ID for Phase-2 authorization</li>
 * <li>Role for RBAC</li>
 * </ul>
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final Long collegeId;
    private final Long orgUnitId; // Phase-2 ready
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
        this.collegeId = user.getCollege() != null ? user.getCollege().getId() : null;
        this.active = user.getIsActive() != null && user.getIsActive();

        // Prefix role with "ROLE_" for Spring Security
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Phase-2: Get primary org unit ID from assignment
        this.orgUnitId = user.getPrimaryAssignment()
                .map(a -> a.getOrganizationUnit().getId())
                .orElse(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
