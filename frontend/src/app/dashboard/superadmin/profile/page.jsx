'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import api from '@/services/api';
import { useAuth } from '@/hooks/useAuth';
import { Lock, Save, User, Phone, Mail, Shield } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CardSkeleton, Skeleton } from '@/components/ui/Skeleton';

export default function ProfileSettingsPage() {
    const { user, refreshUser } = useAuth();
    const [loading, setLoading] = useState(true);
    const [updatingProfile, setUpdatingProfile] = useState(false);
    const [changingPassword, setChangingPassword] = useState(false);

    const [profileData, setProfileData] = useState({
        name: '',
        phoneNumber: '',
        email: '' // Read only
    });

    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    useEffect(() => {
        if (user) {
            setProfileData({
                name: user.name || '',
                phoneNumber: user.phoneNumber || '',
                email: user.email || ''
            });
            setLoading(false);
        }
    }, [user]);

    const handleProfileUpdate = async (e) => {
        e.preventDefault();
        setUpdatingProfile(true);
        try {
            const response = await api.profile.updateDetails({
                name: profileData.name,
                phoneNumber: profileData.phoneNumber
            });

            if (response.success) {
                toast.success('Profile updated successfully');
                if (refreshUser) refreshUser();
            } else {
                toast.error(response.message || 'Failed to update profile');
            }
        } catch (error) {
            console.error('Profile update error:', error);
            toast.error(error.message || 'Failed to update user profile');
        } finally {
            setUpdatingProfile(false);
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            toast.error('New passwords do not match');
            return;
        }

        setChangingPassword(true);
        try {
            const response = await api.profile.changePassword({
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword,
                confirmPassword: passwordData.confirmPassword
            });

            if (response.success) {
                toast.success('Password changed successfully');
                setPasswordData({
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                });
            } else {
                toast.error(response.message || 'Failed to change password');
            }
        } catch (error) {
            console.error('Password change error:', error);
            toast.error(error.message || 'Failed to change password');
        } finally {
            setChangingPassword(false);
        }
    };

    if (loading) {
        return (
            <div className="space-y-6 animate-fade-in">
                <Skeleton className="h-10 w-48 mb-6" />
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <CardSkeleton />
                    <CardSkeleton />
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 animate-fade-in">
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Profile Settings</h1>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Personal Information */}
                <Card title="Personal Information" className="border border-gray-100 h-fit">
                    <form onSubmit={handleProfileUpdate} className="space-y-5">
                        <Input
                            label="Full Name"
                            value={profileData.name}
                            onChange={(e) => setProfileData({ ...profileData, name: e.target.value })}
                            icon={User}
                            placeholder="Your full name"
                            required
                        />
                        
                        <div className="opacity-75">
                            <Input
                                label="Email Address"
                                value={profileData.email}
                                icon={Mail}
                                disabled
                                className="bg-gray-50 text-gray-500 cursor-not-allowed"
                            />
                            <p className="text-xs text-gray-400 mt-1 ml-1">Email cannot be changed.</p>
                        </div>

                        <Input
                            label="Phone Number"
                            value={profileData.phoneNumber}
                            onChange={(e) => setProfileData({ ...profileData, phoneNumber: e.target.value })}
                            icon={Phone}
                            placeholder="+91 98765 43210"
                        />

                        <div className="pt-2 flex justify-end">
                            <Button 
                                type="submit" 
                                loading={updatingProfile}
                                className="bg-indigo-600 hover:bg-indigo-700 w-full sm:w-auto"
                            >
                                <Save className="h-4 w-4 mr-2" />
                                Save Changes
                            </Button>
                        </div>
                    </form>
                </Card>

                {/* Security Settings */}
                <Card title="Security" className="border border-gray-100 h-fit">
                    <form onSubmit={handlePasswordChange} className="space-y-5">
                        <div className="p-3 bg-amber-50 border border-amber-100 rounded-lg flex gap-3 text-amber-800 text-sm mb-4">
                            <Shield className="h-5 w-5 shrink-0 text-amber-600" />
                            <p>For your security, you will be logged out of other sessions after changing your password.</p>
                        </div>

                        <Input
                            label="Current Password"
                            type="password"
                            value={passwordData.currentPassword}
                            onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                            icon={Lock}
                            required
                            placeholder="••••••••"
                        />

                        <div className="h-px bg-gray-100 my-2"></div>

                        <Input
                            label="New Password"
                            type="password"
                            value={passwordData.newPassword}
                            onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                            icon={Lock}
                            required
                            placeholder="Min. 8 characters"
                        />
                        <Input
                            label="Confirm New Password"
                            type="password"
                            value={passwordData.confirmPassword}
                            onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                            icon={Lock}
                            required
                            placeholder="Re-enter new password"
                        />

                        <div className="pt-2 flex justify-end">
                            <Button 
                                type="submit" 
                                loading={changingPassword}
                                className="bg-gray-900 hover:bg-gray-800 w-full sm:w-auto"
                            >
                                <Lock className="h-4 w-4 mr-2" />
                                Update Password
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}
