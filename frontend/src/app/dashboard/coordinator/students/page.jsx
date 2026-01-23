'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import api from '@/services/api';
import { Loader2, Mail, Search, User, UserPlus } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function StudentOnboardingPage() {
    const [students, setStudents] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [newStudent, setNewStudent] = useState({
        name: '',
        email: '',
        password: '',
        phoneNumber: '',
        organizationUnitId: ''
    });

    const fetchData = async () => {
        try {
            setLoading(true);
            const [studentsRes, deptRes] = await Promise.all([
                api.students.getAll(),
                api.organizations.getDepartments()
            ]);

            if (studentsRes.success) {
                setStudents(studentsRes.data || []);
            }
            if (deptRes.success) {
                setDepartments(deptRes.data || []);
            }
        } catch (error) {
            console.error('Failed to fetch data:', error);
            toast.error('Failed to load data');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const filteredStudents = students.filter(student => {
        const matchesSearch =
            student.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            student.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            student.departmentName?.toLowerCase().includes(searchTerm.toLowerCase());

        const isPlaced = student.isPlaced || student.placementStatus === 'PLACED';
        const matchesStatus =
            statusFilter === 'ALL' ||
            (statusFilter === 'Placed' && isPlaced) ||
            (statusFilter === 'Pending' && !isPlaced);

        return matchesSearch && matchesStatus;
    });

    const columns = [
        { header: 'Student Name', accessor: 'name' },
        { header: 'Email', accessor: 'email' },
        { header: 'Department', accessor: 'departmentName' },
        { header: 'CGPA', accessor: 'cgpa' },
        {
            header: 'Status',
            accessor: 'placementStatus',
            render: (status) => (
                <Badge variant={status === 'PLACED' ? 'success' : 'warning'}>
                    {status === 'PLACED' ? 'Placed' : 'Unplaced'}
                </Badge>
            )
        },
    ];

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const payload = {
                ...newStudent,
                organizationUnitId: newStudent.organizationUnitId ? parseInt(newStudent.organizationUnitId) : null
            };
            const response = await api.students.create(payload);
            if (response.success) {
                toast.success('Student onboarded successfully');
                fetchData();
                setIsModalOpen(false);
                setNewStudent({ name: '', email: '', password: '', phoneNumber: '', organizationUnitId: '' });
            } else {
                toast.error(response.message || 'Failed to onboard student');
            }
        } catch (error) {
            console.error('Create student error:', error);
            toast.error(error.message || 'Failed to onboard student');
        } finally {
            setSubmitting(false);
        }
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

                {loading ? (
                    <div className="flex justify-center py-12">
                        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
                    </div>
                ) : (
                    <Table columns={columns} data={filteredStudents} emptyMessage="No students found" />
                )}
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
                        label="Password"
                        name="password"
                        type="password"
                        value={newStudent.password}
                        onChange={handleChange}
                        placeholder="Initial password"
                        required
                    />
                    <Input
                        label="Phone Number"
                        name="phoneNumber"
                        value={newStudent.phoneNumber}
                        onChange={handleChange}
                        placeholder="e.g. +91 9876543210"
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
                        <select
                            name="organizationUnitId"
                            value={newStudent.organizationUnitId}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        >
                            <option value="">Select Department</option>
                            {departments.map(dept => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                    </div>

                    <div className="pt-4 flex gap-3">
                        <Button type="submit" loading={submitting} className="flex-1">Register Student</Button>
                        <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)} className="flex-1">Cancel</Button>
                    </div>
                </form>
            </Modal>
        </div>
    );
}
