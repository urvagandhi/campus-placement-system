'use client';

import Card from '@/components/ui/Card';
import Table from '@/components/ui/Table';

// Mock Data
const DEPARTMENTS = [
    { id: 1, name: 'Computer Science', students: 450, placements: 120 },
    { id: 2, name: 'Mechanical Engineering', students: 300, placements: 85 },
    { id: 3, name: 'Electrical Engineering', students: 280, placements: 90 },
    { id: 4, name: 'Civil Engineering', students: 220, placements: 60 },
    { id: 5, name: 'Electronics & Comm.', students: 320, placements: 110 },
];

export default function Departments() {
    const columns = [
        { header: 'Department Name', accessor: 'name' },
        { header: 'Total Students', accessor: 'students' },
        { header: 'Placements (YTD)', accessor: 'placements' },
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
        </div>
    );
}
