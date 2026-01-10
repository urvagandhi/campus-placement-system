/**
 * API Service for backend communication
 *
 * Handles all HTTP requests to the Java backend with:
 * - Base URL configuration
 * - Cookie-based authentication (httpOnly cookies)
 * - Error handling
 * - Automatic token refresh on 401
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

// Flag to prevent multiple simultaneous refresh attempts
let isRefreshing = false;
let refreshPromise = null;

/**
 * Build request headers
 */
const getHeaders = () => {
    return {
        'Content-Type': 'application/json',
    };
};

/**
 * Attempt to refresh the access token
 */
const attemptTokenRefresh = async () => {
    // If already refreshing, wait for the existing refresh
    if (isRefreshing && refreshPromise) {
        return refreshPromise;
    }

    isRefreshing = true;
    refreshPromise = (async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', // Cookies sent automatically
            });

            if (!response.ok) {
                return false;
            }

            const data = await response.json();
            return data.success;
        } catch (error) {
            console.error('Token refresh failed:', error);
            return false;
        } finally {
            isRefreshing = false;
            refreshPromise = null;
        }
    })();

    return refreshPromise;
};

/**
 * Clear auth data and redirect to login
 */
const clearAuthAndRedirect = () => {
    // Clear only user info from localStorage (tokens are in httpOnly cookies)
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    localStorage.removeItem('collegeId');
    localStorage.removeItem('userName');
    window.location.href = '/login';
};

/**
 * Handle API response with token refresh on 401
 */
const handleResponse = async (response, retryFn = null) => {
    // If 401 and we have a retry function, attempt refresh
    if (response.status === 401 && retryFn) {
        const refreshed = await attemptTokenRefresh();
        if (refreshed) {
            // Retry the original request with new token
            return retryFn();
        } else {
            // Refresh failed, redirect to login
            clearAuthAndRedirect();
            throw new Error('Session expired. Please log in again.');
        }
    }

    const data = await response.json();

    if (!response.ok) {
        // Handle specific error codes
        if (response.status === 401) {
            // No retry function provided, just redirect
            clearAuthAndRedirect();
        }
        throw new Error(data.message || 'An error occurred');
    }

    return data;
};

/**
 * Generic fetch wrapper with automatic token refresh
 */
const fetchApi = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;

    const makeRequest = async () => {
        const config = {
            headers: getHeaders(),
            credentials: 'include', // Include cookies
            ...options,
        };
        return fetch(url, config);
    };

    const response = await makeRequest();

    // Pass retry function for 401 handling
    return handleResponse(response, async () => {
        const retryResponse = await makeRequest();
        return handleResponse(retryResponse); // No retry on second attempt
    });
};

// ==================== Auth API ====================

export const authApi = {
    login: async (email, password) => {
        return fetchApi('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password }),
        });
    },

    register: async (data) => {
        return fetchApi('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    logout: async () => {
        const result = await fetchApi('/auth/logout', { method: 'POST' });
        localStorage.removeItem('auth_token');
        return result;
    },

    getCurrentUser: async () => {
        return fetchApi('/auth/me');
    },
};

// ==================== Students API ====================

export const studentsApi = {
    getAll: async () => {
        return fetchApi('/students');
    },

    getById: async (id) => {
        return fetchApi(`/students/${id}`);
    },

    getMyProfile: async () => {
        return fetchApi('/students/me');
    },

    /**
     * Update career-layer profile fields only.
     * Academic fields will be ignored by backend.
     */
    updateCareerProfile: async (data) => {
        return fetchApi('/students/me/profile', {
            method: 'PATCH',
            body: JSON.stringify(data),
        });
    },

    updateProfile: async (data) => {
        return fetchApi('/students', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    updateSkills: async (id, skills) => {
        return fetchApi(`/students/${id}/skills`, {
            method: 'PATCH',
            body: JSON.stringify(skills),
        });
    },
};

// ==================== Drives API ====================

export const drivesApi = {
    getAll: async () => {
        return fetchApi('/drives');
    },

    getById: async (id) => {
        return fetchApi(`/drives/${id}`);
    },

    getUpcoming: async () => {
        return fetchApi('/drives/upcoming');
    },

    create: async (data) => {
        return fetchApi('/drives', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    update: async (id, data) => {
        return fetchApi(`/drives/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    },
};

// ==================== Applications API ====================

export const applicationsApi = {
    getMyApplications: async () => {
        return fetchApi('/applications/my');
    },

    getByDrive: async (driveId) => {
        return fetchApi(`/applications/drive/${driveId}`);
    },

    apply: async (driveId, coverLetter = '') => {
        return fetchApi('/applications/apply', {
            method: 'POST',
            body: JSON.stringify({ driveId, coverLetter }),
        });
    },

    withdraw: async (id) => {
        return fetchApi(`/applications/${id}/withdraw`, {
            method: 'DELETE',
        });
    },
};

// ==================== Eligibility API ====================

export const eligibilityApi = {
    checkEligibility: async (studentId, driveId) => {
        return fetchApi(`/eligibility/check?studentId=${studentId}&driveId=${driveId}`);
    },

    getMyEligibility: async (driveId) => {
        return fetchApi(`/eligibility/my/${driveId}`);
    },
};

// ==================== Analytics API ====================

export const analyticsApi = {
    getOverview: async () => {
        return fetchApi('/analytics/overview');
    },

    getDepartmentStats: async () => {
        return fetchApi('/analytics/departments');
    },
};

// ==================== Companies API ====================

export const companiesApi = {
    getAll: async () => {
        return fetchApi('/companies');
    },

    getById: async (id) => {
        return fetchApi(`/companies/${id}`);
    },

    create: async (data) => {
        return fetchApi('/companies', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    update: async (id, data) => {
        return fetchApi(`/companies/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    },
};

export default {
    auth: authApi,
    students: studentsApi,
    drives: drivesApi,
    applications: applicationsApi,
    eligibility: eligibilityApi,
    analytics: analyticsApi,
    companies: companiesApi,
};
