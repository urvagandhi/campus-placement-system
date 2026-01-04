'use client';

/**
 * Dashboard layout shell
 * Role-specific layouts (AdminLayout, StudentLayout, etc.) handle the actual navigation.
 * This global dashboard layout is just a container.
 */
export default function DashboardLayout({ children }) {
    return (
        <div className="min-h-screen bg-gray-50">
            {children}
        </div>
    );
}
