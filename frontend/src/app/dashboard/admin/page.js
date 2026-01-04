'use client';

import Card from '@/components/ui/Card';
import { Briefcase, Building2, GraduationCap, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data
const ADMIN_STATS = [
    { label: 'Total Students', value: '2,450', icon: GraduationCap, color: 'bg-blue-100 text-blue-600' },
    { label: 'Total TPOs', value: '12', icon: Users, color: 'bg-purple-100 text-purple-600' },
    { label: 'Total Drives', value: '45', icon: Briefcase, color: 'bg-orange-100 text-orange-600' },
    { label: 'Active Drives', value: '8', icon: Briefcase, color: 'bg-green-100 text-green-600' },
];

export default function AdminDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 800);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>

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
                    ADMIN_STATS.map((stat) => {
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

            {/* Welcome/Placeholder */}
            <Card className="border border-emerald-100 bg-emerald-50">
                <div className="flex items-center gap-4">
                    <div className="p-3 bg-emerald-100 rounded-full">
                        <Building2 className="h-6 w-6 text-emerald-600" />
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-emerald-900">Welcome to College Administration</h3>
                        <p className="text-emerald-700 mt-1">Manage users, departments, and oversee placement activities from here.</p>
                    </div>
                </div>
            </Card>
        </div>
    );
}
