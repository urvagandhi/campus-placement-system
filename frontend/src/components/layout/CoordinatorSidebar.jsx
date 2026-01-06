'use client';

import {
    BarChart3,
    Briefcase,
    LayoutDashboard,
    PlusCircle,
    UserPlus,
    Users
} from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function CoordinatorSidebar() {
    const pathname = usePathname();

    const menuItems = [
        { name: 'Dashboard', href: '/dashboard/coordinator', icon: LayoutDashboard },
        { name: 'Onboard Students', href: '/dashboard/coordinator/students', icon: UserPlus },
        { name: 'Manage Drives', href: '/dashboard/coordinator/drives', icon: Briefcase },
        { name: 'View Applicants', href: '/dashboard/coordinator/applicants', icon: Users },
        { name: 'Analytics', href: '/dashboard/coordinator/analytics', icon: BarChart3 },
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

                    {/* Quick Action */}
                    <div className="mt-8 px-4">
                        <Link href="/dashboard/coordinator/drives/create">
                            <div className="bg-gradient-to-br from-indigo-600 to-indigo-700 rounded-2xl p-4 text-white shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all cursor-pointer group relative overflow-hidden">
                                <div className="absolute top-0 right-0 w-16 h-16 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2 blur-2xl"></div>

                                <div className="flex items-center gap-2 mb-2 relative z-10">
                                    <div className="p-1 bg-white/20 rounded-lg">
                                        <PlusCircle className="h-4 w-4 text-white" />
                                    </div>
                                    <span className="font-semibold text-sm">Create New Drive</span>
                                </div>
                                <p className="text-xs text-indigo-100 opacity-90 relative z-10">Schedule a new campus recruitment drive.</p>
                            </div>
                        </Link>
                    </div>
                </nav>
            </div>
        </aside>
    );
}
