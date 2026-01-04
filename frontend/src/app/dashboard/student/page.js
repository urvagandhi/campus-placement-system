'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { ArrowRight, CheckCircle, Clock, TrendingUp } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function StudentDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Mock loading
        const timer = setTimeout(() => setLoading(false), 1000);
        return () => clearTimeout(timer);
    }, []);

    if (loading) {
        return (
            <div className="space-y-6 animate-pulse">
                <div className="h-20 bg-gray-200 rounded-xl w-full"></div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="h-32 bg-gray-200 rounded-xl"></div>
                    <div className="h-32 bg-gray-200 rounded-xl"></div>
                    <div className="h-32 bg-gray-200 rounded-xl"></div>
                </div>
                <div className="h-64 bg-gray-200 rounded-xl w-full"></div>
            </div>
        );
    }

    const stats = [
        { label: 'Applied Drives', value: '12', icon: TrendingUp, color: 'text-blue-600', bg: 'bg-blue-100' },
        { label: 'Shortlisted', value: '5', icon: CheckCircle, color: 'text-green-600', bg: 'bg-green-100' },
        { label: 'Pending', value: '4', icon: Clock, color: 'text-yellow-600', bg: 'bg-yellow-100' },
    ];

    return (
        <div className="space-y-8">
            {/* Welcome Section */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Welcome back, John! 👋</h1>
                    <p className="text-gray-600 mt-1">Here's what's happening with your job applications today.</p>
                </div>
                <Link href="/dashboard/student/drives">
                    <Button>Browse Drives</Button>
                </Link>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {stats.map((stat, index) => {
                    const Icon = stat.icon;
                    return (
                        <Card key={index} className="hover:shadow-lg transition-shadow duration-200">
                            <div className="flex items-center">
                                <div className={`${stat.bg} p-3 rounded-lg mr-4`}>
                                    <Icon className={`h-6 w-6 ${stat.color}`} />
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                                    <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                                </div>
                            </div>
                        </Card>
                    );
                })}
            </div>

            {/* Recent Activity / Applications */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Recent Applications */}
                <Card title="Recent Applications"
                    footer={
                        <Link href="/dashboard/student/applications" className="text-indigo-600 text-sm font-medium hover:text-indigo-800 flex items-center">
                            View all applications <ArrowRight className="ml-1 h-4 w-4" />
                        </Link>
                    }
                >
                    <div className="space-y-4">
                        {[
                            { company: 'Google', role: 'SDE-1', date: '2 days ago', status: 'shortlisted' },
                            { company: 'Microsoft', role: 'Support Engineer', date: '4 days ago', status: 'pending' },
                            { company: 'Amazon', role: 'SDE Intern', date: '1 week ago', status: 'rejected' },
                        ].map((app, i) => (
                            <div key={i} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors border border-gray-100">
                                <div className="flex items-center gap-3">
                                    <div className="h-10 w-10 rounded-full bg-gray-100 flex items-center justify-center font-bold text-gray-600 text-xs">
                                        {app.company[0]}
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900 text-sm">{app.company}</p>
                                        <p className="text-xs text-gray-500">{app.role}</p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <Badge variant={
                                        app.status === 'shortlisted' ? 'success' :
                                            app.status === 'pending' ? 'warning' : 'error'
                                    }>
                                        {app.status.charAt(0).toUpperCase() + app.status.slice(1)}
                                    </Badge>
                                    <p className="text-xs text-gray-400 mt-1">{app.date}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>

                {/* Recommended Drives */}
                <Card title="Recommended for You"
                    footer={
                        <Link href="/dashboard/student/drives" className="text-indigo-600 text-sm font-medium hover:text-indigo-800 flex items-center">
                            View all drives <ArrowRight className="ml-1 h-4 w-4" />
                        </Link>
                    }
                >
                    <div className="space-y-4">
                        {[
                            { company: 'Adobe', role: 'Product Intern', eligibility: 'Eligible', deadline: 'Tomorrow' },
                            { company: 'Salesforce', role: 'MTS', eligibility: 'Eligible', deadline: 'in 2 days' },
                        ].map((drive, i) => (
                            <div key={i} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors border border-gray-100">
                                <div>
                                    <p className="font-medium text-gray-900 text-sm">{drive.company}</p>
                                    <p className="text-xs text-gray-500">{drive.role}</p>
                                </div>
                                <div className="text-right">
                                    <span className="text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-md">
                                        {drive.eligibility}
                                    </span>
                                    <p className="text-xs text-red-500 mt-1 font-medium">Ends {drive.deadline}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>
            </div>
        </div>
    );
}
