'use client';

import Card from '@/components/ui/Card';
import { Activity, Building2, Server, ShieldCheck } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data
const SYSTEM_STATS = [
    { label: 'Total Colleges', value: '1', icon: Building2, color: 'bg-blue-100 text-blue-600' },
    { label: 'Total Admins', value: '3', icon: ShieldCheck, color: 'bg-indigo-100 text-indigo-600' },
    { label: 'System Uptime', value: '99.9%', icon: Activity, color: 'bg-green-100 text-green-600' },
    { label: 'Server Status', value: 'Healthy', icon: Server, color: 'bg-emerald-100 text-emerald-600' },
];

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate loading
        const timer = setTimeout(() => setLoading(false), 800);
        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">System Overview</h1>

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
                    SYSTEM_STATS.map((stat) => {
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

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* System Info */}
                <Card title="System Information" className="border border-gray-100">
                    <div className="space-y-4">
                        <div className="flex justify-between py-2 border-b border-gray-50">
                            <span className="text-gray-500">Version</span>
                            <span className="font-medium">v1.0.0</span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-gray-50">
                            <span className="text-gray-500">Environment</span>
                            <span className="font-medium">Production</span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-gray-50">
                            <span className="text-gray-500">Last Deployment</span>
                            <span className="font-medium">Oct 24, 2023, 14:30 UTC</span>
                        </div>
                        <div className="flex justify-between py-2">
                            <span className="text-gray-500">Database Connection</span>
                            <span className="text-green-600 font-medium flex items-center gap-1">
                                <span className="h-2 w-2 rounded-full bg-green-500"></span>
                                Connected
                            </span>
                        </div>
                    </div>
                </Card>

                {/* Recent Activity Mock */}
                <Card title="Recent System Events" className="border border-gray-100">
                    <div className="space-y-4">
                        <div className="flex gap-3">
                            <div className="h-2 w-2 mt-2 rounded-full bg-blue-500"></div>
                            <div>
                                <p className="text-sm text-gray-900">System backup completed successfully.</p>
                                <p className="text-xs text-gray-500">2 hours ago</p>
                            </div>
                        </div>
                        <div className="flex gap-3">
                            <div className="h-2 w-2 mt-2 rounded-full bg-yellow-500"></div>
                            <div>
                                <p className="text-sm text-gray-900">New college admin account created.</p>
                                <p className="text-xs text-gray-500">5 hours ago</p>
                            </div>
                        </div>
                        <div className="flex gap-3">
                            <div className="h-2 w-2 mt-2 rounded-full bg-green-500"></div>
                            <div>
                                <p className="text-sm text-gray-900">Maintenance scheduled for next Sunday.</p>
                                <p className="text-xs text-gray-500">1 day ago</p>
                            </div>
                        </div>
                    </div>
                </Card>
            </div>
        </div>
    );
}
