'use client';

import { motion } from 'framer-motion';
import { ArrowLeft, Home, Search } from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function NotFound() {
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted) return null;

    return (
        <div className="h-screen w-full relative flex flex-col items-center justify-center overflow-hidden bg-[#f5f5f7] font-sans selection:bg-indigo-100 selection:text-indigo-900">

            {/* Sophisticated Animated Background Mesh */}
            <div className="absolute inset-0 z-0 overflow-hidden">
                <div className="absolute top-[-10%] left-[-10%] w-[800px] h-[800px] bg-indigo-300/20 rounded-full blur-[120px] animate-blob mix-blend-multiply" />
                <div className="absolute top-[20%] right-[-10%] w-[600px] h-[600px] bg-purple-300/20 rounded-full blur-[120px] animate-blob animation-delay-2000 mix-blend-multiply" />
                <div className="absolute bottom-[-20%] left-[20%] w-[600px] h-[600px] bg-blue-300/20 rounded-full blur-[120px] animate-blob animation-delay-4000 mix-blend-multiply" />
            </div>

            {/* Grid Pattern Overlay for Tech Feel */}
            <div className="absolute inset-0 z-0 bg-[linear-gradient(rgba(0,0,0,0.02)_1px,transparent_1px),linear-gradient(90deg,rgba(0,0,0,0.02)_1px,transparent_1px)] bg-[size:40px_40px] [mask-image:radial-gradient(ellipse_at_center,black_40%,transparent_80%)] pointer-events-none" />

            <div className="relative z-10 w-full max-w-4xl px-6 flex flex-col items-center justify-center h-full max-h-screen py-4">

                {/* 404 Glitch Text Effect - Compacted */}
                <motion.div
                    initial={{ opacity: 0, y: -20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.8, ease: "easeOut" }}
                    className="relative mb-0 md:mb-2 shrink-0"
                >
                    <h1 className="text-[100px] md:text-[140px] leading-none font-black text-transparent bg-clip-text bg-gradient-to-br from-indigo-600 via-purple-600 to-indigo-800 drop-shadow-2xl opacity-90 select-none tracking-tighter font-mono">
                        404
                    </h1>

                    {/* Decorative Elements around number */}
                    <motion.div
                        animate={{ rotate: 360 }}
                        transition={{ duration: 20, repeat: Infinity, ease: "linear" }}
                        className="absolute -top-4 -right-4 md:-top-8 md:-right-8 text-indigo-200/50 w-[60px] h-[60px] md:w-[100px] md:h-[100px]"
                    >
                        <svg width="100%" height="100%" viewBox="0 0 100 100">
                            <path d="M50 0 L100 50 L50 100 L0 50 Z" fill="none" stroke="currentColor" strokeWidth="1" />
                        </svg>
                    </motion.div>
                </motion.div>

                {/* Main Content Card - Compacted */}
                <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.2, duration: 0.5 }}
                    className="w-full max-w-[600px] bg-white/60 backdrop-blur-2xl border border-white/60 shadow-xl shadow-indigo-500/10 rounded-[2rem] p-1.5 overflow-hidden ring-1 ring-white/80 shrink-0"
                >
                    <div className="bg-white/40 rounded-[1.7rem] px-8 py-6 md:p-8 text-center relative overflow-hidden group">

                        {/* Inner Gradient Highlight */}
                        <div className="absolute inset-0 bg-gradient-to-tr from-white/0 via-white/50 to-indigo-50/30 opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none" />

                        <h2 className="text-2xl md:text-3xl font-bold text-gray-900 mb-2 tracking-tight relative z-10">
                            Page Not Found
                        </h2>

                        <p className="text-base text-gray-500 mb-6 max-w-sm mx-auto leading-relaxed relative z-10">
                            The destination you are looking for might have been moved, deleted, or never existed in our placement records.
                        </p>

                        {/* Preserved GIF in a sophisticated container - Reduced Height */}
                        <div className="relative w-full h-32 md:h-40 mb-6 rounded-xl overflow-hidden shadow-inner bg-gray-50 border border-gray-100 mx-auto max-w-sm">
                            <div className="absolute inset-0 flex items-center justify-center bg-gray-100 animate-pulse" />
                            <div
                                className="absolute inset-0 bg-center bg-contain bg-no-repeat opacity-90 mix-blend-multiply transform transition-transform duration-700 hover:scale-105"
                                style={{
                                    backgroundImage: "url('https://cdn.dribbble.com/users/285475/screenshots/2083086/dribbble_1.gif')"
                                }}
                            />
                            {/* Overlay Vignette */}
                            <div className="absolute inset-0 bg-gradient-to-t from-white/20 to-transparent pointer-events-none" />
                        </div>

                        {/* Action Buttons */}
                        <div className="flex items-center justify-center gap-3 relative z-10">
                            <button
                                onClick={() => window.history.back()}
                                className="px-5 py-2.5 rounded-xl bg-white border border-gray-200 text-gray-700 font-semibold shadow-sm hover:bg-gray-50 hover:border-gray-300 hover:shadow transition-all flex items-center justify-center gap-2 group text-sm"
                            >
                                <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                                Go Back
                            </button>

                            <Link
                                href="/"
                                className="px-5 py-2.5 rounded-xl bg-gray-900 text-white font-semibold shadow-lg shadow-gray-900/20 hover:bg-black hover:shadow-gray-900/30 hover:scale-[1.02] transition-all flex items-center justify-center gap-2 text-sm"
                            >
                                <Home className="w-4 h-4" />
                                Back to Home
                            </Link>

                        </div>
                    </div>
                </motion.div>

                {/* Innovative Search Teaser */}
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.8 }}
                    className="mt-6 md:mt-8 flex items-center gap-3 px-5 py-2 bg-white/40 backdrop-blur-md rounded-full border border-white/50 shadow-sm shrink-0"
                >
                    <Search className="w-3.5 h-3.5 text-gray-400" />
                    <span className="text-xs md:text-sm font-medium text-gray-500">
                        Try searching for <span className="text-indigo-600">Students</span>, <span className="text-indigo-600">Drives</span>...
                    </span>
                </motion.div>

                <motion.p
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 1 }}
                    className="mt-6 text-[10px] font-semibold text-gray-400 tracking-widest uppercase shrink-0"
                >
                    &copy; {new Date().getFullYear()} PlacementPro System. Secure & Encrypted.
                </motion.p>
            </div>
        </div>
    );
}
