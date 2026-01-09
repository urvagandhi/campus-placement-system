'use client';

import SessionsManager from '@/components/auth/SessionsManager';

export default function StudentSessionsPage() {
    return (
        <div className="max-w-4xl mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold text-gray-900 mb-8">Security & Sessions</h1>
            <SessionsManager />
        </div>
    );
}
