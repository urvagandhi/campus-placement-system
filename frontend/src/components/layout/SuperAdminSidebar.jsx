'use client';

import {
    Building2,
    LayoutDashboard,
    Settings,
    Shield
} from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function SuperAdminSidebar() {
    const pathname = usePathname();

    const menuItems = [
        { name: 'Dashboard', href: '/dashboard/superadmin', icon: LayoutDashboard },
        { name: 'Manage Colleges', href: '/dashboard/superadmin/colleges', icon: Building2 },
        { name: 'Security Monitoring', href: '/dashboard/superadmin/security', icon: Shield },
        { name: 'System Settings', href: '/dashboard/superadmin/settings', icon: Settings },
    ];

    const isActive = (path) => pathname === path || pathname.startsWith(`${path}/`);

    return (
        <aside className="w-64 glass-sidebar fixed left-0 top-16 bottom-0 overflow-y-auto hidden md:block z-20">
            <div className="py-6 px-4">
                {/* Navigation */}
                <nav className="space-y-1">
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const active = isActive(item.href);
                        return (
                            <Link
                                key={item.name}
                                href={item.href}
                                className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 ${active
                                    ? 'bg-indigo-50/80 text-indigo-700 shadow-sm ring-1 ring-indigo-100'
                                    : 'text-gray-600 hover:bg-gray-50/80 hover:text-gray-900'
                                    }`}
                            >
                                <Icon className={`h-5 w-5 ${active ? 'text-indigo-600' : 'text-gray-400'}`} />
                                {item.name}
                            </Link>
                        );
                    })}
                </nav>
            </div>
        </aside>
    );
}
