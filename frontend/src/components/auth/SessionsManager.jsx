'use client';

import { getActiveSessions, revokeSession } from '@/services/authService';
import { Laptop, LogOut, Monitor, Shield, Smartphone, Tablet } from 'lucide-react';
import { useEffect, useState } from 'react';

/**
 * SessionsManager Component
 *
 * Displays and manages active user sessions/devices.
 * Allows users to view where they are logged in and revoke specific sessions.
 */
export default function SessionsManager() {
    const [sessions, setSessions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [revokingId, setRevokingId] = useState(null);

    const fetchSessions = async () => {
        try {
            setIsLoading(true);
            const data = await getActiveSessions();
            setSessions(data);
            setError('');
        } catch (err) {
            setError('Failed to load active sessions.');
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchSessions();
    }, []);

    const handleRevoke = async (sessionId) => {
        if (!window.confirm('Are you sure you want to terminate this session? You will be logged out on that device.')) {
            return;
        }

        try {
            setRevokingId(sessionId);
            await revokeSession(sessionId);
            // Refresh list
            await fetchSessions();
        } catch (err) {
            alert(err.message || 'Failed to revoke session');
        } finally {
            setRevokingId(null);
        }
    };

    const getDeviceIcon = (userAgent) => {
        const ua = userAgent.toLowerCase();
        if (ua.includes('mobi')) return Smartphone;
        if (ua.includes('tablet') || ua.includes('ipad')) return Tablet;
        if (ua.includes('macintosh') || ua.includes('windows') || ua.includes('linux')) return Monitor;
        return Laptop;
    };

    const formatUA = (userAgent) => {
        // Simple UA parser logic
        if (userAgent.includes('Windows')) return 'Windows PC';
        if (userAgent.includes('Macintosh')) return 'MacBook / iMac';
        if (userAgent.includes('iPhone')) return 'iPhone';
        if (userAgent.includes('Android')) return 'Android Device';
        if (userAgent.includes('Linux')) return 'Linux System';
        return 'Generic Device';
    };

    const getBrowser = (userAgent) => {
        if (userAgent.includes('Edg/')) return 'Edge';
        if (userAgent.includes('Chrome/')) return 'Chrome';
        if (userAgent.includes('Safari/') && !userAgent.includes('Chrome/')) return 'Safari';
        if (userAgent.includes('Firefox/')) return 'Firefox';
        return 'Web Browser';
    };

    if (isLoading && sessions.length === 0) {
        return (
            <div className="flex justify-center items-center py-20">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between mb-2">
                <div>
                    <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                        <Shield className="h-5 w-5 text-indigo-600" />
                        Active Sessions
                    </h2>
                    <p className="text-sm text-gray-500 mt-1">
                        Manage your active logins across multiple devices and browsers.
                    </p>
                </div>
                <button
                    onClick={fetchSessions}
                    className="text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                    Refresh
                </button>
            </div>

            {error && (
                <div className="p-4 bg-red-50 border border-red-100 rounded-xl text-sm text-red-600">
                    {error}
                </div>
            )}

            <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-sm">
                <ul className="divide-y divide-gray-100">
                    {sessions.map((session) => {
                        const Icon = getDeviceIcon(session.userAgent);
                        return (
                            <li key={session.id} className={`p-5 flex items-center justify-between transition-colors ${session.isCurrent ? 'bg-indigo-50/30' : 'hover:bg-gray-50'}`}>
                                <div className="flex items-center gap-4">
                                    <div className={`p-3 rounded-xl ${session.isCurrent ? 'bg-indigo-100 text-indigo-600' : 'bg-gray-100 text-gray-500'}`}>
                                        <Icon className="h-6 w-6" />
                                    </div>
                                    <div>
                                        <div className="flex items-center gap-2">
                                            <span className="font-semibold text-gray-900">
                                                {formatUA(session.userAgent)}
                                            </span>
                                            {session.isCurrent && (
                                                <span className="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-bold rounded-full uppercase tracking-wider">
                                                    Current Device
                                                </span>
                                            )}
                                        </div>
                                        <div className="text-sm text-gray-500 flex flex-wrap gap-x-3 gap-y-1 mt-0.5">
                                            <span>{getBrowser(session.userAgent)}</span>
                                            <span>•</span>
                                            <span>{session.ipAddress}</span>
                                            <span>•</span>
                                            <span>
                                                Last active: {new Date(session.lastActiveAt).toLocaleString([], {
                                                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                                                })}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {!session.isCurrent && (
                                    <button
                                        onClick={() => handleRevoke(session.id)}
                                        disabled={revokingId === session.id}
                                        className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all disabled:opacity-50"
                                        title="Terminate Session"
                                    >
                                        {revokingId === session.id ? (
                                            <div className="h-5 w-5 border-2 border-red-600 border-t-transparent animate-spin rounded-full" />
                                        ) : (
                                            <LogOut className="h-5 w-5" />
                                        )}
                                    </button>
                                )}
                            </li>
                        );
                    })}
                </ul>
            </div>

            <div className="p-4 bg-amber-50 border border-amber-100 rounded-xl">
                <div className="flex gap-3">
                    <Shield className="h-5 w-5 text-amber-600 flex-shrink-0" />
                    <p className="text-sm text-amber-800">
                        <strong>Security Tip:</strong> If you don't recognize a device or location, terminate the session immediately and change your password.
                    </p>
                </div>
            </div>
        </div>
    );
}
