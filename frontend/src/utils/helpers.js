/**
 * General helper functions
 */

/**
 * Format date to readable string
 */
export const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    });
};

/**
 * Format datetime to readable string
 */
export const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

/**
 * Format package in LPA
 */
export const formatPackage = (lpa) => {
    if (!lpa) return 'Not specified';
    return `₹${lpa} LPA`;
};

/**
 * Get status color class
 */
export const getStatusColor = (status) => {
    const colors = {
        PENDING: 'bg-yellow-100 text-yellow-800',
        SHORTLISTED: 'bg-blue-100 text-blue-800',
        SELECTED: 'bg-green-100 text-green-800',
        REJECTED: 'bg-red-100 text-red-800',
        WITHDRAWN: 'bg-gray-100 text-gray-800',
        UPCOMING: 'bg-purple-100 text-purple-800',
        ONGOING: 'bg-blue-100 text-blue-800',
        COMPLETED: 'bg-green-100 text-green-800',
        CANCELLED: 'bg-red-100 text-red-800',
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
};

/**
 * Truncate text with ellipsis
 */
export const truncateText = (text, maxLength = 100) => {
    if (!text || text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
};

/**
 * Parse comma-separated string to array
 */
export const parseCommaSeparated = (str) => {
    if (!str) return [];
    return str.split(',').map(s => s.trim()).filter(Boolean);
};

/**
 * Join array to comma-separated string
 */
export const joinToCommaSeparated = (arr) => {
    if (!arr || !Array.isArray(arr)) return '';
    return arr.join(', ');
};

/**
 * Capitalize first letter
 */
export const capitalize = (str) => {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};

/**
 * Generate initials from name
 */
export const getInitials = (name) => {
    if (!name) return '';
    return name
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);
};
