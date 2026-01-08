'use client';

import Card from '@/components/ui/Card';
import { Activity, Building2, Server, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data for Super Admin
const SUPER_ADMIN_STATS = [
    { label: 'Registered Colleges', value: '12', icon: Building2, color: 'bg-blue-100/50 text-blue-600' },
    { label: 'Total System Users', value: '3,450', icon: Users, color: 'bg-purple-100/50 text-purple-600' },
    { label: 'System Uptime', value: '99.9%', icon: Server, color: 'bg-orange-100/50 text-orange-600' },
    { label: 'Active Sessions', value: '124', icon: Activity, color: 'bg-emerald-100/50 text-emerald-600' },
];

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 800);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-8 animate-fade-in">
            <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Super Admin Overview</h1>

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
                    SUPER_ADMIN_STATS.map((stat) => (
                        <Card key={stat.label} hover={true} className="border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                            <div className="flex items-start justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                                    <h3 className="text-3xl font-bold text-gray-900 mt-2 tracking-tight">{stat.value}</h3>
                                </div>
                                <div className={`p-3 rounded-2xl ${stat.color} shadow-inner`}>
                                    <stat.icon className="h-6 w-6" />
                                </div>
                            </div>
                        </Card>
                    ))
                )}
            </div>

            {/* Welcome/System Status */}
            <Card className="border-0 ring-1 ring-indigo-100 bg-gradient-to-r from-indigo-50/50 to-white/60 backdrop-blur-xl">
                <div className="flex items-center gap-6">
                    <div className="p-4 bg-indigo-100/50 rounded-2xl shadow-sm">
                        <Server className="h-8 w-8 text-indigo-600" />
                    </div>
                    <div>
                        <h3 className="text-xl font-bold text-indigo-900">System Status: Operational</h3>
                        <p className="text-indigo-700/80 mt-1 text-lg">Platform is running optimally. All services are online.</p>
                    </div>
                </div>
            </Card>
        </div>
    );
}
