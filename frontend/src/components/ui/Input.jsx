'use client';

import { Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';

/**
 * Reusable Input component with glassmorphism styling
 * Supports password visibility toggle
 */
export default function Input({
    label,
    type = 'text',
    name,
    value,
    onChange,
    placeholder,
    error,
    required = false,
    disabled = false,
    icon: Icon,
    showPasswordToggle = false,
    className = '',
    ...props
}) {
    const [showPassword, setShowPassword] = useState(false);

    // Determine actual input type (for password visibility toggle)
    const inputType = type === 'password' && showPassword ? 'text' : type;

    return (
        <div className={`w-full ${className}`}>
            {label && (
                <label
                    htmlFor={name}
                    className="block text-sm font-semibold text-gray-700 mb-1.5 ml-1"
                >
                    {label}
                    {required && <span className="text-red-500 ml-1">*</span>}
                </label>
            )}
            <div className="relative">
                {/* Left Icon */}
                {Icon && (
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none z-10">
                        <Icon className="h-5 w-5 text-gray-500" strokeWidth={2} />
                    </div>
                )}

                <input
                    type={inputType}
                    id={name}
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    required={required}
                    disabled={disabled}
                    className={`
                        w-full px-4 py-3
                        bg-white/80 backdrop-blur-sm
                        border rounded-xl
                        text-gray-900 placeholder-gray-400
                        focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 focus:bg-white
                        disabled:bg-gray-100/80 disabled:cursor-not-allowed disabled:text-gray-500
                        transition-all duration-200
                        ${Icon ? 'pl-11' : ''}
                        ${type === 'password' && showPasswordToggle ? 'pr-11' : ''}
                        ${error ? 'border-red-400 focus:ring-red-500/50 focus:border-red-500' : 'border-gray-200'}
                    `}
                    {...props}
                />

                {/* Password Visibility Toggle */}
                {type === 'password' && showPasswordToggle && (
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-gray-500 hover:text-gray-700 transition-colors focus:outline-none"
                        tabIndex={-1}
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                        {showPassword ? (
                            <EyeOff className="h-5 w-5" strokeWidth={2} />
                        ) : (
                            <Eye className="h-5 w-5" strokeWidth={2} />
                        )}
                    </button>
                )}
            </div>
            {error && (
                <p className="mt-1.5 text-sm text-red-500 flex items-center gap-1 ml-1">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {error}
                </p>
            )}
        </div>
    );
}
