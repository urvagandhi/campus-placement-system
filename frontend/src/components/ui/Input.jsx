'use client';

/**
 * Reusable Input component with glassmorphism styling
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
    className = '',
    ...props
}) {
    return (
        <div className={`w-full ${className}`}>
            {label && (
                <label
                    htmlFor={name}
                    className="block text-sm font-medium text-gray-700 mb-1.5"
                >
                    {label}
                    {required && <span className="text-red-500 ml-1">*</span>}
                </label>
            )}
            <div className="relative">
                {Icon && (
                    <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                        <Icon className="h-5 w-5 text-gray-400" />
                    </div>
                )}
                <input
                    type={type}
                    id={name}
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    required={required}
                    disabled={disabled}
                    className={`
                        w-full px-4 py-2.5
                        bg-white/60 backdrop-blur-sm
                        border rounded-xl
                        text-gray-900 placeholder-gray-400
                        focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 focus:bg-white/80
                        disabled:bg-gray-100/80 disabled:cursor-not-allowed disabled:text-gray-500
                        transition-all duration-200
                        ${Icon ? 'pl-11' : ''}
                        ${error ? 'border-red-400 focus:ring-red-500/50 focus:border-red-500' : 'border-gray-200/80'}
                    `}
                    {...props}
                />
            </div>
            {error && (
                <p className="mt-1.5 text-sm text-red-500 flex items-center gap-1">
                    <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {error}
                </p>
            )}
        </div>
    );
}
