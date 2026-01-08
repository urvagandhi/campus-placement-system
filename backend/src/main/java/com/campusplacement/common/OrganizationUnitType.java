package com.campusplacement.common;

/**
 * Types of organization units in the hierarchy.
 *
 * <p>
 * Hierarchy levels:
 * </p>
 * <ul>
 * <li>UNIVERSITY - Root level (parent_id must be NULL)</li>
 * <li>INSTITUTE - Child of University</li>
 * <li>DEPARTMENT - Child of Institute</li>
 * </ul>
 */
public enum OrganizationUnitType {
    UNIVERSITY,
    INSTITUTE,
    DEPARTMENT
}
