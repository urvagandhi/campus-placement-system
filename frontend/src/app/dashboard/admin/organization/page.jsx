'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import OrganizationTree from '@/components/ui/OrganizationTree';

import api from '@/services/api';
import { Plus, RefreshCw } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';

export default function Departments() {
    const [hierarchy, setHierarchy] = useState(null);
    const [institutes, setInstitutes] = useState([]);
    const [showCreateInstituteModal, setShowCreateInstituteModal] = useState(false);
    const [showCreateDeptModal, setShowCreateDeptModal] = useState(false);
    const [loading, setLoading] = useState(true);



    // Form State for New Department
    const [newDept, setNewDept] = useState({
        name: '',
        code: '',
        type: 'DEPARTMENT',
        parentUnitId: ''
    });
    
    // Form State for New Institute
    const [newInstitute, setNewInstitute] = useState({
        name: '',
        code: '',
        type: 'INSTITUTE',
        parentUnitId: null
    });

    // Fetch data on component mount
    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [hierarchyRes, instRes] = await Promise.all([
                api.organizations.getHierarchy(),
                api.organizations.getInstitutes()
            ]);

            if (hierarchyRes.success) {
                setHierarchy(hierarchyRes.data);
            } else {
                console.error('Failed to fetch hierarchy:', hierarchyRes.message);
            }

            if (instRes.success) {
                setInstitutes(instRes.data || []);
            } else {
                console.error('Failed to fetch institutes:', instRes.message);
            }


        } catch (error) {
            console.error('Failed to fetch data:', error);
            toast.error('Failed to load department data');
        } finally {
            setLoading(false);
        }
    };



    const handleCreateInstitute = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newInstitute,
                parentUnitId: null
            };
            const response = await api.organizations.createUnit(payload);
            if (response.success) {
                toast.success('Institute created successfully');
                setShowCreateInstituteModal(false);
                setNewInstitute({ name: '', code: '', type: 'INSTITUTE', parentUnitId: null });
                fetchData(); // Refresh hierarchy
            } else {
                toast.error(response.message || 'Failed to create institute');
            }
        } catch (error) {
            console.error('Create institute error:', error);
            toast.error(error.message || 'Failed to create institute');
        }
    };

    const handleCreateDept = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...newDept,
                parentUnitId: parseInt(newDept.parentUnitId)
            };
            const response = await api.organizations.createUnit(payload);
            if (response.success) {
                toast.success('Department created successfully');
                setShowCreateDeptModal(false);
                setNewDept({ name: '', code: '', type: 'DEPARTMENT', parentUnitId: '' });
                fetchData(); // Refresh hierarchy
            } else {
                toast.error(response.message || 'Failed to create department');
            }
        } catch (error) {
            console.error('Create department error:', error);
            toast.error(error.message || 'Failed to create department');
        }
    };



    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Departments & Hierarchy</h1>
                    <p className="text-gray-500">Manage your organization structure and track placement statistics</p>
                </div>
                <div className="flex items-center gap-3">
                    <Button 
                        variant="outline" 
                        size="sm" 
                        onClick={fetchData}
                        disabled={loading}
                    >
                        <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                        Refresh
                    </Button>
                    <Button size="sm" onClick={() => setShowCreateInstituteModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Institute
                    </Button>
                    <Button size="sm" onClick={() => setShowCreateDeptModal(true)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Department
                    </Button>
                </div>
            </div>

            {/* Organization Tree */}
            <div className="relative overflow-hidden rounded-2xl border border-gray-200/50 bg-gradient-to-b from-gray-50/50 to-white/50 backdrop-blur-xl">
                <div className="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]" />
                <div className="relative p-6 min-h-[300px]">
                    <OrganizationTree hierarchy={hierarchy} loading={loading} onRefresh={fetchData} />
                </div>
            </div>



            {/* Add Institute Modal */}
            <Modal
                isOpen={showCreateInstituteModal}
                onClose={() => setShowCreateInstituteModal(false)}
                title="Add New Institute"
            >
                <form onSubmit={handleCreateInstitute} className="space-y-4">
                    <Input
                        label="Institute Name"
                        value={newInstitute.name}
                        onChange={(e) => setNewInstitute({ ...newInstitute, name: e.target.value })}
                        placeholder="e.g. Institute of Science"
                        required
                    />
                    
                    <Input
                        label="Institute Code"
                        value={newInstitute.code}
                        onChange={(e) => setNewInstitute({ ...newInstitute, code: e.target.value.toUpperCase() })}
                        placeholder="e.g. IOS"
                        required
                    />

                    <Button type="submit" className="w-full">
                        Create Institute
                    </Button>
                </form>
            </Modal>


            
            {/* Add Department Modal */}
            <Modal
                isOpen={showCreateDeptModal}
                onClose={() => setShowCreateDeptModal(false)}
                title="Add New Department"
            >
                <form onSubmit={handleCreateDept} className="space-y-4">
                    <Input
                        label="Department Name"
                        value={newDept.name}
                        onChange={(e) => setNewDept({ ...newDept, name: e.target.value })}
                        placeholder="e.g. Chemical Engineering"
                        required
                    />
                    
                    <Input
                        label="Department Code"
                        value={newDept.code}
                        onChange={(e) => setNewDept({ ...newDept, code: e.target.value.toUpperCase() })}
                        placeholder="e.g. CHEM"
                        required
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Parent Institute</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                            value={newDept.parentUnitId}
                            onChange={(e) => setNewDept({ ...newDept, parentUnitId: e.target.value })}
                            required
                        >
                            <option value="">Select Institute</option>
                            {institutes.map(inst => (
                                <option key={inst.id} value={inst.id}>{inst.name}</option>
                            ))}
                        </select>
                    </div>

                    <Button type="submit" className="w-full">
                        Create Department
                    </Button>
                </form>
            </Modal>
        </div>
    );
}
