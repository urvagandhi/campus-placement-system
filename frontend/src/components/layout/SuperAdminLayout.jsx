'use client';

import SuperAdminNavbar from './SuperAdminNavbar';
import SuperAdminRouteGuard from './SuperAdminRouteGuard';
import SuperAdminSidebar from './SuperAdminSidebar';

export default function SuperAdminLayout({ children }) {
    return (
        <SuperAdminRouteGuard>
            <div className="min-h-screen bg-gray-50 flex">
                {/* Sidebar */}
                <SuperAdminSidebar />

                {/* Main Content Area */}
                <div className="flex-1 flex flex-col ml-64 transition-all duration-300">
                    <SuperAdminNavbar />
                    <main className="flex-1 p-6 md:p-8 overflow-y-auto">
                        <div className="max-w-7xl mx-auto w-full">
                            {children}
                        </div>
                    </main>
                </div>
            </div>
        </SuperAdminRouteGuard>
    );
}
