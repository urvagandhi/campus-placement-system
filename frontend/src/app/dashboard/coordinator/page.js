'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import { Activity, ArrowRight, Briefcase, Calendar, UserCheck, Users } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

// Mock Data
const MOCK_STATS = [
    { label: 'Total Drives', value: '12', icon: Briefcase, color: 'bg-blue-100 text-blue-600' },
    { label: 'Active Drives', value: '4', icon: Activity, color: 'bg-green-100 text-green-600' },
    { label: 'Total Applicants', value: '148', icon: Users, color: 'bg-purple-100 text-purple-600' },
    { label: 'Shortlisted', value: '45', icon: UserCheck, color: 'bg-indigo-100 text-indigo-600' },
];

const RECENT_DRIVES = [
    { id: 1, company: 'Google', role: 'Software Engineer', date: '2023-11-20', applicants: 45, status: 'Active' },
    { id: 2, company: 'Microsoft', role: 'Support Engineer', date: '2023-11-25', applicants: 32, status: 'Active' },
    { id: 3, company: 'Amazon', role: 'SDE Intern', date: '2023-11-18', applicants: 28, status: 'Completed' },
];

export default function CoordinatorDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 1000);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Welcome, Placement Coordinator</h1>
                    <p className="text-gray-600 mt-1">Here is an overview of the ongoing and upcoming placement activities.</p>
                </div>
                <div className="flex items-center text-sm font-medium text-gray-500 bg-white px-3 py-1.5 rounded-lg shadow-sm border border-gray-200">
                    <Calendar className="mr-2 h-4 w-4" />
                    October 25, 2023
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {loading ? (
                    // Loading State
                    [1, 2, 3, 4].map((i) => (
                        <div key={i} className="bg-white rounded-xl h-32 animate-pulse border border-gray-100 p-6 flex flex-col justify-between">
                            <div className="flex justify-between">
                                <div className="h-10 w-10 bg-gray-200 rounded-lg"></div>
                                <div className="h-4 w-12 bg-gray-200 rounded"></div>
                            </div>
                            <div className="h-8 w-16 bg-gray-200 rounded mt-2"></div>
                        </div>
                    ))
                ) : (
                    MOCK_STATS.map((stat) => {
                        const Icon = stat.icon;
                        return (
                            <Card key={stat.label} className="border border-gray-100 hover:shadow-md transition-all">
                                <div className="flex items-start justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                                        <h3 className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</h3>
                                    </div>
                                    <div className={`p-3 rounded-xl ${stat.color}`}>
                                        <Icon className="h-6 w-6" />
                                    </div>
                                </div>
                            </Card>
                        );
                    })
                )}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Recent Drives */}
                <div className="lg:col-span-2">
                    <Card title="Recent Drives" className="h-full border border-gray-100">
                        {loading ? (
                            <div className="space-y-4">
                                {[1, 2, 3].map(i => (
                                    <div key={i} className="h-16 w-full bg-gray-50 animate-pulse rounded-lg"></div>
                                ))}
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {RECENT_DRIVES.length > 0 ? (
                                    RECENT_DRIVES.map((drive) => (
                                        <div key={drive.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-100 hover:bg-gray-100 transition-colors">
                                            <div>
                                                <h4 className="font-semibold text-gray-900">{drive.company}</h4>
                                                <p className="text-sm text-gray-500">{drive.role}</p>
                                            </div>
                                            <div className="flex items-center gap-4">
                                                <div className="text-right hidden sm:block">
                                                    <p className="text-sm font-medium text-gray-900">{drive.applicants} Applicants</p>
                                                    <p className="text-xs text-gray-500">{drive.date}</p>
                                                </div>
                                                <Badge variant={drive.status === 'Active' ? 'success' : 'neutral'}>
                                                    {drive.status}
                                                </Badge>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="text-center py-8 text-gray-500">
                                        <Briefcase className="h-10 w-10 mx-auto text-gray-300 mb-2" />
                                        <p>No drives created yet</p>
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="mt-6 pt-4 border-t border-gray-100">
                            <Link
                                href="/dashboard/coordinator/drives"
                                className="flex items-center text-sm font-medium text-indigo-600 hover:text-indigo-700"
                            >
                                View all drives <ArrowRight className="ml-1 h-4 w-4" />
                            </Link>
                        </div>
                    </Card>
                </div>

                {/* Quick Actions */}
                <div className="lg:col-span-1">
                    <Card title="Quick Actions" className="h-full border border-gray-100 bg-gradient-to-br from-white to-gray-50">
                        {loading ? (
                            <div className="space-y-3">
                                <div className="h-10 w-full bg-gray-200 animate-pulse rounded-lg"></div>
                                <div className="h-10 w-full bg-gray-200 animate-pulse rounded-lg"></div>
                            </div>
                        ) : (
                            <div className="space-y-3">
                                <Link href="/dashboard/coordinator/drives/create" className="block p-3 bg-white border border-gray-200 rounded-lg shadow-sm hover:border-indigo-300 hover:ring-1 hover:ring-indigo-300 transition-all group">
                                    <h4 className="font-medium text-gray-900 group-hover:text-indigo-600 flex items-center gap-2">
                                        <div className="h-6 w-6 bg-indigo-100 rounded flex items-center justify-center text-indigo-600">
                                            +
                                        </div>
                                        Create New Drive
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-1 pl-8">Schedule a drive and invite students.</p>
                                </Link>

                                <Link href="/dashboard/coordinator/applicants" className="block p-3 bg-white border border-gray-200 rounded-lg shadow-sm hover:border-indigo-300 hover:ring-1 hover:ring-indigo-300 transition-all group">
                                    <h4 className="font-medium text-gray-900 group-hover:text-indigo-600 flex items-center gap-2">
                                        <div className="h-6 w-6 bg-purple-100 rounded flex items-center justify-center text-purple-600">
                                            <Users className="h-3 w-3" />
                                        </div>
                                        Review Applicants
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-1 pl-8">12 Pending reviews from yesterday.</p>
                                </Link>

                                <Link href="/dashboard/coordinator/analytics" className="block p-3 bg-white border border-gray-200 rounded-lg shadow-sm hover:border-indigo-300 hover:ring-1 hover:ring-indigo-300 transition-all group">
                                    <h4 className="font-medium text-gray-900 group-hover:text-indigo-600 flex items-center gap-2">
                                        <div className="h-6 w-6 bg-blue-100 rounded flex items-center justify-center text-blue-600">
                                            <Activity className="h-3 w-3" />
                                        </div>
                                        Generate Reports
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-1 pl-8">Export placement data for college admin.</p>
                                </Link>
                            </div>
                        )}
                    </Card>
                </div>
            </div>
        </div>
    );
}
