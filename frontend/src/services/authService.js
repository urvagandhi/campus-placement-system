/**
 * Authentication API Service
 *
 * This service ONLY calls APIs - it does NOT touch localStorage.
 * Token storage and lifecycle are managed exclusively by AuthContext.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8080';

/**
 * Login with email and password.
 * Returns API response - tokens are set as httpOnly cookies by the server.
 *
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<Object>} Response containing userId, role, collegeId, redirectUrl
 */
export async function login(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies
        body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Login failed');
    }

    // Return data - tokens are in httpOnly cookies, not accessible via JS
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
 * Server clears httpOnly cookies.
 *
 * @returns {Promise<void>}
 */
export async function logout() {
    try {
        await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include', // Include cookies
        });
    } catch (error) {
        console.warn('Backend logout failed:', error);
        // Continue with local logout anyway
    }
}

/**
 * Refresh the access token using the httpOnly cookie.
 * Server handles the refresh token from the cookie.
 *
 * @returns {Promise<Object>} Response containing new tokens (in cookies)
 */
export async function refreshAccessToken() {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Token refresh failed');
    }

    return data;
}

/**
 * Get the current user info from the backend.
 *
 * @returns {Promise<Object>} Current user details
 */
export async function getCurrentUser() {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies
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
    refreshAccessToken,
    getCurrentUser,
    getRedirectUrlForRole,
};
