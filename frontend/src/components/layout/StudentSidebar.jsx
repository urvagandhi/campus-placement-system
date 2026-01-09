'use client';

import { Briefcase, FileText, LayoutDashboard, Shield, TrendingUp, User } from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function StudentSidebar() {
    const pathname = usePathname();

    const navigation = [
        { name: 'Dashboard', href: '/dashboard/student', icon: LayoutDashboard },
        { name: 'Placement Drives', href: '/dashboard/student/drives', icon: Briefcase },
        { name: 'My Applications', href: '/dashboard/student/applications', icon: FileText },
        { name: 'Career Insights', href: '/dashboard/student/insights', icon: TrendingUp },
        { name: 'My Profile', href: '/dashboard/student/profile', icon: User },
        { name: 'Security & Sessions', href: '/dashboard/student/sessions', icon: Shield },
    ];

    const isActive = (path) => {
        if (path === '/dashboard/student' && pathname === '/dashboard/student') {
            return true;
        }
        if (path !== '/dashboard/student' && pathname.startsWith(path)) {
            return true;
        }
        return false;
    };

    return (
        <aside className="w-64 glass-sidebar fixed left-0 top-16 bottom-0 overflow-y-auto hidden md:block z-20">
            <div className="py-6 px-4">
                <nav className="space-y-1">
                    {navigation.map((item) => {
                        const active = isActive(item.href);
                        const Icon = item.icon;

                        return (
                            <Link
                                key={item.name}
                                href={item.href}
                                className={`
                                    flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200
                                    ${active
                                        ? 'bg-indigo-50/80 text-indigo-700 shadow-sm ring-1 ring-indigo-100'
                                        : 'text-gray-600 hover:bg-gray-50/80 hover:text-gray-900'
                                    }
                                `}
                            >
                                <Icon
                                    className={`h-5 w-5 ${active ? 'text-indigo-600' : 'text-gray-400 group-hover:text-gray-600 transition-colors'}`}
                                />
                                {item.name}
                            </Link>
                        );
                    })}
                </nav>

                <div className="mt-8 px-4">
                    <div className="bg-gradient-to-br from-indigo-50 to-white rounded-2xl p-4 border border-indigo-100/50 shadow-sm">
                        <h4 className="text-sm font-semibold text-indigo-900 mb-1">Upcoming Drive</h4>
                        <p className="text-xs text-indigo-700 mb-3 opacity-80">Google India Recruitment Drive starts tomorrow.</p>
                        <Link href="/dashboard/student/drives" className="text-xs font-medium text-indigo-600 hover:text-indigo-800 hover:underline">
                            View details &rarr;
                        </Link>
                    </div>
                </div>
            </div>
        </aside>
    );
}
