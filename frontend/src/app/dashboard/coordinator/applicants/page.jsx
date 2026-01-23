'use client';

import Badge from '@/components/ui/Badge';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import { applicationsApi } from '@/services/api';
import { Ban, CheckCircle, Search, User, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';

export default function ApplicantsPage() {
    const [loading, setLoading] = useState(true);
    const [students, setStudents] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filter, setFilter] = useState('All'); // All, Pending, Shortlisted, Rejected
    const [updating, setUpdating] = useState(null); // ID of application being updated

    useEffect(() => {
        fetchApplicants();
    }, []);

    const fetchApplicants = async () => {
        try {
            setLoading(true);
            const response = await applicationsApi.getAll();
            const data = response.data || [];
            // Transform data if needed or use as is if DTO matches
            setStudents(data);
        } catch (error) {
            console.error("Failed to fetch applicants:", error);
            toast.error("Failed to load applicants");
        } finally {
            setLoading(false);
        }
    };

    const handleAction = async (id, newStatus) => {
        if (updating) return; // Prevent concurrent updates
        
        try {
            setUpdating(id);
            const response = await applicationsApi.updateStatus(id, newStatus.toUpperCase());
            if (response.success) {
                toast.success(`Application updated to ${newStatus}`);
                // Update local state
                setStudents(prev => prev.map(student =>
                    student.id === id ? { ...student, status: newStatus.toUpperCase() } : student
                ));
            }
        } catch (error) {
            console.error("Failed to update status:", error);
            toast.error(error.message || "Failed to update status");
        } finally {
            setUpdating(null);
        }
    };

    const filteredStudents = students.filter(student => {
        // Safe access to fields
        const name = student.studentName || '';
        const drive = student.driveTitle || student.companyName || '';
        const status = student.status || '';

        const matchesSearch = name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            drive.toLowerCase().includes(searchTerm.toLowerCase());
        
        let matchesFilter = true;
        if (filter === 'All') {
             matchesFilter = true;
        } else if (filter === 'Pending') {
            matchesFilter = ['PENDING', 'APPLIED', 'UNDER_REVIEW'].includes(status);
        } else {
            matchesFilter = status === filter.toUpperCase();
        }

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
                                        const isLocked = ['SHORTLISTED', 'REJECTED', 'SELECTED'].includes(student.status);
                                        const isProcessing = updating === student.id;
                                        
                                        return (
                                            <tr key={student.id} className="hover:bg-gray-50/80 transition-colors">
                                                <td className="px-4 py-3">
                                                    <div className="flex items-center gap-3">
                                                        <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-xs">
                                                            {(student.studentName || 'U').split(' ').map(n => n[0]).join('')}
                                                        </div>
                                                        <span className="font-medium text-gray-900">{student.studentName}</span>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-3 text-sm text-gray-600">{student.studentDepartment || 'N/A'}</td>
                                                <td className="px-4 py-3 text-sm text-gray-600 font-medium">{student.driveTitle || student.companyName}</td>
                                                <td className="px-4 py-3">
                                                    <div className="text-sm">
                                                        {student.studentScore && (
                                                            <>
                                                                <span className="font-medium text-gray-900">{student.studentScore}</span>
                                                                <span className="text-gray-400 mx-1">/</span>
                                                            </>
                                                        )}
                                                        <span className="text-gray-600">CGPA: {student.studentCgpa || 'N/A'}</span>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-3">
                                                    <Badge variant={
                                                        student.status === 'SHORTLISTED' || student.status === 'SELECTED' ? 'success' :
                                                            student.status === 'REJECTED' ? 'error' :
                                                                'info'
                                                    }>
                                                        {student.status}
                                                    </Badge>
                                                </td>
                                                <td className="px-4 py-3 text-right">
                                                    <div className="flex items-center justify-end gap-2">
                                                        {isProcessing ? (
                                                            <Loader2 className="h-4 w-4 animate-spin text-gray-400" />
                                                        ) : (
                                                            <>
                                                                <button
                                                                    onClick={() => handleAction(student.id, 'SHORTLISTED')}
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
                                                                    onClick={() => handleAction(student.id, 'REJECTED')}
                                                                    disabled={isLocked}
                                                                    title="Reject"
                                                                    className={`p-1.5 rounded transition-colors ${isLocked
                                                                            ? 'opacity-30 cursor-not-allowed text-gray-400'
                                                                            : 'text-red-600 hover:bg-red-50'
                                                                        }`}
                                                                >
                                                                    <Ban className="h-5 w-5" />
                                                                </button>
                                                            </>
                                                        )}
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
