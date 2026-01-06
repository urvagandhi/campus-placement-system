'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import { Mail, Search, User, UserPlus } from 'lucide-react';
import { useState } from 'react';

// Mock Data
const MOCK_STUDENTS = [
    { id: 1, name: 'Rahul Sharma', email: 'rahul@student.edu', department: 'Computer Science', cgpa: 8.5, status: 'Placed' },
    { id: 2, name: 'Priya Patel', email: 'priya@student.edu', department: 'Information Technology', cgpa: 9.1, status: 'Placed' },
    { id: 3, name: 'Amit Singh', email: 'amit@student.edu', department: 'Mechanical Engineering', cgpa: 7.8, status: 'Unplaced' },
    { id: 4, name: 'Sneha Gupta', email: 'sneha@student.edu', department: 'Electronics', cgpa: 8.2, status: 'Unplaced' },
    { id: 5, name: 'Vikram Malhotra', email: 'vikram@student.edu', department: 'Computer Science', cgpa: 7.5, status: 'Placed' },
];

export default function StudentOnboardingPage() {
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL'); // ALL | Placed | Pending
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [newStudent, setNewStudent] = useState({
        name: '',
        email: '',
        department: '',
        enrollmentId: ''
    });

    const filteredStudents = MOCK_STUDENTS.filter(student => {
        const matchesSearch =
            student.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            student.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
            student.department.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesStatus =
            statusFilter === 'ALL' ||
            (statusFilter === 'Placed' && student.status === 'Placed') ||
            (statusFilter === 'Pending' && student.status === 'Unplaced');

        return matchesSearch && matchesStatus;
    });

    // CORRECTED COLUMN DEFINITIONS
    const columns = [
        { header: 'Student Name', accessor: 'name' },
        { header: 'Email', accessor: 'email' },
        { header: 'Department', accessor: 'department' },
        { header: 'CGPA', accessor: 'cgpa' },
        {
            header: 'Status',
            accessor: 'status', // Must be a string key
            render: (status) => (
                <Badge variant={status === 'Placed' ? 'success' : 'warning'}>
                    {status}
                </Badge>
            )
        },
    ];

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        setTimeout(() => {
            setLoading(false);
            setIsModalOpen(false);
            setNewStudent({ name: '', email: '', department: '', enrollmentId: '' });
            alert('Student onboarded successfully (Mock)');
        }, 1000);
    };

    const handleChange = (e) => {
        setNewStudent({ ...newStudent, [e.target.name]: e.target.value });
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Student Onboarding</h1>
                    <p className="text-gray-600 mt-1">Register and manage student profiles for upcoming drives.</p>
                </div>
                <Button onClick={() => setIsModalOpen(true)}>
                    <UserPlus className="h-4 w-4 mr-2" />
                    Onboard New Student
                </Button>
            </div>

            <Card className="border border-gray-100">
                {/* UI Matches Manage Drives: Search on Left, Filters on Right/Next */}
                <div className="flex flex-col md:flex-row gap-4 mb-6">
                    <div className="flex-1 relative">
                        <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <Input
                            placeholder="Search students by name, email or department..."
                            className="pl-10"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                    <div className="flex bg-gray-100 p-1 rounded-lg self-start md:self-auto">
                        {['ALL', 'Placed', 'Pending'].map((tab) => (
                            <button
                                key={tab}
                                onClick={() => setStatusFilter(tab)}
                                className={`
                                    px-4 py-1.5 text-sm font-medium rounded-md transition-all
                                    ${statusFilter === tab
                                        ? 'bg-white text-gray-900 shadow-sm'
                                        : 'text-gray-500 hover:text-gray-900'
                                    }
                                `}
                            >
                                {tab}
                            </button>
                        ))}
                    </div>
                </div>

                <Table columns={columns} data={filteredStudents} />
            </Card>

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title="Onboard New Student"
            >
                <form onSubmit={handleSubmit} className="space-y-4">
                    <Input
                        label="Full Name"
                        name="name"
                        value={newStudent.name}
                        onChange={handleChange}
                        placeholder="e.g. John Doe"
                        required
                        icon={User}
                    />
                    <Input
                        label="Email Address"
                        name="email"
                        type="email"
                        value={newStudent.email}
                        onChange={handleChange}
                        placeholder="e.g. john@student.edu"
                        required
                        icon={Mail}
                    />
                    <Input
                        label="Enrollment ID"
                        name="enrollmentId"
                        value={newStudent.enrollmentId}
                        onChange={handleChange}
                        placeholder="e.g. 2023CS101"
                        required
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
                        <select
                            name="department"
                            value={newStudent.department}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                            required
                        >
                            <option value="">Select Department</option>
                            <option value="Computer Science">Computer Science</option>
                            <option value="Information Technology">Information Technology</option>
                            <option value="Mechanical Engineering">Mechanical Engineering</option>
                            <option value="Civil Engineering">Civil Engineering</option>
                            <option value="Electronics">Electronics</option>
                        </select>
                    </div>

                    <div className="pt-4 flex gap-3">
                        <Button type="submit" loading={loading} className="flex-1">Register Student</Button>
                        <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)} className="flex-1">Cancel</Button>
                    </div>
                </form>
            </Modal>
        </div>
    );
}
