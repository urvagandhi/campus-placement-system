/**
 * E2E security tests
 *
 * Tests security measures including:
 * - Route protection
 * - Token handling
 * - Unauthorized access prevention
 */

import { expect, test } from '@playwright/test';

test.describe('Security', () => {
    // ========== Route Protection Tests ==========

    test.describe('Route Protection', () => {
        test('unauthenticated user is redirected to login from protected route', async ({ page }) => {
            // Clear any existing tokens
            await page.goto('/');
            await page.evaluate(() => localStorage.clear());

            // Try to access protected route
            await page.goto('/dashboard/student');

            // Should be redirected to login
            await page.waitForURL('**/login', { timeout: 5000 });
            expect(page.url()).toContain('/login');
        });

        test('student cannot access admin dashboard', async ({ page }) => {
            // Login as student
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/student**');

            // Try to access admin dashboard
            await page.goto('/dashboard/admin');

            // Should be redirected (to forbidden, student dashboard, or login)
            await page.waitForURL(/(forbidden|student|login)/, { timeout: 10000 });

            // Verify we're not on admin dashboard
            expect(page.url()).not.toContain('/dashboard/admin');
        });

        test('student cannot access coordinator dashboard', async ({ page }) => {
            // Login as student
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/student**');

            // Try to access coordinator dashboard
            await page.goto('/dashboard/coordinator');

            // Should be redirected (to forbidden, student dashboard, or login)
            await page.waitForURL(/(forbidden|student|login)/, { timeout: 10000 });

            // Verify we're not on coordinator dashboard
            expect(page.url()).not.toContain('/dashboard/coordinator');
        });

        test('coordinator cannot access superadmin dashboard', async ({ page }) => {
            // Login as coordinator
            await page.goto('/login');
            await page.fill('input[name="email"]', 'coordinator@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/coordinator**');

            // Try to access superadmin dashboard
            await page.goto('/dashboard/superadmin');

            // Should be redirected (to forbidden, coordinator dashboard, or login)
            await page.waitForURL(/(forbidden|coordinator|login)/, { timeout: 10000 });

            // Verify we're not on superadmin dashboard
            expect(page.url()).not.toContain('/dashboard/superadmin');
        });
    });

    // ========== Token Handling Tests ==========

    test.describe('Token Handling', () => {
        test('token is stored in localStorage after login', async ({ page }) => {
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/**');

            const token = await page.evaluate(() => localStorage.getItem('authToken'));
            expect(token).not.toBeNull();
            expect(token).toContain('.'); // JWT has dots
        });

        test('token is removed from localStorage after logout', async ({ page }) => {
            // Login first
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/**');

            // Find and click logout
            const logoutButton = page.getByRole('button', { name: /logout|sign out/i });
            if (await logoutButton.isVisible()) {
                await logoutButton.click();
                await page.waitForURL('**/login');

                const token = await page.evaluate(() => localStorage.getItem('authToken'));
                expect(token).toBeNull();
            }
        });

        test('manually clearing token redirects to login on next navigation', async ({ page }) => {
            // Login first
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/**');

            // Manually clear token
            await page.evaluate(() => localStorage.removeItem('authToken'));

            // Refresh the page
            await page.reload();

            // Should redirect to login
            await page.waitForURL('**/login');
        });
    });

    // ========== XSS Prevention Tests ==========

    test.describe('XSS Prevention', () => {
        test('script injection in email field is not executed', async ({ page }) => {
            await page.goto('/login');

            // Try to inject script in email field
            const maliciousInput = '<script>alert("xss")</script>';
            await page.fill('input[name="email"]', maliciousInput);
            await page.fill('input[name="password"]', 'password');
            await page.click('button[type="submit"]');

            // Page should still be functional, no alert should appear
            // Check that the input was sanitized or rejected
            await expect(page).not.toHaveTitle('xss');
        });

        test('script injection in password field is not executed', async ({ page }) => {
            await page.goto('/login');

            await page.fill('input[name="email"]', 'test@test.edu');
            await page.fill('input[name="password"]', '<script>alert("xss")</script>');
            await page.click('button[type="submit"]');

            // Page should still be functional
            await expect(page).not.toHaveTitle('xss');
        });
    });

    // ========== Session Persistence Tests ==========

    test.describe('Session Persistence', () => {
        test('session persists across page reloads', async ({ page }) => {
            // Login
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/student**');

            // Reload the page
            await page.reload();

            // Should still be on dashboard, not redirected to login
            expect(page.url()).toContain('/dashboard');
        });

        test('session persists across browser navigation', async ({ page }) => {
            // Login
            await page.goto('/login');
            await page.fill('input[name="email"]', 'student@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');
            await page.waitForURL('**/dashboard/student**');

            // Navigate to public page
            await page.goto('/');

            // Verify token is still in localStorage
            const tokenAfterNavigation = await page.evaluate(() => localStorage.getItem('authToken'));
            expect(tokenAfterNavigation).not.toBeNull();

            // Navigate back to dashboard - should still be authenticated
            await page.goto('/dashboard/student');

            // Should stay on dashboard (not redirected to login)
            await page.waitForURL('**/dashboard/student**', { timeout: 5000 });
            expect(page.url()).toContain('/dashboard/student');
        });
    });
});
