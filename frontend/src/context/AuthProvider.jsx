'use client';

import { getCurrentUser as apiGetCurrentUser, login as apiLogin, logout as apiLogout } from '@/services/authService';
import { useRouter } from 'next/navigation';
import { useCallback, useEffect, useMemo, useState } from 'react';
import AuthContext from './AuthContext';

const USER_ID_KEY = 'userId';
const USER_ROLE_KEY = 'userRole';
const COLLEGE_ID_KEY = 'collegeId';
const USER_NAME_KEY = 'userName';

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
 * Manages user state with httpOnly cookie-based authentication.
 * Tokens are stored in httpOnly cookies (not accessible by JS).
 * User info is stored in localStorage for display purposes.
 */
export function AuthProvider({ children }) {
    const router = useRouter();
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    /**
     * Clear user data from localStorage
     */
    const clearAuthData = useCallback(() => {
        localStorage.removeItem(USER_ID_KEY);
        localStorage.removeItem(USER_ROLE_KEY);
        localStorage.removeItem(COLLEGE_ID_KEY);
        localStorage.removeItem(USER_NAME_KEY);
    }, []);

    /**
     * Store user data in localStorage
     */
    const storeUserData = useCallback((userData) => {
        if (userData.id) localStorage.setItem(USER_ID_KEY, userData.id.toString());
        if (userData.role) localStorage.setItem(USER_ROLE_KEY, userData.role);
        if (userData.collegeId) localStorage.setItem(COLLEGE_ID_KEY, userData.collegeId.toString());
        if (userData.name) localStorage.setItem(USER_NAME_KEY, userData.name);
    }, []);

    /**
     * Check authentication status
     */
    const checkAuth = useCallback(async () => {
        try {
            // Try to get current user from backend (uses httpOnly cookie)
            const response = await apiGetCurrentUser();
            if (response.success && response.data) {
                const userData = {
                    id: response.data.userId,
                    role: response.data.role,
                    collegeId: response.data.collegeId,
                    email: response.data.email || '',
                    name: response.data.name || localStorage.getItem(USER_NAME_KEY) || response.data.email?.split('@')[0] || 'User',
                    phoneNumber: response.data.phoneNumber || ''
                };
                setUser(userData);
                storeUserData(userData);
            } else {
                // Not authenticated
                clearAuthData();
                setUser(null);
            }
        } catch (error) {
            // Not authenticated or error
            console.debug('Auth check failed:', error.message);
            clearAuthData();
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, [clearAuthData, storeUserData]);

    /**
     * Check authentication on mount
     */
    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    /**
     * Login function
     * Calls API, stores user data, navigates to dashboard
     */
    const login = useCallback(async (email, password, honeypot) => {
        try {
            // Call API - tokens are set as httpOnly cookies by server
            const response = await apiLogin(email, password, honeypot);

            if (!response.success || !response.data) {
                throw new Error(response.message || 'Login failed');
            }

            const { userId, role, collegeId, redirectUrl, mustChangePassword, firstLoginToken, name } = response.data;

            // Store user data (not tokens - they're in httpOnly cookies)
            const userData = {
                id: userId,
                role: role,
                collegeId: collegeId,
                email: email,
                name: name || email?.split('@')[0] || 'User', // Use backend name, fallback to email prefix
            };

            storeUserData(userData);
            setUser(userData);

            // Navigate to appropriate page
            // If must change password, append the token to the redirect URL
            let targetUrl = redirectUrl || getRedirectUrl(role);
            if (mustChangePassword && firstLoginToken) {
                targetUrl = `/change-password?token=${encodeURIComponent(firstLoginToken)}`;
            }
            router.push(targetUrl);

            return response;
        } catch (error) {
            throw error;
        }
    }, [router, storeUserData]);

    /**
     * Logout function
     * Clears cookies (via API), resets state, redirects to login
     */
    const logout = useCallback(async () => {
        try {
            // Call API logout - clears httpOnly cookies
            await apiLogout().catch(() => { });
        } finally {
            clearAuthData();
            setUser(null);
            router.push('/login');
        }
    }, [router, clearAuthData]);

    /**
     * Idle Timeout Detection
     * Automatically logs out user after inactivity
     */
    useEffect(() => {
        if (!user) return;

        let idleTimer;
        // Match backend idle timeout (30 mins)
        const IDLE_TIMEOUT_MS = 30 * 60 * 1000;

        const resetTimer = () => {
            if (idleTimer) clearTimeout(idleTimer);
            idleTimer = setTimeout(() => {
                console.log('User active session expired due to inactivity');
                logout();
            }, IDLE_TIMEOUT_MS);
        };

        // Events that define "activity"
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];

        // Initial timer
        resetTimer();

        // Add listeners
        events.forEach(event => {
            window.addEventListener(event, resetTimer);
        });

        // Cleanup
        return () => {
            if (idleTimer) clearTimeout(idleTimer);
            events.forEach(event => {
                window.removeEventListener(event, resetTimer);
            });
        };
    }, [user, logout]);

    /**
     * Memoized context value
     */
    const value = useMemo(() => ({
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        refreshUser: checkAuth
    }), [user, isLoading, login, logout, checkAuth]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export default AuthProvider;
