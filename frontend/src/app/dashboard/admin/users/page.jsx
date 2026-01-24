'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import api from '@/services/api';
import { Search, Crown, Building2, UserCog, Mail, Users, ChevronRight, Trash2, AlertTriangle, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function ManageUsers() {
    const { user: currentUser } = useAuth();
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [deleteModal, setDeleteModal] = useState({ isOpen: false, user: null });
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        const fetchUsers = async () => {
             setLoading(true);
             try {
                 const response = await api.users.getAll(searchTerm);
                 if (response.success) {
                     const staffUsers = response.data.filter(u => u.role !== 'STUDENT');
                     setUsers(staffUsers);
                 }
             } catch (error) {
                 console.error('Failed to fetch users:', error);
                 toast.error('Failed to load users');
             } finally {
                 setLoading(false);
             }
        };

        const timer = setTimeout(fetchUsers, 500);
        return () => clearTimeout(timer);
    }, [searchTerm]);

    const handleStatusToggle = async (userId, currentStatus) => {
        if (!currentUser) return;
        if (userId === currentUser.id) {
            toast.error('You cannot deactivate your own account.');
            return;
        }

        const newStatus = currentStatus === 'Active' ? 'Inactive' : 'Active';
        
        try {
            const response = await api.users.updateStatus(userId, newStatus);
            if (response.success) {
                setUsers(prev => prev.map(u => u.id === userId ? { ...u, status: newStatus, isActive: newStatus === 'Active' } : u));
                toast.success(`User ${newStatus === 'Active' ? 'activated' : 'deactivated'}`);
            } else {
                 toast.error(response.message || 'Update failed');
            }
        } catch (error) {
            toast.error('Failed to update status');
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        if (!currentUser) return;
        if (userId === currentUser.id) {
             toast.error('You cannot change your own role.');
             return;
        }

        try {
            const response = await api.users.updateRole(userId, newRole);
            if (response.success) {
                setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
                toast.success('Role updated successfully');
            } else {
                toast.error(response.message || 'Role update failed');
            }
        } catch (error) {
            toast.error('Failed to update role');
        }
    };

    const openDeleteModal = (user) => {
        if (currentUser && user.id === currentUser.id) {
            toast.error('You cannot remove your own account.');
            return;
        }
        setDeleteModal({ isOpen: true, user });
    };

    const closeDeleteModal = () => {
        setDeleteModal({ isOpen: false, user: null });
    };

    const confirmDelete = async () => {
        if (!deleteModal.user) return;
        
        setDeleting(true);
        try {
            const response = await api.users.delete(deleteModal.user.id);
            if (response.success) {
                setUsers(prev => prev.filter(u => u.id !== deleteModal.user.id));
                toast.success('Staff member removed successfully');
                closeDeleteModal();
            } else {
                toast.error(response.message || 'Failed to remove staff member');
            }
        } catch (error) {
            toast.error('Failed to remove staff member');
        } finally {
            setDeleting(false);
        }
    };

    // Extract institute from email (e.g., admin.it@ -> IT, coord.cse@ -> CSE under IT, tpo.law@ -> Law)
    const getStaffInstitute = (email) => {
        const prefix = email?.split('@')[0] || '';
        if (prefix === 'admin') return 'UNIVERSITY';
        
        // Institute admins: admin.it, admin.law, etc.
        if (prefix.startsWith('admin.')) {
            return prefix.replace('admin.', '').toUpperCase();
        }
        
        // TPO: tpo.law, tpo.it, etc.
        if (prefix.startsWith('tpo.')) {
            return prefix.replace('tpo.', '').toUpperCase();
        }
        
        // Department coordinators: coord.cse, coord.ce, coord.me
        // Map departments to their institutes
        if (prefix.startsWith('coord.')) {
            const dept = prefix.replace('coord.', '').toUpperCase();
            // CSE, CE, ME are under IT
            if (['CSE', 'CE', 'ME', 'EE', 'EC'].includes(dept)) return 'IT';
            // Law departments under LAW
            if (['LAW', 'CIVIL', 'CRIMINAL'].includes(dept)) return 'LAW';
            return dept;
        }
        
        return 'OTHER';
    };

    // Detect university admin
    const isUniversityAdmin = (user) => {
        if (user.role !== 'ADMIN') return false;
        const emailPrefix = user.email?.split('@')[0] || '';
        return emailPrefix === 'admin' || !emailPrefix.includes('.');
    };

    // Group users
    const universityAdmin = users.filter(u => isUniversityAdmin(u));
    
    // Group by institute
    const instituteGroups = {};
    users.filter(u => !isUniversityAdmin(u)).forEach(user => {
        const institute = getStaffInstitute(user.email);
        if (institute !== 'UNIVERSITY' && institute !== 'OTHER') {
            if (!instituteGroups[institute]) {
                instituteGroups[institute] = { admin: null, coordinators: [] };
            }
            if (user.role === 'ADMIN') {
                instituteGroups[institute].admin = user;
            } else {
                instituteGroups[institute].coordinators.push(user);
            }
        }
    });

    // Institute display names
    const instituteNames = {
        'IT': 'Institute of Technology',
        'LAW': 'Institute of Law',
        'MANAGEMENT': 'Institute of Management',
        'PHARMACY': 'Institute of Pharmacy',
        'SCIENCE': 'Institute of Science'
    };

    const StaffCard = ({ user, variant = 'default' }) => {
        const isCurrentUser = currentUser && user.id === currentUser.id;
        const initials = user.name?.split(' ').map(n => n[0]).join('').toUpperCase() || '?';
        
        const variants = {
            university: { avatar: 'bg-gradient-to-br from-amber-500 to-orange-600', border: 'border-amber-100' },
            admin: { avatar: 'bg-gradient-to-br from-blue-500 to-blue-600', border: 'border-blue-100' },
            coordinator: { avatar: 'bg-gradient-to-br from-violet-500 to-violet-600', border: 'border-violet-100' },
            default: { avatar: 'bg-gradient-to-br from-gray-500 to-gray-600', border: 'border-gray-100' }
        };
        
        const style = variants[variant] || variants.default;
        
        return (
            <div className={`bg-white rounded-xl border ${style.border} p-4 transition-all hover:shadow-md`}>
                <div className="flex items-start gap-3">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm shrink-0 ${style.avatar}`}>
                        {initials}
                    </div>
                    
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-0.5">
                            <h4 className="font-semibold text-gray-900 text-sm truncate">{user.name}</h4>
                            {isCurrentUser && (
                                <span className="text-[9px] font-medium text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">You</span>
                            )}
                        </div>
                        <p className="text-xs text-gray-500 truncate mb-2">{user.email}</p>
                        
                        <div className="flex items-center gap-2 flex-wrap">
                            <Badge variant={user.status === 'Active' ? 'success' : 'error'} className="text-[10px]">
                                {user.status}
                            </Badge>
                            <div className="flex gap-1.5 ml-auto">
                                <Button
                                    variant={user.status === 'Active' ? 'danger' : 'secondary'}
                                    size="sm"
                                    onClick={() => handleStatusToggle(user.id, user.status)}
                                    disabled={isCurrentUser}
                                    className="text-[10px] px-2 py-1"
                                >
                                    {user.status === 'Active' ? 'Deactivate' : 'Activate'}
                                </Button>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => openDeleteModal(user)}
                                    disabled={isCurrentUser}
                                    className="text-[10px] px-2 py-1 text-red-600 border-red-200 hover:bg-red-50"
                                >
                                    <Trash2 size={12} />
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    return (
        <div className="space-y-8">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Staff Management</h1>
                    <p className="text-gray-500 text-sm mt-1">Manage staff across institutes and departments</p>
                </div>
                <div className="relative w-full sm:w-64">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Search className="h-4 w-4 text-gray-400" />
                    </div>
                    <input
                        type="text"
                        placeholder="Search staff..."
                        className="pl-10 block w-full shadow-sm focus:ring-indigo-500 focus:border-indigo-500 text-sm border-gray-200 rounded-lg py-2.5 border bg-white/60 backdrop-blur-xl"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div className="space-y-6 animate-pulse">
                    <div className="h-32 bg-amber-50 rounded-xl" />
                    <div className="h-48 bg-blue-50 rounded-xl" />
                    <div className="h-48 bg-blue-50 rounded-xl" />
                </div>
            ) : users.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 text-center">
                    <div className="p-4 bg-gray-100/50 rounded-2xl mb-4">
                        <Users className="w-8 h-8 text-gray-400" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900">No Staff Found</h3>
                    <p className="text-sm text-gray-500 mt-1 max-w-sm">
                        {searchTerm ? 'Try adjusting your search terms.' : 'No staff members found.'}
                    </p>
                </div>
            ) : (
                <div className="space-y-8">
                    {/* University Administration */}
                    {universityAdmin.length > 0 && (
                        <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-2xl border border-amber-100 p-6">
                            <div className="flex items-center gap-3 mb-4">
                                <div className="p-2.5 rounded-xl bg-amber-100 text-amber-600">
                                    <Crown size={20} />
                                </div>
                                <div>
                                    <h3 className="font-bold text-gray-900">University Administration</h3>
                                    <p className="text-xs text-amber-700">Top-level university management</p>
                                </div>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {universityAdmin.map(user => (
                                    <StaffCard key={user.id} user={user} variant="university" />
                                ))}
                            </div>
                        </div>
                    )}
                    
                    {/* Institute Sections */}
                    {Object.entries(instituteGroups).map(([code, group]) => (
                        <div key={code} className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl border border-blue-100 overflow-hidden">
                            {/* Institute Header */}
                            <div className="px-6 py-4 border-b border-blue-100/50">
                                <div className="flex items-center gap-3">
                                    <div className="p-2.5 rounded-xl bg-blue-100 text-blue-600">
                                        <Building2 size={20} />
                                    </div>
                                    <div>
                                        <h3 className="font-bold text-gray-900">{instituteNames[code] || code}</h3>
                                        <p className="text-xs text-blue-700">
                                            {group.admin ? '1 Admin' : '0 Admins'} · {group.coordinators.length} Coordinator{group.coordinators.length !== 1 ? 's' : ''}
                                        </p>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="p-6 space-y-6">
                                {/* Institute Admin */}
                                {group.admin && (
                                    <div>
                                        <div className="flex items-center gap-2 text-xs font-semibold text-blue-700 uppercase tracking-wider mb-3">
                                            <ChevronRight size={14} />
                                            Institute Admin
                                        </div>
                                        <StaffCard user={group.admin} variant="admin" />
                                    </div>
                                )}
                                
                                {/* Department Coordinators */}
                                {group.coordinators.length > 0 && (
                                    <div>
                                        <div className="flex items-center gap-2 text-xs font-semibold text-violet-700 uppercase tracking-wider mb-3">
                                            <ChevronRight size={14} />
                                            Department Coordinators
                                        </div>
                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                            {group.coordinators.map(user => (
                                                <StaffCard key={user.id} user={user} variant="coordinator" />
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Delete Confirmation Modal */}
            {deleteModal.isOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center">
                    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" onClick={closeDeleteModal} />
                    <div className="relative bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 p-6 animate-in fade-in zoom-in-95 duration-200">
                        <button
                            onClick={closeDeleteModal}
                            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            <X size={20} />
                        </button>
                        
                        <div className="flex items-start gap-4">
                            <div className="p-3 bg-red-100 rounded-xl">
                                <AlertTriangle className="w-6 h-6 text-red-600" />
                            </div>
                            <div className="flex-1">
                                <h3 className="text-lg font-semibold text-gray-900">Remove Staff Member</h3>
                                <p className="mt-2 text-sm text-gray-600">
                                    Are you sure you want to remove <strong>{deleteModal.user?.name}</strong>? 
                                    This action cannot be undone.
                                </p>
                            </div>
                        </div>
                        
                        <div className="mt-6 flex gap-3 justify-end">
                            <Button
                                variant="secondary"
                                onClick={closeDeleteModal}
                                disabled={deleting}
                            >
                                Cancel
                            </Button>
                            <Button
                                variant="danger"
                                onClick={confirmDelete}
                                disabled={deleting}
                            >
                                {deleting ? 'Removing...' : 'Remove'}
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}



