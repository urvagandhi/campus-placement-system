'use client';

import { useState, useEffect, useMemo } from 'react';

// Need to add imports first
import AlertDialog from '@/components/ui/AlertDialog';
import Input from '@/components/ui/Input';
import AddStaffDialog from '@/components/ui/AddStaffDialog';
import { 
    BookOpen, 
    Briefcase, 
    Building2, 
    ChevronDown, 
    ChevronRight, 
    Landmark, 
    Users, 
    FolderTree,
    Trash2,
    Power,
    MoreVertical,
    Search,
    UserPlus,
    Phone
} from 'lucide-react';
import api from '@/services/api';
import toast from 'react-hot-toast';
import { useAuth } from '@/hooks/useAuth';

// Role hierarchy for permission checks
const ROLE_VALUES = {
    'SUPER_ADMIN': 4,
    'ADMIN': 3,
    'COORDINATOR': 2,
    'STUDENT': 1
};

/**
 * Clean Tree Node - Matches app's existing Card design language
 */
function TreeNode({ node, level = 0, defaultExpanded = true, onRefresh, showConfirm, forceExpand = false }) {
    const { user } = useAuth();
    const [isExpanded, setIsExpanded] = useState(defaultExpanded);
    const hasChildren = node.children && node.children.length > 0;
    const [actionLoading, setActionLoading] = useState(null);

    // Auto-expand if forced (e.g., during search)
    useEffect(() => {
        if (forceExpand) {
            setIsExpanded(true);
        }
    }, [forceExpand]);

    // Check if current user can modify target staff
    const canModify = (staff) => {
        if (!user) return false;
        
        // Cannot modify self
        if (staff.userId === user.id) return false;
        
        // Cannot modify superior
        const targetLevel = ROLE_VALUES[staff.role] || 0;
        const currentLevel = ROLE_VALUES[user.role] || 0;
        
        return targetLevel <= currentLevel;
    };

    const getDisabledReason = (staff) => {
        if (staff.userId === user.id) return "Cannot modify your own account";
        
        const targetLevel = ROLE_VALUES[staff.role] || 0;
        const currentLevel = ROLE_VALUES[user.role] || 0;
        
        if (targetLevel > currentLevel) return "Cannot modify superior account";
        return "";
    };

    // Toggle Staff Status
    const handleToggleStatus = (staff) => {
        if (!canModify(staff)) return;

        showConfirm({
            title: staff.userIsActive ? 'Deactivate User?' : 'Activate User?',
            description: `Are you sure you want to ${staff.userIsActive ? 'deactivate' : 'activate'} ${staff.name}?`,
            confirmText: staff.userIsActive ? 'Deactivate' : 'Activate',
            variant: staff.userIsActive ? 'danger' : 'primary',
            onConfirm: async () => {
                try {
                    setActionLoading(staff.userId);
                    const newStatus = staff.userIsActive ? 'False' : 'Active'; // API expects 'Active' or something else? Backend says "Active".equalsIgnoreCase(status) -> true, else false.
                    // Backend logic: boolean isActive = "Active".equalsIgnoreCase(status);
                    // So sending 'Active' makes it active. Sending anything else (e.g. 'Inactive') makes it inactive.
                    
                    const statusToSend = staff.userIsActive ? 'Inactive' : 'Active';
                    const response = await api.users.updateStatus(staff.userId, statusToSend);
                    if (response.success) {
                        toast.success(`User ${statusToSend.toLowerCase()}d successfully`);
                        if (onRefresh) onRefresh();
                    } else {
                        toast.error(response.message || 'Failed to update status');
                    }
                } catch (error) {
                    console.error('Status update error:', error);
                    toast.error('Failed to update status');
                } finally {
                    setActionLoading(null);
                }
            }
        });
    };

    // Delete Staff
    const handleDeleteStaff = (staff) => {
        if (!canModify(staff)) return;

        showConfirm({
            title: 'Remove Staff?',
            description: `Are you sure you want to remove ${staff.name}? This action cannot be undone.`,
            confirmText: 'Remove',
            variant: 'danger',
            onConfirm: async () => {
                try {
                    setActionLoading(staff.userId);
                    const response = await api.users.deleteUser(staff.userId);
                    if (response.success) {
                        toast.success('Staff member removed successfully');
                        if (onRefresh) onRefresh();
                    } else {
                        toast.error(response.message || 'Failed to remove staff');
                    }
                } catch (error) {
                    console.error('Delete error:', error);
                    toast.error('Failed to remove staff');
                } finally {
                    setActionLoading(null);
                }
            }
        });
    };

    // Delete Organization Unit
    const handleDeleteUnit = (e) => {
        e.stopPropagation();
        
        showConfirm({
            title: `Delete ${node.type === 'INSTITUTE' ? 'Institute' : 'Department'}?`,
            description: `Are you sure you want to delete "${node.name}"? This will also remove ALL associated staff, students, and sub-units. This action is final.`,
            confirmText: 'Delete Everything',
            variant: 'danger',
            onConfirm: async () => {
                try {
                    setActionLoading('unit-' + node.id);
                    const response = await api.organizations.deleteUnit(node.id);
                    if (response.success) {
                        toast.success(`${node.type === 'INSTITUTE' ? 'Institute' : 'Department'} deleted successfully`);
                        if (onRefresh) onRefresh();
                    } else {
                        toast.error(response.message || 'Failed to delete unit');
                    }
                } catch (error) {
                    console.error('Delete unit error:', error);
                    toast.error(error.message || 'Failed to delete unit. Check if it has any dependencies.');
                } finally {
                    setActionLoading(null);
                }
            }
        });
    };

    const getConfig = (type) => {
        switch (type) {
            case 'UNIVERSITY':
                return {
                    icon: Landmark,
                    iconBg: 'bg-violet-100/50 text-violet-600',
                    accent: 'ring-violet-100'
                };
            case 'INSTITUTE':
                return {
                    icon: Building2,
                    iconBg: 'bg-blue-100/50 text-blue-600',
                    accent: 'ring-blue-100'
                };
            case 'DEPARTMENT':
                return {
                    icon: BookOpen,
                    iconBg: 'bg-emerald-100/50 text-emerald-600',
                    accent: 'ring-emerald-100'
                };
            default:
                return {
                    icon: FolderTree,
                    iconBg: 'bg-gray-100/50 text-gray-600',
                    accent: 'ring-gray-100'
                };
        }
    };

    const config = getConfig(node.type);
    const Icon = config.icon;
    const indent = level * 32;
    const isDeletable = level > 0 && user?.role === 'ADMIN';

    return (
        <div className="relative">
            {/* Simple connector line */}
            {level > 0 && (
                <div 
                    className="absolute left-0 top-0 bottom-0 w-px bg-gray-200"
                    style={{ left: `${(level - 1) * 32 + 12}px` }}
                />
            )}

            <div 
                className="relative flex items-start gap-3 py-3"
                style={{ paddingLeft: `${indent}px` }}
            >
                {/* Horizontal connector */}
                {level > 0 && (
                    <div 
                        className="absolute h-px bg-gray-200 top-6"
                        style={{ left: `${(level - 1) * 32 + 12}px`, width: '20px' }}
                    />
                )}

                {/* Toggle button - disabled at root level */}
                <button
                    onClick={() => hasChildren && level > 0 && setIsExpanded(!isExpanded)}
                    disabled={!hasChildren || level === 0}
                    className={`shrink-0 w-6 h-6 flex items-center justify-center rounded-md transition-colors mt-2.5
                        ${hasChildren && level > 0
                            ? 'hover:bg-gray-100 cursor-pointer text-gray-500' 
                            : 'text-gray-300 cursor-default'}`}
                >
                    {hasChildren && level > 0 && (isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />)}
                </button>

                {/* Card - matching app's Card component style */}
                <div 
                    onClick={() => hasChildren && level > 0 && setIsExpanded(!isExpanded)}
                    className={`flex-1 bg-white/60 backdrop-blur-xl rounded-2xl p-4 ring-1 ring-black/5 ${config.accent}
                        ${hasChildren && level > 0 ? 'cursor-pointer hover:ring-2 transition-all' : ''}`}
                >
                    <div className="flex items-center justify-between gap-4 flex-wrap">
                        {/* Left side - Icon & Info */}
                        <div className="flex items-center gap-4">
                            <div className={`p-3 rounded-2xl shadow-inner ${config.iconBg}`}>
                                <Icon className="h-5 w-5" />
                            </div>
                            <div>
                                <div className="flex items-center gap-2">
                                    <h3 className="font-semibold text-gray-900">{node.name}</h3>
                                    {node.code && (
                                        <span className="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                                            {node.code}
                                        </span>
                                    )}
                                </div>
                                <p className="text-sm text-gray-500 capitalize">{node.type?.toLowerCase()}</p>
                            </div>
                        </div>

                        {/* Right side - Actions */}
                        <div className="flex items-center gap-6">
                            {/* Unit Actions */}
                            {isDeletable && (
                                <button
                                    onClick={handleDeleteUnit}
                                    disabled={actionLoading === 'unit-' + node.id}
                                    className="p-1.5 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-600 transition-colors"
                                    title={`Delete ${node.type?.toLowerCase()}`}
                                >
                                    <Trash2 size={16} className={actionLoading === 'unit-' + node.id ? 'animate-pulse' : ''} />
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Staff Section */}
                    {node.staff && node.staff.length > 0 && (
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <div className="flex items-center gap-2 mb-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                                <span>Staff & Coordinators</span>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                                {node.staff.map((staff) => (
                                    <div key={staff.userId} className="relative group flex items-center gap-3 p-3 rounded-xl bg-gray-50/80 border border-gray-200/50 hover:bg-white hover:shadow-sm hover:border-blue-100 transition-all duration-200">
                                         <div className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 shadow-sm
                                            ${staff.userIsActive ? 'bg-gradient-to-br from-blue-50 to-indigo-50 text-indigo-600' : 'bg-gray-100 text-gray-400 grayscale'}`}>
                                            <span className="text-sm font-bold">{staff.name.charAt(0)}</span>
                                        </div>
                                        <div className="min-w-0 flex-1">
                                            <div className="flex items-center gap-2">
                                                <p className={`text-sm font-semibold truncate ${staff.userIsActive ? 'text-gray-900' : 'text-gray-500'}`}>
                                                    {staff.name}
                                                </p>
                                                {!staff.userIsActive && (
                                                    <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-gray-200 text-gray-500">INACTIVE</span>
                                                )}
                                            </div>
                                            <div className="flex items-center flex-wrap gap-1.5">
                                                <span className="text-xs text-gray-500 font-medium">{staff.designation || 'Staff'}</span>
                                                <span className="w-0.5 h-0.5 rounded-full bg-gray-300 shrink-0" />
                                                <span className={`text-[10px] font-bold uppercase tracking-wider ${
                                                    staff.role === 'ADMIN' ? 'text-purple-600' : 
                                                    staff.role === 'COORDINATOR' ? 'text-blue-600' : 'text-gray-500'
                                                }`}>{staff.role}</span>
                                            </div>
                                            <div className="flex flex-col gap-0.5 mt-1 text-xs text-gray-500">
                                                <span className="hover:text-gray-700">{staff.email}</span>
                                                {staff.phoneNumber && (
                                                    <span className="flex items-center gap-1">
                                                        <Phone size={10} className="stroke-2" />
                                                        {staff.phoneNumber}
                                                    </span>
                                                )}
                                            </div>
                                        </div>

                                        {/* Actions */}
                                        <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                            <button 
                                                onClick={(e) => { e.stopPropagation(); handleToggleStatus(staff); }}
                                                disabled={actionLoading === staff.userId || !canModify(staff)}
                                                className={`p-1.5 rounded-lg transition-colors ${
                                                    !canModify(staff) 
                                                        ? 'opacity-50 cursor-not-allowed text-gray-300' 
                                                        : staff.userIsActive 
                                                            ? 'hover:bg-amber-50 text-gray-400 hover:text-amber-600' 
                                                            : 'hover:bg-emerald-50 text-gray-400 hover:text-emerald-600'
                                                }`}
                                                title={!canModify(staff) ? getDisabledReason(staff) : (staff.userIsActive ? "Deactivate User" : "Activate User")}
                                            >
                                                <Power size={14} className={actionLoading === staff.userId ? 'animate-pulse' : ''} />
                                            </button>
                                            <button
                                                onClick={(e) => { e.stopPropagation(); handleDeleteStaff(staff); }}
                                                disabled={actionLoading === staff.userId || !canModify(staff)}
                                                className={`p-1.5 rounded-lg transition-colors ${
                                                    !canModify(staff)
                                                        ? 'opacity-50 cursor-not-allowed text-gray-300'
                                                        : 'hover:bg-red-50 text-gray-400 hover:text-red-600'
                                                }`}
                                                title={!canModify(staff) ? getDisabledReason(staff) : "Remove Staff"}
                                            >
                                                <Trash2 size={14} className={actionLoading === staff.userId ? 'animate-pulse' : ''} />
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Children */}
            {hasChildren && isExpanded && (
                <div>
                    {node.children.map((child, index) => (
                        <TreeNode
                            key={child.id}
                            node={child}
                            level={level + 1}
                            defaultExpanded={false}
                            onRefresh={onRefresh}
                            showConfirm={showConfirm}
                            forceExpand={forceExpand}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

const filterHierarchy = (node, query) => {
    if (!node) return null;
    if (!query) return node;

    const lowerQuery = query.toLowerCase();

    // Check if staff matches (Name, Designation, or Role)
    const matchingStaff = (node.staff || []).filter(staff =>
        (staff.name && staff.name.toLowerCase().includes(lowerQuery)) ||
        (staff.designation && staff.designation.toLowerCase().includes(lowerQuery)) ||
        (staff.role && staff.role.toLowerCase().includes(lowerQuery))
    );

    // Check if children match (recursive)
    const matchingChildren = (node.children || [])
        .map(child => filterHierarchy(child, query))
        .filter(child => child !== null);

    // Filter Logic:
    // 1. If we have matching staff, keep the node and show ONLY matching staff.
    // 2. If we have matching children, keep the node.
    // 3. If the node name itself matches, should we show it?
    //    User asked for "Search Staff", so we prioritize staff visibility.
    //    However, usually seeing the containing department is useful.
    //    If ONLY the department matches but has no matching staff/children,
    //    it might be confusing if the user expects to see staff.
    //    But let's include it for completeness, but maybe show empty staff list.

    // For "Search Staff" specifically, let's keep the node visible if:
    // a) It has matching staff
    // b) It has matching children
    // c) It matches strictly itself (optional, but good for navigation)

    const nodeMatches = node.name.toLowerCase().includes(lowerQuery) ||
                        (node.code && node.code.toLowerCase().includes(lowerQuery));

    if (matchingStaff.length > 0 || matchingChildren.length > 0 || nodeMatches) {
        return {
            ...node,
            staff: matchingStaff, // Show only matching staff
            children: matchingChildren
        };
    }

    return null;
};

/**
 * Organization Tree - Clean design matching app aesthetic
 */

export default function OrganizationTree({ hierarchy, loading = false, onRefresh }) {
    const [searchQuery, setSearchQuery] = useState('');
    const [isAddStaffOpen, setIsAddStaffOpen] = useState(false);

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

    const showConfirm = ({ title, description, confirmText, variant, onConfirm }) => {
        setAlertState({
            isOpen: true,
            title,
            description,
            confirmText,
            variant,
            onConfirm: async () => {
                setAlertState(prev => ({ ...prev, loading: true }));
                try {
                    await onConfirm();
                } finally {
                    setAlertState(prev => ({ ...prev, loading: false, isOpen: false }));
                }
            }
        });
    };

    const handleCloseAlert = () => {
        setAlertState(prev => ({ ...prev, isOpen: false }));
    };

    // Filter the hierarchy based on search query
    const filteredHierarchy = useMemo(() => {
        return filterHierarchy(hierarchy, searchQuery);
    }, [hierarchy, searchQuery]);

    return (
        <div className="space-y-4">
            {/* Header: Search & Add Staff */}
            <div className="flex items-center gap-3">
                <div className="flex-1 max-w-md">
                    <Input
                        placeholder="Search staff by name, designation or role..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        icon={Search}
                        className="bg-white/50"
                    />
                </div>
                <button
                    onClick={() => setIsAddStaffOpen(true)}
                    className="flex items-center gap-2 px-4 py-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition-colors shadow-sm font-medium whitespace-nowrap"
                >
                    <UserPlus size={18} />
                    Add Staff
                </button>
            </div>

            {/* Tree Content */}
            {loading ? (
                <div className="space-y-4">
                    {[1, 2, 3].map((i) => (
                        <div key={i} className="flex gap-3 animate-pulse" style={{ paddingLeft: `${(i - 1) * 32}px` }}>
                            <div className="w-6 h-6 bg-gray-100 rounded" />
                            <div className="flex-1 h-20 bg-gray-100/50 rounded-2xl" />
                        </div>
                    ))}
                </div>
            ) : !hierarchy ? (
                <div className="flex flex-col items-center justify-center py-16 text-center">
                    <div className="p-4 bg-gray-100/50 rounded-2xl mb-4">
                        <FolderTree className="w-8 h-8 text-gray-400" />
                    </div>
                    <h3 className="text-lg font-semibold text-gray-900">No Organization Structure</h3>
                    <p className="text-sm text-gray-500 mt-1 max-w-sm">
                        Add institutes and departments to build your hierarchy.
                    </p>
                </div>
            ) : filteredHierarchy ? (
                <div className="py-2">
                    <TreeNode
                        node={filteredHierarchy}
                        level={0}
                        defaultExpanded={true}
                        onRefresh={onRefresh}
                        showConfirm={showConfirm}
                        forceExpand={!!searchQuery}
                    />
                </div>
            ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center text-gray-500 bg-gray-50/50 rounded-2xl border border-dashed border-gray-200">
                    <Search className="w-6 h-6 mb-2 opacity-50" />
                    <p className="text-sm font-medium">No results found for &quot;{searchQuery}&quot;</p>
                </div>
            )}

            <AddStaffDialog 
                isOpen={isAddStaffOpen} 
                onClose={() => setIsAddStaffOpen(false)} 
                onSuccess={onRefresh}
                hierarchy={hierarchy}
            />

            <AlertDialog
                isOpen={alertState.isOpen}
                onClose={handleCloseAlert}
                onConfirm={alertState.onConfirm}
                title={alertState.title}
                description={alertState.description}
                confirmText={alertState.confirmText}
                variant={alertState.variant}
                loading={alertState.loading}
            />
        </div>
    );
}

