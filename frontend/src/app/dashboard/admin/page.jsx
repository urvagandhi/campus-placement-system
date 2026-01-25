'use client';

import { analyticsApi } from '@/services/api';
import { useAuth } from '@/hooks/useAuth';
import { 
    Briefcase, 
    TrendingUp, 
    Banknote,
    FileText,
    Building2,
    GraduationCap,
    ChevronDown,
    ChevronUp,
    Calendar,
} from 'lucide-react';
import { useEffect, useState, useCallback } from 'react';

export default function AdminDashboard() {
    const { user } = useAuth();
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState(null);
    const [selectedYear, setSelectedYear] = useState(null);
    const [currentYear, setCurrentYear] = useState(null);
    const [expandedInstitutes, setExpandedInstitutes] = useState({});

    // Generate academic year options (current year and 4 previous years)
    const generateYearOptions = useCallback(() => {
        if (!currentYear) return [];
        const years = [];
        const startYear = parseInt(currentYear.substring(0, 4));
        for (let i = 0; i < 5; i++) {
            const year = startYear - i;
            years.push(`${year}-${String(year + 1).substring(2)}`);
        }
        return years;
    }, [currentYear]);

    useEffect(() => {
        const fetchCurrentYear = async () => {
            try {
                const response = await analyticsApi.getCurrentAcademicYear();
                if (response.success) {
                    setCurrentYear(response.data);
                    setSelectedYear(response.data);
                }
            } catch (error) {
                console.error("Failed to fetch current academic year:", error);
                // Fallback to manual calculation
                const now = new Date();
                const year = now.getMonth() >= 6 ? now.getFullYear() : now.getFullYear() - 1;
                const fallbackYear = `${year}-${String(year + 1).substring(2)}`;
                setCurrentYear(fallbackYear);
                setSelectedYear(fallbackYear);
            }
        };
        fetchCurrentYear();
    }, []);

    useEffect(() => {
        if (!selectedYear) return;

        const fetchStats = async () => {
            setLoading(true);
            try {
                const response = await analyticsApi.getOverview(selectedYear);
                if (response.success) {
                    setStats(response.data);
                }
            } catch (error) {
                console.error("Admin stats fetch error:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [selectedYear]);

    const toggleInstitute = (instituteId) => {
        setExpandedInstitutes(prev => ({
            ...prev,
            [instituteId]: !prev[instituteId]
        }));
    };

    const StatCard = ({ title, value, subtext, icon: Icon, colorClass, delay }) => (
        <div className={`relative overflow-hidden bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-all duration-300 animate-slide-up`} style={{ animationDelay: `${delay}ms` }}>
            <div className={`absolute top-0 right-0 p-3 opacity-10 ${colorClass}`}>
                <Icon size={80} />
            </div>
            <div className="relative z-10">
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${colorClass.replace('text-', 'bg-').replace('600', '50')} ${colorClass}`}>
                    <Icon size={24} />
                </div>
                <h3 className="text-3xl font-black text-gray-900 tracking-tight mb-1">{value}</h3>
                <p className="text-sm font-semibold text-gray-500 uppercase tracking-wide">{title}</p>
                {subtext && <p className="text-xs font-medium text-gray-400 mt-2">{subtext}</p>}
            </div>
        </div>
    );

    const InstituteCard = ({ institute }) => {
        const isExpanded = expandedInstitutes[institute.instituteId];
        
        return (
            <div className="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm hover:shadow-md transition-all duration-200">
                <button
                    onClick={() => toggleInstitute(institute.instituteId)}
                    className="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-50 transition-colors"
                >
                    <div className="flex items-center gap-4">
                        <div className="w-10 h-10 rounded-lg bg-violet-50 flex items-center justify-center">
                            <Building2 className="text-violet-600" size={20} />
                        </div>
                        <div className="text-left">
                            <h3 className="font-semibold text-gray-900">{institute.instituteName}</h3>
                            <p className="text-sm text-gray-500">{institute.instituteCode || 'Institute'}</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-6">
                        <div className="text-right">
                            <p className="text-lg font-bold text-gray-900">{Math.round(institute.placementRate || 0)}%</p>
                            <p className="text-xs text-gray-500">Placement Rate</p>
                        </div>
                        <div className="text-right">
                            <p className="text-lg font-bold text-emerald-600">₹{(institute.averagePackage || 0).toFixed(1)} LPA</p>
                            <p className="text-xs text-gray-500">Avg Package</p>
                        </div>
                        <div className="text-right">
                            <p className="text-lg font-bold text-gray-700">{institute.placedStudents || 0}/{institute.totalStudents || 0}</p>
                            <p className="text-xs text-gray-500">Placed</p>
                        </div>
                        {isExpanded ? <ChevronUp className="text-gray-400" size={20} /> : <ChevronDown className="text-gray-400" size={20} />}
                    </div>
                </button>
                
                {isExpanded && institute.departmentStats && institute.departmentStats.length > 0 && (
                    <div className="border-t border-gray-100 bg-gray-50/50 p-4">
                        <h4 className="text-sm font-semibold text-gray-600 mb-3 flex items-center gap-2">
                            <GraduationCap size={16} />
                            Department Breakdown
                        </h4>
                        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
                            {institute.departmentStats.map((dept) => (
                                <DepartmentCard key={dept.departmentId} department={dept} compact />
                            ))}
                        </div>
                    </div>
                )}
            </div>
        );
    };

    const DepartmentCard = ({ department, compact = false }) => {
        if (compact) {
            return (
                <div className="bg-white rounded-lg p-4 border border-gray-100 shadow-sm">
                    <div className="flex items-center justify-between mb-2">
                        <h5 className="font-medium text-gray-900 truncate">{department.departmentName}</h5>
                        <span className="text-sm font-bold text-emerald-600">{Math.round(department.placementRate || 0)}%</span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-gray-500">
                        <span>{department.placedStudents || 0}/{department.totalStudents || 0} placed</span>
                        <span>₹{(department.averagePackage || 0).toFixed(1)} LPA</span>
                    </div>
                </div>
            );
        }

        return (
            <div className="bg-white rounded-xl p-5 border border-gray-100 shadow-sm hover:shadow-md transition-all">
                <div className="flex items-center gap-3 mb-4">
                    <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                        <GraduationCap className="text-blue-600" size={20} />
                    </div>
                    <div>
                        <h4 className="font-semibold text-gray-900">{department.departmentName}</h4>
                        {department.instituteName && (
                            <p className="text-xs text-gray-500">{department.instituteName}</p>
                        )}
                    </div>
                </div>
                <div className="grid grid-cols-3 gap-3 text-center">
                    <div className="bg-gray-50 rounded-lg p-2">
                        <p className="text-lg font-bold text-gray-900">{Math.round(department.placementRate || 0)}%</p>
                        <p className="text-xs text-gray-500">Rate</p>
                    </div>
                    <div className="bg-emerald-50 rounded-lg p-2">
                        <p className="text-lg font-bold text-emerald-600">₹{(department.averagePackage || 0).toFixed(1)}</p>
                        <p className="text-xs text-gray-500">Avg LPA</p>
                    </div>
                    <div className="bg-blue-50 rounded-lg p-2">
                        <p className="text-lg font-bold text-blue-600">{department.placedStudents || 0}</p>
                        <p className="text-xs text-gray-500">Placed</p>
                    </div>
                </div>
            </div>
        );
    };

    if (loading) {
        return (
            <div className="space-y-8 animate-pulse">
                <div className="flex items-center justify-between">
                    <div className="h-8 w-64 bg-gray-200 rounded-lg" />
                    <div className="h-10 w-40 bg-gray-200 rounded-lg" />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {[1, 2, 3, 4].map(i => (
                        <div key={i} className="h-40 bg-gray-100/80 rounded-2xl" />
                    ))}
                </div>
                <div className="h-64 bg-gray-100/80 rounded-2xl" />
            </div>
        );
    }

    const yearOptions = generateYearOptions();

    return (
        <div className="space-y-8 pb-10">
            {/* Header Section */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-black text-gray-900 tracking-tight">Executive Overview</h1>
                    <p className="text-gray-500 font-medium mt-1">
                        {stats?.scopeLevel === 'COLLEGE' && 'College-wide placement analytics'}
                        {stats?.scopeLevel === 'INSTITUTE' && `${stats?.scopeName || 'Institute'} placement analytics`}
                        {stats?.scopeLevel === 'DEPARTMENT' && `${stats?.scopeName || 'Department'} placement analytics`}
                        {!stats?.scopeLevel && 'Real-time placement analytics & management'}
                    </p>
                </div>
                <div className="flex items-center gap-3">
                    {/* Academic Year Selector */}
                    <div className="flex items-center gap-2 bg-white border border-gray-200 rounded-xl px-3 py-2 shadow-sm">
                        <Calendar className="text-gray-400" size={18} />
                        <select
                            value={selectedYear || ''}
                            onChange={(e) => setSelectedYear(e.target.value)}
                            className="bg-transparent border-none text-sm font-medium text-gray-700 focus:outline-none cursor-pointer pr-6"
                        >
                            {yearOptions.map(year => (
                                <option key={year} value={year}>
                                    AY {year}
                                </option>
                            ))}
                        </select>
                    </div>
                    <span className="px-3 py-1 bg-emerald-50 text-emerald-700 text-xs font-bold uppercase tracking-wider rounded-full border border-emerald-100">
                        Live System
                    </span>
                </div>
            </div>

            {/* Scope Badge */}
            {stats?.scopeLevel && (
                <div className="flex items-center gap-2">
                    <span className="px-4 py-2 bg-gradient-to-r from-violet-50 to-blue-50 text-violet-700 text-sm font-semibold rounded-lg border border-violet-100">
                        Viewing: {stats.scopeName || stats.scopeLevel} ({stats.academicYear})
                    </span>
                </div>
            )}

            {/* Primary Metrics Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard 
                    title="Placement Rate" 
                    value={`${Math.round(stats?.placementRate || 0)}%`}
                    subtext={`${stats?.placedStudents || 0} / ${stats?.totalStudents || 0} Students Placed`}
                    icon={TrendingUp}
                    colorClass="text-emerald-600"
                    delay={0}
                />
                <StatCard 
                    title="Average Package" 
                    value={`₹${(stats?.averagePackage || 0).toFixed(1)} LPA`}
                    subtext={`Highest: ₹${(stats?.highestPackage || 0).toFixed(1)} LPA`}
                    icon={Banknote}
                    colorClass="text-blue-600"
                    delay={100}
                />
                <StatCard 
                    title="Total Drives" 
                    value={stats?.totalDrives || 0}
                    subtext={`${stats?.activeDrives || 0} Currently Active`}
                    icon={Briefcase}
                    colorClass="text-violet-600"
                    delay={200}
                />
                <StatCard 
                    title="Total Applications" 
                    value={stats?.totalApplications || 0}
                    subtext={`${stats?.companiesVisited || 0} Companies Participating`}
                    icon={FileText}
                    colorClass="text-orange-600"
                    delay={300}
                />
            </div>

            {/* Institute Breakdown (for College-level users) */}
            {stats?.instituteStats && stats.instituteStats.length > 0 && (
                <div className="space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                            <Building2 className="text-violet-600" size={24} />
                            Institute-wise Statistics
                        </h2>
                        <span className="text-sm text-gray-500">{stats.instituteStats.length} Institutes</span>
                    </div>
                    <div className="space-y-3">
                        {stats.instituteStats.map((institute) => (
                            <InstituteCard key={institute.instituteId} institute={institute} />
                        ))}
                    </div>
                </div>
            )}

            {/* Department Breakdown (for Institute-level users or when no institutes) */}
            {stats?.departmentStats && stats.departmentStats.length > 0 && !stats?.instituteStats?.length && (
                <div className="space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                            <GraduationCap className="text-blue-600" size={24} />
                            Department-wise Statistics
                        </h2>
                        <span className="text-sm text-gray-500">{stats.departmentStats.length} Departments</span>
                    </div>
                    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                        {stats.departmentStats.map((dept) => (
                            <DepartmentCard key={dept.departmentId} department={dept} />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
