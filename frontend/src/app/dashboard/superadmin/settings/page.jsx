'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Modal from '@/components/ui/Modal';
import { AlertTriangle, Info, ToggleLeft, ToggleRight } from 'lucide-react';
import { useState } from 'react';

export default function SystemSettings() {
    const [settings, setSettings] = useState({
        registrationEnabled: true,
        maintenanceMode: false
    });

    // Modal State
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [pendingMaintenanceState, setPendingMaintenanceState] = useState(false);

    const toggleRegistration = () => {
        setSettings(prev => ({ ...prev, registrationEnabled: !prev.registrationEnabled }));
    };

    const handleMaintenanceClick = () => {
        // Prepare the new state but don't apply it yet
        setPendingMaintenanceState(!settings.maintenanceMode);
        // Show confirmation modal
        setShowConfirmModal(true);
    };

    const confirmMaintenanceToggle = () => {
        setSettings(prev => ({ ...prev, maintenanceMode: pendingMaintenanceState }));
        setShowConfirmModal(false);
    };

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">System Configuration</h1>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card title="General Settings" className="border border-gray-100">
                    <div className="space-y-6">
                        {/* Registration Toggle */}
                        <div className="flex items-center justify-between">
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">Enable Registration</h4>
                                <p className="text-xs text-gray-500 mt-1">Allow new colleges and students to register.</p>
                            </div>
                            <button
                                onClick={toggleRegistration}
                                className={`transition-colors ${settings.registrationEnabled ? 'text-emerald-600' : 'text-gray-400'}`}
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
                                className={`transition-colors ${settings.maintenanceMode ? 'text-red-600' : 'text-gray-400'}`}
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
                                <p className="text-sm text-gray-600">Production (v1.0.0)</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-3">
                            <AlertTriangle className="h-5 w-5 text-yellow-500 mt-0.5" />
                            <div>
                                <h4 className="text-sm font-medium text-gray-900">System Warnings</h4>
                                <p className="text-sm text-gray-600">No active warnings. System is running smoothly.</p>
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
                            {pendingMaintenanceState && ' This will log out all active users and prevent new logins.'}
                        </p>
                    </div>
                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
                            Cancel
                        </Button>
                        <Button variant="primary" onClick={confirmMaintenanceToggle}>
                            Confirm
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
