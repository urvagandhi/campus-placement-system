'use client';

import { 
    BookOpen, 
    Briefcase, 
    Building2, 
    ChevronDown, 
    ChevronRight, 
    Landmark, 
    Users, 
    FolderTree
} from 'lucide-react';
import { useState } from 'react';

/**
 * Clean Tree Node - Matches app's existing Card design language
 */
function TreeNode({ node, level = 0, defaultExpanded = true }) {
    const [isExpanded, setIsExpanded] = useState(defaultExpanded);
    const hasChildren = node.children && node.children.length > 0;

    // Simple, clean styling matching the app's design
    const getConfig = (type) => {
        switch (type) {
            case 'UNIVERSITY':
                return {
                    icon: Landmark,
                    iconBg: 'bg-violet-100/50 text-violet-600',
                    accent: 'ring-violet-100'
                };
            case 'INSTITUTE':
                return {
                    icon: Building2,
                    iconBg: 'bg-blue-100/50 text-blue-600',
                    accent: 'ring-blue-100'
                };
            case 'DEPARTMENT':
                return {
                    icon: BookOpen,
                    iconBg: 'bg-emerald-100/50 text-emerald-600',
                    accent: 'ring-emerald-100'
                };
            default:
                return {
                    icon: FolderTree,
                    iconBg: 'bg-gray-100/50 text-gray-600',
                    accent: 'ring-gray-100'
                };
        }
    };

    const config = getConfig(node.type);
    const Icon = config.icon;
    const indent = level * 32;

    return (
        <div className="relative">
            {/* Simple connector line */}
            {level > 0 && (
                <div 
                    className="absolute left-0 top-0 bottom-0 w-px bg-gray-200"
                    style={{ left: `${(level - 1) * 32 + 12}px` }}
                />
            )}

            <div 
                className="relative flex items-start gap-3 py-2"
                style={{ paddingLeft: `${indent}px` }}
            >
                {/* Horizontal connector */}
                {level > 0 && (
                    <div 
                        className="absolute h-px bg-gray-200 top-6"
                        style={{ left: `${(level - 1) * 32 + 12}px`, width: '20px' }}
                    />
                )}

                {/* Toggle button - disabled at root level */}
                <button
                    onClick={() => hasChildren && level > 0 && setIsExpanded(!isExpanded)}
                    disabled={!hasChildren || level === 0}
                    className={`shrink-0 w-6 h-6 flex items-center justify-center rounded-md transition-colors mt-2.5
                        ${hasChildren && level > 0
                            ? 'hover:bg-gray-100 cursor-pointer text-gray-500' 
                            : 'text-gray-300 cursor-default'}`}
                >
                    {hasChildren && level > 0 && (isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />)}
                </button>

                {/* Card - matching app's Card component style */}
                <div 
                    onClick={() => hasChildren && level > 0 && setIsExpanded(!isExpanded)}
                    className={`flex-1 bg-white/60 backdrop-blur-xl rounded-2xl p-4 ring-1 ring-black/5 ${config.accent}
                        ${hasChildren && level > 0 ? 'cursor-pointer hover:ring-2 transition-all' : ''}`}
                >
                    <div className="flex items-center justify-between gap-4 flex-wrap">
                        {/* Left side - Icon & Info */}
                        <div className="flex items-center gap-4">
                            <div className={`p-3 rounded-2xl shadow-inner ${config.iconBg}`}>
                                <Icon className="h-5 w-5" />
                            </div>
                            <div>
                                <div className="flex items-center gap-2">
                                    <h3 className="font-semibold text-gray-900">{node.name}</h3>
                                    {node.code && (
                                        <span className="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                                            {node.code}
                                        </span>
                                    )}
                                </div>
                                <p className="text-sm text-gray-500 capitalize">{node.type?.toLowerCase()}</p>
                            </div>
                        </div>

                        {/* Right side - Stats (for Institutes & Departments) */}
                        {(node.type === 'DEPARTMENT' || node.type === 'INSTITUTE') && (
                            <div className="flex items-center gap-6">
                                <div className="text-center">
                                    <div className="flex items-center gap-1.5 text-gray-400 mb-1">
                                        <Users size={14} />
                                        <span className="text-xs font-medium">Students</span>
                                    </div>
                                    <p className="text-lg font-bold text-gray-900">{node.studentCount || 0}</p>
                                </div>
                                <div className="text-center">
                                    <div className="flex items-center gap-1.5 text-gray-400 mb-1">
                                        <Briefcase size={14} />
                                        <span className="text-xs font-medium">Placed</span>
                                    </div>
                                    <p className="text-lg font-bold text-emerald-600">{node.placementCount || 0}</p>
                                </div>
                                {node.studentCount > 0 && (
                                    <div className="text-center px-3 py-1.5 bg-emerald-50 rounded-lg">
                                        <p className="text-sm font-bold text-emerald-700">
                                            {Math.round(((node.placementCount || 0) / node.studentCount) * 100)}%
                                        </p>
                                        <p className="text-xs text-emerald-600">Success</p>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Children */}
            {hasChildren && isExpanded && (
                <div>
                    {node.children.map((child, index) => (
                        <TreeNode 
                            key={child.id} 
                            node={child} 
                            level={level + 1}
                            defaultExpanded={false}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

/**
 * Organization Tree - Clean design matching app aesthetic
 */
export default function OrganizationTree({ hierarchy, loading = false }) {
    if (loading) {
        return (
            <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                    <div key={i} className="flex gap-3 animate-pulse" style={{ paddingLeft: `${(i - 1) * 32}px` }}>
                        <div className="w-6 h-6 bg-gray-100 rounded" />
                        <div className="flex-1 h-20 bg-gray-100/50 rounded-2xl" />
                    </div>
                ))}
            </div>
        );
    }

    if (!hierarchy) {
        return (
            <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="p-4 bg-gray-100/50 rounded-2xl mb-4">
                    <FolderTree className="w-8 h-8 text-gray-400" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900">No Organization Structure</h3>
                <p className="text-sm text-gray-500 mt-1 max-w-sm">
                    Add institutes and departments to build your hierarchy.
                </p>
            </div>
        );
    }

    return (
        <div className="py-4">
            <TreeNode node={hierarchy} level={0} defaultExpanded={true} />
        </div>
    );
}

