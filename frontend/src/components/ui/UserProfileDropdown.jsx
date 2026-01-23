'use client';

import { useAuth } from '@/hooks/useAuth';
import { ChevronDown, LogOut, Settings, User } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useState, useRef, useEffect } from 'react';

export default function UserProfileDropdown() {
    const { user } = useAuth();
    const router = useRouter();
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    const getInitials = (name) => {
        if (!name) return 'U';
        return name
            .split(' ')
            .map((n) => n[0])
            .join('')
            .toUpperCase()
            .substring(0, 2);
    };

    const formatRole = (role) => {
        if (!role) return 'User';
        return role
            .replace(/_/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, (c) => c.toUpperCase());
    };

    const handleProfileClick = () => {
        setIsOpen(false);
        const roleBasePath = user?.role?.toLowerCase().replace('_', '');
        router.push(`/dashboard/${roleBasePath}/profile`);
    };

    const handleSettingsClick = () => {
        setIsOpen(false);
        const roleBasePath = user?.role?.toLowerCase().replace('_', '');
        router.push(`/dashboard/${roleBasePath}/settings`);
    };

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    return (
        <div className="relative" ref={dropdownRef}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center gap-2 focus:outline-none rounded-full p-0.5 ring-offset-2 focus:ring-2 focus:ring-indigo-500/20 transition-all hover:ring-2 hover:ring-indigo-500/10"
            >
                <div className="h-9 w-9 rounded-full bg-gradient-to-br from-indigo-100 to-white flex items-center justify-center text-indigo-700 font-bold border border-indigo-100 shadow-sm text-sm">
                    {getInitials(user?.name)}
                </div>
                <ChevronDown
                    className={`h-4 w-4 text-gray-400 transition-transform duration-200 hidden sm:block ${
                        isOpen ? 'rotate-180' : ''
                    }`}
                />
            </button>

            {/* Dropdown Menu */}
            {isOpen && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 z-50 animate-fade-in">
                    {/* User Info */}
                    <div className="px-4 py-3 border-b border-gray-100">
                        <p className="text-sm font-semibold text-gray-900 truncate">{user?.name || 'User'}</p>
                        <p className="text-xs text-gray-500 mt-0.5">{user?.email || ''}</p>
                        <p className="text-xs text-indigo-600 font-medium mt-1">{formatRole(user?.role)}</p>
                    </div>

                    {/* Menu Items */}
                    <div className="py-1">
                        <button
                            onClick={handleProfileClick}
                            className="w-full px-4 py-2.5 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3 transition-colors"
                        >
                            <User className="h-4 w-4 text-gray-400" />
                            <span>My Profile</span>
                        </button>
                        
                        {user?.role === 'SUPER_ADMIN' && (
                            <button
                                onClick={handleSettingsClick}
                                className="w-full px-4 py-2.5 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-3 transition-colors"
                            >
                                <Settings className="h-4 w-4 text-gray-400" />
                                <span>System Settings</span>
                            </button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
