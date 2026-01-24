'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import PhoneInput from '@/components/ui/PhoneInput';
import Modal from '@/components/ui/Modal';
import Table from '@/components/ui/Table';
import AlertDialog from '@/components/ui/AlertDialog'; // Import new component
import api from '@/services/api';
import { Building2, Globe, MapPin, Plus, Search, Trash2, Ban, CheckCircle, Phone, User, Mail, Copy, Check, Key } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CardSkeleton, ListSkeleton, Skeleton } from '@/components/ui/Skeleton';
import { formatPhoneForDisplay } from '@/utils/phoneUtils';

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
        contactPhone: '',
        website: '',
        // Admin account fields
        adminName: '',
        adminEmail: '',
        adminPhone: ''
    });

    // Credentials modal state (shown after successful registration)
    const [credentialsModal, setCredentialsModal] = useState({
        isOpen: false,
        adminEmail: '',
        temporaryPassword: '',
        collegeName: ''
    });
    const [copiedField, setCopiedField] = useState(null);


    // Alert Dialog State
    const [alertState, setAlertState] = useState({
        isOpen: false,
        title: '',
        description: '',
        confirmText: 'Confirm',
        variant: 'primary',
        onConfirm: () => {},
        loading: false
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
        (college.name || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (college.address || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (college.code || '').toLowerCase().includes(searchTerm.toLowerCase())
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
                            handleStatusToggleClick(college);
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
    const [viewModalOpen, setViewModalOpen] = useState(false);
    const [viewCollege, setViewCollege] = useState(null);

    // --- Actions ---

    // 1. Status Toggle (Activate/Deactivate)
    const handleStatusToggleClick = (college) => {
        const isDeactivating = college.isActive;
        setAlertState({
            isOpen: true,
            title: isDeactivating ? 'Deactivate College?' : 'Activate College?',
            description: `Are you sure you want to ${isDeactivating ? 'deactivate' : 'activate'} ${college.name}? Users may lose access.`,
            confirmText: isDeactivating ? 'Deactivate' : 'Activate',
            variant: isDeactivating ? 'danger' : 'primary',
            onConfirm: () => performStatusUpdate(college)
        });
    };

    const performStatusUpdate = async (college) => {
        setAlertState(prev => ({ ...prev, loading: true }));
        try {
            setActionLoading(college.id);
            const response = await api.users.updateCollegeStatus(college.id, !college.isActive);
            if (response.success) {
                toast.success(`${college.name} has been ${college.isActive ? 'deactivated' : 'activated'} successfully`);
                setColleges(prev => prev.map(c => c.id === college.id ? response.data : c));
                setAlertState(prev => ({ ...prev, isOpen: false }));
            } else {
                toast.error(response.message || 'Failed to update status');
            }
        } catch (error) {
            console.error('Status update error:', error);
            toast.error('Failed to update status');
        } finally {
            setActionLoading(null);
            setAlertState(prev => ({ ...prev, loading: false }));
        }
    };

    // 2. Delete College
    const handleDeleteClick = (college) => {
        setAlertState({
            isOpen: true,
            title: 'Delete College?',
            description: `Are you sure you want to delete ${college.name}? This action cannot be undone.`,
            confirmText: 'Delete',
            variant: 'danger',
            onConfirm: () => performDelete(college)
        });
    };

    const performDelete = async (college) => {
        setAlertState(prev => ({ ...prev, loading: true }));
        try {
            setActionLoading(college.id);
            const response = await api.users.deleteCollege(college.id);
            if (response.success) {
                toast.success(`${college.name} has been deleted successfully`);
                setColleges(prev => prev.filter(c => c.id !== college.id));
                setAlertState(prev => ({ ...prev, isOpen: false }));
            } else {
                toast.error(response.message || 'Failed to delete college');
            }
        } catch (error) {
            console.error('Delete error:', error);
            toast.error('Failed to delete college');
        } finally {
            setActionLoading(null);
            setAlertState(prev => ({ ...prev, loading: false }));
        }
    };

    // 3. Create College
    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const response = await api.users.createCollege(newCollege);
            if (response.success) {
                const { college, adminEmail, temporaryPassword } = response.data;
                
                // Add to list (use college from onboarding response)
                setColleges(prev => [...prev, college]);
                setIsModalOpen(false);
                
                // Reset form
                setNewCollege({ 
                    name: '', code: '', address: '', contactEmail: '', 
                    contactPhone: '', website: '', adminName: '', adminEmail: '', adminPhone: '' 
                });
                
                // Show credentials modal
                setCredentialsModal({
                    isOpen: true,
                    adminEmail: adminEmail,
                    temporaryPassword: temporaryPassword,
                    collegeName: college.name
                });
                
                toast.success(`${college.name} registered successfully!`);
            } else {
                if (response.data && typeof response.data === 'object' && !response.data.college) {
                    const errorMessages = Object.entries(response.data)
                        .map(([field, msg]) => `${msg}`)
                        .join('\n');
                    toast.error(errorMessages || response.message || 'Failed to register college');
                } else {
                    toast.error(response.message || 'Failed to register college');
                }
            }
        } catch (error) {
            console.error('Create college error:', error);
            toast.error(error.message || 'Failed to register college');
        } finally {
            setSubmitting(false);
        }
    };

    // Copy to clipboard helper
    const copyToClipboard = (text, field) => {
        navigator.clipboard.writeText(text);
        setCopiedField(field);
        setTimeout(() => setCopiedField(null), 2000);
        toast.success('Copied to clipboard!');
    };

    const handleRowClick = (college) => {
        setViewCollege(college);
        setViewModalOpen(true);
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
                        {/* Header */}
                        <div className="flex items-center justify-between pb-4 border-b border-gray-100">
                            <div>
                                <h3 className="text-lg font-bold text-gray-900">{viewCollege.name}</h3>
                                <p className="text-gray-500">{viewCollege.code}</p>
                            </div>
                            <Badge variant={viewCollege.isActive ? 'success' : 'neutral'}>
                                {viewCollege.isActive ? 'Active' : 'Inactive'}
                            </Badge>
                        </div>

                        {/* Institution Details */}
                        <div className="space-y-3">
                            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Institution Details</h4>
                            <div className="grid grid-cols-1 gap-3 text-sm">
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                        <Building2 className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Contact Email</p>
                                        <p className="text-gray-600 mt-0.5">{viewCollege.contactEmail || 'N/A'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                        <Phone className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Contact Phone</p>
                                        <p className="text-gray-600 mt-0.5">{formatPhoneForDisplay(viewCollege.contactPhone) || 'N/A'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                        <MapPin className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Address</p>
                                        <p className="text-gray-600 mt-0.5">{viewCollege.address || 'N/A'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                                        <Globe className="h-4 w-4" />
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
                                            <p className="text-gray-400 mt-0.5">N/A</p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Admin Account */}
                        <div className="space-y-3 pt-4 border-t border-gray-100">
                            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-1">
                                <Key className="h-3 w-3" /> Admin Account
                            </h4>
                            <div className="grid grid-cols-1 gap-3 text-sm">
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
                                        <User className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Admin Name</p>
                                        <p className="text-gray-600 mt-0.5">{viewCollege.adminName || 'N/A'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
                                        <Mail className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Admin Email</p>
                                        <p className="text-gray-600 mt-0.5">{viewCollege.adminEmail || 'N/A'}</p>
                                    </div>
                                </div>
                                <div className="flex items-start gap-3">
                                    <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
                                        <Phone className="h-4 w-4" />
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">Admin Phone</p>
                                        <p className="text-gray-600 mt-0.5">{formatPhoneForDisplay(viewCollege.adminPhone) || 'N/A'}</p>
                                    </div>
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
                size="lg"
            >
                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* College Details Section */}
                    <div className="space-y-4">
                        <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-2">
                            <Building2 className="h-4 w-4" />
                            College Details
                        </h3>
                        <Input
                            label="Institution Name"
                            name="name"
                            value={newCollege.name}
                            onChange={handleChange}
                            placeholder="e.g. National Institute of Technology"
                            required
                            icon={Building2}
                        />
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <Input
                                label="Contact Email (Institutional)"
                                name="contactEmail"
                                type="email"
                                value={newCollege.contactEmail}
                                onChange={handleChange}
                                placeholder="admissions@college.edu"
                                icon={Mail}
                            />
                            <PhoneInput
                                label="Contact Phone"
                                value={newCollege.contactPhone}
                                onChange={(e) => handleChange({ target: { name: 'contactPhone', value: e.target.value } })}
                            />
                        </div>
                        <Input
                            label="Website"
                            name="website"
                            value={newCollege.website}
                            onChange={handleChange}
                            placeholder="https://..."
                            icon={Globe}
                        />
                    </div>

                    {/* Admin Account Section */}
                    <div className="space-y-4 pt-4 border-t border-gray-100">
                        <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-2">
                            <User className="h-4 w-4" />
                            Admin Account (Auto-Created)
                        </h3>
                        <p className="text-xs text-gray-500">
                            An admin account will be created with a temporary password. Share the credentials with the college admin.
                        </p>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <Input
                                label="Admin Name"
                                name="adminName"
                                value={newCollege.adminName}
                                onChange={handleChange}
                                placeholder="e.g. Dr. Rajesh Kumar"
                                required
                                icon={User}
                            />
                            <Input
                                label="Admin Email (Login)"
                                name="adminEmail"
                                type="email"
                                value={newCollege.adminEmail}
                                onChange={handleChange}
                                placeholder="admin@college.edu"
                                required
                                icon={Mail}
                            />
                        </div>
                        <PhoneInput
                            label="Admin Phone (Optional)"
                            value={newCollege.adminPhone}
                            onChange={(e) => handleChange({ target: { name: 'adminPhone', value: e.target.value } })}
                        />
                    </div>

                    <div className="pt-4 flex gap-3">
                        <Button type="submit" loading={submitting} className="flex-1 bg-gray-900 hover:bg-gray-800">
                            Register College
                        </Button>
                        <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)} className="flex-1">
                            Cancel
                        </Button>
                    </div>
                </form>
            </Modal>

            {/* Premium Apple-style Alert Dialog */}
            <AlertDialog
                isOpen={alertState.isOpen}
                onClose={() => setAlertState(prev => ({ ...prev, isOpen: false }))}
                onConfirm={alertState.onConfirm}
                title={alertState.title}
                description={alertState.description}
                confirmText={alertState.confirmText}
                variant={alertState.variant}
                loading={alertState.loading}
            />

            {/* Admin Credentials Modal */}
            <Modal
                isOpen={credentialsModal.isOpen}
                onClose={() => setCredentialsModal(prev => ({ ...prev, isOpen: false }))}
                title="Admin Credentials Created"
            >
                <div className="space-y-6">
                    <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl">
                        <div className="flex items-center gap-2 text-emerald-700 font-medium mb-2">
                            <CheckCircle className="h-5 w-5" />
                            College Registered Successfully
                        </div>
                        <p className="text-sm text-emerald-600">
                            <strong>{credentialsModal.collegeName}</strong> has been registered. 
                            An admin account has been created with the following credentials.
                        </p>
                    </div>

                    <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
                        <div className="flex items-center gap-2 text-amber-700 font-medium mb-3">
                            <Key className="h-4 w-4" />
                            Share these credentials securely
                        </div>
                        
                        <div className="space-y-3">
                            <div className="flex items-center justify-between bg-white rounded-lg p-3 border border-amber-100">
                                <div>
                                    <p className="text-xs text-gray-500 uppercase">Admin Email</p>
                                    <p className="font-mono text-sm font-medium text-gray-900">{credentialsModal.adminEmail}</p>
                                </div>
                                <button
                                    onClick={() => copyToClipboard(credentialsModal.adminEmail, 'email')}
                                    className="p-2 hover:bg-amber-100 rounded-lg transition-colors"
                                >
                                    {copiedField === 'email' ? (
                                        <Check className="h-4 w-4 text-emerald-600" />
                                    ) : (
                                        <Copy className="h-4 w-4 text-gray-500" />
                                    )}
                                </button>
                            </div>
                            
                            <div className="flex items-center justify-between bg-white rounded-lg p-3 border border-amber-100">
                                <div>
                                    <p className="text-xs text-gray-500 uppercase">Temporary Password</p>
                                    <p className="font-mono text-sm font-medium text-gray-900">{credentialsModal.temporaryPassword}</p>
                                </div>
                                <button
                                    onClick={() => copyToClipboard(credentialsModal.temporaryPassword, 'password')}
                                    className="p-2 hover:bg-amber-100 rounded-lg transition-colors"
                                >
                                    {copiedField === 'password' ? (
                                        <Check className="h-4 w-4 text-emerald-600" />
                                    ) : (
                                        <Copy className="h-4 w-4 text-gray-500" />
                                    )}
                                </button>
                            </div>
                        </div>
                        
                        <p className="text-xs text-amber-600 mt-3">
                            The admin will be required to change their password on first login.
                        </p>
                    </div>

                    <div className="pt-2">
                        <Button 
                            onClick={() => setCredentialsModal(prev => ({ ...prev, isOpen: false }))}
                            className="w-full bg-gray-900 hover:bg-gray-800"
                        >
                            Done
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}

