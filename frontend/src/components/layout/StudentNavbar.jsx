'use client';

import { Bell, LogOut } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

export default function StudentNavbar() {
    const router = useRouter();

    const handleLogout = () => {
        // Mock logout
        localStorage.removeItem('userRole');
        router.push('/login');
    };

    return (
        <nav className="bg-white border-b border-gray-200 h-16 fixed w-full top-0 z-30">
            <div className="px-4 sm:px-6 lg:px-8 h-full">
                <div className="flex justify-between items-center h-full">
                    {/* Logo / Brand */}
                    <div className="flex items-center">
                        <Link href="/dashboard/student" className="flex items-center gap-2">
                            <div className="bg-indigo-600 text-white p-1.5 rounded-lg">
                                <span className="font-bold text-lg">P</span>
                            </div>
                            <span className="font-bold text-xl text-gray-900 hidden sm:block">
                                Placement<span className="text-indigo-600">Pro</span>
                            </span>
                        </Link>
                    </div>

                    {/* Right Side Actions */}
                    <div className="flex items-center gap-4">
                        <button className="relative p-2 text-gray-500 hover:text-indigo-600 transition-colors rounded-full hover:bg-gray-100">
                            <Bell className="h-5 w-5" />
                            <span className="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full border border-white"></span>
                        </button>

                        <div className="h-8 w-px bg-gray-200 mx-1"></div>

                        <div className="flex items-center gap-3">
                            <div className=" hidden md:block text-right">
                                <p className="text-sm font-medium text-gray-900">John Doe</p>
                                <p className="text-xs text-gray-500">Student</p>
                            </div>
                            <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-semibold border border-indigo-200">
                                JD
                            </div>
                            <button
                                onClick={handleLogout}
                                className="p-2 text-gray-500 hover:text-red-600 transition-colors rounded-full hover:bg-gray-100 ml-1"
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
