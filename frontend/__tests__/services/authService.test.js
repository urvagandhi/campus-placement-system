/**
 * Unit tests for authService
 *
 * Tests the authentication API service in isolation.
 * Mocks fetch to test API interactions without network calls.
 */

import { getCurrentUser, getRedirectUrlForRole, login, logout, register } from '@/services/authService';

// API base URL used in tests
const API_BASE_URL = 'http://127.0.0.1:8080';

describe('authService', () => {
    beforeEach(() => {
        // Clear all mocks before each test
        jest.clearAllMocks();
        global.fetch = jest.fn();
    });

    // ========== login tests ==========

    describe('login', () => {
        const mockSuccessResponse = {
            success: true,
            data: {
                token: 'jwt-token-here',
                userId: 1,
                role: 'STUDENT',
                collegeId: 1,
                redirectUrl: '/dashboard/student',
            },
            message: 'Login successful',
        };

        it('calls API with correct payload', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockSuccessResponse),
            });

            await login('user@test.edu', 'password123');

            expect(global.fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/api/v1/auth/login`,
                expect.objectContaining({
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: 'user@test.edu', password: 'password123' }),
                })
            );
        });

        it('returns data on success', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockSuccessResponse),
            });

            const result = await login('user@test.edu', 'password123');

            expect(result.success).toBe(true);
            expect(result.data.token).toBe('jwt-token-here');
            expect(result.data.role).toBe('STUDENT');
        });

        it('throws error on failure', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                json: () => Promise.resolve({ success: false, message: 'Invalid credentials' }),
            });

            await expect(login('user@test.edu', 'wrongpassword'))
                .rejects.toThrow('Invalid credentials');
        });

        it('throws generic error when no message provided', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                json: () => Promise.resolve({ success: false }),
            });

            await expect(login('user@test.edu', 'wrongpassword'))
                .rejects.toThrow('Login failed');
        });
    });

    // ========== register tests ==========

    describe('register', () => {
        it('calls API with correct payload', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ success: true, data: 'User registered successfully' }),
            });

            const userData = {
                name: 'Test User',
                email: 'test@example.com',
                password: 'password123',
                confirmPassword: 'password123',
            };

            await register(userData);

            expect(global.fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/api/v1/auth/register`,
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify(userData),
                })
            );
        });

        it('throws error on registration failure', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                json: () => Promise.resolve({ success: false, message: 'Email already exists' }),
            });

            await expect(register({ email: 'existing@test.edu', password: 'pass', name: 'Test' }))
                .rejects.toThrow('Email already exists');
        });
    });

    // ========== logout tests ==========

    describe('logout', () => {
        it('calls API with token', async () => {
            global.fetch.mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({}) });

            await logout('valid-token');

            expect(global.fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/api/v1/auth/logout`,
                expect.objectContaining({
                    method: 'POST',
                    headers: expect.objectContaining({
                        'Authorization': 'Bearer valid-token',
                    }),
                })
            );
        });

        it('does nothing when token is null', async () => {
            await logout(null);

            expect(global.fetch).not.toHaveBeenCalled();
        });

        it('does nothing when token is undefined', async () => {
            await logout(undefined);

            expect(global.fetch).not.toHaveBeenCalled();
        });

        it('handles network error gracefully', async () => {
            // Suppress expected console.warn during this test
            const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => { });

            global.fetch.mockRejectedValueOnce(new Error('Network error'));

            // Should not throw
            await expect(logout('token')).resolves.toBeUndefined();

            // Restore console.warn
            warnSpy.mockRestore();
        });
    });

    // ========== getCurrentUser tests ==========

    describe('getCurrentUser', () => {
        it('returns user data when authenticated', async () => {
            const mockUserResponse = {
                success: true,
                data: {
                    userId: 1,
                    role: 'STUDENT',
                    collegeId: 1,
                },
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockUserResponse),
            });

            const result = await getCurrentUser('valid-token');

            expect(result.data.userId).toBe(1);
            expect(result.data.role).toBe('STUDENT');
        });

        it('throws error when token is missing', async () => {
            await expect(getCurrentUser(null))
                .rejects.toThrow('Not authenticated');
        });

        it('throws error when API returns failure', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                json: () => Promise.resolve({ success: false, message: 'Token expired' }),
            });

            await expect(getCurrentUser('expired-token'))
                .rejects.toThrow('Token expired');
        });

        it('calls API with correct authorization header', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ success: true, data: {} }),
            });

            await getCurrentUser('my-token');

            expect(global.fetch).toHaveBeenCalledWith(
                `${API_BASE_URL}/api/v1/auth/me`,
                expect.objectContaining({
                    method: 'GET',
                    headers: expect.objectContaining({
                        'Authorization': 'Bearer my-token',
                    }),
                })
            );
        });
    });

    // ========== getRedirectUrlForRole tests ==========

    describe('getRedirectUrlForRole', () => {
        it('returns /dashboard/student for STUDENT role', () => {
            expect(getRedirectUrlForRole('STUDENT')).toBe('/dashboard/student');
        });

        it('returns /dashboard/coordinator for COORDINATOR role', () => {
            expect(getRedirectUrlForRole('COORDINATOR')).toBe('/dashboard/coordinator');
        });

        it('returns /dashboard/admin for ADMIN role', () => {
            expect(getRedirectUrlForRole('ADMIN')).toBe('/dashboard/admin');
        });

        it('returns /dashboard/superadmin for SUPER_ADMIN role', () => {
            expect(getRedirectUrlForRole('SUPER_ADMIN')).toBe('/dashboard/superadmin');
        });

        it('returns /dashboard/student for unknown role', () => {
            expect(getRedirectUrlForRole('UNKNOWN')).toBe('/dashboard/student');
        });

        it('returns /dashboard/student for null role', () => {
            expect(getRedirectUrlForRole(null)).toBe('/dashboard/student');
        });

        it('returns /dashboard/student for undefined role', () => {
            expect(getRedirectUrlForRole(undefined)).toBe('/dashboard/student');
        });
    });
});
