'use client';

import AdminNavbar from './AdminNavbar';
import AdminRouteGuard from './AdminRouteGuard';
import AdminSidebar from './AdminSidebar';

export default function AdminLayout({ children }) {
    return (
        <AdminRouteGuard>
            <div className="min-h-screen bg-gray-50">
                <AdminNavbar />
                <AdminSidebar />
                <main className="pt-24 md:pl-64 min-h-screen transition-all duration-200">
                    <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                        {children}
                    </div>
                </main>
            </div>
        </AdminRouteGuard>
    );
}
