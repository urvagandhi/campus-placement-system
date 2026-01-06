'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Table from '@/components/ui/Table';
import { Calendar, Plus } from 'lucide-react';

// Mock Data
const DEPARTMENTS = [
    { id: 1, name: 'Computer Science', students: 450, placements: 120 },
    { id: 2, name: 'Mechanical Engineering', students: 300, placements: 85 },
    { id: 3, name: 'Electrical Engineering', students: 280, placements: 90 },
    { id: 4, name: 'Civil Engineering', students: 220, placements: 60 },
    { id: 5, name: 'Electronics & Comm.', students: 320, placements: 110 },

];

const ACADEMIC_EXAMS = [
    { id: 1, department: 'Computer Science', exam: 'End Sem Exams', startDate: '2023-11-20', endDate: '2023-11-30' },
    { id: 2, department: 'Information Technology', exam: 'Practical Exams', startDate: '2023-11-15', endDate: '2023-11-18' },
    { id: 3, department: 'Mechanical Engg', exam: 'Industrial Visit', startDate: '2023-11-25', endDate: '2023-11-26' },
];

export default function Departments() {
    const columns = [
        { header: 'Department Name', accessor: 'name' },
        { header: 'Total Students', accessor: 'students' },
        { header: 'Placements (YTD)', accessor: 'placements' },
    ];

    const examColumns = [
        { header: 'Department', accessor: 'department' },
        { header: 'Exam / Event', accessor: 'exam' },
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
                    data={DEPARTMENTS}
                />

            </Card>

            <div className="flex items-center justify-between pt-4">
                <div>
                    <h2 className="text-xl font-bold text-gray-900">Academic Calendar & Constraints</h2>
                    <p className="text-gray-500 text-sm">Manage verified exam schedules to prevent placement conflicts.</p>
                </div>
                <Button size="sm">
                    <Plus className="h-4 w-4 mr-2" />
                    Add Exam Schedule
                </Button>
            </div>

            <Card className="border border-gray-100 shadow-sm">
                <div className="p-4 bg-gray-50 border-b border-gray-100 flex items-center gap-2 text-sm text-gray-600">
                    <Calendar className="h-4 w-4" />
                    <span>Showing upcoming academic events for Nov-Dec 2023</span>
                </div>
                <Table
                    columns={examColumns}
                    data={ACADEMIC_EXAMS}
                />
            </Card>
        </div>
    );
}
