'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Modal from '@/components/ui/Modal';
import api from '@/services/api';
import { AlertTriangle, Info, ToggleLeft, ToggleRight } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CardSkeleton, Skeleton } from '@/components/ui/Skeleton';

export default function SystemSettings() {
    const [settings, setSettings] = useState({
        registrationEnabled: true,
        maintenanceMode: false,
        appVersion: '1.0.0'
    });
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);

    // Modal State
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [pendingMaintenanceState, setPendingMaintenanceState] = useState(false);

    useEffect(() => {
        const fetchSettings = async () => {
            try {
                const response = await api.users.getSettings();
                if (response.success) {
                    setSettings(response.data);
                }
            } catch (error) {
                console.error('Failed to fetch settings:', error);
                toast.error('Failed to load settings');
            } finally {
                setLoading(false);
            }
        };

        fetchSettings();
    }, []);

    const updateSetting = async (key, value, customSuccessMessage = 'Setting updated') => {
        setUpdating(true);
        try {
            const response = await api.users.updateSettings({ [key]: value });
            if (response.success) {
                setSettings(response.data);
                toast.success(customSuccessMessage);
            } else {
                toast.error(response.message || 'Failed to update');
            }
        } catch (error) {
            console.error('Failed to update setting:', error);
            toast.error(error.message || 'Failed to update setting');
        } finally {
            setUpdating(false);
        }
    };

    const toggleRegistration = () => {
        const newState = !settings.registrationEnabled;
        updateSetting(
            'registrationEnabled', 
            newState, 
            `User registration ${newState ? 'enabled' : 'disabled'} successfully`
        );
    };

    const handleMaintenanceClick = () => {
        setPendingMaintenanceState(!settings.maintenanceMode);
        setShowConfirmModal(true);
    };

    const confirmMaintenanceToggle = async () => {
        setShowConfirmModal(false);
        await updateSetting(
            'maintenanceMode', 
            pendingMaintenanceState,
            `Maintenance mode ${pendingMaintenanceState ? 'activated' : 'deactivated'} successfully`
        );
    };

    if (loading) {
        return (
            <div className="space-y-6 animate-fade-in">
                <Skeleton className="h-10 w-64 mb-8" />
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <CardSkeleton />
                    <CardSkeleton />
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 animate-fade-in">
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">System Configuration</h1>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card title="General Settings" className="border border-gray-100">
                    <div className="space-y-6">
                        {/* Registration Toggle */}
                        <div className="flex items-center justify-between">
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">Enable User Registration</h4>
                                <p className="text-xs text-gray-500 mt-1">Allow Admins to create Coordinators and Coordinators to create Students.</p>
                                <p className="text-xs text-amber-600 mt-0.5">Note: Super Admin functions are always enabled.</p>
                            </div>
                            <button
                                onClick={toggleRegistration}
                                disabled={updating}
                                className={`transition-colors ${settings.registrationEnabled ? 'text-emerald-600' : 'text-gray-400'} disabled:opacity-50`}
                            >
                                {settings.registrationEnabled ? (
                                    <ToggleRight className="h-10 w-10" />
                                ) : (
                                    <ToggleLeft className="h-10 w-10" />
                                )}
                            </button>
                        </div>

                        <div className="h-px bg-gray-100"></div>

                        {/* Maintenance Mode Toggle */}
                        <div className="flex items-center justify-between">
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">Maintenance Mode</h4>
                                <p className="text-xs text-gray-500 mt-1">Take the system offline for all users EXCEPT super admins.</p>
                            </div>
                            <button
                                onClick={handleMaintenanceClick}
                                disabled={updating}
                                className={`transition-colors ${settings.maintenanceMode ? 'text-red-600' : 'text-gray-400'} disabled:opacity-50`}
                            >
                                {settings.maintenanceMode ? (
                                    <ToggleRight className="h-10 w-10" />
                                ) : (
                                    <ToggleLeft className="h-10 w-10" />
                                )}
                            </button>
                        </div>
                    </div>
                </Card>

                <Card title="Environment Info" className="border border-gray-100 bg-gray-50">
                    <div className="space-y-4">
                        <div className="flex items-start gap-3">
                            <Info className="h-5 w-5 text-indigo-500 mt-0.5" />
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">Current Environment</h4>
                                <p className="text-sm text-gray-600">Production ({settings.appVersion})</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-3">
                            <AlertTriangle className={`h-5 w-5 mt-0.5 ${settings.maintenanceMode ? 'text-red-500' : 'text-yellow-500'}`} />
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">System Status</h4>
                                <p className="text-sm text-gray-600">
                                    {settings.maintenanceMode 
                                        ? 'Maintenance mode is ACTIVE. Only super admins can access.' 
                                        : 'System is running normally.'}
                                </p>
                            </div>
                        </div>
                    </div>
                </Card>
            </div>

            {/* Confirmation Modal */}
            <Modal
                isOpen={showConfirmModal}
                onClose={() => setShowConfirmModal(false)}
                title="Confirm System Change"
            >
                <div className="space-y-4">
                    <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start gap-3 text-yellow-800">
                        <AlertTriangle className="h-5 w-5 shrink-0 mt-0.5" />
                        <p className="text-sm">
                            Are you sure you want to <strong>{pendingMaintenanceState ? 'enable' : 'disable'}</strong> Maintenance Mode?
                            {pendingMaintenanceState && ' This will prevent all users except super admins from accessing the system.'}
                        </p>
                    </div>
                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
                            Cancel
                        </Button>
                        <Button variant="primary" onClick={confirmMaintenanceToggle} loading={updating}>
                            Confirm
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
