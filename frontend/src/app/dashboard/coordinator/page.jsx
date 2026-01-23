'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import { analyticsApi, drivesApi } from '@/services/api';
import { Activity, ArrowRight, Briefcase, Calendar, Plus, UserCheck, Users } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';

export default function CoordinatorDashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalDrives: 0,
        activeDrives: 0,
        totalApplicants: 0,
        shortlisted: 0
    });
    const [recentDrives, setRecentDrives] = useState([]);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [analyticsResponse, drivesResponse] = await Promise.all([
                    analyticsApi.getOverview().catch(err => {
                        console.error("Failed to fetch analytics:", err);
                        // Return default structure if failed
                        return { data: { totalDrives: 0, activeDrives: 0, totalApplications: 0, shortlistedCount: 0 } };
                    }),
                    drivesApi.getAll().catch(err => {
                        console.error("Failed to fetch drives:", err);
                        return { data: { content: [] } };
                    })
                ]);

                // Update stats from analytics response
                // Assuming analytics API returns { totalDrives, activeDrives, totalApplications, shortlistedCount }
                // Adjust property names based on actual API response
                const analytics = analyticsResponse.data || {};
                
                // If analytics endpoint is not fully ready, we can compute from drives too, but prefer analytics API
                setStats({
                    totalDrives: analytics.totalDrives || 0,
                    activeDrives: analytics.activeDrives || 0,
                    totalApplicants: analytics.totalApplications || 0,
                    shortlisted: analytics.shortlistedCount || 0
                });

                // Recent drives
                // drivesResponse.data could be a Page object or List, check your API
                // Assuming Page object: { content: [...] }
                const drivesList = drivesResponse.data?.content || (Array.isArray(drivesResponse.data) ? drivesResponse.data : []) || [];
                setRecentDrives(drivesList);

            } catch (error) {
                console.error("Dashboard data fetch error:", error);
                toast.error("Failed to load dashboard data");
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const statItems = [
        { label: 'Total Drives', value: stats.totalDrives, icon: Briefcase, color: 'bg-blue-100/50 text-blue-600' },
        { label: 'Active Drives', value: stats.activeDrives, icon: Activity, color: 'bg-emerald-100/50 text-emerald-600' },
        { label: 'Total Applicants', value: stats.totalApplicants, icon: Users, color: 'bg-purple-100/50 text-purple-600' },
        { label: 'Shortlisted', value: stats.shortlisted, icon: UserCheck, color: 'bg-indigo-100/50 text-indigo-600' },
    ];

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Header */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Coordinator Dashboard</h1>
                    <p className="text-gray-500 mt-1 text-lg">Overview of placement activities and recruitment drives.</p>
                </div>
                <div className="flex items-center text-sm font-medium text-gray-500 bg-white/60 backdrop-blur-md px-4 py-2 rounded-xl shadow-sm border border-white/50">
                    <Calendar className="mr-2 h-4 w-4 text-indigo-500" />
                    {new Date().toLocaleDateString('en-US', { day: 'numeric', month: 'long', year: 'numeric' })}
                </div>
            </div>

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
                            <div className="h-8 w-16 bg-gray-200/50 rounded mt-2"></div>
                        </div>
                    ))
                ) : (
                    statItems.map((stat) => {
                        const Icon = stat.icon;
                        return (
                            <Card key={stat.label} variant="default" hover={true} className="border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
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
                {/* Recent Drives */}
                <div className="lg:col-span-2">
                    <Card title="Recent Drives" className="h-full border-0 ring-1 ring-black/5 bg-white/60 backdrop-blur-xl">
                        {loading ? (
                            <div className="space-y-4">
                                {[1, 2, 3].map(i => (
                                    <div key={i} className="h-16 w-full bg-gray-50/50 animate-pulse rounded-xl"></div>
                                ))}
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {recentDrives.length > 0 ? (
                                    recentDrives.slice(0, 5).map((drive) => (
                                        <div key={drive.id} className="flex items-center justify-between p-4 bg-white/40 hover:bg-white/60 rounded-2xl border border-transparent hover:border-gray-100 transition-all group">
                                            <div>
                                                <h4 className="font-semibold text-gray-900">{drive.company?.name || 'Company'}</h4>
                                                <p className="text-sm text-gray-500">{drive.jobTitle}</p>
                                            </div>
                                            <div className="flex items-center gap-4">
                                                <div className="text-right hidden sm:block">
                                                    <p className="text-sm font-medium text-gray-900">
                                                        {drive.eligibleDepartments ? drive.eligibleDepartments.length : 0} Depts
                                                    </p>
                                                    <p className="text-xs text-gray-400">
                                                        {new Date(drive.createdAt).toLocaleDateString()}
                                                    </p>
                                                </div>
                                                <Badge variant={drive.status === 'ACTIVE' ? 'success' : 'neutral'}>
                                                    {drive.status}
                                                </Badge>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="text-center py-12 text-gray-500">
                                        <Briefcase className="h-12 w-12 mx-auto text-gray-300 mb-3" />
                                        <p>No drives created yet</p>
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="mt-6 pt-4 border-t border-gray-100/50">
                            <Link
                                href="/dashboard/coordinator/drives"
                                className="flex items-center text-sm font-medium text-indigo-600 hover:text-indigo-800 transition-colors"
                            >
                                View all drives <ArrowRight className="ml-1 h-4 w-4" />
                            </Link>
                        </div>
                    </Card>
                </div>

                {/* Quick Actions */}
                <div className="lg:col-span-1">
                    <Card title="Quick Actions" className="h-full border-0 ring-1 ring-black/5 bg-gradient-to-br from-white/80 to-indigo-50/30 backdrop-blur-xl">
                        {loading ? (
                            <div className="space-y-3">
                                <div className="h-12 w-full bg-gray-200/50 animate-pulse rounded-xl"></div>
                                <div className="h-12 w-full bg-gray-200/50 animate-pulse rounded-xl"></div>
                            </div>
                        ) : (
                            <div className="space-y-3">
                                <Link href="/dashboard/coordinator/drives/create" className="block p-4 bg-white/60 border border-white/60 rounded-2xl shadow-sm hover:shadow-md hover:scale-[1.02] transition-all group relative overflow-hidden">
                                    <div className="absolute top-0 right-0 w-12 h-12 bg-indigo-500/10 rounded-bl-2xl"></div>
                                    <h4 className="font-semibold text-gray-900 group-hover:text-indigo-600 flex items-center gap-3">
                                        <div className="h-8 w-8 bg-indigo-100 rounded-lg flex items-center justify-center text-indigo-600">
                                            <Plus className="h-5 w-5" />
                                        </div>
                                        Create New Drive
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-2 pl-11">Schedule a drive and invite students.</p>
                                </Link>

                                <Link href="/dashboard/coordinator/applicants" className="block p-4 bg-white/60 border border-white/60 rounded-2xl shadow-sm hover:shadow-md hover:scale-[1.02] transition-all group">
                                    <h4 className="font-semibold text-gray-900 group-hover:text-indigo-600 flex items-center gap-3">
                                        <div className="h-8 w-8 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                            <Users className="h-4 w-4" />
                                        </div>
                                        Review Applicants
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-2 pl-11">Check pending applications.</p>
                                </Link>

                                <Link href="/dashboard/coordinator/analytics" className="block p-4 bg-white/60 border border-white/60 rounded-2xl shadow-sm hover:shadow-md hover:scale-[1.02] transition-all group">
                                    <h4 className="font-medium text-gray-900 group-hover:text-indigo-600 flex items-center gap-3">
                                        <div className="h-8 w-8 bg-blue-100 rounded-lg flex items-center justify-center text-blue-600">
                                            <Activity className="h-4 w-4" />
                                        </div>
                                        Generate Reports
                                    </h4>
                                    <p className="text-xs text-gray-500 mt-2 pl-11">Export placement data for college admin.</p>
                                </Link>
                            </div>
                        )}
                    </Card>
                </div>
            </div>
        </div>
    );
}
