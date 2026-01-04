'use client';

import {
    BarChart3,
    Briefcase,
    LayoutDashboard,
    LogOut,
    PlusCircle,
    Users
} from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function CoordinatorSidebar() {
    const pathname = usePathname();

    const menuItems = [
        { name: 'Dashboard', href: '/dashboard/coordinator', icon: LayoutDashboard },
        { name: 'Manage Drives', href: '/dashboard/coordinator/drives', icon: Briefcase },
        { name: 'View Applicants', href: '/dashboard/coordinator/applicants', icon: Users },
        { name: 'Analytics', href: '/dashboard/coordinator/analytics', icon: BarChart3 },
    ];

    const isActive = (path) => pathname === path || pathname.startsWith(`${path}/`);

    return (
        <aside className="w-64 bg-white border-r border-gray-200 min-h-screen flex flex-col fixed left-0 top-0 h-full z-10">
            {/* Logo Area */}
            <div className="p-6 border-b border-gray-100 flex items-center gap-3">
                <div className="h-8 w-8 bg-indigo-600 rounded-lg flex items-center justify-center">
                    <span className="text-white font-bold text-lg">P</span>
                </div>
                <h1 className="text-xl font-bold text-gray-800 tracking-tight">Placement<span className="text-indigo-600">Pro</span></h1>
            </div>

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
                <p className="px-4 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 mt-2">
                    Coordinator Menu
                </p>
                {menuItems.map((item) => {
                    const Icon = item.icon;
                    const active = isActive(item.href);
                    return (
                        <Link
                            key={item.name}
                            href={item.href}
                            className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 ${active
                                ? 'bg-indigo-50 text-indigo-700'
                                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                }`}
                        >
                            <Icon className={`h-5 w-5 ${active ? 'text-indigo-600' : 'text-gray-400'}`} />
                            {item.name}
                        </Link>
                    );
                })}

                {/* Quick Action */}
                <div className="mt-8 px-4">
                    <Link href="/dashboard/coordinator/drives/create">
                        <div className="bg-gradient-to-r from-indigo-600 to-indigo-700 rounded-xl p-4 text-white shadow-lg hover:shadow-xl transition-all cursor-pointer group">
                            <div className="flex items-center gap-2 mb-2">
                                <PlusCircle className="h-5 w-5 text-indigo-200" />
                                <span className="font-semibold text-sm">Create New Drive</span>
                            </div>
                            <p className="text-xs text-indigo-100 opacity-90">Schedule a new campus recruitment drive.</p>
                        </div>
                    </Link>
                </div>
            </nav>

            {/* Footer */}
            <div className="p-4 border-t border-gray-100">
                <Link
                    href="/login" // In real app, this would trigger logout logic
                    onClick={() => localStorage.removeItem('userRole')}
                    className="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition-all duration-200"
                >
                    <LogOut className="h-5 w-5" />
                    Sign Out
                </Link>
            </div>
        </aside>
    );
}
