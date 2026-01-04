'use client';

import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function StudentRouteGuard({ children }) {
    const router = useRouter();
    const pathname = usePathname();
    const [authorized, setAuthorized] = useState(false);

    useEffect(() => {
        // Mock authentication check
        // In a real app, check token/session here
        const checkAuth = () => {
            // For now, assume if we are on login page, we are not authenticated
            // If we are on any other page, we must be logged in as STUDENT

            // Allow access to login page
            if (pathname === '/login') {
                setAuthorized(true);
                return;
            }

            // Mock user role from local storage or session
            // For this phase, we'll simulate a logged-in student state
            // You can store a mock token in localStorage on login
            const userRole = localStorage.getItem('userRole');

            if (userRole === 'STUDENT') {
                setAuthorized(true);
            } else {
                // Redirect to login if not authenticated or not authorized
                router.push('/login');
            }
        };

        checkAuth();
    }, [router, pathname]);

    // Prevent flashing content before redirect
    if (!authorized && pathname !== '/login') {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
            </div>
        );
    }

    return <>{children}</>;
}
