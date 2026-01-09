'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { drivesApi, applicationsApi, eligibilityApi } from '@/services/api';
import {
    Building2,
    Calendar,
    CheckCircle,
    Clock,
    Filter,
    Loader2,
    MapPin,
    Search,
    TrendingUp,
    XCircle
} from 'lucide-react';
import { useEffect, useState } from 'react';

/**
 * Student Drives Page
 * 
 * Lists all available placement drives with eligibility status.
 * Students can view drives and apply to eligible ones.
 */
export default function StudentDrivesPage() {
    const [drives, setDrives] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [applying, setApplying] = useState(null);

    useEffect(() => {
        fetchDrives();
    }, []);

    const fetchDrives = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await drivesApi.getAll();
            if (response.success) {
                setDrives(response.data || []);
            }
        } catch (err) {
            setError(err.message || 'Failed to load drives');
        } finally {
            setLoading(false);
        }
    };

    const handleApply = async (driveId) => {
        try {
            setApplying(driveId);
            const response = await applicationsApi.apply(driveId);
            if (response.success) {
                // Update the drive to show applied status
                setDrives(prev => prev.map(d =>
                    d.id === driveId ? { ...d, hasApplied: true } : d
                ));
            }
        } catch (err) {
            alert(err.message || 'Failed to apply');
        } finally {
            setApplying(null);
        }
    };

    const filteredDrives = drives.filter(drive => {
        const matchesSearch = drive.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            drive.companyName?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesFilter = filterStatus === 'all' || drive.status === filterStatus;
        return matchesSearch && matchesFilter;
    });

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
            </div>
        );
    }

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Placement Drives</h1>
                <p className="text-gray-600 mt-1">Browse and apply to active placement opportunities</p>
            </div>

            {/* Search and Filter */}
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input
                        type="text"
                        placeholder="Search drives by company or title..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                    />
                </div>
                <div className="flex items-center gap-2">
                    <Filter className="h-5 w-5 text-gray-400" />
                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                    >
                        <option value="all">All Status</option>
                        <option value="UPCOMING">Upcoming</option>
                        <option value="ONGOING">Ongoing</option>
                        <option value="COMPLETED">Completed</option>
                    </select>
                </div>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
                    {error}
                </div>
            )}

            {/* Drives Grid */}
            {filteredDrives.length === 0 ? (
                <div className="text-center py-12">
                    <TrendingUp className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                    <p className="text-gray-500">No drives found matching your criteria</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredDrives.map((drive) => (
                        <DriveCard
                            key={drive.id}
                            drive={drive}
                            onApply={() => handleApply(drive.id)}
                            isApplying={applying === drive.id}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

function DriveCard({ drive, onApply, isApplying }) {
    const statusColors = {
        UPCOMING: 'warning',
        ONGOING: 'success',
        COMPLETED: 'secondary'
    };

    const isEligible = drive.eligibilityStatus !== 'NOT_ELIGIBLE';
    const hasApplied = drive.hasApplied;
    const canApply = isEligible && !hasApplied && drive.status !== 'COMPLETED';

    return (
        <Card className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5 hover:ring-indigo-200 transition-all">
            <div className="space-y-4">
                {/* Company Logo and Status */}
                <div className="flex justify-between items-start">
                    <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-indigo-50 to-white flex items-center justify-center font-bold text-indigo-600 text-lg shadow-sm border border-indigo-50">
                        {drive.companyName?.charAt(0) || 'C'}
                    </div>
                    <Badge variant={statusColors[drive.status] || 'secondary'}>
                        {drive.status}
                    </Badge>
                </div>

                {/* Company and Role */}
                <div>
                    <h3 className="font-semibold text-gray-900 text-lg">{drive.companyName}</h3>
                    <p className="text-gray-600">{drive.title}</p>
                </div>

                {/* Details */}
                <div className="space-y-2 text-sm text-gray-500">
                    <div className="flex items-center gap-2">
                        <Calendar className="h-4 w-4" />
                        <span>{new Date(drive.driveDate).toLocaleDateString()}</span>
                    </div>
                    {drive.location && (
                        <div className="flex items-center gap-2">
                            <MapPin className="h-4 w-4" />
                            <span>{drive.location}</span>
                        </div>
                    )}
                    {drive.package && (
                        <div className="flex items-center gap-2">
                            <TrendingUp className="h-4 w-4" />
                            <span>{drive.package}</span>
                        </div>
                    )}
                </div>

                {/* Eligibility Status */}
                <div className="pt-2 border-t border-gray-100">
                    {isEligible ? (
                        <div className="flex items-center gap-2 text-emerald-600">
                            <CheckCircle className="h-4 w-4" />
                            <span className="text-sm font-medium">You are eligible</span>
                        </div>
                    ) : (
                        <div className="flex items-center gap-2 text-red-500">
                            <XCircle className="h-4 w-4" />
                            <span className="text-sm font-medium">Not eligible</span>
                        </div>
                    )}
                </div>

                {/* Apply Button */}
                <div>
                    {hasApplied ? (
                        <Button variant="outline" disabled className="w-full">
                            <CheckCircle className="h-4 w-4 mr-2" /> Applied
                        </Button>
                    ) : canApply ? (
                        <Button onClick={onApply} disabled={isApplying} className="w-full">
                            {isApplying ? (
                                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                            ) : null}
                            Apply Now
                        </Button>
                    ) : (
                        <Button variant="outline" disabled className="w-full">
                            {drive.status === 'COMPLETED' ? 'Drive Completed' : 'Not Eligible'}
                        </Button>
                    )}
                </div>
            </div>
        </Card>
    );
}
