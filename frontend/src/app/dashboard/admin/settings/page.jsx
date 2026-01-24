'use client';

import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import api from '@/services/api';
import { Building, Globe, MapPin, Hash, Phone, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CardSkeleton } from '@/components/ui/Skeleton';

export default function CollegeSettingsPage() {
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [collegeData, setCollegeData] = useState({
        name: '',
        code: '',
        address: '',
        website: '',
        contactPhone: ''
    });

    useEffect(() => {
        const fetchCollege = async () => {
             try {
                 const response = await api.colleges.getMyCollege();
                 if (response.success) {
                     setCollegeData(response.data);
                 }
             } catch (error) {
                 console.error('Failed to fetch college:', error);
                 toast.error('Failed to load college details');
             } finally {
                 setLoading(false);
             }
        };

        fetchCollege();
    }, []);

    const handleUpdate = async (e) => {
        e.preventDefault();
        setUpdating(true);
        try {
            const response = await api.colleges.updateMyCollege({
                name: collegeData.name,
                address: collegeData.address,
                website: collegeData.website,
                contactPhone: collegeData.contactPhone
            });

            if (response.success) {
                toast.success('College details updated successfully');
                setCollegeData(response.data);
            } else {
                toast.error(response.message || 'Failed to update college details');
            }
        } catch (error) {
            console.error('Update error:', error);
            toast.error(error.message || 'Failed to update college details');
        } finally {
            setUpdating(false);
        }
    };

    if (loading) {
        return (
            <div className="space-y-6 animate-fade-in">
                <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">College Settings</h1>
                <CardSkeleton />
            </div>
        );
    }

    return (
        <div className="space-y-6 animate-fade-in">
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">College Settings</h1>

            <Card className="border border-gray-100">
                <form onSubmit={handleUpdate} className="space-y-6">
                    <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg border border-gray-100 mb-6">
                        <div className="p-2 bg-white rounded-md shadow-sm">
                            <Building className="h-6 w-6 text-indigo-600" />
                        </div>
                        <div>
                            <h3 className="font-semibold text-gray-900">Institution Details</h3>
                            <p className="text-sm text-gray-500">Manage your college&apos;s public information</p>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="md:col-span-2">
                            <Input
                                label="College Name"
                                value={collegeData.name}
                                onChange={(e) => setCollegeData({ ...collegeData, name: e.target.value })}
                                icon={Building}
                                required
                                placeholder="Enter college name"
                            />
                        </div>

                        <Input
                            label="College Code"
                            value={collegeData.code}
                            icon={Hash}
                            disabled
                            className="bg-gray-50 text-gray-500 cursor-not-allowed"
                            helperText="College code cannot be changed. Contact Super Admin for assistance."
                        />

                        <Input
                            label="Contact Phone"
                            value={collegeData.contactPhone}
                            onChange={(e) => setCollegeData({ ...collegeData, contactPhone: e.target.value })}
                            icon={Phone}
                            placeholder="Official contact number"
                        />

                        <div className="md:col-span-2">
                            <Input
                                label="Address"
                                value={collegeData.address}
                                onChange={(e) => setCollegeData({ ...collegeData, address: e.target.value })}
                                icon={MapPin}
                                placeholder="Full address"
                            />
                        </div>

                        <div className="md:col-span-2">
                            <Input
                                label="Website"
                                value={collegeData.website}
                                onChange={(e) => setCollegeData({ ...collegeData, website: e.target.value })}
                                icon={Globe}
                                placeholder="https://example.com"
                            />
                        </div>
                    </div>

                    <div className="pt-4 flex justify-end border-t border-gray-100 mt-6">
                        <Button 
                            type="submit" 
                            loading={updating}
                            className="bg-indigo-600 hover:bg-indigo-700"
                        >
                            <Save className="h-4 w-4 mr-2" />
                            Save Changes
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
}
