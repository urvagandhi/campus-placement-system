'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import { Ban, CheckCircle, Search, User } from 'lucide-react';
import { useEffect, useState } from 'react';

// Mock Data
const INITIAL_STUDENTS = [
    { id: 1, name: 'Aditya Sharma', dept: 'CSE', cgpa: 9.2, score: 95, status: 'Applied', drive: 'Google' },
    { id: 2, name: 'Priya Patel', dept: 'IT', cgpa: 8.8, score: 88, status: 'Shortlisted', drive: 'Google' },
    { id: 3, name: 'Rahul Singh', dept: 'ECE', cgpa: 7.9, score: 72, status: 'Applied', drive: 'Microsoft' },
    { id: 4, name: 'Anjali Gupta', dept: 'CSE', cgpa: 8.5, score: 82, status: 'Rejected', drive: 'Microsoft' },
    { id: 5, name: 'Karthik R', dept: 'MECH', cgpa: 7.2, score: 65, status: 'Applied', drive: 'Amazon' },
    { id: 6, name: 'Sneha Reddy', dept: 'CSE', cgpa: 9.0, score: 91, status: 'Shortlisted', drive: 'Amazon' },
];

export default function ApplicantsPage() {
    const [loading, setLoading] = useState(true);
    const [students, setStudents] = useState(INITIAL_STUDENTS);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('All'); // All, Pending, Shortlisted, Rejected

    useEffect(() => {
        const timer = setTimeout(() => setLoading(false), 1000);
        return () => clearTimeout(timer);
    }, []);

    const handleAction = (id, newStatus) => {
        setStudents(prev => prev.map(student =>
            student.id === id ? { ...student, status: newStatus } : student
        ));
    };

    const filteredStudents = students.filter(student => {
        const matchesSearch = student.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            student.drive.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesFilter = filter === 'All'
            ? true
            : filter === 'Pending'
                ? student.status === 'Applied'
                : student.status === filter;

        return matchesSearch && matchesFilter;
    });

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Review Applicants</h1>
                <p className="text-gray-600 mt-1">Shortlist or reject candidates based on their profile and scores.</p>
            </div>

            <Card className="border border-gray-100">
                {/* Filters */}
                <div className="flex flex-col md:flex-row gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <Input
                            placeholder="Search by student name or company..."
                            className="pl-10"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <div className="flex bg-gray-100 p-1 rounded-lg">
                        {['All', 'Pending', 'Shortlisted', 'Rejected'].map((f) => (
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
                            {[1, 2, 3, 4, 5].map(i => (
                                <div key={i} className="h-10 w-full bg-gray-50 animate-pulse rounded"></div>
                            ))}
                        </div>
                    ) : (
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="border-b border-gray-100 text-xs text-gray-500 uppercase tracking-wider bg-gray-50/50">
                                    <th className="px-4 py-3 font-semibold">Candidate</th>
                                    <th className="px-4 py-3 font-semibold">Department</th>
                                    <th className="px-4 py-3 font-semibold">Applied For</th>
                                    <th className="px-4 py-3 font-semibold">Score/CGPA</th>
                                    <th className="px-4 py-3 font-semibold">Status</th>
                                    <th className="px-4 py-3 font-semibold text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                                {filteredStudents.length > 0 ? (
                                    filteredStudents.map((student) => {
                                        const isLocked = student.status === 'Shortlisted' || student.status === 'Rejected';
                                        return (
                                            <tr key={student.id} className="hover:bg-gray-50/80 transition-colors">
                                                <td className="px-4 py-3">
                                                    <div className="flex items-center gap-3">
                                                        <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-xs">
                                                            {student.name.split(' ').map(n => n[0]).join('')}
                                                        </div>
                                                        <span className="font-medium text-gray-900">{student.name}</span>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-600">{student.dept}</td>
                                                <td className="px-4 py-3 text-sm text-gray-600 font-medium">{student.drive}</td>
                                                <td className="px-4 py-3">
                                                    <div className="text-sm">
                                                        <span className="font-medium text-gray-900">{student.score}</span>
                                                        <span className="text-gray-400 mx-1">/</span>
                                                        <span className="text-gray-600">CGPA: {student.cgpa}</span>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-3">
                                                    <Badge variant={
                                                        student.status === 'Shortlisted' ? 'success' :
                                                            student.status === 'Rejected' ? 'error' :
                                                                'info'
                                                    }>
                                                        {student.status.toUpperCase()}
                                                    </Badge>
                                                </td>
                                                <td className="px-4 py-3 text-right">
                                                    <div className="flex items-center justify-end gap-2">
                                                        <button
                                                            onClick={() => handleAction(student.id, 'Shortlisted')}
                                                            disabled={isLocked}
                                                            title="Shortlist"
                                                            className={`p-1.5 rounded transition-colors ${isLocked
                                                                    ? 'opacity-30 cursor-not-allowed text-gray-400'
                                                                    : 'text-green-600 hover:bg-green-50'
                                                                }`}
                                                        >
                                                            <CheckCircle className="h-5 w-5" />
                                                        </button>
                                                        <button
                                                            onClick={() => handleAction(student.id, 'Rejected')}
                                                            disabled={isLocked}
                                                            title="Reject"
                                                            className={`p-1.5 rounded transition-colors ${isLocked
                                                                    ? 'opacity-30 cursor-not-allowed text-gray-400'
                                                                    : 'text-red-600 hover:bg-red-50'
                                                                }`}
                                                        >
                                                            <Ban className="h-5 w-5" />
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })
                                ) : (
                                    <tr>
                                        <td colSpan="6" className="py-12 text-center text-gray-500 bg-gray-50/30 rounded-lg border border-dashed border-gray-200 mt-2">
                                            <User className="h-8 w-8 mx-auto text-gray-300 mb-2" />
                                            <p>No applicants found.</p>
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
