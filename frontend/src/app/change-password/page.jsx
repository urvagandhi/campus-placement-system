'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Lock, Eye, EyeOff, CheckCircle, AlertCircle, Shield } from 'lucide-react';
import toast from 'react-hot-toast';
import api from '@/services/api';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { validatePassword, passwordsMatch, getPasswordRequirements } from '@/utils/passwordPolicy';

/**
 * Change Password Page
 * 
 * Used for:
 * 1. First-login password change (when admin logs in with temporary password)
 * 2. General password change from user settings
 * 
 * Query params:
 * - token: First-login token (required for first-login flow)
 */
export default function ChangePasswordPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');

    const [formData, setFormData] = useState({
        newPassword: '',
        confirmPassword: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Password validation states (using global policy)
    const [validations, setValidations] = useState({
        minLength: false,
        hasUppercase: false,
        hasLowercase: false,
        hasNumber: false,
        hasSpecial: false,
        passwordsMatch: false
    });

    // Get requirements from global policy
    const requirements = getPasswordRequirements();

    // Validate password on change using global policy
    useEffect(() => {
        const { newPassword, confirmPassword } = formData;
        const passwordValidation = validatePassword(newPassword);
        setValidations({
            ...passwordValidation,
            passwordsMatch: passwordsMatch(newPassword, confirmPassword)
        });
    }, [formData]);

    const handleChange = (e) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
        setError('');
    };

    const isValidPassword = () => {
        return validations.isValid && validations.passwordsMatch;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!isValidPassword()) {
            setError('Please meet all password requirements');
            return;
        }

        setLoading(true);
        try {
            const response = await api.auth.changePassword({
                token: token,
                newPassword: formData.newPassword,
                confirmPassword: formData.confirmPassword
            });

            if (response.success) {
                toast.success('Password changed successfully! Please log in with your new password.');
                router.push('/login?message=password_changed');
            } else {
                setError(response.message || 'Failed to change password');
            }
        } catch (err) {
            console.error('Password change error:', err);
            setError(err.message || 'An error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const ValidationItem = ({ valid, text }) => (
        <div className={`flex items-center gap-2 text-sm ${valid ? 'text-emerald-600' : 'text-gray-400'}`}>
            {valid ? (
                <CheckCircle className="h-4 w-4" />
            ) : (
                <div className="h-4 w-4 rounded-full border-2 border-current" />
            )}
            <span>{text}</span>
        </div>
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                {/* Header */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-gray-900 to-gray-700 rounded-2xl shadow-xl mb-4">
                        <Shield className="h-8 w-8 text-white" />
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">Change Your Password</h1>
                    <p className="text-gray-600 mt-2">
                        {token ? 
                            'Create a new password to secure your account.' :
                            'Update your account password.'
                        }
                    </p>
                </div>

                {/* Form Card */}
                <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Error Message */}
                        {error && (
                            <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
                                <AlertCircle className="h-5 w-5 text-red-500 flex-shrink-0 mt-0.5" />
                                <p className="text-sm text-red-600">{error}</p>
                            </div>
                        )}

                        {/* New Password */}
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                New Password
                            </label>
                            <div className="relative">
                                <Input
                                    type={showPassword ? 'text' : 'password'}
                                    name="newPassword"
                                    value={formData.newPassword}
                                    onChange={handleChange}
                                    placeholder="Enter new password"
                                    icon={Lock}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>
                        </div>

                        {/* Confirm Password */}
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-gray-700">
                                Confirm Password
                            </label>
                            <div className="relative">
                                <Input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    placeholder="Confirm new password"
                                    icon={Lock}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                >
                                    {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>
                        </div>

                        {/* Password Requirements - Using global policy */}
                        <div className="bg-gray-50 rounded-xl p-4 space-y-2">
                            <p className="text-sm font-medium text-gray-700 mb-2">Password Requirements:</p>
                            <div className="grid grid-cols-2 gap-2">
                                {requirements.map(req => (
                                    <ValidationItem 
                                        key={req.key} 
                                        valid={validations[req.key]} 
                                        text={req.text} 
                                    />
                                ))}
                            </div>
                            <div className="pt-2 border-t border-gray-200 mt-2">
                                <ValidationItem valid={validations.passwordsMatch} text="Passwords match" />
                            </div>
                        </div>

                        {/* Submit Button */}
                        <Button
                            type="submit"
                            loading={loading}
                            disabled={!isValidPassword()}
                            className="w-full bg-gray-900 hover:bg-gray-800 disabled:bg-gray-300"
                        >
                            Change Password
                        </Button>
                    </form>
                </div>

                {/* Back to Login Link */}
                <div className="text-center mt-6">
                    <button
                        onClick={() => router.push('/login')}
                        className="text-sm text-gray-600 hover:text-gray-900"
                    >
                        Back to Login
                    </button>
                </div>
            </div>
        </div>
    );
}
