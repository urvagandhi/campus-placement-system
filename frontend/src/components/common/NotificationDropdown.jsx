'use client';

import { useState, useRef, useEffect } from 'react';
import { Bell, Check, Info, AlertTriangle, AlertCircle, CheckCircle } from 'lucide-react';
import api from '@/services/api';
import { useRouter } from 'next/navigation';

export default function NotificationDropdown() {
    const [isOpen, setIsOpen] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isLoading, setIsLoading] = useState(false);
    const dropdownRef = useRef(null);
    const router = useRouter();
    const PAGE_SIZE = 5;

    const fetchNotifications = async (isLoadMore = false) => {
        if (isLoading) return;
        
        try {
            setIsLoading(true);
            
            // If not loading more (initial load or refresh), also get unread count
            if (!isLoadMore) {
                const countRes = await api.notifications.getUnreadCount();
                if (countRes.success) {
                    setUnreadCount(countRes.data);
                }
            }

            const currentPage = isLoadMore ? page + 1 : 0;
            const notifRes = await api.notifications.getAll(currentPage, PAGE_SIZE);
            
            if (notifRes.success && notifRes.data) {
                const newNotifications = notifRes.data.content || [];
                
                if (isLoadMore) {
                    setNotifications(prev => [...(prev || []), ...newNotifications]);
                    setPage(currentPage);
                } else {
                    setNotifications(newNotifications);
                    setPage(0);
                }
                
                setHasMore(!notifRes.data.last);
            }
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        } finally {
            setIsLoading(false);
        }
    };

    // Initial fetch and polling
    useEffect(() => {
        fetchNotifications();
        const interval = setInterval(() => fetchNotifications(false), 60000);
        return () => clearInterval(interval);
    }, []);

    // Fetch on open if empty
    useEffect(() => {
        if (isOpen && notifications.length === 0) {
            fetchNotifications(false);
        }
    }, [isOpen]);

    // Close on outside click
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

    const handleMarkAllRead = async () => {
        try {
            await api.notifications.markAllAsRead();
            setUnreadCount(0);
            // Update local state to show all as read
            setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
        } catch (error) {
            console.error('Failed to mark all read:', error);
        }
    };

    const handleNotificationClick = async (notification) => {
        if (!notification.isRead) {
            try {
                await api.notifications.markAsRead(notification.id);
                setUnreadCount((prev) => Math.max(0, prev - 1));
                setNotifications((prev) =>
                    prev.map((n) => (n.id === notification.id ? { ...n, isRead: true } : n))
                );
            } catch (error) {
                console.error('Failed to mark read:', error);
            }
        }

        if (notification.link) {
            setIsOpen(false);
            router.push(notification.link);
        }
    };

    const handleLoadMore = (e) => {
        e.stopPropagation();
        fetchNotifications(true);
    };

    const getIcon = (type) => {
        switch (type) {
            case 'SUCCESS':
                return <CheckCircle className="h-5 w-5 text-green-500" />;
            case 'WARNING':
                return <AlertTriangle className="h-5 w-5 text-amber-500" />;
            case 'ERROR':
                return <AlertCircle className="h-5 w-5 text-red-500" />;
            case 'INFO':
            default:
                return <Info className="h-5 w-5 text-blue-500" />;
        }
    };

    return (
        <div className="relative" ref={dropdownRef}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="relative p-2.5 text-gray-500 hover:text-indigo-600 transition-colors rounded-xl hover:bg-gray-100/50 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                title="Notifications"
            >
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                    <span className="absolute top-2.5 right-2.5 h-2.5 w-2.5 bg-red-500 rounded-full border-2 border-white shadow-sm flex items-center justify-center">
                        <span className="sr-only">{unreadCount} unread notifications</span>
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 z-50 animate-fade-in origin-top-right max-h-[85vh] flex flex-col">
                    <div className="px-4 py-3 border-b border-gray-100 flex justify-between items-center bg-white rounded-t-2xl z-10">
                        <h3 className="font-semibold text-gray-900">Notifications</h3>
                        {unreadCount > 0 && (
                            <button
                                onClick={handleMarkAllRead}
                                className="text-xs text-indigo-600 hover:text-indigo-700 font-medium flex items-center gap-1"
                            >
                                <Check className="h-3 w-3" />
                                Mark all read
                            </button>
                        )}
                    </div>

                    <div className="overflow-y-auto flex-1 custom-scrollbar">
                        {notifications.length === 0 && !isLoading ? (
                            <div className="px-4 py-8 text-center text-gray-500 text-sm">
                                <Bell className="h-8 w-8 mx-auto text-gray-300 mb-2" />
                                <p>No notifications yet</p>
                            </div>
                        ) : (
                            <div className="divide-y divide-gray-50">
                                {notifications.map((notification) => (
                                    <div
                                        key={notification.id}
                                        onClick={() => handleNotificationClick(notification)}
                                        className={`px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors flex gap-3 ${
                                            !notification.isRead ? 'bg-indigo-50/30' : ''
                                        }`}
                                    >
                                        <div className="flex-shrink-0 mt-0.5">
                                            {getIcon(notification.type)}
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className={`text-sm ${!notification.isRead ? 'font-semibold text-gray-900' : 'text-gray-700'}`}>
                                                {notification.title}
                                            </p>
                                            <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">
                                                {notification.message}
                                            </p>
                                            <p className="text-[10px] text-gray-400 mt-1">
                                                {notification.timeAgo}
                                            </p>
                                        </div>
                                        {!notification.isRead && (
                                            <div className="flex-shrink-0 self-center">
                                                <div className="h-2 w-2 bg-indigo-600 rounded-full"></div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                        
                        {isLoading && (
                            <div className="py-4 flex justify-center">
                                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-indigo-600"></div>
                            </div>
                        )}
                    </div>
                    
                    {hasMore && !isLoading && notifications.length > 0 && (
                        <div className="px-4 py-2 border-t border-gray-100 bg-gray-50/50 rounded-b-2xl">
                            <button 
                                className="text-xs text-center w-full text-indigo-600 hover:text-indigo-700 font-medium transition-colors py-1"
                                onClick={handleLoadMore}
                            >
                                Load more
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
