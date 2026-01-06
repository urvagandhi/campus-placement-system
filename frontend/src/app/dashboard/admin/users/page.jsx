'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Table from '@/components/ui/Table';
import { Search } from 'lucide-react';
import { useState } from 'react';

// Mock Data
const MOCK_USERS = [
    { id: 1, name: 'Admin User', email: 'admin@college.edu', role: 'ADMIN', status: 'Active' },
    { id: 2, name: 'John Doe', email: 'john@student.edu', role: 'STUDENT', status: 'Active' },
    { id: 3, name: 'Jane Smith', email: 'jane@coordinator.edu', role: 'COORDINATOR', status: 'Active' },
    { id: 4, name: 'Inactive Student', email: 'inactive@student.edu', role: 'STUDENT', status: 'Inactive' },
    { id: 5, name: 'New TPO', email: 'tpo@college.edu', role: 'COORDINATOR', status: 'Active' },
];

export default function ManageUsers() {
    const [users, setUsers] = useState(MOCK_USERS);
    const [searchTerm, setSearchTerm] = useState('');
    const currentUserEmail = 'admin@college.edu'; // Mock current user

    const handleStatusToggle = (userId) => {
        setUsers(users.map(user => {
            if (user.id === userId) {
                // Prevent deactivating self
                if (user.email === currentUserEmail) {
                    alert('You cannot deactivate your own account.');
                    return user;
                }
                return { ...user, status: user.status === 'Active' ? 'Inactive' : 'Active' };
            }
            return user;
        }));
    };

    const handleRoleChange = (userId, newRole) => {
        setUsers(users.map(user => {
            if (user.id === userId) {
                // Prevent demoting self
                if (user.email === currentUserEmail) {
                    alert('You cannot change your own role.');
                    return user;
                }
                return { ...user, role: newRole };
            }
            return user;
        }));
    };

    const filteredUsers = users.filter(user =>
        user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

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
                    disabled={row.email === currentUserEmail}
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
                    onClick={() => handleStatusToggle(row.id)}
                    disabled={row.email === currentUserEmail}
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
                    data={filteredUsers}
                    emptyMessage="No users found"
                />
            </Card>
        </div>
    );
}
