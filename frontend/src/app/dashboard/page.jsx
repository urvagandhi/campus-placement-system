'use client';

import Card from '@/components/ui/Card';

/**
 * Dashboard page - Landing for /dashboard
 */
export default function DashboardPage() {
    return (
        <div className="min-h-screen flex flex-col bg-gray-50">
            <main className="flex-grow py-8">
                <div className="max-w-7xl mx-auto px-4">
                    <h1 className="text-2xl font-bold text-gray-900 mb-6">
                        Dashboard Overview
                    </h1>

                    {/* Quick Stats */}
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                        <Card className="bg-blue-50">
                            <div className="text-center">
                                <div className="text-3xl font-bold text-blue-600">--</div>
                                <div className="text-sm text-gray-600 mt-1">Total Students</div>
                            </div>
                        </Card>

                        <Card className="bg-green-50">
                            <div className="text-center">
                                <div className="text-3xl font-bold text-green-600">--</div>
                                <div className="text-sm text-gray-600 mt-1">Active Drives</div>
                            </div>
                        </Card>

                        <Card className="bg-purple-50">
                            <div className="text-center">
                                <div className="text-3xl font-bold text-purple-600">--</div>
                                <div className="text-sm text-gray-600 mt-1">Companies</div>
                            </div>
                        </Card>

                        <Card className="bg-orange-50">
                            <div className="text-center">
                                <div className="text-3xl font-bold text-orange-600">--%</div>
                                <div className="text-sm text-gray-600 mt-1">Placement Rate</div>
                            </div>
                        </Card>
                    </div>

                    {/* Placeholder Content */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <Card title="Upcoming Drives" subtitle="Recent placement opportunities">
                            <div className="text-center py-8 text-gray-500">
                                {/* TODO: Fetch and display upcoming drives */}
                                <p>No upcoming drives</p>
                                <p className="text-sm mt-2">Connect to backend to see data</p>
                            </div>
                        </Card>

                        <Card title="Recent Applications" subtitle="Latest student applications">
                            <div className="text-center py-8 text-gray-500">
                                {/* TODO: Fetch and display recent applications */}
                                <p>No recent applications</p>
                                <p className="text-sm mt-2">Connect to backend to see data</p>
                            </div>
                        </Card>
                    </div>
                </div>
            </main>
        </div>
    );
}
