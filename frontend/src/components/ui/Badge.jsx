/**
 * Badge component with glassmorphism styling
 */
export default function Badge({
    children,
    variant = 'info',
    size = 'md',
    className = ''
}) {
    const variants = {
        success: 'bg-emerald-100/80 text-emerald-700 border-emerald-200/50',
        warning: 'bg-amber-100/80 text-amber-700 border-amber-200/50',
        error: 'bg-red-100/80 text-red-700 border-red-200/50',
        info: 'bg-blue-100/80 text-blue-700 border-blue-200/50',
        neutral: 'bg-gray-100/80 text-gray-700 border-gray-200/50',
        primary: 'bg-indigo-100/80 text-indigo-700 border-indigo-200/50',
        purple: 'bg-purple-100/80 text-purple-700 border-purple-200/50',
    };

    const sizes = {
        sm: 'px-2 py-0.5 text-xs',
        md: 'px-2.5 py-0.5 text-xs',
        lg: 'px-3 py-1 text-sm',
    };

    return (
        <span
            className={`
                inline-flex items-center font-medium rounded-full
                backdrop-blur-sm border
                ${variants[variant]}
                ${sizes[size]}
                ${className}
            `}
        >
            {children}
        </span>
    );
}
