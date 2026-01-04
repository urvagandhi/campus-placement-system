'use client';

import { Bell, Search, User } from 'lucide-react';

export default function AdminNavbar() {
    return (
        <header className="bg-white border-b border-gray-200 h-16 flex items-center justify-between px-6 md:px-8 sticky top-0 z-20">
            {/* Search Bar (Mock) */}
            <div className="flex items-center gap-2 text-gray-400 bg-gray-50 px-4 py-2 rounded-lg border border-gray-100 w-96">
                <Search className="h-4 w-4" />
                <span className="text-sm">Search users, departments...</span>
            </div>

            {/* Right Side Actions */}
            <div className="flex items-center gap-4">
                <button className="p-2 text-gray-500 hover:bg-gray-100 rounded-full relative">
                    <Bell className="h-5 w-5" />
                    <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white"></span>
                </button>

                <div className="flex items-center gap-3 pl-4 border-l border-gray-100">
                    <div className="text-right hidden md:block">
                        <p className="text-sm font-semibold text-gray-900">College Admin</p>
                        <p className="text-xs text-gray-500">admin@college.edu</p>
                    </div>
                    <div className="h-10 w-10 bg-emerald-100 rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                        <User className="h-5 w-5 text-emerald-600" />
                    </div>
                </div>
            </div>
        </header>
    );
}
