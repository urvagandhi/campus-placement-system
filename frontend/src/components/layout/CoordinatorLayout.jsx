'use client';

import CoordinatorNavbar from './CoordinatorNavbar';
import CoordinatorRouteGuard from './CoordinatorRouteGuard';
import CoordinatorSidebar from './CoordinatorSidebar';

export default function CoordinatorLayout({ children }) {
    return (
        <CoordinatorRouteGuard>
            <div className="min-h-screen bg-gray-50 flex">
                {/* Sidebar */}
                <CoordinatorSidebar />

                {/* Main Content Area */}
                <div className="flex-1 flex flex-col ml-64 transition-all duration-300">
                    <CoordinatorNavbar />
                    <main className="flex-1 p-6 md:p-8 overflow-y-auto">
                        <div className="max-w-7xl mx-auto w-full">
                            {children}
                        </div>
                    </main>
                </div>
            </div>
        </CoordinatorRouteGuard>
    );
}
