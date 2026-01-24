'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import OrganizationTree from '@/components/ui/OrganizationTree';
import Table from '@/components/ui/Table';
import api from '@/services/api';
import { Calendar, Plus, RefreshCw } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function Departments() {
    const [hierarchy, setHierarchy] = useState(null);
    const [institutes, setInstitutes] = useState([]);
    const [exams, setExams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);
    const [showCreateInstituteModal, setShowCreateInstituteModal] = useState(false);
    const [showCreateDeptModal, setShowCreateDeptModal] = useState(false);

    // Form State for New Event
    const [newEvent, setNewEvent] = useState({
        name: '',
        eventType: 'EXAM',
        startDate: '',
        endDate: '',
        description: '',
        organizationUnitId: ''
    });

    // Form State for New Department
    const [newDept, setNewDept] = useState({
        name: '',
        code: '',
        type: 'DEPARTMENT',
        parentUnitId: ''
    });
    
    // Form State for New Institute
    const [newInstitute, setNewInstitute] = useState({
        name: '',
        code: '',
        type: 'INSTITUTE',
        parentUnitId: null
    });

    // Fetch data on component mount
    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [hierarchyRes, instRes, eventsRes] = await Promise.all([
                api.organizations.getHierarchy(),
                api.organizations.getInstitutes(),
                api.organizations.getEvents()
            ]);

            if (hierarchyRes.success) {
                setHierarchy(hierarchyRes.data);
            } else {
                console.error('Failed to fetch hierarchy:', hierarchyRes.message);
            }

            if (instRes.success) {
                setInstitutes(instRes.data || []);
            } else {
                console.error('Failed to fetch institutes:', instRes.message);
            }

            if (eventsRes.success) {
                setExams(eventsRes.data || []);
            } else {
                console.error('Failed to fetch events:', eventsRes.message);
            }
        } catch (error) {
            console.error('Failed to fetch data:', error);
            toast.error('Failed to load department data');
        } finally {
            setLoading(false);
        }
    };

    // Extract departments from hierarchy for the event form dropdown
    const getDepartmentsFromHierarchy = (node) => {
        if (!node) return [];
        let departments = [];
        if (node.type === 'DEPARTMENT') {
            departments.push({ id: node.id, name: node.name });
        }
        if (node.children) {
            node.children.forEach(child => {
                departments = [...departments, ...getDepartmentsFromHierarchy(child)];
            });
        }
        return departments;
    };

    const departments = getDepartmentsFromHierarchy(hierarchy);

    const handleCreateInstitute = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newInstitute,
                parentUnitId: null
            };
            const response = await api.organizations.createUnit(payload);
            if (response.success) {
                toast.success('Institute created successfully');
                setShowCreateInstituteModal(false);
                setNewInstitute({ name: '', code: '', type: 'INSTITUTE', parentUnitId: null });
                fetchData(); // Refresh hierarchy
            } else {
                toast.error(response.message || 'Failed to create institute');
            }
        } catch (error) {
            console.error('Create institute error:', error);
            toast.error(error.message || 'Failed to create institute');
        }
    };

    const handleCreateDept = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newDept,
                parentUnitId: parseInt(newDept.parentUnitId)
            };
            const response = await api.organizations.createUnit(payload);
            if (response.success) {
                toast.success('Department created successfully');
                setShowCreateDeptModal(false);
                setNewDept({ name: '', code: '', type: 'DEPARTMENT', parentUnitId: '' });
                fetchData(); // Refresh hierarchy
            } else {
                toast.error(response.message || 'Failed to create department');
            }
        } catch (error) {
            console.error('Create department error:', error);
            toast.error(error.message || 'Failed to create department');
        }
    };

    const handleCreateEvent = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newEvent,
                organizationUnitId: parseInt(newEvent.organizationUnitId)
            };
            const response = await api.organizations.createEvent(payload);
            if (response.success) {
                toast.success('Academic event scheduled');
                setExams(prev => [...prev, response.data]);
                setShowAddModal(false);
                setNewEvent({ name: '', eventType: 'EXAM', startDate: '', endDate: '', description: '', organizationUnitId: '' });
            } else {
                toast.error(response.message || 'Failed to schedule event');
            }
        } catch (error) {
            console.error('Create event error:', error);
            toast.error(error.message || 'Failed to schedule event');
        }
    };

    const examColumns = [
        { header: 'Department', accessor: 'organizationUnitName' },
        { header: 'Exam / Event', accessor: 'name' },
        { header: 'Type', accessor: 'eventType' },
        { header: 'Start Date', accessor: 'startDate' },
        { header: 'End Date', accessor: 'endDate' },
    ];

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Departments & Hierarchy</h1>
                    <p className="text-gray-500">Manage your organization structure and track placement statistics</p>
                </div>
                <div className="flex items-center gap-3">
                    <Button 
                        variant="outline" 
                        size="sm" 
                        onClick={fetchData}
                        disabled={loading}
                    >
                        <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                        Refresh
                    </Button>
                    <Button size="sm" onClick={() => setShowCreateInstituteModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Institute
                    </Button>
                    <Button size="sm" onClick={() => setShowCreateDeptModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Department
                    </Button>
                </div>
            </div>

            {/* Organization Tree */}
            <div className="relative overflow-hidden rounded-2xl border border-gray-200/50 bg-gradient-to-b from-gray-50/50 to-white/50 backdrop-blur-xl">
                <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]" />
                <div className="relative p-6 min-h-[300px]">
                    <OrganizationTree hierarchy={hierarchy} loading={loading} />
                </div>
            </div>

            {/* Academic Calendar Section */}
            <div className="pt-6">
                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="text-xl font-bold text-gray-900">Academic Calendar</h2>
                        <p className="text-gray-500 text-sm mt-1">Manage exam schedules to prevent placement conflicts</p>
                    </div>
                    <Button size="sm" onClick={() => setShowAddModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Event
                    </Button>
                </div>

                {/* Events Grid */}
                {loading ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
                        {[1, 2, 3].map((i) => (
                            <div key={i} className="h-32 bg-gray-100 rounded-xl" />
                        ))}
                    </div>
                ) : exams.length === 0 ? (
                    <div className="relative overflow-hidden rounded-2xl border border-dashed border-gray-200 bg-gradient-to-b from-gray-50/50 to-white/50 p-12 text-center">
                        <Calendar className="w-12 h-12 mx-auto text-gray-300 mb-4" />
                        <h3 className="text-lg font-semibold text-gray-900">No Events Scheduled</h3>
                        <p className="text-gray-500 text-sm mt-1 max-w-sm mx-auto">
                            Add exam dates and academic events to prevent scheduling conflicts with placement drives.
                        </p>
                        <Button size="sm" className="mt-4" onClick={() => setShowAddModal(true)}>
                            <Plus className="h-4 w-4 mr-2" />
                            Schedule First Event
                        </Button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {[...exams].sort((a, b) => new Date(a.startDate) - new Date(b.startDate)).map((event) => {
                            const typeConfig = {
                                EXAM: { bg: 'bg-red-50', border: 'border-red-100', text: 'text-red-700', icon: 'bg-red-100' },
                                HOLIDAY: { bg: 'bg-amber-50', border: 'border-amber-100', text: 'text-amber-700', icon: 'bg-amber-100' },
                                EVENT: { bg: 'bg-blue-50', border: 'border-blue-100', text: 'text-blue-700', icon: 'bg-blue-100' }
                            }[event.eventType] || { bg: 'bg-gray-50', border: 'border-gray-100', text: 'text-gray-700', icon: 'bg-gray-100' };

                            const formatDate = (dateStr) => {
                                if (!dateStr) return '';
                                const date = new Date(dateStr);
                                return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
                            };

                            return (
                                <div 
                                    key={event.id} 
                                    className={`relative overflow-hidden rounded-xl border ${typeConfig.border} ${typeConfig.bg} p-5 transition-all hover:shadow-md`}
                                >
                                    {/* Type Badge */}
                                    <div className="flex items-center justify-between mb-3">
                                        <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-1 rounded-full ${typeConfig.icon} ${typeConfig.text}`}>
                                            {event.eventType}
                                        </span>
                                    </div>
                                    
                                    {/* Event Name */}
                                    <h4 className="font-bold text-gray-900 text-lg mb-1">{event.name}</h4>
                                    
                                    {/* Department */}
                                    <p className="text-sm text-gray-500 mb-3">{event.organizationUnitName}</p>
                                    
                                    {/* Date Range */}
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

            {/* Add Institute Modal */}
            <Modal
                isOpen={showCreateInstituteModal}
                onClose={() => setShowCreateInstituteModal(false)}
                title="Add New Institute"
            >
                <form onSubmit={handleCreateInstitute} className="space-y-4">
                    <Input
                        label="Institute Name"
                        value={newInstitute.name}
                        onChange={(e) => setNewInstitute({ ...newInstitute, name: e.target.value })}
                        placeholder="e.g. Institute of Science"
                        required
                    />
                    
                    <Input
                        label="Institute Code"
                        value={newInstitute.code}
                        onChange={(e) => setNewInstitute({ ...newInstitute, code: e.target.value.toUpperCase() })}
                        placeholder="e.g. IOS"
                        required
                    />

                    <Button type="submit" className="w-full">
                        Create Institute
                    </Button>
                </form>
            </Modal>

            {/* Add Event Modal */}
            <Modal
                isOpen={showAddModal}
                onClose={() => setShowAddModal(false)}
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
            
            {/* Add Department Modal */}
            <Modal
                isOpen={showCreateDeptModal}
                onClose={() => setShowCreateDeptModal(false)}
                title="Add New Department"
            >
                <form onSubmit={handleCreateDept} className="space-y-4">
                    <Input
                        label="Department Name"
                        value={newDept.name}
                        onChange={(e) => setNewDept({ ...newDept, name: e.target.value })}
                        placeholder="e.g. Chemical Engineering"
                        required
                    />
                    
                    <Input
                        label="Department Code"
                        value={newDept.code}
                        onChange={(e) => setNewDept({ ...newDept, code: e.target.value.toUpperCase() })}
                        placeholder="e.g. CHEM"
                        required
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Parent Institute</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            value={newDept.parentUnitId}
                            onChange={(e) => setNewDept({ ...newDept, parentUnitId: e.target.value })}
                            required
                        >
                            <option value="">Select Institute</option>
                            {institutes.map(inst => (
                                <option key={inst.id} value={inst.id}>{inst.name}</option>
                            ))}
                        </select>
                    </div>

                    <Button type="submit" className="w-full">
                        Create Department
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
