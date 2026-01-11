# Frontend Authentication Feature

Complete documentation for the authentication system in PlacementPro frontend.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components](#components)
4. [Hooks](#hooks)
5. [Services](#services)
6. [Token Management](#token-management)
7. [Protected Routes](#protected-routes)
8. [Error Handling](#error-handling)
9. [Testing](#testing)

---

## Overview

The frontend authentication system provides:

- **React Context-based state management**: Centralized auth state
- **JWT token handling**: Secure storage and automatic refresh
- **Role-based routing**: Automatic redirects based on user role
- **Protected routes**: Guards for authenticated-only pages
- **Persistent sessions**: Survives page reloads

### Key Features

1. **Single source of truth**: AuthProvider manages all auth state
2. **Automatic token validation**: On mount, validates stored token
3. **Secure storage**: Token stored in localStorage
4. **Role-based redirects**: After login, user goes to correct dashboard

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              App Layout                                      │
│                                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                          AuthProvider                                  │  │
│  │                                                                        │  │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │  │
│  │  │                        State                                     │  │  │
│  │  │  • user: { id, role, email, collegeId }                         │  │  │
│  │  │  • isLoading: boolean                                           │  │  │
│  │  │  • isAuthenticated: boolean                                     │  │  │
│  │  └─────────────────────────────────────────────────────────────────┘  │  │
│  │                                                                        │  │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │  │
│  │  │                       Methods                                    │  │  │
│  │  │  • login(email, password)                                       │  │  │
│  │  │  • logout()                                                     │  │  │
│  │  │  • getCurrentUser()                                             │  │  │
│  │  └─────────────────────────────────────────────────────────────────┘  │  │
│  │                                                                        │  │
│  │  ┌─────────────────────┐  ┌─────────────────────────────────────┐    │  │
│  │  │    Login Page       │  │    Protected Dashboard               │    │  │
│  │  │    (Public)         │  │    (Requires Auth)                   │    │  │
│  │  └─────────────────────┘  └─────────────────────────────────────┘    │  │
│  │                                                                        │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Components

### AuthProvider

**Location**: `src/context/AuthProvider.jsx`

The root provider that manages authentication state.

```jsx
'use client';

import { AuthContext } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { useState, useCallback, useEffect } from 'react';

const TOKEN_KEY = 'authToken';
const USER_ID_KEY = 'userId';
const USER_ROLE_KEY = 'userRole';
const COLLEGE_ID_KEY = 'collegeId';
const USER_NAME_KEY = 'userName';

export function AuthProvider({ children }) {
    const router = useRouter();
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    // Initialize from localStorage on mount
    useEffect(() => {
        initializeAuth();
    }, []);

    const initializeAuth = async () => {
        setIsLoading(true);
        try {
            const token = localStorage.getItem(TOKEN_KEY);
            if (!token) {
                setUser(null);
                return;
            }

            // Parse and validate JWT
            const payload = parseJwt(token);
            if (!payload || isTokenExpired(payload)) {
                clearAuthData();
                setUser(null);
                return;
            }

            // Set user from token
            setUser({
                id: payload.uid,
                role: payload.role,
                email: payload.sub,
                collegeId: payload.cid,
            });
        } catch (error) {
            console.error('Auth initialization failed:', error);
            clearAuthData();
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    };

    const login = useCallback(async (email, password, redirectUrl) => {
        const response = await authService.login(email, password);

        storeAuthData(response.token, {
            id: response.userId,
            role: response.role,
            email,
            collegeId: response.collegeId,
        });

        setUser({
            id: response.userId,
            role: response.role,
            email,
            collegeId: response.collegeId,
        });

        router.push(redirectUrl || getRedirectUrl(response.role));
        return response;
    }, [router]);

    const logout = useCallback(async () => {
        try {
            const token = localStorage.getItem(TOKEN_KEY);
            await authService.logout(token).catch(() => {});
        } finally {
            clearAuthData();
            setUser(null);
            router.push('/login');
        }
    }, [router]);

    return (
        <AuthContext.Provider value={{
            user,
            isLoading,
            isAuthenticated: !!user,
            login,
            logout
        }}>
            {children}
        </AuthContext.Provider>
    );
}
```

### AuthContext

**Location**: `src/context/AuthContext.jsx`

```jsx
'use client';

import { createContext } from 'react';

export const AuthContext = createContext({
    user: null,
    isLoading: true,
    isAuthenticated: false,
    login: async () => {},
    logout: async () => {},
});
```

---

## Hooks

### useAuth

**Location**: `src/hooks/useAuth.js`

Custom hook to access authentication state and methods.

```jsx
'use client';

import { useContext } from 'react';
import { AuthContext } from '@/context/AuthContext';

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
}
```

**Usage:**

```jsx
import { useAuth } from '@/hooks/useAuth';

function MyComponent() {
    const { user, isLoading, isAuthenticated, login, logout } = useAuth();

    if (isLoading) {
        return <LoadingSpinner />;
    }

    if (!isAuthenticated) {
        return <LoginPrompt />;
    }

    return (
        <div>
            <p>Welcome, {user.email}!</p>
            <p>Role: {user.role}</p>
            <button onClick={logout}>Logout</button>
        </div>
    );
}
```

---

## Services

### authService

**Location**: `src/services/authService.js`

API service for authentication endpoints.

```javascript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export const authService = {
    /**
     * Login with email and password
     * @param {string} email
     * @param {string} password
     * @returns {Promise<LoginResponse>}
     */
    async login(email, password) {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Login failed');
        }

        const data = await response.json();
        return data.data;
    },

    /**
     * Logout current user
     * @param {string} token
     */
    async logout(token) {
        if (!token) return;

        try {
            await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
        } catch (error) {
            console.warn('Logout API call failed:', error.message);
        }
    },

    /**
     * Get current user info
     * @param {string} token
     * @returns {Promise<UserInfo>}
     */
    async getCurrentUser(token) {
        if (!token) {
            throw new Error('No token provided');
        }

        const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error('Failed to get current user');
        }

        const data = await response.json();
        return data.data;
    },

    /**
     * Get redirect URL for role
     * @param {string} role
     * @returns {string}
     */
    getRedirectUrlForRole(role) {
        const routes = {
            'STUDENT': '/dashboard/student',
            'COORDINATOR': '/dashboard/coordinator',
            'ADMIN': '/dashboard/admin',
            'SUPER_ADMIN': '/dashboard/superadmin',
        };
        return routes[role] || '/dashboard/student';
    },
};
```

---

## Token Management

### Storage Keys

| Key | Purpose |
|-----|---------|
| `authToken` | JWT token |
| `userId` | User's database ID |
| `userRole` | User's role |
| `collegeId` | User's college ID |
| `userName` | User's display name |

### Storing Token

```javascript
const storeAuthData = (token, userData) => {
    localStorage.setItem('authToken', token);
    localStorage.setItem('userId', userData.id);
    localStorage.setItem('userRole', userData.role);
    if (userData.collegeId) {
        localStorage.setItem('collegeId', userData.collegeId);
    }
    if (userData.name) {
        localStorage.setItem('userName', userData.name);
    }
};
```

### Clearing Token

```javascript
const clearAuthData = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    localStorage.removeItem('collegeId');
    localStorage.removeItem('userName');
};
```

### Parsing JWT

```javascript
const parseJwt = (token) => {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        return null;
    }
};
```

### Checking Expiration

```javascript
const isTokenExpired = (payload) => {
    if (!payload || !payload.exp) return true;
    const expirationTime = payload.exp * 1000; // Convert to milliseconds
    return Date.now() >= expirationTime;
};
```

---

## Protected Routes

### Route Guard Component

```jsx
'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export function ProtectedRoute({ children, allowedRoles }) {
    const { user, isLoading, isAuthenticated } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            router.push('/login');
            return;
        }

        if (!isLoading && user && allowedRoles && !allowedRoles.includes(user.role)) {
            router.push('/forbidden');
        }
    }, [isLoading, isAuthenticated, user, allowedRoles, router]);

    if (isLoading) {
        return <LoadingScreen />;
    }

    if (!isAuthenticated) {
        return null;
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return null;
    }

    return children;
}
```

### Usage in Layout

```jsx
// app/dashboard/student/layout.jsx
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';

export default function StudentLayout({ children }) {
    return (
        <ProtectedRoute allowedRoles={['STUDENT']}>
            {children}
        </ProtectedRoute>
    );
}
```

---

## Error Handling

### Login Error Handling

```jsx
const [error, setError] = useState('');

const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
        await login(email, password);
    } catch (err) {
        if (err.message.includes('Invalid credentials')) {
            setError('Invalid email or password');
        } else if (err.message.includes('deactivated')) {
            setError('Your account has been deactivated. Contact support.');
        } else if (err.message.includes('College')) {
            setError('Your college is not active. Contact your administrator.');
        } else {
            setError('An error occurred. Please try again.');
        }
    } finally {
        setIsLoading(false);
    }
};
```

### API Error Handling

```javascript
// In authService.js
async login(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));

        switch (response.status) {
            case 401:
                throw new Error(error.message || 'Invalid credentials');
            case 403:
                throw new Error(error.message || 'Account deactivated');
            case 404:
                throw new Error(error.message || 'Resource not found');
            default:
                throw new Error(error.message || 'Login failed');
        }
    }

    return (await response.json()).data;
}
```

---

## Testing

### Unit Tests (Jest)

```jsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AuthProvider } from '@/context/AuthProvider';
import LoginPage from '@/app/login/page';

// Mock router
jest.mock('next/navigation', () => ({
    useRouter: () => ({
        push: jest.fn(),
    }),
}));

// Mock authService
jest.mock('@/services/authService', () => ({
    authService: {
        login: jest.fn(),
    },
}));

describe('LoginPage', () => {
    it('renders login form', () => {
        render(
            <AuthProvider>
                <LoginPage />
            </AuthProvider>
        );

        expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    });

    it('shows error on failed login', async () => {
        authService.login.mockRejectedValue(new Error('Invalid credentials'));

        render(
            <AuthProvider>
                <LoginPage />
            </AuthProvider>
        );

        fireEvent.change(screen.getByPlaceholderText(/email/i), {
            target: { value: 'test@test.edu' },
        });
        fireEvent.change(screen.getByPlaceholderText(/password/i), {
            target: { value: 'wrongpassword' },
        });
        fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

        await waitFor(() => {
            expect(screen.getByText(/invalid/i)).toBeInTheDocument();
        });
    });
});
```

### E2E Tests (Playwright)

```typescript
import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
    test('successful login redirects to dashboard', async ({ page }) => {
        await page.goto('/login');

        await page.fill('input[name="email"]', 'student@test.edu');
        await page.fill('input[name="password"]', 'password123');
        await page.click('button[type="submit"]');

        await page.waitForURL('**/dashboard/student**');
        expect(page.url()).toContain('/dashboard/student');
    });

    test('invalid credentials show error', async ({ page }) => {
        await page.goto('/login');

        await page.fill('input[name="email"]', 'test@test.edu');
        await page.fill('input[name="password"]', 'wrongpassword');
        await page.click('button[type="submit"]');

        await expect(page.getByText(/invalid/i)).toBeVisible();
    });
});
```
