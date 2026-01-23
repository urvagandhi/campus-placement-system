'use client';

import Card from '@/components/ui/Card';
import { analyticsApi } from '@/services/api';
import { Briefcase, Building2, GraduationCap, Users } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';

export default function AdminDashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalStudents: 0,
        totalTPOs: 0,
        totalDrives: 0,
        activeDrives: 0
    });

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await analyticsApi.getOverview();
                const data = response.data || {};
                
                setStats({
                    totalStudents: data.totalStudents || 0,
                    totalTPOs: data.totalAVG || 0, // Using a placeholder if specific TPO count isn't in overview
                    totalDrives: data.totalDrives || 0,
                    activeDrives: data.activeDrives || 0
                });
            } catch (error) {
                console.error("Admin stats fetch error:", error);
                
                // If endpoint fails (e.g. 403), we might want to fail gracefully
                if (error.message && error.message.includes("Access Denied")) {
                    toast.error("You do not have permission to view analytics.");
                }
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    const adminStats = [
        { label: 'Total Students', value: stats.totalStudents, icon: GraduationCap, color: 'bg-blue-100/50 text-blue-600' },
        { label: 'Total Drives', value: stats.totalDrives, icon: Briefcase, color: 'bg-orange-100/50 text-orange-600' },
        { label: 'Active Drives', value: stats.activeDrives, icon: Briefcase, color: 'bg-emerald-100/50 text-emerald-600' },
    ];

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
                    adminStats.map((stat) => {
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
