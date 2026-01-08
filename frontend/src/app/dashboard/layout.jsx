'use client';

import ProtectedRoute from '@/components/auth/ProtectedRoute';

/**
 * Dashboard layout shell
 *
 * ProtectedRoute is applied ONLY at this /dashboard boundary.
 * Individual dashboard pages do NOT need additional route guards.
 *
 * Role-specific layouts (AdminLayout, StudentLayout, etc.) handle the actual navigation.
 */
export default function DashboardLayout({ children }) {
    return (
        <ProtectedRoute
            allowedRoles={['STUDENT', 'COORDINATOR', 'ADMIN', 'SUPER_ADMIN']}
        >
            <div className="min-h-screen bg-gray-50">
                {children}
            </div>
        </ProtectedRoute>
    );
}
