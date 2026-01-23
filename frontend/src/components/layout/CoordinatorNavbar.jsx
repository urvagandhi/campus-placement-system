'use client';

import Logo from '@/components/ui/Logo';
import UserProfileDropdown from '@/components/ui/UserProfileDropdown';
import { useAuth } from '@/hooks/useAuth';
import { Bell, LogOut, Search } from 'lucide-react';

export default function CoordinatorNavbar() {
    const { user, logout } = useAuth();

    const handleLogout = async () => {
        await logout();
    };

    const formatRole = (role) => {
        if (!role) return 'TPO Role';
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

                    {/* Search Bar - Center */}
                    <div className="hidden md:flex flex-1 max-w-lg mx-8">
                        <div className="relative w-full">
                            <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                            <input
                                type="text"
                                placeholder="Search..."
                                className="w-full pl-10 pr-4 py-2 bg-gray-50/50 border border-gray-200/50 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-100 text-sm text-gray-700 transition-all backdrop-blur-sm"
                            />
                        </div>
                    </div>

                    {/* Right Side Actions */}
                    <div className="flex items-center gap-2 sm:gap-4">
                        <button className="relative p-2.5 text-gray-500 hover:text-indigo-600 transition-colors rounded-xl hover:bg-gray-100/50 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                            <Bell className="h-5 w-5" />
                            <span className="absolute top-2.5 right-2.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white shadow-sm"></span>
                        </button>

                        <div className="h-8 w-px bg-gray-200/50 mx-1 hidden sm:block"></div>

                        <div className="flex items-center gap-3 pl-2 sm:pl-0">
                            <div className="hidden sm:block text-right">
                                <p className="text-sm font-semibold text-gray-900 leading-none">{user?.name || 'Coordinator'}</p>
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
