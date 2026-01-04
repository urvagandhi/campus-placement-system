'use client';

import {
    LayoutDashboard,
    LogOut,
    Settings
} from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function SuperAdminSidebar() {
    const pathname = usePathname();

    const menuItems = [
        { name: 'Dashboard', href: '/dashboard/super-admin', icon: LayoutDashboard },
        { name: 'System Settings', href: '/dashboard/super-admin/settings', icon: Settings },
    ];

    const isActive = (path) => pathname === path || pathname.startsWith(`${path}/`);

    return (
        <aside className="w-64 bg-white border-r border-gray-200 min-h-screen flex flex-col fixed left-0 top-0 h-full z-10">
            {/* Logo Area */}
            <div className="p-6 border-b border-gray-100 flex items-center gap-3">
                <div className="h-8 w-8 bg-red-600 rounded-lg flex items-center justify-center">
                    <span className="text-white font-bold text-lg">P</span>
                </div>
                <h1 className="text-xl font-bold text-gray-800 tracking-tight">Placement<span className="text-red-600">Pro</span></h1>
            </div>

            {/* Navigation */}
            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
                <p className="px-4 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 mt-2">
                    System Menu
                </p>
                {menuItems.map((item) => {
                    const Icon = item.icon;
                    const active = isActive(item.href);
                    return (
                        <Link
                            key={item.name}
                            href={item.href}
                            className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 ${active
                                ? 'bg-red-50 text-red-700'
                                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                }`}
                        >
                            <Icon className={`h-5 w-5 ${active ? 'text-red-600' : 'text-gray-400'}`} />
                            {item.name}
                        </Link>
                    );
                })}
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
