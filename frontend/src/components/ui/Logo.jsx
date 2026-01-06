'use client';

import { GraduationCap } from 'lucide-react';
import Link from 'next/link';

/**
 * PlacementPro Logo Component
 * Professional logo with gradient icon and branding
 */
export default function Logo({
    size = 'md',
    showText = true,
    href = null,
    className = ''
}) {
    const sizes = {
        sm: {
            icon: 'h-7 w-7',
            iconInner: 'h-3.5 w-3.5',
            text: 'text-lg',
        },
        md: {
            icon: 'h-9 w-9',
            iconInner: 'h-4.5 w-4.5',
            text: 'text-xl',
        },
        lg: {
            icon: 'h-11 w-11',
            iconInner: 'h-5 w-5',
            text: 'text-2xl',
        },
    };

    const currentSize = sizes[size] || sizes.md;

    const LogoContent = () => (
        <div className={`flex items-center gap-2.5 ${className}`}>
            {/* Icon with gradient */}
            <div className={`${currentSize.icon} bg-gradient-to-br from-indigo-500 via-purple-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/25 ring-1 ring-white/20`}>
                <GraduationCap className={`${currentSize.iconInner} text-white`} strokeWidth={2.5} />
            </div>

            {/* Text */}
            {showText && (
                <span className={`font-bold ${currentSize.text} text-gray-900 tracking-tight`}>
                    Placement<span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">Pro</span>
                </span>
            )}
        </div>
    );

    if (href) {
        return (
            <Link href={href} className="focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 rounded-lg">
                <LogoContent />
            </Link>
        );
    }

    return <LogoContent />;
}
