'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function SuperAdminRouteGuard({ children }) {
    const router = useRouter();
    const [authorized, setAuthorized] = useState(false);

    useEffect(() => {
        // Mock Authentication Check
        // Role guards use localStorage only for mock authentication in UI-UX phase; backend-based authorization will replace this later.
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
