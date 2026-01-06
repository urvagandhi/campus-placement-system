'use client';

import Logo from '@/components/ui/Logo';
import { Bell, ChevronDown, LogOut } from 'lucide-react';
import { useRouter } from 'next/navigation';

export default function StudentNavbar() {
    const router = useRouter();

    const handleLogout = () => {
        // Mock logout
        localStorage.removeItem('userRole');
        router.push('/login');
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
                        <button className="relative p-2.5 text-gray-500 hover:text-indigo-600 transition-colors rounded-xl hover:bg-gray-100/50 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                            <Bell className="h-5 w-5" />
                            <span className="absolute top-2.5 right-2.5 h-2 w-2 bg-red-500 rounded-full border-2 border-white shadow-sm"></span>
                        </button>

                        <div className="h-8 w-px bg-gray-200/50 mx-1 hidden sm:block"></div>

                        <div className="flex items-center gap-3 pl-2 sm:pl-0">
                            <div className="hidden sm:block text-right">
                                <p className="text-sm font-semibold text-gray-900 leading-none">John Doe</p>
                                <p className="text-xs text-gray-500 mt-1">Student</p>
                            </div>

                            <div className="relative group">
                                <button className="flex items-center gap-2 focus:outline-none rounded-full p-0.5 ring-offset-2 focus:ring-2 focus:ring-indigo-500/20">
                                    <div className="h-9 w-9 rounded-full bg-gradient-to-br from-indigo-100 to-white flex items-center justify-center text-indigo-700 font-bold border border-indigo-100 shadow-sm text-sm">
                                        JD
                                    </div>
                                    <ChevronDown className="h-4 w-4 text-gray-400 group-hover:text-gray-600 transition-colors hidden sm:block" />
                                </button>

                                {/* Dropdown would go here in full implementation */}
                            </div>

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
