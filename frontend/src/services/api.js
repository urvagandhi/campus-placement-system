/**
 * API Service for backend communication
 *
 * Handles all HTTP requests to the Java backend with:
 * - Base URL configuration
 * - Auth token injection
 * - Error handling
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

/**
 * Get stored auth token
 */
const getToken = () => {
    if (typeof window !== 'undefined') {
        return localStorage.getItem('auth_token');
    }
    return null;
};

/**
 * Build request headers with auth token
 */
const getHeaders = () => {
    const headers = {
        'Content-Type': 'application/json',
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
};

/**
 * Handle API response
 */
const handleResponse = async (response) => {
    const data = await response.json();

    if (!response.ok) {
        // Handle specific error codes
        if (response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('auth_token');
            window.location.href = '/login';
        }
        throw new Error(data.message || 'An error occurred');
    }

    return data;
};

/**
 * Generic fetch wrapper
 */
const fetchApi = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;

    const config = {
        headers: getHeaders(),
        ...options,
    };

    const response = await fetch(url, config);
    return handleResponse(response);
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

    apply: async (driveId, notes = '') => {
        return fetchApi('/applications/apply', {
            method: 'POST',
            body: JSON.stringify({ driveId, notes }),
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
