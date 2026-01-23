'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Table from '@/components/ui/Table';
import { useAuth } from '@/hooks/useAuth';
import api from '@/services/api';
import { Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function ManageUsers() {
    const { user: currentUser } = useAuth();
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);

    // Debounce search
    useEffect(() => {
        const fetchUsers = async () => {
             setLoading(true);
             try {
                 const response = await api.users.getAll(searchTerm);
                 if (response.success) {
                     setUsers(response.data);
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
        
        // Prevent deactivating self
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
            console.error('Status update error:', error);
            toast.error('Failed to update status');
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        if (!currentUser) return;

        // Prevent demoting self
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
            console.error('Role update error:', error);
            toast.error('Failed to update role');
        }
    };

    const columns = [
        { header: 'Name', accessor: 'name' },
        { header: 'Email', accessor: 'email' },
        {
            header: 'Role',
            accessor: 'role',
            render: (role, row) => (
                <select
                    value={role}
                    onChange={(e) => handleRoleChange(row.id, e.target.value)}
                    className="block w-full pl-3 pr-10 py-1 text-xs border-gray-300 focus:outline-none focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm rounded-md bg-white border"
                    disabled={currentUser && row.id === currentUser.id}
                >
                    <option value="STUDENT">Student</option>
                    <option value="COORDINATOR">Coordinator</option>
                    <option value="ADMIN">Admin</option>
                </select>
            )
        },
        {
            header: 'Status',
            accessor: 'status',
            render: (status) => (
                <Badge variant={status === 'Active' ? 'success' : 'error'}>
                    {status}
                </Badge>
            )
        },
        {
            header: 'Actions',
            accessor: 'actions',
            render: (_, row) => (
                <Button
                    variant={row.status === 'Active' ? 'danger' : 'secondary'}
                    size="sm"
                    onClick={() => handleStatusToggle(row.id, row.status)}
                    disabled={currentUser && row.id === currentUser.id}
                >
                    {row.status === 'Active' ? 'Deactivate' : 'Activate'}
                </Button>
            )
        }
    ];

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <h1 className="text-2xl font-bold text-gray-900">Manage Users</h1>
                <div className="relative w-full sm:w-64">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <Search className="h-4 w-4 text-gray-400" />
                    </div>
                    <input
                        type="text"
                        placeholder="Search users..."
                        className="pl-10 block w-full shadow-sm focus:ring-emerald-500 focus:border-emerald-500 sm:text-sm border-gray-300 rounded-md py-2 border"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            <Card className="border border-gray-100 shadow-sm">
                <Table
                    columns={columns}
                    data={users}
                    isLoading={loading}
                    emptyMessage="No users found"
                />
            </Card>
        </div>
    );
}
