'use client';

import { exportAuditLogs, getSecurityStats } from '@/services/authService';
import {
    Activity,
    AlertTriangle,
    Download,
    Fingerprint,
    RefreshCw,
    RotateCcw,
    ShieldAlert,
    ShieldCheck,
    Users
} from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Skeleton, StatsSkeleton, ListSkeleton } from '@/components/ui/Skeleton';

/**
 * Security Operations Center Dashboard
 * Restricted to SUPER_ADMIN only.
 * Provides real-time security monitoring, anomaly detection, and audit log export.
 */
export default function SecurityDashboard() {
    const [stats, setStats] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isExporting, setIsExporting] = useState(false);
    const [error, setError] = useState('');

    const fetchData = async () => {
        try {
            setIsLoading(true);
            setError('');
            const data = await getSecurityStats();
            setStats(data);
        } catch (err) {
            setError(err.message || 'Failed to fetch security analytics.');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 60000); // Auto refresh every minute
        return () => clearInterval(interval);
    }, []);

    const handleExport = async () => {
        try {
            setIsExporting(true);
            await exportAuditLogs();
            toast.success('Security audit logs exported successfully');
        } catch (err) {
            console.error(err);
            toast.error(err.message || 'Failed to export audit logs');
        } finally {
            setIsExporting(false);
        }
    };

    if (isLoading && !stats) {
        return (
            <div className="space-y-8 animate-pulse">
                <div className="flex justify-between items-center mb-10">
                    <div className="space-y-3">
                        <Skeleton className="h-10 w-64" />
                        <Skeleton className="h-4 w-96" />
                    </div>
                    <Skeleton className="h-10 w-40 rounded-xl" />
                </div>
                <StatsSkeleton />
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    <div className="lg:col-span-2 space-y-4">
                        <Skeleton className="h-8 w-48 mb-4" />
                        <div className="border border-gray-100 rounded-2xl p-6 bg-white">
                            <ListSkeleton count={6} />
                        </div>
                    </div>
                    <div className="space-y-4">
                        <Skeleton className="h-8 w-32 mb-4" />
                        <div className="border border-gray-100 rounded-2xl p-6 bg-white space-y-6">
                            {[1, 2, 3].map(i => (
                                <div key={i} className="space-y-2">
                                    <Skeleton className="h-3 w-24" />
                                    <Skeleton className="h-2 w-full" />
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center h-96 text-center">
                <div className="bg-red-50 p-4 rounded-full mb-4">
                    <ShieldAlert className="h-10 w-10 text-red-600" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">Access Denied or Data Unavailable</h3>
                <p className="text-gray-500 max-w-md mb-6">{error}</p>
                <button
                    onClick={fetchData}
                    className="px-6 py-2 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition"
                >
                    Retry
                </button>
            </div>
        );
    }

    if (!stats) return null;

    const StatCard = ({ title, value, icon: Icon, color, trend }) => (
        <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm transition-hover hover:shadow-md">
            <div className="flex items-center justify-between mb-4">
                <div className={`p-3 rounded-xl ${color}`}>
                    <Icon className="h-6 w-6" />
                </div>
                {trend && (
                    <span className={`text-xs font-bold px-2 py-1 rounded-full ${trend > 0 ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'}`}>
                        {trend > 0 ? '+' : ''}{trend}%
                    </span>
                )}
            </div>
            <h3 className="text-sm font-medium text-gray-500">{title}</h3>
            <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
        </div>
    );

    return (
        <div className="space-y-8 animate-fade-in">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight flex items-center gap-3">
                        <ShieldCheck className="h-8 w-8 text-indigo-600" />
                        Security Operations Center
                    </h1>
                    <p className="mt-2 text-sm text-gray-500 max-w-2xl">
                        Real-time threat monitoring, anomaly detection, and security audit command center.
                    </p>
                </div>
                <div className="flex items-center gap-3">
                    <button
                        onClick={fetchData}
                        className="p-2 text-gray-500 hover:bg-gray-100 rounded-lg transition-colors border border-gray-200"
                        title="Refresh Data"
                    >
                        <RefreshCw className={`h-5 w-5 ${isLoading ? 'animate-spin' : ''}`} />
                    </button>
                    <button
                        onClick={handleExport}
                        disabled={isExporting}
                        className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition-all font-semibold shadow-sm shadow-indigo-200 disabled:opacity-50"
                    >
                        {isExporting ? <RefreshCw className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
                        Export Audit Logs
                    </button>
                </div>
            </div>

            {/* Quick Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard
                    title="Logins (24h)"
                    value={stats.totalLogins24h}
                    icon={Users}
                    color="bg-blue-50 text-blue-600"
                />
                <StatCard
                    title="Failed Attempts"
                    value={stats.failedLogins24h}
                    icon={ShieldAlert}
                    color="bg-amber-50 text-amber-600"
                    trend={stats.failureRate > 5 ? stats.failureRate : null}
                />
                <StatCard
                    title="Token Reuse Detections"
                    value={stats.tokenReuseAttempts24h}
                    icon={RotateCcw}
                    color="bg-red-50 text-red-600"
                />
                <StatCard
                    title="Device Alerts"
                    value={stats.unauthorizedDevices24h}
                    icon={Fingerprint}
                    color="bg-purple-50 text-purple-600"
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Recent Anomalies Table */}
                <div className="lg:col-span-2 space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                            <AlertTriangle className="h-5 w-5 text-amber-500" />
                            Recent Anomalies & Alerts
                        </h2>
                    </div>

                    <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden shadow-sm">
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead className="bg-gray-50 border-b border-gray-100 uppercase text-[10px] font-bold text-gray-500 tracking-wider">
                                    <tr>
                                        <th className="px-6 py-4">Time</th>
                                        <th className="px-6 py-4">Event Type</th>
                                        <th className="px-6 py-4">Email</th>
                                        <th className="px-6 py-4">IP Address</th>
                                        <th className="px-6 py-4 text-right">Status</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-50">
                                    {stats.recentAnomalies?.map((log) => (
                                        <tr key={log.id} className="hover:bg-gray-50/50 transition-colors">
                                            <td className="px-6 py-4 whitespace-nowrap text-gray-600">
                                                {new Date(log.loginTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                            </td>
                                            <td className="px-6 py-4">
                                                <div className="flex flex-col">
                                                    <span className="font-semibold text-gray-900">{log.eventType.replace(/_/g, ' ')}</span>
                                                    <span className="text-[10px] text-gray-400 truncate max-w-[200px]">{log.failureReason}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 text-gray-900 font-medium">{log.email}</td>
                                            <td className="px-6 py-4 text-gray-500 font-mono">{log.ipAddress}</td>
                                            <td className="px-6 py-4 text-right">
                                                <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${log.success ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                                                    }`}>
                                                    {log.success ? 'Success' : 'Detected'}
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                    {(!stats.recentAnomalies || stats.recentAnomalies.length === 0) && (
                                        <tr>
                                            <td colSpan="5" className="px-6 py-10 text-center text-gray-400 italic">
                                                No anomalies detected in the last window.
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Event Distribution Summary */}
                <div className="space-y-4">
                    <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                        <Activity className="h-5 w-5 text-indigo-500" />
                        Distribution
                    </h2>
                    <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm space-y-6">
                        {Object.entries(stats.eventsByType || {})
                            .filter(([_, count]) => count > 0)
                            .sort((a, b) => b[1] - a[1])
                            .map(([type, count]) => (
                                <div key={type} className="space-y-2">
                                    <div className="flex justify-between text-xs font-bold uppercase tracking-wider">
                                        <span className="text-gray-500">{type.replace(/_/g, ' ')}</span>
                                        <span className="text-gray-900">{count}</span>
                                    </div>
                                    <div className="w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
                                        <div
                                            className="bg-indigo-600 h-full rounded-full transition-all duration-500"
                                            style={{ width: `${Math.min(100, (count / stats.totalLogins24h) * 100 || 0)}%` }}
                                        />
                                    </div>
                                </div>
                            ))}
                    </div>

                    <div className="p-4 bg-indigo-50 border border-indigo-100 rounded-xl">
                        <div className="flex gap-3">
                            <ShieldCheck className="h-5 w-5 text-indigo-600 flex-shrink-0" />
                            <p className="text-[12px] text-indigo-800 leading-relaxed">
                                <strong>System Pulse:</strong> Anomaly detection engine is running. Geofencing and device binding are active for all sessions.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
