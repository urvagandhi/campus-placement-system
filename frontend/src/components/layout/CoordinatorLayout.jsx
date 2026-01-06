'use client';

import CoordinatorNavbar from './CoordinatorNavbar';
import CoordinatorRouteGuard from './CoordinatorRouteGuard';
import CoordinatorSidebar from './CoordinatorSidebar';

export default function CoordinatorLayout({ children }) {
    return (
        <CoordinatorRouteGuard>
            <div className="min-h-screen bg-gray-50">
                <CoordinatorNavbar />
                <CoordinatorSidebar />
                <main className="pt-24 md:pl-64 min-h-screen transition-all duration-200">
                    <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                        {children}
                    </div>
                </main>
            </div>
        </CoordinatorRouteGuard>
    );
}
