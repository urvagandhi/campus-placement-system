'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

/**
 * Route guard for Super Admin pages.
 *
 * Uses direct localStorage check for consistency with Admin and Coordinator route guards.
 * This avoids potential race conditions with complex auth hooks during initial load.
 */
export default function SuperAdminRouteGuard({ children }) {
    const router = useRouter();
    const [authorized, setAuthorized] = useState(false);

    useEffect(() => {
        // Direct Synchronous Check (mimicking AdminRouteGuard)
        const userRole = localStorage.getItem('userRole');

        if (userRole !== 'SUPER_ADMIN') {
            // Redirect to login if not a super admin
            router.push('/login');
        } else {
            setAuthorized(true);
        }
    }, [router]);

    if (!authorized) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
            </div>
        );
    }

    return <>{children}</>;
}
