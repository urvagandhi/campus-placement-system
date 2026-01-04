'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import { Edit2, Filter, Plus, Search } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

// Mock Data
const MOCK_DRIVES = [
    {
        id: 1,
        company: 'Google',
        role: 'Software Engineer',
        date: '2023-11-20',
        applicants: 45,
        status: 'Active',
        location: 'Bangalore',
        pkg: '14-20 LPA'
    },
    {
        id: 2,
        company: 'Microsoft',
        role: 'Support Engineer',
        date: '2023-11-25',
        applicants: 32,
        status: 'Active',
        location: 'Hyderabad',
        pkg: '12-16 LPA'
    },
    {
        id: 3,
        company: 'Amazon',
        role: 'SDE Intern',
        date: '2023-11-18',
        applicants: 28,
        status: 'Completed',
        location: 'Pune',
        pkg: '80k/mo'
    },
    {
        id: 4,
        company: 'Adobe',
        role: 'Product Manager',
        date: '2023-12-05',
        applicants: 12,
        status: 'Upcoming',
        location: 'Noida',
        pkg: '18 LPA'
    },
];

export default function ManageDrivesPage() {
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('All'); // All, Active, Completed, Upcoming

    useEffect(() => {
        const timer = setTimeout(() => setLoading(false), 800);
        return () => clearTimeout(timer);
    }, []);

    const filteredDrives = MOCK_DRIVES.filter(drive => {
        const matchesSearch = drive.company.toLowerCase().includes(searchTerm.toLowerCase()) ||
            drive.role.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesFilter = filter === 'All' || drive.status === filter;

        return matchesSearch && matchesFilter;
    });

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Manage Placement Drives</h1>
                    <p className="text-gray-600 mt-1">Create, update, and monitor recruitment drives.</p>
                </div>
                <Link href="/dashboard/coordinator/drives/create">
                    <Button>
                        <Plus className="h-4 w-4 mr-2" />
                        Create New Drive
                    </Button>
                </Link>
            </div>

            <Card className="border border-gray-100">
                {/* Dictionary & Filters */}
                <div className="flex flex-col md:flex-row gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <Input
                            placeholder="Search by company or role..."
                            className="pl-10"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <div className="flex bg-gray-100 p-1 rounded-lg">
                        {['All', 'Active', 'Completed', 'Upcoming'].map((f) => (
                            <button
                                key={f}
                                onClick={() => setFilter(f)}
                                className={`px-4 py-1.5 text-sm font-medium rounded-md transition-all ${filter === f
                                        ? 'bg-white text-gray-900 shadow-sm'
                                        : 'text-gray-500 hover:text-gray-900'
                                    }`}
                            >
                                {f}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    {loading ? (
                        <div className="space-y-4 py-4">
                            {[1, 2, 3, 4].map(i => (
                                <div key={i} className="h-12 w-full bg-gray-50 animate-pulse rounded"></div>
                            ))}
                        </div>
                    ) : (
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-gray-100 text-xs text-gray-500 uppercase tracking-wider bg-gray-50/50">
                                    <th className="px-4 py-3 font-semibold">Company & Role</th>
                                    <th className="px-4 py-3 font-semibold">Date</th>
                                    <th className="px-4 py-3 font-semibold">Package/Stipend</th>
                                    <th className="px-4 py-3 font-semibold">Applicants</th>
                                    <th className="px-4 py-3 font-semibold">Status</th>
                                    <th className="px-4 py-3 font-semibold text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                                {filteredDrives.length > 0 ? (
                                    filteredDrives.map((drive) => (
                                        <tr key={drive.id} className="hover:bg-gray-50/80 transition-colors group">
                                            <td className="px-4 py-3">
                                                <div className="font-medium text-gray-900">{drive.company}</div>
                                                <div className="text-sm text-gray-500">{drive.role}</div>
                                            </td>
                                            <td className="px-4 py-3 text-sm text-gray-600">{drive.date}</td>
                                            <td className="px-4 py-3 text-sm text-gray-600">{drive.pkg}</td>
                                            <td className="px-4 py-3">
                                                <div className="flex items-center gap-2">
                                                    <span className="text-sm font-medium text-gray-900">{drive.applicants}</span>
                                                    <span className="text-xs text-gray-400">students</span>
                                                </div>
                                            </td>
                                            <td className="px-4 py-3">
                                                <Badge variant={
                                                    drive.status === 'Active' ? 'success' :
                                                        drive.status === 'Completed' ? 'neutral' :
                                                            'warning'
                                                }>
                                                    {drive.status}
                                                </Badge>
                                            </td>
                                            <td className="px-4 py-3 text-right">
                                                <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                                    <Link href="/dashboard/coordinator/applicants">
                                                        <Button variant="secondary" size="sm" className="h-8 px-2" title="View Applicants">
                                                            <Users className="h-4 w-4" />
                                                        </Button>
                                                    </Link>
                                                    <Button variant="ghost" size="sm" className="h-8 px-2 text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50" title="Edit Drive">
                                                        <Edit2 className="h-4 w-4" />
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="6" className="py-12 text-center text-gray-500 bg-gray-50/30 rounded-lg border border-dashed border-gray-200 mt-2">
                                            <Filter className="h-8 w-8 mx-auto text-gray-300 mb-2" />
                                            <p>No drives match your filters.</p>
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    )}
                </div>
            </Card>
        </div>
    );
}
