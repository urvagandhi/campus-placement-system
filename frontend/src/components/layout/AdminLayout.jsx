'use client';

import AdminNavbar from './AdminNavbar';
import AdminRouteGuard from './AdminRouteGuard';
import AdminSidebar from './AdminSidebar';

export default function AdminLayout({ children }) {
    return (
        <AdminRouteGuard>
            <div className="min-h-screen bg-gray-50 flex">
                {/* Sidebar */}
                <AdminSidebar />

                {/* Main Content Area */}
                <div className="flex-1 flex flex-col ml-64 transition-all duration-300">
                    <AdminNavbar />
                    <main className="flex-1 p-6 md:p-8 overflow-y-auto">
                        <div className="max-w-7xl mx-auto w-full">
                            {children}
                        </div>
                    </main>
                </div>
            </div>
        </AdminRouteGuard>
    );
}
