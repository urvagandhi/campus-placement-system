'use client';

import { AlertCircle, Home, RotateCcw } from 'lucide-react';
import Link from 'next/link';
import { useEffect } from 'react';

export default function Error({ error, reset }) {
    useEffect(() => {
        // Log the error to an error reporting service
        console.error('Application Error:', error);
    }, [error]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f5f5f7] px-6">
            <div className="max-w-md w-full text-center space-y-8 animate-fade-in">
                <div className="flex justify-center">
                    <div className="p-4 bg-red-50 rounded-full">
                        <AlertCircle className="h-16 w-16 text-red-600" />
                    </div>
                </div>

                <div className="space-y-4">
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
                        Something went wrong
                    </h1>
                    <p className="text-gray-500 font-medium">
                        An unexpected error occurred in our system. Don't worry, our team has been notified.
                        <br />
                        <span className="text-xs text-gray-400 mt-2 block">
                            Reference: {new Date().getTime().toString(36).toUpperCase()}
                        </span>
                    </p>
                </div>

                <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                    <button
                        onClick={() => reset()}
                        className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition-all font-semibold shadow-lg shadow-indigo-200 w-full sm:w-auto"
                    >
                        <RotateCcw className="h-4 w-4" />
                        Try Again
                    </button>
                    <Link
                        href="/"
                        className="flex items-center gap-2 px-6 py-3 bg-white text-gray-700 border border-gray-200 rounded-xl hover:bg-gray-50 transition-all font-semibold shadow-sm w-full sm:w-auto"
                    >
                        <Home className="h-4 w-4" />
                        Go Home
                    </Link>
                </div>

                <div className="mt-12 pt-8 border-t border-gray-100 italic text-sm text-gray-400">
                    "The greatest glory in living lies not in never falling, but in rising every time we fall."
                </div>
            </div>
        </div>
    );
}
