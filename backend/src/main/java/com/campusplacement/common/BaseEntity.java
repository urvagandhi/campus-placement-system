package com.campusplacement.common;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity class providing common fields for all JPA entities.
 *
 * <p>
 * Includes automatic timestamp management for audit purposes.
 * </p>
 *
 * <p>
 * <strong>Common Fields:</strong>
 * </p>
 * <ul>
 * <li>id - Primary key with auto-generation</li>
 * <li>createdAt - Timestamp when entity was created</li>
 * <li>updatedAt - Timestamp when entity was last modified</li>
 * </ul>
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets the createdAt timestamp before entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the updatedAt timestamp before entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
