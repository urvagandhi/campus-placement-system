'use client';

import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Logo from '@/components/ui/Logo';
import { useAuth } from '@/hooks/useAuth';
import { Lock, Mail } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';

export default function LoginPage() {
    const { login } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        username: '', // Honeypot field
    });

    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        // Validation
        if (!formData.email || !formData.password) {
            setError('Please fill in all fields.');
            setIsLoading(false);
            return;
        }

        try {
            // Use AuthContext login (pass honeypot field)
            await login(formData.email, formData.password, formData.username);
        } catch (err) {
            // Handle specific error cases
            if (err.message.includes('credentials')) {
                setError('Invalid email or password.');
            } else if (err.message.includes('deactivated')) {
                setError('Your account has been deactivated. Please contact an administrator.');
            } else if (err.message.includes('institution') || err.message.includes('active')) {
                setError('Access Denied: Your institution has been deactivated. Please contact your administrator.');
            } else if (err.message.includes('Anti-automation') || err.message.includes('refresh')) {
                setError('Browser check failed. Please refresh the page and try again.');
            } else {
                setError(err.message || 'An error occurred. Please try again.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-[#f5f5f7]">
            {/* Sophisticated Background Mesh */}
            <div className="absolute inset-0 z-0">
                <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-blue-400/20 blur-[120px]" />
                <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-purple-400/20 blur-[120px]" />
            </div>

            {/* Main Content Container */}
            <div className="relative z-10 w-full max-w-5xl flex flex-col items-center">

                {/* Brand Header */}
                <div className="mb-8 flex flex-col items-center animate-fade-in-down">
                    <Logo size="lg" className="mb-4" />
                    <h1 className="text-4xl font-bold tracking-tight text-gray-900 bg-clip-text text-transparent bg-gradient-to-r from-gray-900 to-gray-700">
                        Welcome Back
                    </h1>
                    <p className="mt-2 text-lg text-gray-500 font-medium">
                        Sign in to your professional workspace
                    </p>
                </div>

                {/* Glass Card - The "Hero" */}
                <div className="w-full max-w-md">
                    <div className="glass-card p-8 sm:p-10 backdrop-blur-2xl bg-white/70 border border-white/50 shadow-2xl shadow-black/5 ring-1 ring-black/5 rounded-[2rem]">
                        <form className="space-y-6" onSubmit={handleSubmit}>

                            {/* Demo Disclaimer */}
                            <div className="mb-6 p-4 bg-amber-50/80 border border-amber-100 rounded-2xl text-sm text-amber-800 animate-fade-in">
                                <p className="font-medium flex gap-2">
                                    <span className="text-xl">⚠️</span>
                                    <span>
                                        <strong>Role is determined by the system.</strong><br />
                                        Your role is assigned by administrators and cannot be changed during login.
                                    </span>
                                </p>
                            </div>

                            <div className="space-y-4">
                                {/* Honeypot field - hidden from humans */}
                                <div className="hidden" aria-hidden="true">
                                    <input
                                        type="text"
                                        name="username"
                                        autoComplete="off"
                                        tabIndex="-1"
                                        value={formData.username}
                                        onChange={handleChange}
                                    />
                                </div>
                                <Input
                                    label="Email"
                                    name="email"
                                    type="email"
                                    placeholder="name@university.edu"
                                    value={formData.email}
                                    onChange={handleChange}
                                    icon={Mail}
                                    required
                                    className="scale-100 transition-transform focus-within:scale-[1.01]"
                                />

                                <div>
                                    <div className="flex items-center justify-between mb-1.5">
                                        <label className="text-sm font-semibold text-gray-700 ml-1">Password</label>
                                        <Link href="#" className="text-sm font-medium text-indigo-600 hover:text-indigo-500 transition-colors">
                                            Forgot?
                                        </Link>
                                    </div>
                                    <Input
                                        name="password"
                                        type="password"
                                        placeholder="••••••••"
                                        value={formData.password}
                                        onChange={handleChange}
                                        icon={Lock}
                                        showPasswordToggle
                                        required
                                        className="scale-100 transition-transform focus-within:scale-[1.01]"
                                    />
                                </div>
                            </div>

                            {error && (
                                <div className="rounded-2xl bg-red-50/50 p-4 border border-red-100 flex items-center gap-3 animate-shake">
                                    <div className="h-2 w-2 rounded-full bg-red-500 flex-shrink-0" />
                                    <p className="text-sm font-medium text-red-600">{error}</p>
                                </div>
                            )}

                            <Button
                                type="submit"
                                className="w-full py-3.5 text-base font-semibold shadow-xl shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all rounded-2xl"
                                loading={isLoading}
                                size="lg"
                            >
                                Sign In
                            </Button>
                        </form>

                        {/* Footer - Registration removed: handled by TPO in Student Onboarding */}
                    </div>

                    <div className="mt-8 text-center">
                        <p className="text-xs text-gray-400 font-medium tracking-wide">
                            &copy; {new Date().getFullYear()} PlacementPro System. Secure & Encrypted.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
