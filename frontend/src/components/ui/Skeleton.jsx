import React from 'react';

/**
 * High-fidelity Skeleton component for loading states.
 * Uses a subtle gradient pulse and glassmorphism.
 */
export const Skeleton = ({ className = '', ...props }) => {
    return (
        <div
            className={`animate-pulse rounded-lg bg-gray-200/50 backdrop-blur-sm shadow-inner ${className}`}
            {...props}
        />
    );
};

export const CardSkeleton = () => (
    <div className="glass-card p-6 border border-gray-100/50 space-y-4">
        <div className="flex items-center justify-between">
            <Skeleton className="h-6 w-1/3" />
            <Skeleton className="h-8 w-8 rounded-full" />
        </div>
        <Skeleton className="h-10 w-1/2" />
        <Skeleton className="h-4 w-2/3" />
    </div>
);

export const StatsSkeleton = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {[1, 2, 3, 4].map((i) => (
            <div key={i} className="glass-card p-6 border border-gray-100/50 space-y-3">
                <div className="flex justify-between items-start">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-10 w-10 rounded-xl" />
                </div>
                <Skeleton className="h-8 w-16" />
                <Skeleton className="h-3 w-32" />
            </div>
        ))}
    </div>
);

export const ListSkeleton = ({ count = 5 }) => (
    <div className="space-y-4">
        {[...Array(count)].map((_, i) => (
            <div key={i} className="flex items-center gap-4 p-4 rounded-xl border border-gray-50/50">
                <Skeleton className="h-12 w-12 rounded-xl" />
                <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-1/4" />
                    <Skeleton className="h-3 w-1/3" />
                </div>
                <Skeleton className="h-8 w-20 rounded-lg" />
            </div>
        ))}
    </div>
);
