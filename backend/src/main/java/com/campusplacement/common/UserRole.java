package com.campusplacement.common;

/**
 * System-defined user roles for RBAC.
 *
 * <p>
 * Roles are assigned by the system, not selected by users.
 * </p>
 *
 * <ul>
 * <li>STUDENT - End user, applies to drives</li>
 * <li>COORDINATOR - Placement operations (TPO)</li>
 * <li>ADMIN - College governance</li>
 * <li>SUPER_ADMIN - Platform owner</li>
 * </ul>
 */
public enum UserRole {
    STUDENT,
    COORDINATOR,
    ADMIN,
    SUPER_ADMIN
}
