/**
 * Component tests for LoginPage
 *
 * Tests the login page UI and interactions including:
 * - Form rendering
 * - Loading states
 * - Error messages
 * - Password visibility toggle
 */

import LoginPage from '@/app/login/page';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';

// Mock the useAuth hook
const mockLogin = jest.fn();
jest.mock('@/hooks/useAuth', () => ({
    useAuth: () => ({
        login: mockLogin,
        isAuthenticated: false,
        isLoading: false,
    }),
}));

// Mock Next.js components
jest.mock('next/link', () => {
    const MockLink = ({ children, href }) => {
        return <a href={href}>{children}</a>;
    };
    MockLink.displayName = 'MockLink';
    return MockLink;
});

// Mock lucide-react icons
jest.mock('lucide-react', () => ({
    Lock: () => <span data-testid="lock-icon">LockIcon</span>,
    Mail: () => <span data-testid="mail-icon">MailIcon</span>,
    Eye: () => <span data-testid="eye-icon">EyeIcon</span>,
    EyeOff: () => <span data-testid="eyeoff-icon">EyeOffIcon</span>,
    GraduationCap: () => <span data-testid="grad-icon">GradIcon</span>,
}));

describe('LoginPage', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    // ========== Rendering Tests ==========

    describe('Rendering', () => {
        it('renders email input', () => {
            render(<LoginPage />);
            expect(screen.getByPlaceholderText(/university\.edu/i)).toBeInTheDocument();
        });

        it('renders password input', () => {
            render(<LoginPage />);
            expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
        });

        it('renders submit button', () => {
            render(<LoginPage />);
            expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
        });

        it('renders welcome message', () => {
            render(<LoginPage />);
            expect(screen.getByText('Welcome Back')).toBeInTheDocument();
        });

        it('displays role assignment notice', () => {
            render(<LoginPage />);
            expect(screen.getByText(/role is determined by the system/i)).toBeInTheDocument();
        });

        it('does not render role selection dropdown', () => {
            render(<LoginPage />);
            // There should be no role selector - role is backend-controlled
            expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
        });
    });

    // ========== Form Submission Tests ==========

    describe('Form Submission', () => {
        it('calls login with email and password when form is valid', async () => {
            mockLogin.mockResolvedValueOnce({});

            render(<LoginPage />);

            const emailInput = screen.getByPlaceholderText(/university\.edu/i);
            const passwordInput = screen.getByPlaceholderText('••••••••');

            fireEvent.change(emailInput, { target: { value: 'user@test.edu' } });
            fireEvent.change(passwordInput, { target: { value: 'password123' } });

            const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
            fireEvent.submit(form);

            await waitFor(() => {
                expect(mockLogin).toHaveBeenCalledWith('user@test.edu', 'password123');
            });
        });
    });

    // ========== Error Message Tests ==========

    describe('Error Messages', () => {
        it('shows invalid credentials error', async () => {
            mockLogin.mockRejectedValueOnce(new Error('Invalid credentials'));

            render(<LoginPage />);

            const emailInput = screen.getByPlaceholderText(/university\.edu/i);
            const passwordInput = screen.getByPlaceholderText('••••••••');

            fireEvent.change(emailInput, { target: { value: 'user@test.edu' } });
            fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });

            const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
            fireEvent.submit(form);

            await waitFor(() => {
                expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument();
            });
        });

        it('shows deactivated account error', async () => {
            mockLogin.mockRejectedValueOnce(new Error('Account deactivated'));

            render(<LoginPage />);

            const emailInput = screen.getByPlaceholderText(/university\.edu/i);
            const passwordInput = screen.getByPlaceholderText('••••••••');

            fireEvent.change(emailInput, { target: { value: 'user@test.edu' } });
            fireEvent.change(passwordInput, { target: { value: 'password123' } });

            const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
            fireEvent.submit(form);

            await waitFor(() => {
                expect(screen.getByText(/account has been deactivated/i)).toBeInTheDocument();
            });
        });

        it('shows inactive college error', async () => {
            mockLogin.mockRejectedValueOnce(new Error('College is not active'));

            render(<LoginPage />);

            const emailInput = screen.getByPlaceholderText(/university\.edu/i);
            const passwordInput = screen.getByPlaceholderText('••••••••');

            fireEvent.change(emailInput, { target: { value: 'user@test.edu' } });
            fireEvent.change(passwordInput, { target: { value: 'password123' } });

            const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
            fireEvent.submit(form);

            await waitFor(() => {
                expect(screen.getByText(/college is not active/i)).toBeInTheDocument();
            });
        });

        it('clears error when input changes', async () => {
            mockLogin.mockRejectedValueOnce(new Error('Invalid credentials'));

            render(<LoginPage />);

            const emailInput = screen.getByPlaceholderText(/university\.edu/i);
            const passwordInput = screen.getByPlaceholderText('••••••••');

            fireEvent.change(emailInput, { target: { value: 'user@test.edu' } });
            fireEvent.change(passwordInput, { target: { value: 'wrong' } });

            const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
            fireEvent.submit(form);

            await waitFor(() => {
                expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument();
            });

            // Change input to clear error
            fireEvent.change(emailInput, { target: { value: 'newuser@test.edu' } });

            await waitFor(() => {
                expect(screen.queryByText(/invalid email or password/i)).not.toBeInTheDocument();
            });
        });
    });

    // ========== Password Toggle Tests ==========

    describe('Password Toggle', () => {
        it('renders password toggle button', () => {
            render(<LoginPage />);
            const toggleButton = screen.getByRole('button', { name: /show password|hide password/i });
            expect(toggleButton).toBeInTheDocument();
        });

        it('toggles password visibility when clicked', () => {
            render(<LoginPage />);

            // Get password input by placeholder - more specific
            const passwordInput = screen.getByPlaceholderText('••••••••');
            const toggleButton = screen.getByRole('button', { name: /show password/i });

            // Initially password type
            expect(passwordInput).toHaveAttribute('type', 'password');

            // Click to show password
            fireEvent.click(toggleButton);
            expect(passwordInput).toHaveAttribute('type', 'text');

            // Click again to hide password
            const hideButton = screen.getByRole('button', { name: /hide password/i });
            fireEvent.click(hideButton);
            expect(passwordInput).toHaveAttribute('type', 'password');
        });
    });

    // ========== Accessibility Tests ==========

    describe('Accessibility', () => {
        it('has proper form structure', () => {
            render(<LoginPage />);
            expect(screen.getByRole('button', { name: /sign in/i })).toHaveAttribute('type', 'submit');
        });

        it('has password toggle with aria-label', () => {
            render(<LoginPage />);
            const toggleButton = screen.getByRole('button', { name: /show password/i });
            expect(toggleButton).toHaveAttribute('aria-label');
        });
    });
});
