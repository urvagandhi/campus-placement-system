'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import api from '@/services/api';
import { AlertTriangle, BarChart, Loader2, TrendingUp } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function CareerInsightsPage() {
    const [loading, setLoading] = useState(true);
    const [departmentStats, setDepartmentStats] = useState([]);
    const [overview, setOverview] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [deptRes, overviewRes] = await Promise.all([
                    api.analytics.getDepartmentStats(),
                    api.analytics.getOverview()
                ]);

                if (deptRes.success) {
                    setDepartmentStats(deptRes.data || []);
                }
                if (overviewRes.success) {
                    setOverview(overviewRes.data);
                }
            } catch (error) {
                console.error('Failed to fetch insights:', error);
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
                <h1 className="text-2xl font-bold text-gray-900">Career Insights</h1>
                <div className="flex items-center gap-2 mt-1">
                    <Badge variant="neutral" className="bg-gray-100 text-gray-600 border-gray-200">
                        Historical Data
                    </Badge>
                    <span className="text-gray-500 text-sm">
                        Placement statistics and trends from your college.
                    </span>
                </div>
            </div>

            {/* Disclaimer Alert */}
            <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 flex items-start gap-3">
                <AlertTriangle className="h-5 w-5 text-blue-600 mt-0.5" />
                <div>
                    <h4 className="text-sm font-semibold text-blue-900">Important Note</h4>
                    <p className="text-sm text-blue-700 mt-1">
                        These insights are based on historical data from previous placements.
                        They are descriptive statistics and <strong>not</strong> predictions of your individual placement chances.
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Department Placement Trends */}
                <Card className="border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <TrendingUp className="h-4 w-4 text-indigo-500" />
                            Department Placement Trends
                        </h3>
                    </div>

                    <div className="space-y-4">
                        {departmentStats.length > 0 ? (
                            departmentStats.map((dept) => (
                                <div key={dept.departmentId}>
                                    <div className="flex justify-between text-sm mb-1">
                                        <span className="text-gray-600 font-medium">{dept.departmentName}</span>
                                        <span className="text-gray-900 font-bold">
                                            {dept.placementRate?.toFixed(0) || 0}%
                                        </span>
                                    </div>
                                    <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-indigo-500 rounded-full"
                                            style={{ width: `${dept.placementRate || 0}%` }}
                                        ></div>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-gray-500 text-center py-4">No department data available</p>
                        )}
                    </div>
                    <p className="text-xs text-gray-400 mt-4 text-right">Based on current placement data</p>
                </Card>

                {/* Package Statistics */}
                <Card className="border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <BarChart className="h-4 w-4 text-green-500" />
                            Package Statistics
                        </h3>
                    </div>

                    <div className="space-y-5">
                        {overview ? (
                            <>
                                <div>
                                    <div className="flex justify-between text-sm mb-1">
                                        <span className="text-gray-600 font-medium">Highest Package</span>
                                        <span className="text-gray-900 font-bold">{overview.highestPackage?.toFixed(1) || 0} LPA</span>
                                    </div>
                                    <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                        <div className="h-full bg-green-500 rounded-full" style={{ width: '100%' }}></div>
                                    </div>
                                </div>
                                <div>
                                    <div className="flex justify-between text-sm mb-1">
                                        <span className="text-gray-600 font-medium">Average Package</span>
                                        <span className="text-gray-900 font-bold">{overview.averagePackage?.toFixed(1) || 0} LPA</span>
                                    </div>
                                    <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-emerald-500 rounded-full"
                                            style={{ width: overview.highestPackage > 0 ? `${(overview.averagePackage / overview.highestPackage) * 100}%` : '0%' }}
                                        ></div>
                                    </div>
                                </div>
                                <div>
                                    <div className="flex justify-between text-sm mb-1">
                                        <span className="text-gray-600 font-medium">Lowest Package</span>
                                        <span className="text-gray-900 font-bold">{overview.lowestPackage?.toFixed(1) || 0} LPA</span>
                                    </div>
                                    <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-teal-500 rounded-full"
                                            style={{ width: overview.highestPackage > 0 ? `${(overview.lowestPackage / overview.highestPackage) * 100}%` : '0%' }}
                                        ></div>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <p className="text-gray-500 text-center py-4">No package data available</p>
                        )}
                    </div>
                    <p className="text-xs text-gray-400 mt-4 text-right">Based on verified offers</p>
                </Card>
            </div>
        </div>
    );
}
