'use client';

import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Logo from '@/components/ui/Logo';
import { Lock, Mail } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

export default function LoginPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        password: '',
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

        // Simulate API call delay
        await new Promise((resolve) => setTimeout(resolve, 1000));

        // Mock Validation
        if (!formData.email || !formData.password) {
            setError('Please fill in all fields.');
            setIsLoading(false);
            return;
        }

        // Mock Login Success - Role Detection Logic
        let role = 'STUDENT';
        const emailLower = formData.email.toLowerCase();

        if (emailLower.includes('tpo') || emailLower.includes('coordinator')) {
            role = 'COORDINATOR';
        } else if (emailLower.startsWith('admin')) {
            role = 'ADMIN';
        } else if (emailLower.startsWith('super') || emailLower.includes('superadmin')) {
            role = 'SUPER_ADMIN';
        } else {
            role = 'STUDENT';
        }

        localStorage.setItem('userRole', role);

        // Route based on role
        switch (role) {
            case 'STUDENT':
                router.push('/dashboard/student');
                break;
            case 'COORDINATOR':
                router.push('/dashboard/coordinator');
                break;
            case 'ADMIN':
                router.push('/dashboard/admin');
                break;
            case 'SUPER_ADMIN':
                router.push('/dashboard/super-admin');
                break;
            default:
                router.push('/dashboard/student');
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-[#f5f5f7]">
            {/* Sophisticated Background Mesh - Not "Orbs" */}
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
                                        <strong>Role assignment is mocked in UI.</strong><br />
                                        Use specific emails to test roles (e.g., student@..., tpo@..., admin@..., super@...).
                                        Final roles are backend-determined.
                                    </span>
                                </p>
                            </div>

                            {/* Role Selector Removed - System Decided Role */}
                            <div className="hidden">
                                {/* Hidden input if needed for accessibility or form data structure, but logic handles it separately */}
                            </div>

                            <div className="space-y-4">
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

                        <div className="mt-8 pt-6 border-t border-gray-100/60 text-center">
                            <p className="text-sm text-gray-500">
                                New to the platform?{' '}
                                <Link href="/register" className="font-semibold text-indigo-600 hover:text-indigo-700 hover:underline transition-all">
                                    Create an account
                                </Link>
                            </p>
                        </div>
                    </div>

                    <div className="mt-8 text-center">
                        <p className="text-xs text-gray-400 font-medium tracking-wide">
                            © 2024 PlacementPro System. Secure & Encrypted.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
