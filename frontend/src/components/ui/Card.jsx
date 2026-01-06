/**
 * Reusable Card component with glassmorphism effect
 */
export default function Card({
    children,
    title,
    subtitle,
    footer,
    variant = 'default',
    hover = true,
    className = '',
    ...props
}) {
    const variants = {
        default: 'glass-card',
        solid: 'bg-white rounded-xl shadow-md',
        gradient: 'bg-gradient-to-br from-white/90 to-white/70 backdrop-blur-xl rounded-xl border border-white/30 shadow-lg',
    };

    const hoverClass = hover ? 'card-hover' : '';

    return (
        <div
            className={`${variants[variant]} overflow-hidden ${hoverClass} ${className}`}
            {...props}
        >
            {(title || subtitle) && (
                <div className="px-6 py-4 border-b border-gray-100/50">
                    {title && (
                        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
                    )}
                    {subtitle && (
                        <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
                    )}
                </div>
            )}
            <div className="px-6 py-4">
                {children}
            </div>
            {footer && (
                <div className="px-6 py-4 bg-gray-50/50 border-t border-gray-100/50">
                    {footer}
                </div>
            )}
        </div>
    );
}
