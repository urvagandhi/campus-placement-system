'use client';

import { login as apiLogin, logout as apiLogout } from '@/services/authService';
import { useRouter } from 'next/navigation';
import { useCallback, useEffect, useMemo, useState } from 'react';
import AuthContext from './AuthContext';

const TOKEN_KEY = 'authToken';
const USER_ID_KEY = 'userId';
const USER_ROLE_KEY = 'userRole';
const COLLEGE_ID_KEY = 'collegeId';
const USER_NAME_KEY = 'userName';

/**
 * Parse JWT token to extract user info
 * Returns null if parsing fails (with try/catch for safety)
 */
function parseJwt(token) {
    try {
        if (!token) return null;
        const parts = token.split('.');
        if (parts.length !== 3) return null;

        const payload = JSON.parse(atob(parts[1]));

        // Check if token is expired
        if (payload.exp && payload.exp * 1000 < Date.now()) {
            return null;
        }

        return {
            id: payload.uid,
            email: payload.sub,
            role: payload.role,
            collegeId: payload.cid || null,
        };
    } catch (error) {
        console.error('Failed to parse JWT:', error);
        return null;
    }
}

/**
 * Get redirect URL based on role
 */
function getRedirectUrl(role) {
    const routes = {
        'STUDENT': '/dashboard/student',
        'COORDINATOR': '/dashboard/coordinator',
        'ADMIN': '/dashboard/admin',
        'SUPER_ADMIN': '/dashboard/superadmin',
    };
    return routes[role] || '/dashboard/student';
}

/**
 * AuthProvider Component
 *
 * Single source of truth for token storage and lifecycle.
 * Parses JWT on mount with try/catch - if parsing fails, auto logout.
 */
export function AuthProvider({ children }) {
    const router = useRouter();
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    /**
     * Clear all auth data from localStorage
     */
    const clearAuthData = useCallback(() => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_ID_KEY);
        localStorage.removeItem(USER_ROLE_KEY);
        localStorage.removeItem(COLLEGE_ID_KEY);
        localStorage.removeItem(USER_NAME_KEY);
    }, []);

    /**
     * Store auth data in localStorage
     */
    const storeAuthData = useCallback((token, userData) => {
        localStorage.setItem(TOKEN_KEY, token);
        if (userData.id) localStorage.setItem(USER_ID_KEY, userData.id.toString());
        if (userData.role) localStorage.setItem(USER_ROLE_KEY, userData.role);
        if (userData.collegeId) localStorage.setItem(COLLEGE_ID_KEY, userData.collegeId.toString());
        if (userData.name) localStorage.setItem(USER_NAME_KEY, userData.name);
    }, []);

    /**
     * Check authentication on mount
     */
    useEffect(() => {
        const checkAuth = () => {
            try {
                const token = localStorage.getItem(TOKEN_KEY);
                if (!token) {
                    setUser(null);
                    setIsLoading(false);
                    return;
                }

                const parsedUser = parseJwt(token);
                if (!parsedUser) {
                    // Token invalid or expired - auto logout
                    console.warn('Invalid or expired token, clearing auth data');
                    clearAuthData();
                    setUser(null);
                    setIsLoading(false);
                    return;
                }

                // Restore additional user data from localStorage
                const userName = localStorage.getItem(USER_NAME_KEY);
                setUser({
                    ...parsedUser,
                    name: userName || parsedUser.email?.split('@')[0] || 'User',
                });
            } catch (error) {
                console.error('Auth check failed:', error);
                clearAuthData();
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        };

        checkAuth();
    }, [clearAuthData]);

    /**
     * Login function
     * Calls API, stores token, sets user state
     */
    const login = useCallback(async (email, password) => {
        try {
            // Call API (authService only makes API calls, no localStorage)
            const response = await apiLogin(email, password);

            if (!response.success || !response.data) {
                throw new Error(response.message || 'Login failed');
            }

            const { token, userId, role, collegeId, redirectUrl } = response.data;

            // Parse token to get user info
            const parsedUser = parseJwt(token);
            if (!parsedUser) {
                throw new Error('Invalid token received');
            }

            // Store auth data (AuthContext is the single source of truth)
            const userData = {
                id: userId,
                role: role,
                collegeId: collegeId,
                email: parsedUser.email,
                name: parsedUser.email?.split('@')[0] || 'User',
            };

            storeAuthData(token, userData);
            setUser(userData);

            // Navigate to dashboard
            router.push(redirectUrl || getRedirectUrl(role));

            return response;
        } catch (error) {
            throw error;
        }
    }, [router, storeAuthData]);

    /**
     * Logout function
     * Clears token, resets state, redirects to login
     */
    const logout = useCallback(async () => {
        try {
            // Get token before clearing (required for API call)
            const token = localStorage.getItem(TOKEN_KEY);
            // Call API logout (optional, for audit)
            await apiLogout(token).catch(() => { });
        } finally {
            clearAuthData();
            setUser(null);
            router.push('/login');
        }
    }, [router, clearAuthData]);

    /**
     * Memoized context value
     */
    const value = useMemo(() => ({
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
    }), [user, isLoading, login, logout]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export default AuthProvider;
