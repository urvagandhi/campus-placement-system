"use client";

import React from "react";
import Link from "next/link";

export default function MaintenancePage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="max-w-md w-full text-center space-y-6 bg-white p-10 rounded-2xl shadow-xl">
        <div className="flex justify-center">
          <svg
            className="w-24 h-24 text-amber-500"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
            />
          </svg>
        </div>
        
        <div className="space-y-2">
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">
            System Under Maintenance
          </h1>
          <p className="text-gray-500 text-lg">
            We are currently performing scheduled maintenance. Only Super Administrators can log in at this time.
          </p>
        </div>

        <div className="pt-4 border-t border-gray-100">
          <Link
            href="/login"
            className="text-blue-600 hover:text-blue-800 font-medium transition-colors"
          >
            Check status (Login) &rarr;
          </Link>
        </div>
        
        <div className="text-xs text-gray-400 mt-8">
           &copy; {new Date().getFullYear()} Campus Placement System
        </div>
      </div>
    </div>
  );
}
