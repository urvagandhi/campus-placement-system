'use client';

import Card from '@/components/ui/Card';
import { StatsSkeleton, Skeleton } from '@/components/ui/Skeleton';
import { Activity, AlertTriangle, Building2, CheckCircle, Server, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

import api from '@/services/api';

export default function SuperAdminDashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalColleges: 0,
        totalUsers: 0,
        uptimeSeconds: 0,
        activeSessions: 0,
        maintenanceMode: false
    });

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await api.users.getSystemStats();
                if (response.success) {
                    setStats(response.data);
                }
            } catch (error) {
                console.error("Failed to fetch system stats:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    const formatUptime = (seconds) => {
        if (!seconds) return '0h';
        const hours = Math.floor(seconds / 3600);
        const days = Math.floor(hours / 24);
        if (days > 0) return `${days}d ${hours % 24}h`;
        return `${hours}h ${Math.floor((seconds % 3600) / 60)}m`;
    };

    return (
        <div className="space-y-8 animate-fade-in">
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-gray-900 via-indigo-900 to-gray-900">
                Super Admin Overview
            </h1>

            {/* Stats Grid */}
            {loading ? (
                <StatsSkeleton />
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {[
                        { label: 'Registered Colleges', value: stats.totalColleges, icon: Building2, color: 'bg-blue-100/50 text-blue-600' },
                        { label: 'Total System Users', value: stats.totalUsers, icon: Users, color: 'bg-purple-100/50 text-purple-600' },
                        { label: 'System Uptime', value: formatUptime(stats.uptimeSeconds), icon: Server, color: 'bg-orange-100/50 text-orange-600' },
                        { label: 'Active Sessions', value: stats.activeSessions || '--', icon: Activity, color: 'bg-emerald-100/50 text-emerald-600' },
                    ].map((stat) => (
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
                    ))}
                </div>
            )}

            {/* Welcome/System Status */}
            {loading ? (
                <div className="glass-card p-8 border border-gray-100/50 flex items-center gap-6 animate-pulse">
                    <Skeleton className="h-16 w-16 rounded-2xl" />
                    <div className="flex-1 space-y-3">
                        <Skeleton className="h-6 w-1/4" />
                        <Skeleton className="h-4 w-3/4" />
                    </div>
                </div>
            ) : (
                <Card className={`border-0 ring-1 ${stats.maintenanceMode ? 'ring-amber-200 bg-amber-50/50' : 'ring-emerald-200 bg-emerald-50/50'} backdrop-blur-xl transition-all duration-500`}>
                    <div className="flex items-center gap-6 p-2">
                        <div className={`p-4 rounded-2xl shadow-sm ${stats.maintenanceMode ? 'bg-amber-100 text-amber-600' : 'bg-emerald-100 text-emerald-600'}`}>
                            {stats.maintenanceMode ? <AlertTriangle className="h-8 w-8" /> : <CheckCircle className="h-8 w-8" />}
                        </div>
                        <div>
                            <h3 className={`text-xl font-bold ${stats.maintenanceMode ? 'text-amber-900' : 'text-emerald-900'}`}>
                                System Status: {stats.maintenanceMode ? 'Maintenance Mode' : 'Operational'}
                            </h3>
                            <p className={`${stats.maintenanceMode ? 'text-amber-700/80' : 'text-emerald-700/80'} mt-1 text-lg`}>
                                {stats.maintenanceMode 
                                    ? 'System is currently in maintenance mode. User access is restricted to Super Admins only.' 
                                    : 'Platform is running optimally. All services are online.'}
                            </p>
                        </div>
                    </div>
                </Card>
            )}
        </div>
    );
}
