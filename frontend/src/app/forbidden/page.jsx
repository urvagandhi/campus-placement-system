'use client';

import Button from '@/components/ui/Button';
import Link from 'next/link';

/**
 * Forbidden page - Shown when user tries to access unauthorized resources
 */
export default function ForbiddenPage() {
    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <div className="text-center">
                <h1 className="text-6xl font-bold text-red-600">403</h1>
                <h2 className="mt-4 text-2xl font-semibold text-gray-900">
                    Access Forbidden
                </h2>
                <p className="mt-2 text-gray-600 max-w-md">
                    You don't have permission to access this page. Please contact your administrator if you believe this is an error.
                </p>
                <div className="mt-6">
                    <Link href="/dashboard">
                        <Button variant="primary">Back to Dashboard</Button>
                    </Link>
                </div>
            </div>
        </div>
    );
}
