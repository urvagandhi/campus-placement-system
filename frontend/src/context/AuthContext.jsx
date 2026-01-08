'use client';

import { createContext } from 'react';

/**
 * Auth Context
 *
 * Token storage and lifecycle are managed exclusively by AuthContext.
 * authService.js only calls APIs, never touches localStorage.
 */
export const AuthContext = createContext({
    user: null,
    isAuthenticated: false,
    isLoading: true,
    login: async () => { },
    logout: () => { },
});

export default AuthContext;
