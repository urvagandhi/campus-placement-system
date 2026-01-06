'use client';

/**
 * Reusable Button component with glassmorphism support
 */
export default function Button({
    children,
    variant = 'primary',
    size = 'md',
    disabled = false,
    loading = false,
    onClick,
    type = 'button',
    className = '',
    ...props
}) {
    const baseStyles = 'inline-flex items-center justify-center font-semibold rounded-xl transition-all duration-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

    const variants = {
        primary: 'bg-gradient-to-r from-indigo-600 to-indigo-700 text-white hover:from-indigo-700 hover:to-indigo-800 focus-visible:ring-indigo-500 shadow-lg shadow-indigo-500/25 btn-glow',
        secondary: 'glass text-gray-700 hover:bg-white/90 focus-visible:ring-indigo-500',
        outline: 'border-2 border-indigo-500 text-indigo-600 hover:bg-indigo-50 focus-visible:ring-indigo-500 bg-white/50 backdrop-blur-sm',
        danger: 'bg-gradient-to-r from-red-500 to-red-600 text-white hover:from-red-600 hover:to-red-700 focus-visible:ring-red-500 shadow-lg shadow-red-500/25',
        success: 'bg-gradient-to-r from-emerald-500 to-emerald-600 text-white hover:from-emerald-600 hover:to-emerald-700 focus-visible:ring-emerald-500 shadow-lg shadow-emerald-500/25',
        ghost: 'text-gray-600 hover:text-gray-900 hover:bg-gray-100/80 focus-visible:ring-gray-500',
        glass: 'glass text-indigo-700 hover:bg-white/90 focus-visible:ring-indigo-500 border-indigo-200/50',
    };

    const sizes = {
        sm: 'px-3.5 py-1.5 text-sm gap-1.5',
        md: 'px-5 py-2.5 text-sm gap-2',
        lg: 'px-6 py-3 text-base gap-2',
        xl: 'px-8 py-4 text-lg gap-2.5',
    };

    const disabledStyles = disabled || loading
        ? 'opacity-50 cursor-not-allowed'
        : 'cursor-pointer';

    return (
        <button
            type={type}
            onClick={onClick}
            disabled={disabled || loading}
            className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${disabledStyles} ${className}`}
            {...props}
        >
            {loading && (
                <svg className="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
            )}
            {children}
        </button>
    );
}
