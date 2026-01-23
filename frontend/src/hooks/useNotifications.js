import { useState, useEffect, useCallback } from 'react';
import { notificationApi } from '@/services/notificationService';

/**
 * Custom hook for managing notifications
 */
export function useNotifications() {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    /**
     * Fetch all notifications
     */
    const fetchNotifications = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await notificationApi.getAll();
            setNotifications(data);
        } catch (err) {
            console.error('Failed to fetch notifications:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    /**
     * Fetch unread count
     */
    const fetchUnreadCount = useCallback(async () => {
        try {
            const count = await notificationApi.getUnreadCount();
            setUnreadCount(count);
        } catch (err) {
            console.error('Failed to fetch unread count:', err);
        }
    }, []);

    /**
     * Mark a notification as read
     */
    const markAsRead = useCallback(async (notificationId) => {
        try {
            await notificationApi.markAsRead(notificationId);
            // Update local state
            setNotifications((prev) =>
                prev.map((n) =>
                    n.id === notificationId
                        ? { ...n, isRead: true, readAt: new Date().toISOString() }
                        : n
                )
            );
            // Update unread count
            setUnreadCount((prev) => Math.max(0, prev - 1));
        } catch (err) {
            console.error('Failed to mark notification as read:', err);
            throw err;
        }
    }, []);

    /**
     * Mark all notifications as read
     */
    const markAllAsRead = useCallback(async () => {
        try {
            await notificationApi.markAllAsRead();
            // Update local state
            setNotifications((prev) =>
                prev.map((n) => ({
                    ...n,
                    isRead: true,
                    readAt: new Date().toISOString(),
                }))
            );
            // Reset unread count
            setUnreadCount(0);
        } catch (err) {
            console.error('Failed to mark all as read:', err);
            throw err;
        }
    }, []);

    /**
     * Refresh notifications and unread count
     */
    const refresh = useCallback(async () => {
        await Promise.all([fetchNotifications(), fetchUnreadCount()]);
    }, [fetchNotifications, fetchUnreadCount]);

    // Initial fetch
    useEffect(() => {
        refresh();
    }, [refresh]);

    // Poll for new notifications every 30 seconds
    useEffect(() => {
        const interval = setInterval(() => {
            fetchUnreadCount();
        }, 30000); // 30 seconds

        return () => clearInterval(interval);
    }, [fetchUnreadCount]);

    return {
        notifications,
        unreadCount,
        loading,
        error,
        markAsRead,
        markAllAsRead,
        refresh,
    };
}
