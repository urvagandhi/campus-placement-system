'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import api from '@/services/api';
import { Building2, Globe, MapPin, Plus, Search, Trash2, Ban, CheckCircle } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CardSkeleton, ListSkeleton, Skeleton } from '@/components/ui/Skeleton';

export default function ManageCollegesPage() {
    const [colleges, setColleges] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [newCollege, setNewCollege] = useState({
        name: '',
        code: '',
        address: '',
        contactEmail: '',
        website: ''
    });

    const fetchColleges = async () => {
        try {
            setLoading(true);
            const response = await api.users.getAllColleges();
            if (response.success) {
                setColleges(response.data || []);
            }
        } catch (error) {
            console.error('Failed to fetch colleges:', error);
            toast.error('Failed to load colleges');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchColleges();
    }, []);

    const filteredColleges = colleges.filter(college =>
        college.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        college.address?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        college.code?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const columns = [
        { header: 'College Name', accessor: 'name' },
        { header: 'Code', accessor: 'code' },
        { header: 'Address', accessor: 'address' },
        {
            header: 'Status',
            accessor: 'isActive',
            render: (isActive) => (
                <Badge variant={isActive ? 'success' : 'neutral'}>
                    {isActive ? 'Active' : 'Inactive'}
                </Badge>
            )
        },
        {
            header: 'Actions',
            render: (_, college) => (
                <div className="flex items-center gap-2">
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            handleStatusToggle(college);
                        }}
                        disabled={actionLoading === college.id}
                        className={`p-1.5 rounded-lg transition-colors ${
                            college.isActive 
                                ? 'text-amber-600 hover:bg-amber-50 hover:text-amber-700' 
                                : 'text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700'
                        }`}
                        title={college.isActive ? "Deactivate College" : "Activate College"}
                    >
                        {college.isActive ? <Ban className="h-4 w-4" /> : <CheckCircle className="h-4 w-4" />}
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteClick(college);
                        }}
                        disabled={actionLoading === college.id}
                        className="p-1.5 text-red-600 hover:bg-red-50 hover:text-red-700 rounded-lg transition-colors"
                        title="Delete College"
                    >
                        <Trash2 className="h-4 w-4" />
                    </button>
                </div>
            )
        }
    ];

    const [actionLoading, setActionLoading] = useState(null);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [collegeToDelete, setCollegeToDelete] = useState(null);

    const handleStatusToggle = async (college) => {
        if (!confirm(`Are you sure you want to ${college.isActive ? 'deactivate' : 'activate'} ${college.name}?`)) return;

        try {
            setActionLoading(college.id);
            const response = await api.users.updateCollegeStatus(college.id, !college.isActive);
            if (response.success) {
                toast.success(`College ${college.isActive ? 'deactivated' : 'activated'} successfully`);
                setColleges(prev => prev.map(c => c.id === college.id ? response.data : c));
            } else {
                toast.error(response.message || 'Failed to update status');
            }
        } catch (error) {
            console.error('Status update error:', error);
            toast.error('Failed to update status');
        } finally {
            setActionLoading(null);
        }
    };

    const handleDeleteClick = (college) => {
        setCollegeToDelete(college);
        setDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        if (!collegeToDelete) return;

        try {
            setActionLoading(collegeToDelete.id);
            const response = await api.users.deleteCollege(collegeToDelete.id);
            if (response.success) {
                toast.success('College deleted successfully');
                setColleges(prev => prev.filter(c => c.id !== collegeToDelete.id));
                setDeleteModalOpen(false);
                setCollegeToDelete(null);
            } else {
                toast.error(response.message || 'Failed to delete college');
            }
        } catch (error) {
            console.error('Delete error:', error);
            toast.error('Failed to delete college');
        } finally {
            setActionLoading(null);
        }
    };

    const [viewModalOpen, setViewModalOpen] = useState(false);
    const [viewCollege, setViewCollege] = useState(null);

    const handleRowClick = (college) => {
        setViewCollege(college);
        setViewModalOpen(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const response = await api.users.createCollege(newCollege);
            if (response.success) {
                toast.success('College registered successfully');
                setColleges(prev => [...prev, response.data]);
                setIsModalOpen(false);
                setNewCollege({ name: '', code: '', address: '', contactEmail: '', website: '' });
            } else {
                toast.error(response.message || 'Failed to register college');
            }
        } catch (error) {
            console.error('Create college error:', error);
            toast.error(error.message || 'Failed to register college');
        } finally {
            setSubmitting(false);
        }
    };

    const handleChange = (e) => {
        setNewCollege({ ...newCollege, [e.target.name]: e.target.value });
    };

    return (
        <div className="space-y-6 animate-fade-in">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Manage Colleges</h1>
                    <p className="text-gray-600 mt-1 text-lg">Platform-level administration for registered institutions.</p>
                </div>
                <Button onClick={() => setIsModalOpen(true)} className="bg-gray-900 hover:bg-gray-800 shadow-lg shadow-gray-200">
                    <Plus className="h-5 w-5 mr-2" />
                    Register New College
                </Button>
            </div>

            <Card className="border border-gray-100/50 shadow-xl shadow-gray-100/20 bg-white/80 backdrop-blur-xl">
                <div className="flex items-center gap-4 mb-8">
                    <div className="relative flex-1 max-w-md">
                        <Input
                            placeholder="Search colleges by name, code or location..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            icon={Search}
                            className="bg-white/50"
                        />
                    </div>
                </div>

                {loading ? (
                    <div className="space-y-6">
                        <div className="border border-gray-100 rounded-2xl p-4 bg-gray-50/30">
                            <ListSkeleton count={5} />
                        </div>
                    </div>
                ) : (
                    <Table 
                        columns={columns} 
                        data={filteredColleges} 
                        emptyMessage="No colleges found. Start by registering a new institution." 
                        onRowClick={handleRowClick}
                    />
                )}
            </Card>


            <Modal
                isOpen={viewModalOpen}
                onClose={() => setViewModalOpen(false)}
                title="College Details"
            >
                {viewCollege && (
                    <div className="space-y-6">
                        <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                            <div>
                                <h3 className="text-lg font-bold text-gray-900">{viewCollege.name}</h3>
                                <p className="text-gray-500">{viewCollege.code}</p>
                            </div>
                            <Badge variant={viewCollege.isActive ? 'success' : 'neutral'}>
                                {viewCollege.isActive ? 'Active' : 'Inactive'}
                            </Badge>
                        </div>

                        <div className="grid grid-cols-1 gap-4 text-sm">
                            <div className="flex items-start gap-3">
                                <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                    <MapPin className="h-5 w-5" />
                                </div>
                                <div>
                                    <p className="font-medium text-gray-900">Address</p>
                                    <p className="text-gray-600 mt-0.5">{viewCollege.address || 'N/A'}</p>
                                </div>
                            </div>

                            <div className="flex items-start gap-3">
                                <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                    <Building2 className="h-5 w-5" />
                                </div>
                                <div>
                                    <p className="font-medium text-gray-900">Contact Email</p>
                                    <p className="text-gray-600 mt-0.5">{viewCollege.contactEmail || 'N/A'}</p>
                                </div>
                            </div>

                            <div className="flex items-start gap-3">
                                <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                    <Globe className="h-5 w-5" />
                                </div>
                                <div>
                                    <p className="font-medium text-gray-900">Website</p>
                                    {viewCollege.website ? (
                                        <a 
                                            href={viewCollege.website.startsWith('http') ? viewCollege.website : `https://${viewCollege.website}`} 
                                            target="_blank" 
                                            rel="noopener noreferrer"
                                            className="text-indigo-600 hover:underline mt-0.5 block"
                                            onClick={(e) => e.stopPropagation()}
                                        >
                                            {viewCollege.website}
                                        </a>
                                    ) : (
                                        <p className="text-gray-600 mt-0.5">N/A</p>
                                    )}
                                </div>
                            </div>
                        </div>

                        <div className="pt-4 flex justify-end">
                            <Button onClick={() => setViewModalOpen(false)}>
                                Close
                            </Button>
                        </div>
                    </div>
                )}
            </Modal>

            {/* Register New Institution Modal */}
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
                        placeholder="e.g. National Institute of Technology"
                        required
                        icon={Building2}
                    />
                    <Input
                        label="College Code"
                        name="code"
                        value={newCollege.code}
                        onChange={handleChange}
                        placeholder="e.g. NIT-MUM"
                        required
                    />
                    <Input
                        label="Address / Location"
                        name="address"
                        value={newCollege.address}
                        onChange={handleChange}
                        placeholder="e.g. Mumbai, Maharashtra"
                        required
                        icon={MapPin}
                    />
                    <Input
                        label="Contact Email"
                        name="contactEmail"
                        type="email"
                        value={newCollege.contactEmail}
                        onChange={handleChange}
                        placeholder="admin@college.edu"
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
                        <Button type="submit" loading={submitting} className="flex-1 bg-gray-900 hover:bg-gray-800">Register</Button>
                        <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)} className="flex-1">Cancel</Button>
                    </div>
                </form>
            </Modal>

            {/* Delete Confirmation Modal */}
            <Modal
                isOpen={deleteModalOpen}
                onClose={() => setDeleteModalOpen(false)}
                title="Confirm Deletion"
            >
                <div className="space-y-4">
                    <p className="text-gray-600">
                        Are you sure you want to delete <strong>{collegeToDelete?.name}</strong>? 
                        This action cannot be undone and will remove all associated data (users, departments, etc).
                    </p>
                    <div className="pt-4 flex gap-3">
                        <Button 
                            onClick={confirmDelete} 
                            loading={actionLoading === collegeToDelete?.id}
                            className="flex-1 bg-red-600 hover:bg-red-700 text-white"
                        >
                            Delete Permanently
                        </Button>
                        <Button 
                            variant="secondary" 
                            onClick={() => setDeleteModalOpen(false)} 
                            className="flex-1"
                        >
                            Cancel
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
