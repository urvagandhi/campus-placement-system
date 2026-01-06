'use client';

import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Logo from '@/components/ui/Logo';
import { ArrowRight, Lock, Mail, User } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

export default function RegisterPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
    });

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setIsLoading(true);
        setError(null);

        // Simulate API delay
        try {
            await new Promise(resolve => setTimeout(resolve, 1500));
            console.log('Registration attempt:', formData);
            setError('Account creation not enabled in this demo.');
        } catch (err) {
            setError('Registration failed.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-[#f5f5f7]">
            {/* Sophisticated Background Mesh */}
            <div className="absolute inset-0 z-0">
                <div className="absolute top-[-10%] right-[-5%] w-[60%] h-[60%] rounded-full bg-blue-400/10 blur-[100px]" />
                <div className="absolute bottom-[-10%] left-[-5%] w-[60%] h-[60%] rounded-full bg-indigo-400/10 blur-[100px]" />
            </div>

            <div className="relative z-10 w-full max-w-5xl flex flex-col items-center py-10">

                <div className="w-full max-w-lg">
                    {/* Header */}
                    <div className="text-center mb-8">
                        <Logo size="lg" className="justify-center mb-6" />
                        <h1 className="text-3xl font-bold tracking-tight text-gray-900">
                            Create your account
                        </h1>
                        <p className="mt-2 text-gray-500">
                            Start your journey with PlacementPro today
                        </p>
                    </div>

                    <div className="glass-card p-8 sm:p-10 backdrop-blur-2xl bg-white/70 border border-white/50 shadow-2xl shadow-black/5 ring-1 ring-black/5 rounded-[2rem]">
                        <form className="space-y-5" onSubmit={handleSubmit}>
                            {error && (
                                <div className="rounded-2xl bg-red-50/50 p-4 border border-red-100 flex items-center gap-3 animate-shake">
                                    <div className="h-2 w-2 rounded-full bg-red-500 flex-shrink-0" />
                                    <p className="text-sm font-medium text-red-600">{error}</p>
                                </div>
                            )}

                            <Input
                                label="Full Name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="John Doe"
                                icon={User}
                                required
                                className="scale-100 transition-transform focus-within:scale-[1.01]"
                            />

                            <Input
                                label="Email"
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="john@example.com"
                                icon={Mail}
                                required
                                className="scale-100 transition-transform focus-within:scale-[1.01]"
                            />

                            <Input
                                label="Password"
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Create a strong password"
                                icon={Lock}
                                required
                                className="scale-100 transition-transform focus-within:scale-[1.01]"
                            />

                            <Input
                                label="Confirm Password"
                                type="password"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                placeholder="Repeat password"
                                icon={Lock}
                                required
                                className="scale-100 transition-transform focus-within:scale-[1.01]"
                            />

                            <div className="pt-2">
                                <Button
                                    type="submit"
                                    loading={isLoading}
                                    className="w-full py-3.5 text-base font-semibold shadow-xl shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all rounded-2xl"
                                    size="lg"
                                >
                                    Create Account
                                </Button>
                            </div>
                        </form>

                        <div className="mt-8 pt-6 border-t border-gray-100/60 text-center">
                            <p className="text-sm text-gray-500">
                                Already have an account?{' '}
                                <Link href="/login" className="font-semibold text-indigo-600 hover:text-indigo-700 hover:underline transition-all inline-flex items-center gap-1">
                                    Log in
                                    <ArrowRight className="h-3 w-3" />
                                </Link>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
