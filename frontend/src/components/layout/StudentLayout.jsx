'use client';

import StudentNavbar from './StudentNavbar';
import StudentRouteGuard from './StudentRouteGuard';
import StudentSidebar from './StudentSidebar';

export default function StudentLayout({ children }) {
    return (
        <StudentRouteGuard>
            <div className="min-h-screen bg-gray-50">
                <StudentNavbar />
                <StudentSidebar />
                <main className="pt-24 md:pl-64 min-h-screen transition-all duration-200">
                    <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
                        {children}
                    </div>
                </main>
            </div>
        </StudentRouteGuard>
    );
}
