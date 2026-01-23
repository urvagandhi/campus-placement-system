'use client';

import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

/**
 * Apple-style Alert Dialog
 * Mimics iOS/macOS system alerts with glassmorphism and premium interactions.
 */
export default function AlertDialog({
    isOpen,
    onClose,
    onConfirm,
    title,
    description,
    cancelText = 'Cancel',
    confirmText = 'Confirm',
    variant = 'primary', // primary | danger
    loading = false
}) {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        if (isOpen) setIsVisible(true);
        else setTimeout(() => setIsVisible(false), 200); // Animation delay
    }, [isOpen]);

    if (!isVisible) return null;

    return createPortal(
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
            {/* Backdrop */}
            <div 
                className={`
                    fixed inset-0 bg-gray-900/40 backdrop-blur-[2px] 
                    transition-opacity duration-200
                    ${isOpen ? 'opacity-100' : 'opacity-0'}
                `}
                onClick={!loading ? onClose : undefined}
            />

            {/* Alert Box */}
            <div 
                className={`
                    relative w-full max-w-[280px] sm:max-w-xs
                    bg-white/90 backdrop-blur-xl
                    rounded-2xl shadow-2xl
                    transform transition-all duration-200 scale-100
                    ${isOpen ? 'opacity-100 scale-100' : 'opacity-0 scale-95'}
                `}
            >
                {/* Content */}
                <div className="p-5 text-center">
                    <h3 className="text-[17px] font-semibold text-gray-900 leading-6">
                        {title}
                    </h3>
                    {description && (
                        <p className="mt-1 text-[13px] text-gray-500 leading-5">
                            {description}
                        </p>
                    )}
                </div>

                {/* Actions Divider */}
                <div className="h-px bg-gray-300/50 w-full" />

                {/* Buttons Grid */}
                <div className="grid grid-cols-2 divide-x divide-gray-300/50">
                    <button
                        onClick={onClose}
                        disabled={loading}
                        className="
                            py-3 text-[17px] text-blue-500 font-normal 
                            hover:bg-gray-100/50 transition-colors 
                            rounded-bl-2xl active:bg-gray-200/50
                            disabled:opacity-50 disabled:cursor-not-allowed
                        "
                    >
                        {cancelText}
                    </button>
                    <button
                        onClick={onConfirm}
                        disabled={loading}
                        className={`
                            py-3 text-[17px] font-semibold 
                            hover:bg-gray-100/50 transition-colors 
                            rounded-br-2xl active:bg-gray-200/50
                            disabled:opacity-50 disabled:cursor-not-allowed
                            ${variant === 'danger' ? 'text-red-500' : 'text-blue-600'}
                        `}
                    >
                        {loading ? 'Processing...' : confirmText}
                    </button>
                </div>
            </div>
        </div>,
        document.body
    );
}
