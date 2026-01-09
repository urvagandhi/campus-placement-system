package com.campusplacement.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an active user session/device.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionDTO {

    /**
     * Internal ID for revocation requests.
     */
    private Long id;

    /**
     * IP address of the session.
     */
    private String ipAddress;

    /**
     * User-Agent of the device.
     */
    private String userAgent;

    /**
     * When the session was first created.
     */
    private LocalDateTime createdAt;

    /**
     * When the session was last active (token refreshed).
     */
    private LocalDateTime lastActiveAt;

    /**
     * When the session will expire.
     */
    private LocalDateTime expiresAt;

    /**
     * Whether this session is the one currently making the request.
     */
    private boolean isCurrent;
}
