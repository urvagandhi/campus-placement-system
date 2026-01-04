'use client';

import { Bell, User } from 'lucide-react';

export default function SuperAdminNavbar() {
    return (
        <header className="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 md:px-8 sticky top-0 z-20">
            {/* Title / Breadcrumb (Mock) */}
            <div>
                <h2 className="text-lg font-semibold text-gray-800">System Administration</h2>
            </div>

            {/* Right Side Actions */}
            <div className="flex items-center gap-4">
                <button className="p-2 text-gray-500 hover:bg-gray-100 rounded-full relative">
                    <Bell className="h-5 w-5" />
                    <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white"></span>
                </button>

                <div className="flex items-center gap-3 pl-4 border-l border-gray-100">
                    <div className="text-right hidden md:block">
                        <p className="text-sm font-semibold text-gray-900">System Admin</p>
                        <p className="text-xs text-gray-500">root@system.local</p>
                    </div>
                    <div className="h-10 w-10 bg-red-100 rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                        <User className="h-5 w-5 text-red-600" />
                    </div>
                </div>
            </div>
        </header>
    );
}
