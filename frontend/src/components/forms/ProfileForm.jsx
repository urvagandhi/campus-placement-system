'use client';

import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { useState } from 'react';

/**
 * Profile form component for students
 */
export default function ProfileForm({ initialData = {}, onSubmit, loading = false }) {
    const [formData, setFormData] = useState({
        enrollmentNo: initialData.enrollmentNo || '',
        department: initialData.department || '',
        cgpa: initialData.cgpa || '',
        skills: initialData.skills?.join(', ') || '',
        resumeUrl: initialData.resumeUrl || '',
        batchYear: initialData.batchYear || '',
        semester: initialData.semester || '',
        projectsCount: initialData.projectsCount || 0,
        internshipMonths: initialData.internshipMonths || 0,
        linkedinUrl: initialData.linkedinUrl || '',
        githubUrl: initialData.githubUrl || '',
    });

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        // Convert skills string to array
        const submitData = {
            ...formData,
            skills: formData.skills.split(',').map(s => s.trim()).filter(Boolean),
        };
        onSubmit(submitData);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                    label="Enrollment Number"
                    name="enrollmentNo"
                    value={formData.enrollmentNo}
                    onChange={handleChange}
                    placeholder="e.g., 2021CSE001"
                    required
                />

                <Input
                    label="Department"
                    name="department"
                    value={formData.department}
                    onChange={handleChange}
                    placeholder="e.g., CSE"
                    required
                />

                <Input
                    label="CGPA"
                    type="number"
                    step="0.01"
                    min="0"
                    max="10"
                    name="cgpa"
                    value={formData.cgpa}
                    onChange={handleChange}
                    placeholder="e.g., 8.5"
                    required
                />

                <Input
                    label="Batch Year"
                    type="number"
                    name="batchYear"
                    value={formData.batchYear}
                    onChange={handleChange}
                    placeholder="e.g., 2025"
                />

                <Input
                    label="Semester"
                    name="semester"
                    value={formData.semester}
                    onChange={handleChange}
                    placeholder="e.g., 6th"
                />

                <Input
                    label="Projects Count"
                    type="number"
                    min="0"
                    name="projectsCount"
                    value={formData.projectsCount}
                    onChange={handleChange}
                />

                <Input
                    label="Internship (Months)"
                    type="number"
                    min="0"
                    name="internshipMonths"
                    value={formData.internshipMonths}
                    onChange={handleChange}
                />
            </div>

            <Input
                label="Skills"
                name="skills"
                value={formData.skills}
                onChange={handleChange}
                placeholder="e.g., Java, Python, SQL, Machine Learning"
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                    label="LinkedIn URL"
                    type="url"
                    name="linkedinUrl"
                    value={formData.linkedinUrl}
                    onChange={handleChange}
                    placeholder="https://linkedin.com/in/..."
                />

                <Input
                    label="GitHub URL"
                    type="url"
                    name="githubUrl"
                    value={formData.githubUrl}
                    onChange={handleChange}
                    placeholder="https://github.com/..."
                />
            </div>

            <Input
                label="Resume URL"
                type="url"
                name="resumeUrl"
                value={formData.resumeUrl}
                onChange={handleChange}
                placeholder="Link to your resume (Google Drive, etc.)"
            />

            <Button type="submit" loading={loading} className="w-full">
                Save Profile
            </Button>
        </form>
    );
}
