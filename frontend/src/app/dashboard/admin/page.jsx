'use client';

import Card from '@/components/ui/Card';
import { Briefcase, Building2, GraduationCap, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data
const ADMIN_STATS = [
    { label: 'Total Students', value: '2,450', icon: GraduationCap, color: 'bg-blue-100/50 text-blue-600' },
    { label: 'Total TPOs', value: '12', icon: Users, color: 'bg-purple-100/50 text-purple-600' },
    { label: 'Total Drives', value: '45', icon: Briefcase, color: 'bg-orange-100/50 text-orange-600' },
    { label: 'Active Drives', value: '8', icon: Briefcase, color: 'bg-emerald-100/50 text-emerald-600' },
];

export default function AdminDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 800);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-8 animate-fade-in">
            <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Admin Overview</h1>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {loading ? (
                    // Loading State
                    [1, 2, 3, 4].map((i) => (
                        <div key={i} className="glass-card h-32 animate-pulse flex flex-col justify-between p-6">
                            <div className="flex justify-between">
                                <div className="h-10 w-10 bg-gray-200/50 rounded-xl"></div>
                                <div className="h-4 w-12 bg-gray-200/50 rounded"></div>
                            </div>
                        </div>
                    ))
                ) : (
                    ADMIN_STATS.map((stat) => {
                        const Icon = stat.icon;
                        return (
                            <Card key={stat.label} hover={true} className="border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                                <div className="flex items-start justify-between">
                                    <div>
                                        <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                                        <h3 className="text-3xl font-bold text-gray-900 mt-2 tracking-tight">{stat.value}</h3>
                                    </div>
                                    <div className={`p-3 rounded-2xl ${stat.color} shadow-inner`}>
                                        <Icon className="h-6 w-6" />
                                    </div>
                                </div>
                            </Card>
                        );
                    })
                )}
            </div>

            {/* Welcome/Placeholder */}
            <Card className="border-0 ring-1 ring-emerald-100 bg-gradient-to-r from-emerald-50/50 to-white/60 backdrop-blur-xl">
                <div className="flex items-center gap-6">
                    <div className="p-4 bg-emerald-100/50 rounded-2xl shadow-sm">
                        <Building2 className="h-8 w-8 text-emerald-600" />
                    </div>
                    <div>
                        <h3 className="text-xl font-bold text-emerald-900">Welcome to College Administration</h3>
                        <p className="text-emerald-700/80 mt-1 text-lg">Manage users, departments, and oversee placement activities from here.</p>
                    </div>
                </div>
            </Card>
        </div>
    );
}
