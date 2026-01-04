/**
 * Authentication utility functions
 */

/**
 * Check if user is authenticated
 */
export const isAuthenticated = () => {
    if (typeof window === 'undefined') return false;
    return !!localStorage.getItem('auth_token');
};

/**
 * Get current user from localStorage
 */
export const getCurrentUser = () => {
    if (typeof window === 'undefined') return null;
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
};

/**
 * Get user role
 */
export const getUserRole = () => {
    const user = getCurrentUser();
    return user?.role || null;
};

/**
 * Check if user has a specific role
 */
export const hasRole = (role) => {
    const userRole = getUserRole();
    return userRole === role;
};

/**
 * Check if user has any of the specified roles
 */
export const hasAnyRole = (roles) => {
    const userRole = getUserRole();
    return roles.includes(userRole);
};

/**
 * Role constants
 */
export const ROLES = {
    STUDENT: 'STUDENT',
    TPO: 'TPO',
    ADMIN: 'ADMIN',
    SUPER_ADMIN: 'SUPER_ADMIN',
};

/**
 * Save auth data to localStorage
 */
export const saveAuthData = (token, user) => {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('user', JSON.stringify(user));
};

/**
 * Clear auth data from localStorage
 */
export const clearAuthData = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user');
};

/**
 * Get role-based redirect path
 */
export const getRoleBasedRedirect = (role) => {
    switch (role) {
        case ROLES.STUDENT:
            return '/student/profile';
        case ROLES.TPO:
        case ROLES.ADMIN:
            return '/admin/students';
        case ROLES.SUPER_ADMIN:
            return '/dashboard';
        default:
            return '/dashboard';
    }
};
