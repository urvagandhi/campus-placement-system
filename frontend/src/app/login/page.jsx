'use client';

import LoginForm from '@/components/forms/LoginForm';
import Card from '@/components/ui/Card';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

/**
 * Login page
 */
export default function LoginPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleLogin = async (formData) => {
        setLoading(true);
        setError(null);

        try {
            // TODO: Replace with actual API call when backend is ready
            // const response = await authApi.login(formData.email, formData.password);
            // saveAuthData(response.data.token, response.data.user);
            // router.push(getRoleBasedRedirect(response.data.user.role));

            // Placeholder for demo
            console.log('Login attempt:', formData);
            setError('Login functionality not yet implemented. Backend integration pending.');
        } catch (err) {
            setError(err.message || 'Login failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center py-12 px-4">
            <div className="max-w-md w-full">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-bold text-gray-900">
                        Campus Placement System
                    </h1>
                    <p className="mt-2 text-gray-600">
                        AI-Assisted Smart Placement Management
                    </p>
                </div>

                <Card title="Login to your account">
                    <LoginForm
                        onSubmit={handleLogin}
                        loading={loading}
                        error={error}
                    />

                    <div className="mt-4 text-center text-sm">
                        <span className="text-gray-600">Don't have an account? </span>
                        <Link href="/register" className="text-blue-600 hover:text-blue-700">
                            Register here
                        </Link>
                    </div>
                </Card>
            </div>
        </div>
    );
}
