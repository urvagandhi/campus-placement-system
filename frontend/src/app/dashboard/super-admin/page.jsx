'use client';

import Card from '@/components/ui/Card';
import { Activity, Building, Server, Shield, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data
const SUPER_ADMIN_STATS = [
    { label: 'Total Colleges', value: '42', icon: Building, color: 'bg-blue-100/50 text-blue-600' },
    { label: 'Total Admins', value: '156', icon: Users, color: 'bg-purple-100/50 text-purple-600' },
    { label: 'System Uptime', value: '99.9%', icon: Activity, color: 'bg-emerald-100/50 text-emerald-600' },
    { label: 'Server Status', value: 'Healthy', icon: Server, color: 'bg-indigo-100/50 text-indigo-600' },
];

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 900);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-8 animate-fade-in">
            <h1 className="text-3xl font-bold text-gray-900 tracking-tight">System Overview</h1>

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
                    SUPER_ADMIN_STATS.map((stat) => {
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

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* System Info */}
                <div className="lg:col-span-2">
                    <Card title="System Performance" className="h-full border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                         <div className="h-64 flex items-center justify-center text-gray-400">
                            <p>Real-time analytics chart placeholder</p>
                         </div>
                    </Card>
                </div>

                 {/* System Events */}
                 <div className="lg:col-span-1">
                    <Card title="Recent Events" className="h-full border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                        <div className="space-y-4">
                             {[
                                { event: 'Backup Completed', time: '2 hours ago', icon: Shield, color: 'text-emerald-500' },
                                { event: 'New College Added', time: '5 hours ago', icon: Building, color: 'text-blue-500' },
                                { event: 'System Update', time: '1 day ago', icon: Server, color: 'text-indigo-500' },
                             ].map((log, i) => {
                                 const Icon = log.icon;
                                 return (
                                    <div key={i} className="flex items-center gap-3 p-3 hover:bg-white/50 rounded-xl transition-colors">
                                        <div className={`h-8 w-8 rounded-full bg-gray-100/50 flex items-center justify-center ${log.color}`}>
                                            <Icon className="h-4 w-4" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-medium text-gray-900">{log.event}</p>
                                            <p className="text-xs text-gray-500">{log.time}</p>
                                        </div>
                                    </div>
                                 );
                             })}
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
}
