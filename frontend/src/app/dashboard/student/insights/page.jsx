'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import { AlertTriangle, BarChart, TrendingUp } from 'lucide-react';

export default function CareerInsightsPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Career Insights</h1>
                <div className="flex items-center gap-2 mt-1">
                    <Badge variant="neutral" className="bg-gray-100 text-gray-600 border-gray-200">
                        Mock / Descriptive Data
                    </Badge>
                    <span className="text-gray-500 text-sm">
                        Historical placement statistics and market trends.
                    </span>
                </div>
            </div>

            {/* Disclaimer Alert */}
            <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 flex items-start gap-3">
                <AlertTriangle className="h-5 w-5 text-blue-600 mt-0.5" />
                <div>
                    <h4 className="text-sm font-semibold text-blue-900">Important Note</h4>
                    <p className="text-sm text-blue-700 mt-1">
                        These insights are based on historical data from previous years.
                        They are descriptive statistics and <strong>not</strong> AI-generated predictions of your future placement chances.
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Placement Trends Chart (Mock Visual) */}
                <Card className="border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <TrendingUp className="h-4 w-4 text-indigo-500" />
                            Department Placement Trends
                        </h3>
                    </div>

                    {/* CSS Bar Chart Construction */}
                    <div className="space-y-4">
                        {[
                            { label: 'Computer Science', val: '92%', width: '92%' },
                            { label: 'Information Tech', val: '88%', width: '88%' },
                            { label: 'Electronics', val: '75%', width: '75%' },
                            { label: 'Mechanical', val: '68%', width: '68%' },
                            { label: 'Civil', val: '60%', width: '60%' },
                        ].map((track, i) => (
                            <div key={i}>
                                <div className="flex justify-between text-sm mb-1">
                                    <span className="text-gray-600 font-medium">{track.label}</span>
                                    <span className="text-gray-900 font-bold">{track.val}</span>
                                </div>
                                <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                    <div
                                        className="h-full bg-indigo-500 rounded-full"
                                        style={{ width: track.width }}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                    <p className="text-xs text-gray-400 mt-4 text-right">Based on 2023 Batch Data</p>
                </Card>

                {/* Avg Package Stats (Mock Visual) */}
                <Card className="border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <BarChart className="h-4 w-4 text-green-500" />
                            Average Package by Role
                        </h3>
                    </div>

                    <div className="space-y-5">
                        {[
                            { label: 'SDE / Developer', val: '12.5 LPA', width: '85%', color: 'bg-green-500' },
                            { label: 'Data Analyst', val: '9.0 LPA', width: '65%', color: 'bg-emerald-500' },
                            { label: 'QA / Testing', val: '6.5 LPA', width: '45%', color: 'bg-teal-500' },
                            { label: 'Core / Hardware', val: '7.2 LPA', width: '50%', color: 'bg-green-600' },
                        ].map((track, i) => (
                            <div key={i}>
                                <div className="flex justify-between text-sm mb-1">
                                    <span className="text-gray-600 font-medium">{track.label}</span>
                                    <span className="text-gray-900 font-bold">{track.val}</span>
                                </div>
                                <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                                    <div
                                        className={`h-full rounded-full ${track.color}`}
                                        style={{ width: track.width }}
                                    ></div>
                                </div>
                            </div>
                        ))}
                    </div>
                    <p className="text-xs text-gray-400 mt-4 text-right">Based on Verified Offers</p>
                </Card>
            </div>
        </div>
    );
}
