/**
 * Notification API Service
 *
 * Handles all notification-related API calls.
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

/**
 * Generic fetch wrapper for notification API calls
 */
const fetchNotificationApi = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;

    const config = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include', // Include cookies for authentication
        ...options,
    };

    const response = await fetch(url, config);

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || `Failed to ${options.method || 'GET'} notification`);
    }

    return response.json();
};

/**
 * Notification API methods
 */
export const notificationApi = {
    /**
     * Get all notifications for the current user
     */
    getAll: async () => {
        const data = await fetchNotificationApi('/notifications');
        return data.data || [];
    },

    /**
     * Get unread notifications
     */
    getUnread: async () => {
        const data = await fetchNotificationApi('/notifications/unread');
        return data.data || [];
    },

    /**
     * Get count of unread notifications
     */
    getUnreadCount: async () => {
        const data = await fetchNotificationApi('/notifications/unread/count');
        return data.data?.count || 0;
    },

    /**
     * Mark a notification as read
     */
    markAsRead: async (notificationId) => {
        await fetchNotificationApi(`/notifications/${notificationId}/read`, {
            method: 'PUT',
        });
    },

    /**
     * Mark all notifications as read
     */
    markAllAsRead: async () => {
        await fetchNotificationApi('/notifications/read-all', {
            method: 'POST',
        });
    },
};

export default notificationApi;
