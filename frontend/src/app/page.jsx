'use client';

import Button from "@/components/ui/Button";
import Logo from "@/components/ui/Logo";
import { ArrowRight, CheckCircle, Shield, Zap } from "lucide-react";
import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen bg-[#f5f5f7] relative overflow-hidden flex flex-col">

      {/* Background Mesh */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-[-20%] right-[-10%] w-[70%] h-[70%] rounded-full bg-blue-400/10 blur-[120px]" />
        <div className="absolute bottom-[-20%] left-[-10%] w-[70%] h-[70%] rounded-full bg-indigo-400/10 blur-[120px]" />
      </div>

      {/* Navbar */}
      <header className="relative z-10 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex items-center justify-between">
        <Logo size="md" />
        <div className="flex items-center gap-4">
          <Link href="/login">
            <Button variant="ghost" className="font-medium">Sign In</Button>
          </Link>
          <Link href="/register">
            <Button className="rounded-full px-6 shadow-lg shadow-indigo-500/20">Get Started</Button>
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-grow flex flex-col items-center justify-center relative z-10 px-4 sm:px-6 lg:px-8 text-center pb-20">

        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/60 backdrop-blur-md border border-white/50 shadow-sm mb-8 animate-fade-in-down">
          <span className="flex h-2 w-2 rounded-full bg-emerald-500"></span>
          <span className="text-sm font-medium text-gray-600">v1.0 Public Beta is Live</span>
        </div>

        <h1 className="text-5xl md:text-7xl font-bold tracking-tight text-gray-900 mb-6 max-w-4xl mx-auto leading-tight bg-clip-text text-transparent bg-gradient-to-b from-gray-900 to-gray-700">
          Campus Placements, <br />
          <span className="text-indigo-600">Reimagined.</span>
        </h1>

        <p className="text-xl text-gray-500 max-w-2xl mx-auto mb-10 leading-relaxed">
          The modern, intelligent platform for universities to manage recruitment drives, student profiles, and company connections with ease.
        </p>

        <div className="flex flex-col sm:flex-row items-center gap-4 mb-20">
          <Link href="/register">
            <Button size="xl" className="rounded-full px-8 text-lg shadow-xl shadow-indigo-500/20 hover:shadow-indigo-500/30 transition-all">
              Start Free Trial <ArrowRight className="ml-2 h-5 w-5" />
            </Button>
          </Link>
          <Link href="/login">
            <Button variant="secondary" size="xl" className="rounded-full px-8 text-lg bg-white/50 backdrop-blur-md border border-white/60 hover:bg-white/80">
              Live Demo
            </Button>
          </Link>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto w-full text-left">
          <div className="glass-card p-8 bg-white/40 border-white/50 hover:bg-white/60 transition-all">
            <div className="h-12 w-12 bg-indigo-100 rounded-2xl flex items-center justify-center text-indigo-600 mb-6">
              <Zap className="h-6 w-6" />
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Lightning Fast</h3>
            <p className="text-gray-500 leading-relaxed">
              Optimized for speed and performance. Manage thousands of student records without a hitch.
            </p>
          </div>
          <div className="glass-card p-8 bg-white/40 border-white/50 hover:bg-white/60 transition-all">
            <div className="h-12 w-12 bg-emerald-100 rounded-2xl flex items-center justify-center text-emerald-600 mb-6">
              <Shield className="h-6 w-6" />
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Secure by Design</h3>
            <p className="text-gray-500 leading-relaxed">
              Enterprise-grade security to protect sensitive student data and placement records.
            </p>
          </div>
          <div className="glass-card p-8 bg-white/40 border-white/50 hover:bg-white/60 transition-all">
            <div className="h-12 w-12 bg-purple-100 rounded-2xl flex items-center justify-center text-purple-600 mb-6">
              <CheckCircle className="h-6 w-6" />
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Smart Analytics</h3>
            <p className="text-gray-500 leading-relaxed">
              Visual insights and reports to help TPOs make data-driven decisions for better placements.
            </p>
          </div>
        </div>

      </main>

      <footer className="py-8 text-center text-gray-400 text-sm relative z-10">
        <p>© 2024 PlacementPro. Crafted with precision.</p>
      </footer>
    </div>
  );
}
