'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import { Building2, Globe, MapPin, Plus, Search } from 'lucide-react';
import { useState } from 'react';

// Mock Data for Colleges
const MOCK_COLLEGES = [
    { id: 1, name: 'Institute of Technology', location: 'New York', students: 1200, status: 'Active' },
    { id: 2, name: 'State Engineering College', location: 'California', students: 850, status: 'Active' },
    { id: 3, name: 'City Polytechnic', location: 'Texas', students: 600, status: 'Inactive' },
];

export default function ManageCollegesPage() {
    const [searchTerm, setSearchTerm] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [newCollege, setNewCollege] = useState({
        name: '',
        location: '',
        adminEmail: '',
        website: ''
    });

    const filteredColleges = MOCK_COLLEGES.filter(college =>
        college.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        college.location.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const columns = [
        { header: 'College Name', accessor: 'name' },
        { header: 'Location', accessor: 'location' },
        { header: 'Total Students', accessor: 'students' },
        {
            header: 'System Status',
            accessor: (college) => (
                <Badge variant={college.status === 'Active' ? 'success' : 'neutral'}>
                    {college.status}
                </Badge>
            )
        },
    ];

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        // Simulate API
        setTimeout(() => {
            setLoading(false);
            setIsModalOpen(false);
            setNewCollege({ name: '', location: '', adminEmail: '', website: '' });
            alert('New College Registered Successfully (Mock)');
        }, 1200);
    };

    const handleChange = (e) => {
        setNewCollege({ ...newCollege, [e.target.name]: e.target.value });
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Manage Colleges</h1>
                    <p className="text-gray-600 mt-1">Platform-level administration for registered institutions.</p>
                </div>
                <Button onClick={() => setIsModalOpen(true)} className="bg-gray-900 hover:bg-gray-800">
                    <Plus className="h-4 w-4 mr-2" />
                    Register New College
                </Button>
            </div>

            <Card className="border border-gray-100">
                <div className="flex items-center gap-4 mb-6">
                    <div className="relative flex-1 max-w-md">
                        <Search className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
                        <Input
                            placeholder="Search colleges..."
                            className="pl-10"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>

                <Table columns={columns} data={filteredColleges} />
            </Card>

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title="Register New Institution"
            >
                <form onSubmit={handleSubmit} className="space-y-4">
                    <Input
                        label="Institution Name"
                        name="name"
                        value={newCollege.name}
                        onChange={handleChange}
                        placeholder="e.g. National Institute of Tech"
                        required
                        icon={Building2}
                    />
                    <Input
                        label="Location / City"
                        name="location"
                        value={newCollege.location}
                        onChange={handleChange}
                        placeholder="e.g. Mumbai"
                        required
                        icon={MapPin}
                    />
                    <Input
                        label="Admin Email"
                        name="adminEmail"
                        type="email"
                        value={newCollege.adminEmail}
                        onChange={handleChange}
                        placeholder="Admin who will manage this college"
                        required
                    />
                    <Input
                        label="Website"
                        name="website"
                        value={newCollege.website}
                        onChange={handleChange}
                        placeholder="https://..."
                        icon={Globe}
                    />

                    <div className="pt-4 flex gap-3">
                        <Button type="submit" loading={loading} className="flex-1 bg-gray-900 hover:bg-gray-800">Register</Button>
                        <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)} className="flex-1">Cancel</Button>
                    </div>
                </form>
            </Modal>
        </div>
    );
}
