'use client';

import { useState, useMemo } from 'react';
import { X, UserPlus, Building2, Briefcase, Mail, Phone, Key, CheckCircle, Copy, Check, ChevronDown } from 'lucide-react';
import { createPortal } from 'react-dom';
import api from '@/services/api';
import toast from 'react-hot-toast';
import Input from '@/components/ui/Input';
import PhoneInput from '@/components/ui/PhoneInput';
import Button from '@/components/ui/Button';

/**
 * Add Staff Dialog
 * Allows adding a new staff member (Coordinator) to a specific organization unit.
 * Auto-generates password and shows credentials upon success.
 */
export default function AddStaffDialog({ isOpen, onClose, onSuccess, hierarchy }) {
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState('form'); // 'form' | 'success'
    const [credentials, setCredentials] = useState(null);
    const [copiedField, setCopiedField] = useState(null);

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phoneNumber: '',
        designation: '',
        organizationUnitId: ''
    });

    // Flatten hierarchy to get list of units (Institutes & Departments)
    const units = useMemo(() => {
        const result = [];
        const traverse = (node) => {
            if (!node) return;
            // Only allow assigning to Institutes and Departments
            if (node.type === 'INSTITUTE' || node.type === 'DEPARTMENT') {
                result.push(node);
            }
            if (node.children) {
                node.children.forEach(traverse);
            }
        };
        if (Array.isArray(hierarchy)) {
             hierarchy.forEach(traverse);
        } else {
             traverse(hierarchy);
        }
        return result;
    }, [hierarchy]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const generatePassword = () => {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
        let pass = '';
        for (let i = 0; i < 12; i++) {
            pass += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return pass;
    };

    const copyToClipboard = (text, field) => {
        navigator.clipboard.writeText(text);
        setCopiedField(field);
        setTimeout(() => setCopiedField(null), 2000);
        toast.success('Copied to clipboard!');
    };

    const handleClose = () => {
        setStep('form');
        setCredentials(null);
        setFormData({
            name: '',
            email: '',
            phoneNumber: '',
            designation: '',
            organizationUnitId: ''
        });
        onClose();
    };

    const selectedUnit = useMemo(() => {
        return units.find(u => u.id === Number(formData.organizationUnitId));
    }, [units, formData.organizationUnitId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const tempPassword = generatePassword();
            
            // Format designation based on selection and unit
            let formattedDesignation = formData.designation;
            
            if (selectedUnit) {
                if (formData.designation === 'DIRECTOR') {
                    formattedDesignation = `Director - ${selectedUnit.name}`;
                } else if (formData.designation === 'TPO') {
                    formattedDesignation = `Head - Training & Placement (${selectedUnit.code})`;
                } else if (formData.designation === 'HOD') {
                    formattedDesignation = `Head of Department (${selectedUnit.code})`;
                } else if (formData.designation === 'COORD') {
                    formattedDesignation = `Department Coordinator (${selectedUnit.code})`;
                }
            }

            const payload = {
                ...formData,
                designation: formattedDesignation,
                password: tempPassword,
                organizationUnitId: formData.organizationUnitId ? Number(formData.organizationUnitId) : null
            };

            const response = await api.admin.createCoordinator(payload);

            if (response.success) {
                toast.success('Staff member added successfully');
                if (onSuccess) onSuccess();
                
                // Show credentials
                setCredentials({
                    email: formData.email,
                    password: tempPassword,
                    name: formData.name
                });
                setStep('success');
            } else {
                toast.error(response.message || 'Failed to add staff');
            }
        } catch (error) {
            console.error('Add staff error:', error);
            toast.error(error.message || 'Failed to add staff');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return createPortal(
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
            {/* Backdrop */}
            <div 
                className="absolute inset-0 bg-black/40 backdrop-blur-sm transition-opacity"
                onClick={handleClose}
            />

            {/* Modal */}
            <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl ring-1 ring-black/5 flex flex-col max-h-[90vh] overflow-hidden animate-in fade-in zoom-in-95 duration-200">
                {step === 'form' ? (
                    <>
                        {/* Header */}
                        <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                            <div>
                                <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                                    <UserPlus size={20} className="text-indigo-600" />
                                    Add Staff Member
                                </h2>
                                <p className="text-sm text-gray-500 mt-1">Create a new coordinator account</p>
                            </div>
                            <button 
                                onClick={handleClose}
                                className="p-2 rounded-xl hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {/* Form */}
                        <div className="p-6 overflow-y-auto">
                            <form onSubmit={handleSubmit} className="space-y-4">
                                <Input
                                    label="Full Name"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="e.g. Dr. Jane Doe"
                                    required
                                    icon={Briefcase}
                                />

                                <Input
                                    label="Email Address"
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="jane@college.edu"
                                    required
                                    icon={Mail}
                                />

                                <PhoneInput
                                    label="Phone Number"
                                    value={formData.phoneNumber}
                                    onChange={(e) => handleChange({ target: { name: 'phoneNumber', value: e.target.value } })}
                                    required
                                />

                                {/* Organization Unit Selection */}
                                <div className="space-y-1.5">
                                    <label className="block text-sm font-semibold text-gray-700 ml-1">
                                        Department / Institute
                                    </label>
                                    <div className="relative">
                                        <Building2 className="absolute left-3.5 top-3.5 h-5 w-5 text-gray-500 pointer-events-none" />
                                        <select
                                            name="organizationUnitId"
                                            value={formData.organizationUnitId}
                                            onChange={handleChange}
                                            className="w-full pl-11 pr-4 py-3 bg-white/80 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 appearance-none text-gray-900"
                                            required
                                        >
                                            <option value="">Select Assignment...</option>
                                            {units.map(unit => (
                                                <option key={unit.id} value={unit.id}>
                                                    {unit.name} ({unit.type})
                                                </option>
                                            ))}
                                        </select>
                                        <ChevronDown className="absolute right-3.5 top-3.5 h-5 w-5 text-gray-400 pointer-events-none" />
                                    </div>
                                </div>

                                {/* Designation Dropdown */}
                                <div className="space-y-1.5">
                                    <label className="block text-sm font-semibold text-gray-700 ml-1">
                                        Designation
                                    </label>
                                    <div className="relative">
                                        <Briefcase className="absolute left-3.5 top-3.5 h-5 w-5 text-gray-500 pointer-events-none" />
                                        <select
                                            name="designation"
                                            value={formData.designation}
                                            onChange={handleChange}
                                            className="w-full pl-11 pr-4 py-3 bg-white/80 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 appearance-none text-gray-900"
                                            disabled={!formData.organizationUnitId}
                                            required
                                        >
                                            <option value="">
                                                {!formData.organizationUnitId ? 'Select Department First...' : 'Select Designation...'}
                                            </option>
                                            
                                            {/* Institute Options */}
                                            {selectedUnit?.type === 'INSTITUTE' && (
                                                <>
                                                    <option value="DIRECTOR">Institute Director</option>
                                                    <option value="TPO">Head - Training and Placement Cell</option>
                                                </>
                                            )}

                                            {/* Department Options */}
                                            {selectedUnit?.type === 'DEPARTMENT' && (
                                                <>
                                                    <option value="HOD">Head Of Department</option>
                                                    <option value="COORD">Department Coordinator</option>
                                                </>
                                            )}
                                        </select>
                                        <ChevronDown className="absolute right-3.5 top-3.5 h-5 w-5 text-gray-400 pointer-events-none" />
                                    </div>
                                </div>

                                <div className="p-4 bg-gray-50 rounded-xl border border-gray-100 mt-4">
                                    <div className="flex items-center gap-2 text-gray-600 mb-1">
                                        <Key size={16} />
                                        <span className="text-sm font-semibold">Security</span>
                                    </div>
                                    <p className="text-xs text-gray-500">
                                        A secure temporary password will be automatically generated. You will be able to copy it after creation.
                                    </p>
                                </div>
                            </form>
                        </div>

                        {/* Footer */}
                        <div className="p-6 border-t border-gray-100 bg-gray-50/50 flex justify-end gap-3">
                            <button
                                type="button"
                                onClick={handleClose}
                                className="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-200 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSubmit}
                                disabled={loading}
                                className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 shadow-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                            >
                                {loading ? 'Creating...' : 'Create Account'}
                            </button>
                        </div>
                    </>
                ) : (
                    <div className="p-6 space-y-6">
                        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl">
                            <div className="flex items-center gap-2 text-emerald-700 font-medium mb-2">
                                <CheckCircle className="h-5 w-5" />
                                Staff Member Added
                            </div>
                            <p className="text-sm text-emerald-600">
                                <strong>{credentials?.name}</strong> has been added successfully. 
                                Share these credentials securely.
                            </p>
                        </div>

                        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
                            <div className="flex items-center gap-2 text-amber-700 font-medium mb-3">
                                <Key className="h-4 w-4" />
                                Temporary Credentials
                            </div>
                            
                            <div className="space-y-3">
                                <div className="flex items-center justify-between bg-white rounded-lg p-3 border border-amber-100">
                                    <div>
                                        <p className="text-xs text-gray-500 uppercase">Email</p>
                                        <p className="font-mono text-sm font-medium text-gray-900">{credentials?.email}</p>
                                    </div>
                                    <button
                                        onClick={() => copyToClipboard(credentials?.email, 'email')}
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
                                        <p className="text-xs text-gray-500 uppercase">Password</p>
                                        <p className="font-mono text-sm font-medium text-gray-900">{credentials?.password}</p>
                                    </div>
                                    <button
                                        onClick={() => copyToClipboard(credentials?.password, 'password')}
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
                                The user will be required to change their password on first login.
                            </p>
                        </div>

                        <div className="pt-2">
                            <Button 
                                onClick={handleClose}
                                className="w-full bg-gray-900 hover:bg-gray-800"
                            >
                                Done
                            </Button>
                        </div>
                    </div>
                )}
            </div>
        </div>,
        document.body
    );
}
