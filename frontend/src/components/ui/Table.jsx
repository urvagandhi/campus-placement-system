/**
 * Reusable Table component with glassmorphism
 */
export default function Table({
    columns,
    data,
    emptyMessage = 'No data available',
    loading = false,
    className = ''
}) {
    if (loading) {
        return (
            <div className={`overflow-hidden rounded-xl glass-card ${className}`}>
                <div className="animate-pulse">
                    <div className="h-12 bg-gray-100/50"></div>
                    {[1, 2, 3].map((i) => (
                        <div key={i} className="h-16 border-t border-gray-100/50 flex items-center px-6 gap-4">
                            <div className="h-4 bg-gray-200/60 rounded w-1/4"></div>
                            <div className="h-4 bg-gray-200/60 rounded w-1/3"></div>
                            <div className="h-4 bg-gray-200/60 rounded w-1/4"></div>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div className={`glass-card text-center py-12 ${className}`}>
                <div className="text-gray-400 mb-2">
                    <svg className="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
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
                                className="hover:bg-indigo-50/30 transition-colors duration-150"
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
