'use client';

import SuperAdminNavbar from './SuperAdminNavbar';
import SuperAdminRouteGuard from './SuperAdminRouteGuard';
import SuperAdminSidebar from './SuperAdminSidebar';

export default function SuperAdminLayout({ children }) {
    return (
        <SuperAdminRouteGuard>
            <div className="min-h-screen bg-gray-50">
                <SuperAdminNavbar />
                <SuperAdminSidebar />
                <main className="pt-24 md:pl-64 min-h-screen transition-all duration-200">
                    <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                        {children}
                    </div>
                </main>
            </div>
        </SuperAdminRouteGuard>
    );
}
