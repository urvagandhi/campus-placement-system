'use client';

import Card from '@/components/ui/Card';
import { Award, TrendingUp, Users } from 'lucide-react';
import { useEffect, useState } from 'react';

export default function AnalyticsPage() {
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => setLoading(false), 1200);
        return () => clearTimeout(timer);
    }, []);

    // Mock Data for visualization
    const DEPT_STATS = [
        { name: 'CSE', placed: 45, total: 60, percentage: 75 },
        { name: 'IT', placed: 38, total: 55, percentage: 69 },
        { name: 'ECE', placed: 30, total: 50, percentage: 60 },
        { name: 'Civ', placed: 15, total: 40, percentage: 37 },
        { name: 'Mec', placed: 20, total: 45, percentage: 44 },
    ];

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Placement Analytics</h1>
                <p className="text-gray-600 mt-1">Overview of placement performance across departments.</p>
            </div>

            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 animate-pulse">
                    {[1, 2, 3].map(i => (
                        <div key={i} className="h-32 bg-gray-200 rounded-xl"></div>
                    ))}
                    <div className="md:col-span-3 h-80 bg-gray-200 rounded-xl"></div>
                </div>
            ) : (
                <>
                    {/* Summary Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        {/* Total Placed */}
                        <Card className="border border-gray-100 shadow-sm hover:shadow-md transition-shadow p-6">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-gray-500 text-sm font-medium mb-1">Total Placed</p>
                                    <h3 className="text-3xl font-bold text-gray-900">148</h3>
                                    <p className="text-xs text-green-600 flex items-center mt-1">
                                        {/* <TrendingUp className="h-3 w-3 mr-1" /> +12% from last year */}
                                    </p>
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
                                    <h3 className="text-3xl font-bold text-gray-900">65%</h3>
                                    <p className="text-xs text-green-600 flex items-center mt-1">
                                        {/* <TrendingUp className="h-3 w-3 mr-1" /> +5% growth */}
                                    </p>
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
                                        <h3 className="text-3xl font-bold text-gray-900">22</h3>
                                        <span className="text-lg font-semibold text-gray-500">LPA</span>
                                    </div>
                                    <p className="text-xs text-gray-400 mt-1">Offered by Google</p>
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
                            {DEPT_STATS.map((dept) => (
                                <div key={dept.name}>
                                    <div className="flex justify-between items-end mb-1">
                                        <div>
                                            <span className="font-semibold text-gray-900 block">{dept.name}</span>
                                            <span className="text-xs text-gray-500">{dept.placed} / {dept.total} Placed</span>
                                        </div>
                                        <span className="font-bold text-indigo-600">{dept.percentage}%</span>
                                    </div>
                                    <div className="h-2.5 w-full bg-gray-100 rounded-full overflow-hidden">
                                        <div
                                            className="h-full bg-indigo-600 rounded-full transition-all duration-1000"
                                            style={{ width: `${dept.percentage}%` }}
                                        ></div>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <div className="mt-8 pt-4 border-t border-gray-100">
                            <p className="text-xs text-center text-gray-400">
                                * Data is illustrative for UI demonstration purposes. No AI predictions applied.
                            </p>
                        </div>
                    </Card>
                </>
            )}
        </div>
    );
}
