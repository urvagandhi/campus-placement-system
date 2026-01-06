'use client';

import { X } from 'lucide-react';

/**
 * Reusable Modal component with glassmorphism
 */
export default function Modal({
    isOpen,
    onClose,
    title,
    children,
    size = 'md',
    showCloseButton = true
}) {
    if (!isOpen) return null;

    const sizes = {
        sm: 'max-w-md',
        md: 'max-w-lg',
        lg: 'max-w-2xl',
        xl: 'max-w-4xl',
        full: 'max-w-6xl',
    };

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto">
            {/* Backdrop with blur */}
            <div
                className="fixed inset-0 bg-gray-900/60 backdrop-blur-sm transition-opacity"
                onClick={onClose}
            />

            {/* Modal */}
            <div className="flex min-h-full items-center justify-center p-4">
                <div
                    className={`relative glass-card w-full ${sizes[size]} transform transition-all duration-300 animate-in fade-in zoom-in-95`}
                >
                    {/* Header */}
                    {(title || showCloseButton) && (
                        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100/50">
                            {title && (
                                <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
                            )}
                            {showCloseButton && (
                                <button
                                    onClick={onClose}
                                    className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100/80 rounded-lg transition-colors"
                                >
                                    <X className="h-5 w-5" />
                                </button>
                            )}
                        </div>
                    )}

                    {/* Content */}
                    <div className="px-6 py-4">
                        {children}
                    </div>
                </div>
            </div>
        </div>
    );
}
