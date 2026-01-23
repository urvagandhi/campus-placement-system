'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import api from '@/services/api';
import { Calendar, Plus } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function Departments() {
    const [departments, setDepartments] = useState([]);
    const [exams, setExams] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);

    // Form State for New Event
    const [newEvent, setNewEvent] = useState({
        name: '',
        eventType: 'EXAM',
        startDate: '',
        endDate: '',
        description: '',
        organizationUnitId: ''
    });

    const fetchData = async () => {
        setLoading(true);
        try {
            const [deptRes, eventsRes] = await Promise.all([
                api.organizations.getDepartments(),
                api.organizations.getEvents()
            ]);

            if (deptRes.success) setDepartments(deptRes.data);
            if (eventsRes.success) setExams(eventsRes.data);
        } catch (error) {
            console.error('Failed to fetch department data:', error);
            toast.error('Failed to load data');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

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

    const columns = [
        { header: 'Department Name', accessor: 'name' },
        { header: 'Total Students', accessor: 'studentCount' },
        { header: 'Placements (YTD)', accessor: 'placementCount' },
    ];

    const examColumns = [
        { header: 'Department', accessor: 'organizationUnitName' },
        { header: 'Exam / Event', accessor: 'name' },
        { header: 'Type', accessor: 'eventType' },
        { header: 'Start Date', accessor: 'startDate' },
        { header: 'End Date', accessor: 'endDate' },
    ];

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Departments Overview</h1>
            <p className="text-gray-500">Overview of all departments and their placement statistics.</p>

            <Card className="border border-gray-100 shadow-sm">
                <Table
                    columns={columns}
                    data={departments}
                    isLoading={loading}
                    emptyMessage="No departments found"
                />
            </Card>

            <div className="flex items-center justify-between pt-4">
                <div>
                    <h2 className="text-xl font-bold text-gray-900">Academic Calendar & Constraints</h2>
                    <p className="text-gray-500 text-sm">Manage verified exam schedules to prevent placement conflicts.</p>
                </div>
                <Button size="sm" onClick={() => setShowAddModal(true)}>
                    <Plus className="h-4 w-4 mr-2" />
                    Add Exam Schedule
                </Button>
            </div>

            <Card className="border border-gray-100 shadow-sm">
                <div className="p-4 bg-gray-50 border-b border-gray-100 flex items-center gap-2 text-sm text-gray-600">
                    <Calendar className="h-4 w-4" />
                    <span>Upcoming academic events</span>
                </div>
                <Table
                    columns={examColumns}
                    data={exams}
                    isLoading={loading}
                    emptyMessage="No academic events scheduled"
                />
            </Card>

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
        </div>
    );
}
