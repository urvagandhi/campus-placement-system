/**
 * Reusable Table component with glassmorphism
 */
export default function Table({
    columns,
    data,
    emptyMessage = 'No data available',
    loading = false,
    className = '',
    onRowClick
}) {
    if (loading) {
        return (
            <div className={`overflow-hidden rounded-xl glass-card animate-pulse shadow-sm ${className}`}>
                <div className="h-12 bg-gray-50/50 border-b border-gray-100/50" />
                <div className="p-8 space-y-6">
                    {[1, 2, 3, 4, 5].map((i) => (
                        <div key={i} className="flex gap-4">
                            <div className="h-4 bg-gray-100 rounded w-1/4" />
                            <div className="h-4 bg-gray-100 rounded w-1/2" />
                            <div className="h-4 bg-gray-100 rounded w-1/6" />
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div className={`flex flex-col items-center justify-center py-20 bg-gray-50/30 rounded-xl border border-dashed border-gray-200 ${className}`}>
                <div className="p-4 bg-gray-100 rounded-full mb-4">
                    <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
                    </svg>
                </div>
                <p className="text-gray-500 font-medium">{emptyMessage}</p>
            </div>
        );
    }

    return (
        <div className={`overflow-hidden rounded-xl glass-card ${className}`}>
            <div className="overflow-x-auto">
                <table className="min-w-full">
                    <thead>
                        <tr className="bg-gray-50/50 border-b border-gray-100/50">
                            {columns.map((column, index) => (
                                <th
                                    key={index}
                                    className="px-6 py-3.5 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider"
                                >
                                    {column.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100/50">
                        {data.map((row, rowIndex) => (
                            <tr
                                key={rowIndex}
                                onClick={() => onRowClick && onRowClick(row)}
                                className={`hover:bg-indigo-50/30 transition-colors duration-150 ${onRowClick ? 'cursor-pointer' : ''}`}
                            >
                                {columns.map((column, colIndex) => (
                                    <td
                                        key={colIndex}
                                        className="px-6 py-4 whitespace-nowrap text-sm text-gray-700"
                                    >
                                        {column.render
                                            ? column.render(row[column.accessor], row)
                                            : row[column.accessor]
                                        }
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
