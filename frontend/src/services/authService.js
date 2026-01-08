/**
 * Authentication API Service
 *
 * This service ONLY calls APIs - it does NOT touch localStorage.
 * Token storage and lifecycle are managed exclusively by AuthContext.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8080';

/**
 * Login with email and password.
 * Returns API response - does NOT store token (AuthContext handles that).
 *
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<Object>} Response containing token, userId, role, collegeId, redirectUrl
 */
export async function login(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Login failed');
    }

    // Return data - AuthContext will handle token storage
    return data;
}

/**
 * Register a new user.
 *
 * NOTE: This registration is for demo/testing only.
 * In production, student registration is performed by the Placement Coordinator.
 *
 * @param {Object} userData - { name, email, password, confirmPassword }
 * @returns {Promise<Object>} Registration response
 */
export async function register(userData) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Registration failed');
    }

    return data;
}

/**
 * Logout the current user.
 * Called by AuthContext - does NOT clear localStorage here.
 *
 * @param {string} token - Auth token for API call
 * @returns {Promise<void>}
 */
export async function logout(token) {
    if (!token) return;

    try {
        await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });
    } catch (error) {
        console.warn('Backend logout failed:', error);
        // Continue with local logout anyway
    }
}

/**
 * Get the current user info from the backend.
 *
 * @param {string} token - Auth token
 * @returns {Promise<Object>} Current user details
 */
export async function getCurrentUser(token) {
    if (!token) {
        throw new Error('Not authenticated');
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Failed to get user info');
    }

    return data;
}

/**
 * Get redirect URL based on role.
 *
 * @param {string} role - User role
 * @returns {string} Dashboard URL
 */
export function getRedirectUrlForRole(role) {
    const roleRoutes = {
        'STUDENT': '/dashboard/student',
        'COORDINATOR': '/dashboard/coordinator',
        'ADMIN': '/dashboard/admin',
        'SUPER_ADMIN': '/dashboard/superadmin',
    };
    return roleRoutes[role] || '/dashboard/student';
}

export default {
    login,
    register,
    logout,
    getCurrentUser,
    getRedirectUrlForRole,
};
