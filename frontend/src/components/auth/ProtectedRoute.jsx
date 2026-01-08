'use client';

import { useAuth } from '@/hooks/useAuth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

/**
 * ProtectedRoute Component
 *
 * Applied ONLY at /dashboard boundary, not individual pages.
 * - Redirects to /login if not authenticated
 * - Redirects to /forbidden if role not allowed
 *
 * @param {string[]} allowedRoles - Roles that can access this route
 * @param {React.ReactNode} children - Child components to render
 */
export default function ProtectedRoute({ allowedRoles = [], children }) {
    const router = useRouter();
    const { user, isAuthenticated, isLoading } = useAuth();

    useEffect(() => {
        if (isLoading) {
            return;
        }

        if (!isAuthenticated) {
            router.replace('/login');
            return;
        }

        // Check role if allowedRoles specified
        if (allowedRoles.length > 0 && user?.role) {
            if (!allowedRoles.includes(user.role)) {
                router.replace('/forbidden');
            }
        }
    }, [isAuthenticated, isLoading, user, allowedRoles, router]);

    // Show loading state while checking auth
    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-indigo-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600 font-medium">Checking authentication...</p>
                </div>
            </div>
        );
    }

    // Not authenticated - will redirect
    if (!isAuthenticated) {
        return null;
    }

    // Role not allowed - will redirect
    if (allowedRoles.length > 0 && user?.role && !allowedRoles.includes(user.role)) {
        return null;
    }

    return <>{children}</>;
}
