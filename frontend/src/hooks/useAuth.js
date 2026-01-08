'use client';

import { AuthContext } from '@/context/AuthContext';
import { useContext } from 'react';

/**
 * Custom hook for easy auth access
 *
 * Usage:
 * const { user, isAuthenticated, login, logout } = useAuth();
 */
export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
}

export default useAuth;
