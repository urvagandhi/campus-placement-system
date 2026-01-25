'use client';

import Logo from '@/components/ui/Logo';
import UserProfileDropdown from '@/components/ui/UserProfileDropdown';
import { useAuth } from '@/hooks/useAuth';
import { LogOut } from 'lucide-react';

export default function AdminNavbar() {
    const { user, logout } = useAuth();

    const handleLogout = async () => {
        await logout();
    };

    const formatRole = (role) => {
        if (!role) return 'Administrator';
        return role
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, (c) => c.toUpperCase());
    };

    return (
        <nav className="glass-navbar h-16 fixed w-full top-0 z-30 transition-all duration-300">
            <div className="px-4 sm:px-6 lg:px-8 h-full">
                <div className="flex justify-between items-center h-full">
                    {/* Brand */}
                    <div className="flex items-center">
                        <div className="md:hidden">
                            <Logo size="sm" showText={false} />
                        </div>
                        <div className="hidden md:block">
                            <Logo size="md" showText={true} />
                        </div>
                    </div>

                    {/* Right Side Actions */}
                    <div className="flex items-center gap-2 sm:gap-4">
                        <div className="flex items-center gap-3 pl-2 sm:pl-0">
                            <div className="hidden sm:block text-right">
                                <p className="text-sm font-semibold text-gray-900 leading-none">{user?.name || 'College Admin'}</p>
                                <p className="text-xs text-gray-500 mt-1">{formatRole(user?.role)}</p>
                            </div>

                            <UserProfileDropdown />

                            <button
                                onClick={handleLogout}
                                className="p-2.5 text-gray-500 hover:text-red-600 transition-colors rounded-xl hover:bg-red-50/50 ml-1"
                                title="Logout"
                            >
                                <LogOut className="h-5 w-5" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
}
