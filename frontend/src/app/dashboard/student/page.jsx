'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { useAuth } from '@/hooks/useAuth';
import { ArrowRight, CheckCircle, Clock, Hand, TrendingUp } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function StudentDashboard() {
    const { user } = useAuth();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Mock loading
        const timer = setTimeout(() => setLoading(false), 1000);
        return () => clearTimeout(timer);
    }, []);

    if (loading) {
        return (
            <div className="space-y-6 animate-pulse">
                <div className="h-24 bg-gray-200/50 rounded-2xl w-full"></div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="h-32 bg-gray-200/50 rounded-2xl"></div>
                    <div className="h-32 bg-gray-200/50 rounded-2xl"></div>
                    <div className="h-32 bg-gray-200/50 rounded-2xl"></div>
                </div>
                <div className="h-64 bg-gray-200/50 rounded-2xl w-full"></div>
            </div>
        );
    }

    const stats = [
        { label: 'Applied Drives', value: '12', icon: TrendingUp, color: 'text-blue-600', bg: 'bg-blue-100/50' },
        { label: 'Shortlisted', value: '5', icon: CheckCircle, color: 'text-emerald-600', bg: 'bg-emerald-100/50' },
        { label: 'Pending', value: '4', icon: Clock, color: 'text-amber-600', bg: 'bg-amber-100/50' },
    ];

    // Get user's display name from auth context
    const displayName = user?.name || user?.email?.split('@')[0] || 'Student';

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Welcome Section - Apple Style Hero */}
            <div className="glass-panel rounded-3xl p-8 flex flex-col sm:flex-row justify-between items-center gap-6 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/5 rounded-full blur-3xl transform translate-x-1/2 -translate-y-1/2"></div>

                <div className="relative z-10">
                    <div className="flex items-center gap-2 mb-1">
                        <Hand className="h-6 w-6 text-yellow-500 animate-wave origin-bottom-right" />
                        <span className="text-sm font-semibold text-indigo-600 uppercase tracking-wider">Welcome back</span>
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Hello, {displayName}</h1>
                    <p className="text-gray-600 mt-2 max-w-lg text-lg">Your placement journey is looking great. You have 2 new interview calls scheduled.</p>
                </div>
                <Link href="/dashboard/student/drives" className="relative z-10 w-full sm:w-auto">
                    <Button size="lg" className="w-full sm:w-auto shadow-lg shadow-indigo-500/20">
                        Browse Drives
                    </Button>
                </Link>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {stats.map((stat, index) => {
                    const Icon = stat.icon;
                    return (
                        <Card key={index} hover={true} className="border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                            <div className="flex items-center">
                                <div className={`${stat.bg} p-3.5 rounded-2xl mr-5 shadow-inner`}>
                                    <Icon className={`h-7 w-7 ${stat.color}`} />
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-gray-500 mb-0.5">{stat.label}</p>
                                    <p className="text-3xl font-bold text-gray-900 tracking-tight">{stat.value}</p>
                                </div>
                            </div>
                        </Card>
                    );
                })}
            </div>

            {/* Recent Activity / Applications */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Recent Applications */}
                <Card
                    title="Recent Applications"
                    subtitle="Track your latest job applications"
                    className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
                    footer={
                        <Link href="/dashboard/student/applications" className="text-indigo-600 text-sm font-semibold hover:text-indigo-800 flex items-center transition-colors">
                            View all applications <ArrowRight className="ml-1.5 h-4 w-4" />
                        </Link>
                    }
                >
                    <div className="space-y-4">
                        {[
                            { company: 'Google', role: 'SDE-1', date: '2 days ago', status: 'shortlisted', icon: 'G' },
                            { company: 'Microsoft', role: 'Support Engineer', date: '4 days ago', status: 'pending', icon: 'M' },
                            { company: 'Amazon', role: 'SDE Intern', date: '1 week ago', status: 'rejected', icon: 'A' },
                        ].map((app, i) => (
                            <div key={i} className="flex items-center justify-between p-3.5 hover:bg-white/50 rounded-2xl transition-all border border-transparent hover:border-gray-100 group">
                                <div className="flex items-center gap-4">
                                    <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-gray-100 to-white flex items-center justify-center font-bold text-gray-600 text-lg shadow-sm border border-gray-100">
                                        {app.icon}
                                    </div>
                                    <div>
                                        <p className="font-semibold text-gray-900">{app.company}</p>
                                        <p className="text-sm text-gray-500">{app.role}</p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <Badge variant={
                                        app.status === 'shortlisted' ? 'success' :
                                            app.status === 'pending' ? 'warning' : 'error'
                                    } size="md">
                                        {app.status.charAt(0).toUpperCase() + app.status.slice(1)}
                                    </Badge>
                                    <p className="text-xs text-gray-400 mt-1.5 font-medium">{app.date}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>

                {/* Recommended Drives */}
                <Card
                    title="Recommended for You"
                    subtitle="Opportunities matching your profile"
                    className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
                    footer={
                        <Link href="/dashboard/student/drives" className="text-indigo-600 text-sm font-semibold hover:text-indigo-800 flex items-center transition-colors">
                            View all drives <ArrowRight className="ml-1.5 h-4 w-4" />
                        </Link>
                    }
                >
                    <div className="space-y-4">
                        {[
                            { company: 'Adobe', role: 'Product Intern', eligibility: 'Eligible', deadline: 'Tomorrow', icon: 'A' },
                            { company: 'Salesforce', role: 'MTS', eligibility: 'Eligible', deadline: 'in 2 days', icon: 'S' },
                        ].map((drive, i) => (
                            <div key={i} className="flex items-center justify-between p-3.5 hover:bg-white/50 rounded-2xl transition-all border border-transparent hover:border-gray-100">
                                <div className="flex items-center gap-4">
                                    <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-indigo-50 to-white flex items-center justify-center font-bold text-indigo-600 text-lg shadow-sm border border-indigo-50">
                                        {drive.icon}
                                    </div>
                                    <div>
                                        <p className="font-semibold text-gray-900">{drive.company}</p>
                                        <p className="text-sm text-gray-500">{drive.role}</p>
                                    </div>
                                </div>
                                <div className="text-right flex flex-col items-end gap-1">
                                    <span className="inline-flex items-center px-2 py-0.5 rounded-lg text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100">
                                        {drive.eligibility}
                                    </span>
                                    <p className="text-xs text-red-500 font-medium flex items-center gap-1">
                                        <Clock className="h-3 w-3" /> Ends {drive.deadline}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>
            </div>
        </div>
    );
}
