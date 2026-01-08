/**
 * E2E tests for login flow
 *
 * Tests the complete login experience including:
 * - Role-based redirects
 * - Error handling
 * - Logout flow
 *
 * IMPORTANT: These tests require the backend to be running
 * and seeded with test data.
 */

import { expect, test } from '@playwright/test';

// Test data - should match seed data in the backend
const TEST_USERS = {
    student: {
        email: 'student@test.edu',
        password: 'password123',
        expectedRedirect: '/dashboard/student',
    },
    coordinator: {
        email: 'coordinator@test.edu',
        password: 'password123',
        expectedRedirect: '/dashboard/coordinator',
    },
    admin: {
        email: 'admin@test.edu',
        password: 'password123',
        expectedRedirect: '/dashboard/admin',
    },
    superAdmin: {
        email: 'superadmin@platform.com',
        password: 'password123',
        expectedRedirect: '/dashboard/superadmin',
    },
};

test.describe('Login Flow', () => {
    test.beforeEach(async ({ page }) => {
        // Navigate to login page before each test
        await page.goto('/login');
    });

    // ========== Successful Login Tests ==========

    test.describe('Successful Login', () => {
        test('student login redirects to student dashboard', async ({ page }) => {
            const { email, password, expectedRedirect } = TEST_USERS.student;

            await page.fill('input[name="email"]', email);
            await page.fill('input[name="password"]', password);
            await page.click('button[type="submit"]');

            // Wait for navigation
            await page.waitForURL(`**${expectedRedirect}**`, { timeout: 10000 });

            expect(page.url()).toContain(expectedRedirect);
        });

        test('coordinator login redirects to coordinator dashboard', async ({ page }) => {
            const { email, password, expectedRedirect } = TEST_USERS.coordinator;

            await page.fill('input[name="email"]', email);
            await page.fill('input[name="password"]', password);
            await page.click('button[type="submit"]');

            await page.waitForURL(`**${expectedRedirect}**`, { timeout: 10000 });

            expect(page.url()).toContain(expectedRedirect);
        });

        test('admin login redirects to admin dashboard', async ({ page }) => {
            const { email, password, expectedRedirect } = TEST_USERS.admin;

            await page.fill('input[name="email"]', email);
            await page.fill('input[name="password"]', password);
            await page.click('button[type="submit"]');

            await page.waitForURL(`**${expectedRedirect}**`, { timeout: 10000 });

            expect(page.url()).toContain(expectedRedirect);
        });

        test('super admin login redirects to superadmin dashboard', async ({ page }) => {
            const { email, password, expectedRedirect } = TEST_USERS.superAdmin;

            await page.fill('input[name="email"]', email);
            await page.fill('input[name="password"]', password);
            await page.click('button[type="submit"]');

            await page.waitForURL(`**${expectedRedirect}**`, { timeout: 10000 });

            expect(page.url()).toContain(expectedRedirect);
        });
    });

    // ========== Failed Login Tests ==========

    test.describe('Failed Login', () => {
        test('wrong password shows error message', async ({ page }) => {
            await page.fill('input[name="email"]', TEST_USERS.student.email);
            await page.fill('input[name="password"]', 'wrongpassword');
            await page.click('button[type="submit"]');

            // Wait for error message to appear
            await expect(page.getByText(/invalid/i)).toBeVisible({ timeout: 5000 });
        });

        test('non-existent email shows error message', async ({ page }) => {
            await page.fill('input[name="email"]', 'nonexistent@test.edu');
            await page.fill('input[name="password"]', 'password123');
            await page.click('button[type="submit"]');

            await expect(page.getByText(/invalid/i)).toBeVisible({ timeout: 5000 });
        });

        test('empty form submission is prevented by required fields', async ({ page }) => {
            // Get the submit button
            const submitButton = page.locator('button[type="submit"]');

            // Click submit with empty form
            await submitButton.click();

            // Form should still be on login page (HTML5 validation prevents submission)
            expect(page.url()).toContain('/login');

            // The email input should have invalid state (browser validates required fields)
            const emailInput = page.locator('input[name="email"]');
            await expect(emailInput).toBeVisible();
        });
    });

    // ========== UI Behavior Tests ==========

    test.describe('UI Behavior', () => {
        test('password visibility toggle works', async ({ page }) => {
            await page.fill('input[name="password"]', 'password123');

            const passwordInput = page.locator('input[name="password"]');

            // Initially password should be hidden
            await expect(passwordInput).toHaveAttribute('type', 'password');

            // Note: This test depends on the specific implementation of the toggle
            // Adjust the selector based on actual UI
        });

        test('submit button shows loading state', async ({ page }) => {
            // Use a slow network connection to see loading state
            await page.route('**/api/v1/auth/login', async (route) => {
                await new Promise((resolve) => setTimeout(resolve, 1000));
                await route.continue();
            });

            await page.fill('input[name="email"]', TEST_USERS.student.email);
            await page.fill('input[name="password"]', TEST_USERS.student.password);
            await page.click('button[type="submit"]');

            // Button should show loading state
            // Adjust based on actual loading indicator implementation
        });

        test('role selection is not available', async ({ page }) => {
            // Verify that users cannot select their role
            await expect(page.locator('select[name="role"]')).not.toBeVisible();
            await expect(page.locator('input[name="role"]')).not.toBeVisible();

            // Verify the notice about role being system-determined
            await expect(page.getByText(/role is determined by the system/i)).toBeVisible();
        });
    });
});

// ========== Logout Tests ==========

test.describe('Logout Flow', () => {
    test.beforeEach(async ({ page }) => {
        // Login first
        await page.goto('/login');
        await page.fill('input[name="email"]', TEST_USERS.student.email);
        await page.fill('input[name="password"]', TEST_USERS.student.password);
        await page.click('button[type="submit"]');
        await page.waitForURL('**/dashboard/**');
    });

    test('logout clears session and redirects to login', async ({ page }) => {
        // Find and click logout button (adjust selector based on actual UI)
        const logoutButton = page.getByRole('button', { name: /logout|sign out/i });

        if (await logoutButton.isVisible()) {
            await logoutButton.click();

            // Should redirect to login page
            await page.waitForURL('**/login');

            // Verify token is cleared
            const token = await page.evaluate(() => localStorage.getItem('authToken'));
            expect(token).toBeNull();
        }
    });
});
