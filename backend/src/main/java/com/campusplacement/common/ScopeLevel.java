package com.campusplacement.common;

/**
 * Defines the operational reach of a user assignment.
 * Used for Phase-3 authorization logic.
 *
 * <p>
 * Scope meanings:
 * </p>
 * <ul>
 * <li>SELF - Can only act on assigned unit (e.g., Faculty Coordinator for one
 * department)</li>
 * <li>CHILDREN - Can act on direct children only (e.g., limited scope TPO)</li>
 * <li>SUBTREE - Can act on entire subtree (e.g., Central TPO, Admin)</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> This is not enforced in Phase-1 but designed for
 * Phase-3 authorization.
 * </p>
 */
public enum ScopeLevel {
    SELF,
    CHILDREN,
    SUBTREE
}
