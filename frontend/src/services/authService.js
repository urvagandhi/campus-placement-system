/**
 * Authentication API Service
 *
 * This service ONLY calls APIs - it does NOT touch localStorage.
 * Token storage and lifecycle are managed exclusively by AuthContext.
 */

import { fetchWithRetry as fetch } from '@/utils/fetchUtils';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Login with email and password.
 * Returns API response - tokens are set as httpOnly cookies by the server.
 *
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<Object>} Response containing userId, role, collegeId, redirectUrl
 */
export async function login(email, password, honeypot) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies
        body: JSON.stringify({ email, password, username: honeypot }),
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
 * Get all active sessions for the current user.
 *
 * @returns {Promise<Array>} List of active sessions
 */
export async function getActiveSessions() {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/sessions`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Failed to get sessions');
    }

    return data.data; // Return the sessions array
}

/**
 * Revoke a specific session.
 *
 * @param {number} sessionId - ID of the session to revoke
 * @returns {Promise<Object>} Success response
 */
export async function revokeSession(sessionId) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/sessions/revoke`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ id: sessionId }),
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Failed to revoke session');
    }

    return data;
}

/**
 * Get security dashboard statistics.
 *
 * @returns {Promise<Object>} Security stats
 */
export async function getSecurityStats() {
    const response = await fetch(`${API_BASE_URL}/api/v1/security/stats`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    });

    if (response.status === 401 || response.status === 403) {
        throw new Error('Access Denied: You do not have permission to view security analytics.');
    }

    if (!response.ok) {
        throw new Error(`Failed to get security stats: ${response.status} ${response.statusText}`);
    }

    try {
        const data = await response.json();
        return data.data;
    } catch (err) {
        console.error("Failed to parse security stats response:", err);
        throw new Error("Invalid response format from server.");
    }
}

/**
 * Export audit logs as CSV.
 * Directly triggers a browser download.
 */
export async function exportAuditLogs() {
    const response = await fetch(`${API_BASE_URL}/api/v1/security/export`, {
        method: 'GET',
        credentials: 'include',
    });

    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Failed to export logs');
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `security_audit_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
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
        'SECURITY_OFFICER': '/dashboard/admin/security', // Added for completeness
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
    getActiveSessions,
    revokeSession,
    getSecurityStats,
    exportAuditLogs,
};
