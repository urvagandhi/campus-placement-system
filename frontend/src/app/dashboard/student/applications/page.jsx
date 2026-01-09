'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { applicationsApi } from '@/services/api';
import {
    AlertCircle,
    Building2,
    Calendar,
    CheckCircle,
    Clock,
    FileText,
    Loader2,
    XCircle
} from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

/**
 * Student Applications Page
 * 
 * Shows all applications submitted by the student with status tracking.
 * Applications are read-only - status is updated by coordinators.
 */
export default function StudentApplicationsPage() {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [withdrawing, setWithdrawing] = useState(null);

    useEffect(() => {
        fetchApplications();
    }, []);

    const fetchApplications = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await applicationsApi.getMyApplications();
            if (response.success) {
                setApplications(response.data || []);
            }
        } catch (err) {
            setError(err.message || 'Failed to load applications');
        } finally {
            setLoading(false);
        }
    };

    const handleWithdraw = async (applicationId) => {
        if (!confirm('Are you sure you want to withdraw this application?')) {
            return;
        }

        try {
            setWithdrawing(applicationId);
            const response = await applicationsApi.withdraw(applicationId);
            if (response.success) {
                setApplications(prev => prev.filter(a => a.id !== applicationId));
            }
        } catch (err) {
            alert(err.message || 'Failed to withdraw application');
        } finally {
            setWithdrawing(null);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
            </div>
        );
    }

    // Group applications by status
    const activeApplications = applications.filter(a =>
        ['PENDING', 'UNDER_REVIEW', 'SHORTLISTED'].includes(a.status)
    );
    const completedApplications = applications.filter(a =>
        ['SELECTED', 'REJECTED', 'WITHDRAWN'].includes(a.status)
    );

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900 tracking-tight">My Applications</h1>
                <p className="text-gray-600 mt-1">Track your placement application status</p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <StatCard
                    label="Total Applied"
                    value={applications.length}
                    icon={FileText}
                    color="indigo"
                />
                <StatCard
                    label="Pending"
                    value={applications.filter(a => a.status === 'PENDING').length}
                    icon={Clock}
                    color="amber"
                />
                <StatCard
                    label="Shortlisted"
                    value={applications.filter(a => a.status === 'SHORTLISTED').length}
                    icon={CheckCircle}
                    color="emerald"
                />
                <StatCard
                    label="Selected"
                    value={applications.filter(a => a.status === 'SELECTED').length}
                    icon={CheckCircle}
                    color="green"
                />
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl flex items-center gap-2">
                    <AlertCircle className="h-5 w-5" />
                    {error}
                </div>
            )}

            {applications.length === 0 ? (
                <Card className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5 text-center py-12">
                    <FileText className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                    <p className="text-gray-500 mb-4">You haven't applied to any drives yet</p>
                    <Link href="/dashboard/student/drives">
                        <Button>Browse Drives</Button>
                    </Link>
                </Card>
            ) : (
                <>
                    {/* Active Applications */}
                    {activeApplications.length > 0 && (
                        <div>
                            <h2 className="text-xl font-semibold text-gray-900 mb-4">Active Applications</h2>
                            <div className="space-y-4">
                                {activeApplications.map(app => (
                                    <ApplicationCard
                                        key={app.id}
                                        application={app}
                                        onWithdraw={() => handleWithdraw(app.id)}
                                        isWithdrawing={withdrawing === app.id}
                                    />
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Completed Applications */}
                    {completedApplications.length > 0 && (
                        <div>
                            <h2 className="text-xl font-semibold text-gray-900 mb-4">Past Applications</h2>
                            <div className="space-y-4">
                                {completedApplications.map(app => (
                                    <ApplicationCard
                                        key={app.id}
                                        application={app}
                                        isCompleted
                                    />
                                ))}
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

function StatCard({ label, value, icon: Icon, color }) {
    const colorClasses = {
        indigo: 'bg-indigo-100/50 text-indigo-600',
        amber: 'bg-amber-100/50 text-amber-600',
        emerald: 'bg-emerald-100/50 text-emerald-600',
        green: 'bg-green-100/50 text-green-600'
    };

    return (
        <div className="bg-white/60 backdrop-blur-xl rounded-2xl p-4 ring-1 ring-black/5">
            <div className="flex items-center gap-3">
                <div className={`p-2 rounded-xl ${colorClasses[color]}`}>
                    <Icon className="h-5 w-5" />
                </div>
                <div>
                    <p className="text-2xl font-bold text-gray-900">{value}</p>
                    <p className="text-sm text-gray-500">{label}</p>
                </div>
            </div>
        </div>
    );
}

function ApplicationCard({ application, onWithdraw, isWithdrawing, isCompleted }) {
    const statusConfig = {
        PENDING: { variant: 'warning', icon: Clock, label: 'Pending Review' },
        UNDER_REVIEW: { variant: 'primary', icon: FileText, label: 'Under Review' },
        SHORTLISTED: { variant: 'success', icon: CheckCircle, label: 'Shortlisted' },
        SELECTED: { variant: 'success', icon: CheckCircle, label: 'Selected' },
        REJECTED: { variant: 'error', icon: XCircle, label: 'Not Selected' },
        WITHDRAWN: { variant: 'secondary', icon: XCircle, label: 'Withdrawn' }
    };

    const status = statusConfig[application.status] || statusConfig.PENDING;
    const StatusIcon = status.icon;
    const canWithdraw = ['PENDING', 'UNDER_REVIEW'].includes(application.status);

    return (
        <Card className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-gray-100 to-white flex items-center justify-center font-bold text-gray-600 text-lg shadow-sm border border-gray-100">
                        {application.companyName?.charAt(0) || 'C'}
                    </div>
                    <div>
                        <h3 className="font-semibold text-gray-900">{application.companyName}</h3>
                        <p className="text-gray-600">{application.driveTitle}</p>
                        <div className="flex items-center gap-3 mt-2 text-sm text-gray-500">
                            <span className="flex items-center gap-1">
                                <Calendar className="h-4 w-4" />
                                Applied {new Date(application.appliedAt).toLocaleDateString()}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <Badge variant={status.variant} size="lg">
                        <StatusIcon className="h-3 w-3 mr-1" />
                        {status.label}
                    </Badge>

                    {canWithdraw && !isCompleted && (
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={onWithdraw}
                            disabled={isWithdrawing}
                            className="text-red-600 border-red-200 hover:bg-red-50"
                        >
                            {isWithdrawing ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                'Withdraw'
                            )}
                        </Button>
                    )}
                </div>
            </div>
        </Card>
    );
}
