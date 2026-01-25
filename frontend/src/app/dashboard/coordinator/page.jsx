'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import { analyticsApi, drivesApi } from '@/services/api';
import { Activity, ArrowRight, Briefcase, Calendar, Plus, UserCheck, Users } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import Input from '@/components/ui/Input';
import api from '@/services/api';

export default function CoordinatorDashboard() {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        totalDrives: 0,
        activeDrives: 0,
        totalApplicants: 0,
        shortlisted: 0
    });
    const [recentDrives, setRecentDrives] = useState([]);
    const [events, setEvents] = useState([]);
    const [showEventModal, setShowEventModal] = useState(false);
    const [departments, setDepartments] = useState([]);
    
    // Form State for New Event
    const [newEvent, setNewEvent] = useState({
        name: '',
        eventType: 'EXAM',
        startDate: '',
        endDate: '',
        description: '',
        organizationUnitId: ''
    });

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [analyticsResponse, drivesResponse, eventsResponse, hierarchyResponse] = await Promise.all([
                    analyticsApi.getOverview().catch(err => ({ data: {} })),
                    drivesApi.getAll().catch(err => ({ data: { content: [] } })),
                    api.coordinator.getEvents().catch(err => ({ data: [] })),
                    api.organizations.getHierarchy().catch(err => ({ data: null }))
                ]);

                // Update stats
                const analytics = analyticsResponse.data || {};
                setStats({
                    totalDrives: analytics.totalDrives || 0,
                    activeDrives: analytics.activeDrives || 0,
                    totalApplicants: analytics.totalApplications || 0,
                    shortlisted: analytics.shortlistedCount || 0
                });

                // Recent drives
                const drivesList = drivesResponse.data?.content || (Array.isArray(drivesResponse.data) ? drivesResponse.data : []) || [];
                setRecentDrives(drivesList);

                // Events
                setEvents(eventsResponse.data || []);

                // Departments from Hierarchy
                if (hierarchyResponse.data) {
                    setDepartments(getDepartmentsFromHierarchy(hierarchyResponse.data));
                }

            } catch (error) {
                console.error("Dashboard data fetch error:", error);
                toast.error("Failed to load dashboard data");
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const getDepartmentsFromHierarchy = (node) => {
        if (!node) return [];
        let depts = [];
        if (node.type === 'DEPARTMENT') {
            depts.push({ id: node.id, name: node.name });
        }
        if (node.children) {
            node.children.forEach(child => {
                depts = [...depts, ...getDepartmentsFromHierarchy(child)];
            });
        }
        return depts;
    };

    const handleCreateEvent = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newEvent,
                organizationUnitId: parseInt(newEvent.organizationUnitId)
            };
            const response = await api.coordinator.createEvent(payload);
            if (response.success) {
                toast.success('Academic event scheduled');
                setEvents(prev => [...prev, response.data]);
                setShowEventModal(false);
                setNewEvent({ name: '', eventType: 'EXAM', startDate: '', endDate: '', description: '', organizationUnitId: '' });
            } else {
                toast.error(response.message || 'Failed to schedule event');
            }
        } catch (error) {
            console.error('Create event error:', error);
            toast.error(error.message || 'Failed to schedule event');
        }
    };

    const statItems = [
        { label: 'Total Drives', value: stats.totalDrives, icon: Briefcase, color: 'bg-blue-100/50 text-blue-600' },
        { label: 'Active Drives', value: stats.activeDrives, icon: Activity, color: 'bg-emerald-100/50 text-emerald-600' },
        { label: 'Total Applicants', value: stats.totalApplicants, icon: Users, color: 'bg-purple-100/50 text-purple-600' },
        { label: 'Shortlisted', value: stats.shortlisted, icon: UserCheck, color: 'bg-indigo-100/50 text-indigo-600' },
    ];

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    };

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
            
            {/* Academic Calendar Section */}
            <div>
                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="text-xl font-bold text-gray-900">Academic Calendar</h2>
                        <p className="text-gray-500 text-sm mt-1">Manage exam schedules to prevent placement conflicts</p>
                    </div>
                    <Button size="sm" onClick={() => setShowEventModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Event
                    </Button>
                </div>

                {loading ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
                        {[1, 2, 3].map((i) => (
                             <div key={i} className="h-32 bg-gray-100 rounded-xl" />
                        ))}
                    </div>
                ) : events.length === 0 ? (
                    <div className="relative overflow-hidden rounded-2xl border border-dashed border-gray-200 bg-gradient-to-b from-gray-50/50 to-white/50 p-12 text-center">
                        <Calendar className="w-12 h-12 mx-auto text-gray-300 mb-4" />
                        <h3 className="text-lg font-semibold text-gray-900">No Events Scheduled</h3>
                        <p className="text-gray-500 text-sm mt-1 max-w-sm mx-auto">
                            Add exam dates and academic events to prevent scheduling conflicts with placement drives.
                        </p>
                        <Button size="sm" className="mt-4" onClick={() => setShowEventModal(true)}>
                            <Plus className="h-4 w-4 mr-2" />
                            Schedule First Event
                        </Button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {[...events].sort((a, b) => new Date(a.startDate) - new Date(b.startDate)).map((event) => {
                             const typeConfig = {
                                EXAM: { bg: 'bg-red-50', border: 'border-red-100', text: 'text-red-700', icon: 'bg-red-100' },
                                HOLIDAY: { bg: 'bg-amber-50', border: 'border-amber-100', text: 'text-amber-700', icon: 'bg-amber-100' },
                                EVENT: { bg: 'bg-blue-50', border: 'border-blue-100', text: 'text-blue-700', icon: 'bg-blue-100' }
                            }[event.eventType] || { bg: 'bg-gray-50', border: 'border-gray-100', text: 'text-gray-700', icon: 'bg-gray-100' };

                            return (
                                <div 
                                    key={event.id} 
                                    className={`relative overflow-hidden rounded-xl border ${typeConfig.border} ${typeConfig.bg} p-5 transition-all hover:shadow-md`}
                                >
                                    <div className="flex items-center justify-between mb-3">
                                        <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded-full ${typeConfig.icon} ${typeConfig.text}`}>
                                            {event.eventType}
                                        </span>
                                    </div>
                                    <h4 className="font-bold text-gray-900 text-lg mb-1">{event.name}</h4>
                                    <p className="text-sm text-gray-500 mb-3">{event.organizationUnitName}</p>
                                    <div className="flex items-center gap-2 text-sm">
                                        <Calendar className="w-4 h-4 text-gray-400" />
                                        <span className="font-medium text-gray-700">
                                            {formatDate(event.startDate)} — {formatDate(event.endDate)}
                                        </span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>

            {/* Add Event Modal */}
            <Modal
                isOpen={showEventModal}
                onClose={() => setShowEventModal(false)}
                title="Schedule Academic Event"
            >
                <form onSubmit={handleCreateEvent} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            value={newEvent.organizationUnitId}
                            onChange={(e) => setNewEvent({ ...newEvent, organizationUnitId: e.target.value })}
                            required
                        >
                            <option value="">Select Department</option>
                            {departments.map(dept => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                    </div>

                    <Input
                        label="Event Name"
                        value={newEvent.name}
                        onChange={(e) => setNewEvent({ ...newEvent, name: e.target.value })}
                        placeholder="e.g. End Semester Exams"
                        required
                    />

                    <div>
                         <label className="block text-sm font-medium text-gray-700 mb-1">Event Type</label>
                         <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            value={newEvent.eventType}
                            onChange={(e) => setNewEvent({ ...newEvent, eventType: e.target.value })}
                        >
                            <option value="EXAM">Exam</option>
                            <option value="HOLIDAY">Holiday</option>
                            <option value="EVENT">Other Event</option>
                        </select>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            type="date"
                            label="Start Date"
                            value={newEvent.startDate}
                            onChange={(e) => setNewEvent({ ...newEvent, startDate: e.target.value })}
                            required
                        />
                         <Input
                            type="date"
                            label="End Date"
                            value={newEvent.endDate}
                            onChange={(e) => setNewEvent({ ...newEvent, endDate: e.target.value })}
                            required
                        />
                    </div>

                    <Button type="submit" className="w-full">
                        Schedule Event
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
