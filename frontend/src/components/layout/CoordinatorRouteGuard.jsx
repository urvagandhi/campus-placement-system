'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function CoordinatorRouteGuard({ children }) {
    const router = useRouter();
    const [authorized, setAuthorized] = useState(false);

    useEffect(() => {
        // Mock Authentication Check
        const userRole = localStorage.getItem('userRole');

        if (userRole !== 'COORDINATOR') {
            // Redirect to login if not a coordinator
            router.push('/login');
        } else {
            setAuthorized(true);
        }
    }, [router]);

    if (!authorized) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
            </div>
        );
    }

    return <>{children}</>;
}
