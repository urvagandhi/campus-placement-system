'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import { AlertTriangle, ArrowLeft, Plus, X } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

// Mock Academic Calendar Data for Conflict Detection
const ACADEMIC_CALENDAR = [
    { department: 'Computer Science and Engineering', event: 'End Semester Exams', startDate: '2023-11-20', endDate: '2023-11-30' },
    { department: 'Information Technology', event: 'Practical Exams', startDate: '2023-11-15', endDate: '2023-11-18' },
    { department: 'Mechanical Engineering', event: 'Industrial Visit', startDate: '2023-11-25', endDate: '2023-11-26' },
    { department: 'Electronics and Communication', event: 'Project Reviews', startDate: '2023-11-22', endDate: '2023-11-24' }
];

const DEPARTMENTS = [
    'Computer Science and Engineering',
    'Information Technology',
    'Electronics and Communication',
    'Mechanical Engineering',
    'Civil Engineering'
];

export default function CreateDrivePage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [showSuccessModal, setShowSuccessModal] = useState(false);

    // Form State
    const [formData, setFormData] = useState({
        company: '',
        role: '',
        minCGPA: '',
        salary: '',
        description: '',
        date: '',
        deadline: '',
        departments: [],
        skills: [],
        remarks: '' // Department constraints/remarks
    });

    // Conflict State
    const [academicConflicts, setAcademicConflicts] = useState([]);

    // Check for conflicts whenever date or departments change
    useEffect(() => {
        checkAcademicConflicts();
    }, [formData.date, formData.departments]);

    const checkAcademicConflicts = () => {
        if (!formData.date || formData.departments.length === 0) {
            setAcademicConflicts([]);
            return;
        }

        const driveDate = new Date(formData.date);
        const conflicts = [];

        formData.departments.forEach(dept => {
            const deptEvents = ACADEMIC_CALENDAR.filter(e => e.department === dept);
            deptEvents.forEach(event => {
                const start = new Date(event.startDate);
                const end = new Date(event.endDate);

                // Check if drive date falls within event range (inclusive)
                if (driveDate >= start && driveDate <= end) {
                    conflicts.push({
                        department: dept,
                        event: event.event,
                        dateRange: `${event.startDate} to ${event.endDate}`
                    });
                }
            });
        });

        setAcademicConflicts(conflicts);
    };

    const [skillInput, setSkillInput] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const toggleDepartment = (dept) => {
        setFormData(prev => ({
            ...prev,
            departments: prev.departments.includes(dept)
                ? prev.departments.filter(d => d !== dept)
                : [...prev.departments, dept]
        }));
    };

    const addSkill = (e) => {
        if (e.key === 'Enter' && skillInput.trim()) {
            e.preventDefault();
            if (!formData.skills.includes(skillInput.trim())) {
                setFormData(prev => ({
                    ...prev,
                    skills: [...prev.skills, skillInput.trim()]
                }));
            }
            setSkillInput('');
        }
    };

    const removeSkill = (skillToRemove) => {
        setFormData(prev => ({
            ...prev,
            skills: prev.skills.filter(s => s !== skillToRemove)
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        // Simulate API call
        setTimeout(() => {
            setLoading(false);
            setShowSuccessModal(true);
        }, 1500);
    };

    const handleSuccessClose = () => {
        setShowSuccessModal(false);
        router.push('/dashboard/coordinator/drives');
    };

    return (
        <div className="space-y-6 max-w-4xl mx-auto">
            <Link
                href="/dashboard/coordinator/drives"
                className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
            >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Drives
            </Link>

            <div>
                <h1 className="text-2xl font-bold text-gray-900">Schedule New Drive</h1>
                <p className="text-gray-600 mt-1">Fill in the details to create a new placement drive.</p>
            </div>

            <Card className="border border-gray-100">
                <form onSubmit={handleSubmit} className="space-y-8">
                    {/* Basic Info */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-gray-900 border-b border-gray-100 pb-2">Company & Role</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <Input
                                label="Company Name"
                                name="company"
                                value={formData.company}
                                onChange={handleChange}
                                placeholder="e.g. Google"
                                required
                            />
                            <Input
                                label="Job Role"
                                name="role"
                                value={formData.role}
                                onChange={handleChange}
                                placeholder="e.g. Software Engineer"
                                required
                            />
                            <Input
                                label="Salary Package / Stipend"
                                name="salary"
                                value={formData.salary}
                                onChange={handleChange}
                                placeholder="e.g. 12 LPA or 50k/month"
                                required
                            />
                            <div className="flex flex-col md:flex-row gap-4">
                                <Input
                                    type="date"
                                    label="Drive Date"
                                    name="date"
                                    value={formData.date}
                                    onChange={handleChange}
                                    required
                                    className="flex-1"
                                />
                                <Input
                                    type="date"
                                    label="Application Deadline"
                                    name="deadline"
                                    value={formData.deadline}
                                    onChange={handleChange}
                                    required
                                    className="flex-1"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Eligibility */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-gray-900 border-b border-gray-100 pb-2">Eligibility Criteria</h3>
                        <div>
                            <Input
                                label="Minimum CGPA"
                                name="minCGPA"
                                type="number"
                                step="0.1"
                                max="10"
                                value={formData.minCGPA}
                                onChange={handleChange}
                                placeholder="e.g. 7.5"
                                required
                                className="max-w-xs"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Eligible Departments</label>
                            <div className="flex flex-wrap gap-2">
                                {DEPARTMENTS.map(dept => (
                                    <div
                                        key={dept}
                                        onClick={() => toggleDepartment(dept)}
                                        className={`px-3 py-1.5 rounded-full text-sm cursor-pointer border transition-all select-none ${formData.departments.includes(dept)
                                            ? 'bg-indigo-600 text-white border-indigo-600'
                                            : 'bg-white text-gray-600 border-gray-300 hover:border-gray-400'
                                            }`}
                                    >
                                        {dept}
                                    </div>
                                ))}
                            </div>
                            {formData.departments.length === 0 && (
                                <p className="text-xs text-red-500 mt-1">Please select at least one department.</p>
                            )}
                        </div>
                    </div>

                    {/* Academic Conflict Warning */}
                    {academicConflicts.length > 0 && (
                        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 animate-fade-in">
                            <div className="flex items-start gap-3">
                                <AlertTriangle className="h-5 w-5 text-amber-600 mt-0.5" />
                                <div>
                                    <h4 className="text-sm font-semibold text-amber-900">Academic Schedule Conflict Detected</h4>
                                    <p className="text-sm text-amber-700 mt-1">
                                        The selected date conflicts with the following department schedules.
                                        This is informational; you may proceed if approved by updates.
                                    </p>
                                    <ul className="mt-3 space-y-1">
                                        {academicConflicts.map((conflict, idx) => (
                                            <li key={idx} className="text-xs font-medium text-amber-800 flex items-center gap-2">
                                                <span className="w-1.5 h-1.5 rounded-full bg-amber-500"></span>
                                                {conflict.department}: {conflict.event} ({conflict.dateRange})
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            </div>
                        </div>
                    )}


                    {/* Skills & Description */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-gray-900 border-b border-gray-100 pb-2">Additional Details</h3>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">Required Skills</label>
                            <div className="flex flex-wrap gap-2 mb-2 p-2 bg-gray-50 rounded-lg min-h-[44px]">
                                {formData.skills.map((skill) => (
                                    <Badge key={skill} variant="neutral" className="flex items-center gap-1 bg-white border border-gray-200">
                                        {skill}
                                        <X
                                            className="h-3 w-3 cursor-pointer hover:text-red-500"
                                            onClick={() => removeSkill(skill)}
                                        />
                                    </Badge>
                                ))}
                                <input
                                    type="text"
                                    value={skillInput}
                                    onChange={(e) => setSkillInput(e.target.value)}
                                    onKeyDown={addSkill}
                                    placeholder={formData.skills.length === 0 ? "Type skill & press Enter" : ""}
                                    className="bg-transparent focus:outline-none text-sm min-w-[150px]"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Job Description</label>
                            <textarea
                                name="description"
                                rows="4"
                                value={formData.description}
                                onChange={handleChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all resize-none"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Department Constraints / Remarks
                                <span className="ml-2 text-xs text-gray-400 font-normal">(Optional)</span>
                            </label>
                            <textarea
                                name="remarks"
                                rows="2"
                                value={formData.remarks}
                                onChange={handleChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all resize-none"
                                placeholder="Note any specific department constraints or arrangements..."
                            />
                        </div>
                    </div>

                    <div className="flex gap-4 pt-4 border-t border-gray-100">
                        <Button
                            type="submit"
                            loading={loading}
                            className="w-full md:w-auto"
                            disabled={formData.departments.length === 0}
                        >
                            Save & Publish Drive
                        </Button>
                        <Link href="/dashboard/coordinator/drives">
                            <Button variant="secondary" className="w-full md:w-auto">Cancel</Button>
                        </Link>
                    </div>
                </form>
            </Card>

            <Modal
                isOpen={showSuccessModal}
                onClose={handleSuccessClose}
                title="Drive Created Successfully"
            >
                <div className="py-2 text-center">
                    <div className="h-16 w-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Plus className="h-8 w-8 text-green-600" />
                    </div>
                    <p className="text-gray-600 mb-6">
                        The placement drive for <span className="font-bold text-gray-900">{formData.company}</span> has been scheduled and notifications have been sent to eligible students.
                    </p>
                    <Button onClick={handleSuccessClose} className="w-full">
                        Go to Dashboard
                    </Button>
                </div>
            </Modal>
        </div >
    );
}
