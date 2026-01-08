/**
 * Unit tests for AuthProvider context
 *
 * Tests the authentication context provider including:
 * - Initialization from localStorage
 * - Login flow
 * - Logout flow
 * - Token parsing and expiration handling
 */

import { AuthProvider } from '@/context/AuthProvider';
import { useAuth } from '@/hooks/useAuth';
import { act, render, screen, waitFor } from '@testing-library/react';

// Mock the authService module
jest.mock('@/services/authService', () => ({
    login: jest.fn(),
    logout: jest.fn(),
}));

// Mock useRouter
const mockPush = jest.fn();
jest.mock('next/navigation', () => ({
    useRouter: () => ({
        push: mockPush,
    }),
}));

import { login as apiLogin, logout as apiLogout } from '@/services/authService';

// Helper component to test the auth context
function TestConsumer({ testId = 'auth-state' }) {
    const { user, isAuthenticated, isLoading, login, logout } = useAuth();

    return (
        <div data-testid={testId}>
            <span data-testid="loading">{isLoading ? 'loading' : 'ready'}</span>
            <span data-testid="authenticated">{isAuthenticated ? 'true' : 'false'}</span>
            <span data-testid="user">{user ? JSON.stringify(user) : 'null'}</span>
            <button data-testid="login-btn" onClick={() => login('test@test.edu', 'password')}>
                Login
            </button>
            <button data-testid="logout-btn" onClick={() => logout()}>
                Logout
            </button>
        </div>
    );
}

// Helper to create a valid JWT token for testing
function createTestToken(payload, expiresIn = 3600) {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const exp = Math.floor(Date.now() / 1000) + expiresIn;
    const payloadWithExp = { ...payload, exp };
    const payloadBase64 = btoa(JSON.stringify(payloadWithExp));
    const signature = 'test-signature';
    return `${header}.${payloadBase64}.${signature}`;
}

describe('AuthProvider', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        window.localStorage.getItem.mockReset();
        window.localStorage.setItem.mockReset();
        window.localStorage.removeItem.mockReset();
    });

    // ========== Initialization Tests ==========

    describe('Initialization', () => {
        it('initializes with no token as unauthenticated', async () => {
            window.localStorage.getItem.mockReturnValue(null);

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            expect(screen.getByTestId('authenticated').textContent).toBe('false');
            expect(screen.getByTestId('user').textContent).toBe('null');
        });

        it('initializes from valid token in localStorage', async () => {
            const token = createTestToken({
                uid: 1,
                sub: 'user@test.edu',
                role: 'STUDENT',
                cid: 1,
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                if (key === 'userName') return 'Test User';
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            expect(screen.getByTestId('authenticated').textContent).toBe('true');
        });

        it('clears auth data when token is expired', async () => {
            // Create expired token (negative expiry)
            const token = createTestToken({
                uid: 1,
                sub: 'user@test.edu',
                role: 'STUDENT',
            }, -3600); // Expired 1 hour ago

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            expect(screen.getByTestId('authenticated').textContent).toBe('false');
            expect(window.localStorage.removeItem).toHaveBeenCalled();
        });

        it('clears auth data when token is malformed', async () => {
            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return 'not-a-valid-jwt';
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            expect(screen.getByTestId('authenticated').textContent).toBe('false');
            expect(window.localStorage.removeItem).toHaveBeenCalled();
        });
    });

    describe('login', () => {
        it('stores token and sets user on successful login', async () => {
            window.localStorage.getItem.mockReturnValue(null);

            const token = createTestToken({
                uid: 2,
                sub: 'new@test.edu',
                role: 'COORDINATOR',
                cid: 1,
            });

            apiLogin.mockResolvedValueOnce({
                success: true,
                data: {
                    token,
                    userId: 2,
                    role: 'COORDINATOR',
                    collegeId: 1,
                    redirectUrl: '/dashboard/coordinator',
                },
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            // Click login button
            await act(async () => {
                screen.getByTestId('login-btn').click();
            });

            // Wait for login to complete
            await waitFor(() => {
                expect(window.localStorage.setItem).toHaveBeenCalledWith('authToken', token);
            });

            expect(mockPush).toHaveBeenCalledWith('/dashboard/coordinator');
        });

        it('handles login API call correctly', async () => {
            window.localStorage.getItem.mockReturnValue(null);

            // Create a valid token for the test
            const testToken = createTestToken({
                uid: 1,
                sub: 'test@test.edu',
                role: 'STUDENT',
                cid: 1,
            });

            apiLogin.mockResolvedValueOnce({
                success: true,
                data: {
                    token: testToken,
                    userId: 1,
                    role: 'STUDENT',
                    collegeId: 1,
                    redirectUrl: '/dashboard/student',
                },
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            await act(async () => {
                screen.getByTestId('login-btn').click();
            });

            // Verify apiLogin was called
            await waitFor(() => {
                expect(apiLogin).toHaveBeenCalledWith('test@test.edu', 'password');
            });
        });
    });

    // ========== Logout Tests ==========

    describe('logout', () => {
        it('clears auth data and redirects to login', async () => {
            const token = createTestToken({
                uid: 1,
                sub: 'user@test.edu',
                role: 'STUDENT',
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            apiLogout.mockResolvedValueOnce(undefined);

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('authenticated').textContent).toBe('true');
            });

            // Click logout button
            await act(async () => {
                screen.getByTestId('logout-btn').click();
            });

            await waitFor(() => {
                expect(window.localStorage.removeItem).toHaveBeenCalledWith('authToken');
            });

            expect(mockPush).toHaveBeenCalledWith('/login');
        });
    });

    // ========== JWT Parsing Tests ==========

    describe('JWT parsing', () => {
        it('extracts user ID from token', async () => {
            const token = createTestToken({
                uid: 42,
                sub: 'user@test.edu',
                role: 'ADMIN',
                cid: 5,
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            const userJson = screen.getByTestId('user').textContent;
            const user = JSON.parse(userJson);
            expect(user.id).toBe(42);
        });

        it('extracts role from token', async () => {
            const token = createTestToken({
                uid: 1,
                sub: 'user@test.edu',
                role: 'SUPER_ADMIN',
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            const userJson = screen.getByTestId('user').textContent;
            const user = JSON.parse(userJson);
            expect(user.role).toBe('SUPER_ADMIN');
        });

        it('extracts college ID from token when present', async () => {
            const token = createTestToken({
                uid: 1,
                sub: 'user@test.edu',
                role: 'STUDENT',
                cid: 99,
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            const userJson = screen.getByTestId('user').textContent;
            const user = JSON.parse(userJson);
            expect(user.collegeId).toBe(99);
        });

        it('handles null college ID for SUPER_ADMIN', async () => {
            const token = createTestToken({
                uid: 1,
                sub: 'admin@platform.com',
                role: 'SUPER_ADMIN',
                // No cid claim
            });

            window.localStorage.getItem.mockImplementation((key) => {
                if (key === 'authToken') return token;
                return null;
            });

            render(
                <AuthProvider>
                    <TestConsumer />
                </AuthProvider>
            );

            await waitFor(() => {
                expect(screen.getByTestId('loading').textContent).toBe('ready');
            });

            const userJson = screen.getByTestId('user').textContent;
            const user = JSON.parse(userJson);
            expect(user.collegeId).toBeNull();
        });
    });
});
