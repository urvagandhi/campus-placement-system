'use client';

import Card from '@/components/ui/Card';
import api from '@/services/api';
import { Award, Loader2, TrendingUp, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function AnalyticsPage() {
    const [loading, setLoading] = useState(true);
    const [overview, setOverview] = useState(null);
    const [departmentStats, setDepartmentStats] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [overviewRes, deptRes] = await Promise.all([
                    api.analytics.getOverview(),
                    api.analytics.getDepartmentStats()
                ]);

                if (overviewRes.success) {
                    setOverview(overviewRes.data);
                }
                if (deptRes.success) {
                    setDepartmentStats(deptRes.data || []);
                }
            } catch (error) {
                console.error('Failed to fetch analytics:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96">
                <Loader2 className="h-10 w-10 animate-spin text-indigo-600" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Placement Analytics</h1>
                <p className="text-gray-600 mt-1">Overview of placement performance across departments.</p>
            </div>

            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Total Placed */}
                <Card className="border border-gray-100 shadow-sm hover:shadow-md transition-shadow p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-500 text-sm font-medium mb-1">Total Placed</p>
                            <h3 className="text-3xl font-bold text-gray-900">{overview?.placedStudents || 0}</h3>
                        </div>
                        <div className="h-12 w-12 bg-indigo-50 rounded-full flex items-center justify-center">
                            <Users className="h-6 w-6 text-indigo-600" />
                        </div>
                    </div>
                </Card>

                {/* Placement Rate */}
                <Card className="border border-gray-100 shadow-sm hover:shadow-md transition-shadow p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-500 text-sm font-medium mb-1">Placement Rate</p>
                            <h3 className="text-3xl font-bold text-gray-900">
                                {overview?.placementRate?.toFixed(1) || 0}%
                            </h3>
                        </div>
                        <div className="h-12 w-12 bg-green-50 rounded-full flex items-center justify-center">
                            <Award className="h-6 w-6 text-green-600" />
                        </div>
                    </div>
                </Card>

                {/* Highest Package */}
                <Card className="border border-gray-100 shadow-sm hover:shadow-md transition-shadow p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-gray-500 text-sm font-medium mb-1">Highest Package</p>
                            <div className="flex items-baseline gap-1">
                                <h3 className="text-3xl font-bold text-gray-900">
                                    {overview?.highestPackage?.toFixed(1) || 0}
                                </h3>
                                <span className="text-lg font-semibold text-gray-500">LPA</span>
                            </div>
                        </div>
                        <div className="h-12 w-12 bg-orange-50 rounded-full flex items-center justify-center">
                            <Award className="h-6 w-6 text-orange-600" />
                        </div>
                    </div>
                </Card>
            </div>

            {/* Department Progress */}
            <Card title="Department-wise Placement Status">
                <div className="space-y-6">
                    {departmentStats.length > 0 ? (
                        departmentStats.map((dept) => (
                            <div key={dept.departmentId}>
                                <div className="flex justify-between items-end mb-1">
                                    <div>
                                        <span className="font-semibold text-gray-900 block">{dept.departmentName}</span>
                                        <span className="text-xs text-gray-500">
                                            {dept.placedStudents} / {dept.totalStudents} Placed
                                        </span>
                                    </div>
                                    <span className="font-bold text-indigo-600">
                                        {dept.placementRate?.toFixed(0) || 0}%
                                    </span>
                                </div>
                                <div className="h-2.5 w-full bg-gray-100 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-indigo-600 rounded-full transition-all duration-1000"
                                        style={{ width: `${dept.placementRate || 0}%` }}
                                    ></div>
                                </div>
                            </div>
                        ))
                    ) : (
                        <p className="text-gray-500 text-center py-8">No department statistics available yet.</p>
                    )}
                </div>
            </Card>
        </div>
    );
}
