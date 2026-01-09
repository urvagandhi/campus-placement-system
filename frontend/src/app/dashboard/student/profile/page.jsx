'use client';

import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { useAuth } from '@/hooks/useAuth';
import { studentsApi } from '@/services/api';
import {
    BookOpen,
    Briefcase,
    Building2,
    CheckCircle,
    Edit3,
    ExternalLink,
    Github,
    GraduationCap,
    Linkedin,
    Loader2,
    Save,
    User,
    X
} from 'lucide-react';
import { useEffect, useState } from 'react';

/**
 * Student Profile Page
 * 
 * Security Model:
 * - Academic fields are displayed as READ-ONLY (institution-owned)
 * - Career fields are EDITABLE (student-owned)
 * - All data is fetched from and saved to backend
 */
export default function StudentProfilePage() {
    const { user } = useAuth();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [editedProfile, setEditedProfile] = useState({});

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            setLoading(true);
            setError(null);
            const response = await studentsApi.getMyProfile();
            if (response.success) {
                setProfile(response.data);
                setEditedProfile({
                    skills: response.data.skills || [],
                    resumeUrl: response.data.resumeUrl || '',
                    projectsCount: response.data.projectsCount || 0,
                    internshipMonths: response.data.internshipMonths || 0,
                    certifications: response.data.certifications || [],
                    linkedinUrl: response.data.linkedinUrl || '',
                    githubUrl: response.data.githubUrl || '',
                    careerInterests: response.data.careerInterests || []
                });
            }
        } catch (err) {
            setError(err.message || 'Failed to load profile');
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        try {
            setSaving(true);
            setError(null);
            const response = await studentsApi.updateCareerProfile(editedProfile);
            if (response.success) {
                setProfile(response.data);
                setIsEditing(false);
            }
        } catch (err) {
            setError(err.message || 'Failed to save profile');
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = () => {
        setEditedProfile({
            skills: profile.skills || [],
            resumeUrl: profile.resumeUrl || '',
            projectsCount: profile.projectsCount || 0,
            internshipMonths: profile.internshipMonths || 0,
            certifications: profile.certifications || [],
            linkedinUrl: profile.linkedinUrl || '',
            githubUrl: profile.githubUrl || '',
            careerInterests: profile.careerInterests || []
        });
        setIsEditing(false);
    };

    const updateField = (field, value) => {
        setEditedProfile(prev => ({ ...prev, [field]: value }));
    };

    const handleSkillsChange = (value) => {
        const skills = value.split(',').map(s => s.trim()).filter(s => s);
        updateField('skills', skills);
    };

    const handleCertificationsChange = (value) => {
        const certs = value.split(',').map(s => s.trim()).filter(s => s);
        updateField('certifications', certs);
    };

    const handleCareerInterestsChange = (value) => {
        const interests = value.split(',').map(s => s.trim()).filter(s => s);
        updateField('careerInterests', interests);
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
            </div>
        );
    }

    if (error && !profile) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
                <div className="text-red-500 text-center">{error}</div>
                <Button onClick={fetchProfile}>Retry</Button>
            </div>
        );
    }

    return (
        <div className="space-y-8 animate-fade-in max-w-4xl mx-auto">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900 tracking-tight">My Profile</h1>
                    <p className="text-gray-600 mt-1">Manage your profile information</p>
                </div>
                {!isEditing ? (
                    <Button onClick={() => setIsEditing(true)} className="gap-2">
                        <Edit3 className="h-4 w-4" /> Edit Profile
                    </Button>
                ) : (
                    <div className="flex gap-3">
                        <Button variant="outline" onClick={handleCancel} disabled={saving}>
                            <X className="h-4 w-4 mr-1" /> Cancel
                        </Button>
                        <Button onClick={handleSave} disabled={saving} className="gap-2">
                            {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                            Save Changes
                        </Button>
                    </div>
                )}
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
                    {error}
                </div>
            )}

            {/* Profile Completion Status */}
            {!profile?.isProfileComplete && (
                <div className="bg-amber-50 border border-amber-200 text-amber-800 px-4 py-3 rounded-xl flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-amber-100 flex items-center justify-center">
                        <User className="h-5 w-5 text-amber-600" />
                    </div>
                    <div>
                        <p className="font-medium">Complete Your Profile</p>
                        <p className="text-sm text-amber-700">Add your skills and resume to be eligible for placement drives</p>
                    </div>
                </div>
            )}

            {/* Organization Hierarchy - READ ONLY */}
            <Card
                title="Organization"
                subtitle="Your institutional affiliation (managed by institution)"
                className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
            >
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <ReadOnlyField
                        icon={Building2}
                        label="College"
                        value={profile?.collegeName}
                    />
                    <ReadOnlyField
                        icon={GraduationCap}
                        label="Institute"
                        value={profile?.instituteName}
                    />
                    <ReadOnlyField
                        icon={BookOpen}
                        label="Department"
                        value={profile?.departmentName}
                    />
                </div>
            </Card>

            {/* Academic Information - READ ONLY */}
            <Card
                title="Academic Information"
                subtitle="Academic records maintained by institution (read-only)"
                className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
            >
                <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                    <ReadOnlyField
                        label="Enrollment No"
                        value={profile?.enrollmentNo}
                    />
                    <ReadOnlyField
                        label="CGPA"
                        value={profile?.cgpa?.toFixed(2)}
                    />
                    <ReadOnlyField
                        label="Backlogs"
                        value={profile?.backlogs ?? 0}
                    />
                    <ReadOnlyField
                        label="Batch Year"
                        value={profile?.batchYear}
                    />
                    <ReadOnlyField
                        label="Semester"
                        value={profile?.semester}
                    />
                </div>
            </Card>

            {/* Career Information - EDITABLE */}
            <Card
                title="Career Profile"
                subtitle="Your professional information (you can edit these)"
                className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
            >
                <div className="space-y-6">
                    {/* Skills */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Skills <span className="text-gray-400">(comma-separated)</span>
                        </label>
                        {isEditing ? (
                            <input
                                type="text"
                                value={editedProfile.skills?.join(', ') || ''}
                                onChange={(e) => handleSkillsChange(e.target.value)}
                                placeholder="e.g., Java, Python, React, SQL"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div className="flex flex-wrap gap-2">
                                {profile?.skills?.length > 0 ? (
                                    profile.skills.map((skill, i) => (
                                        <Badge key={i} variant="primary" size="md">{skill}</Badge>
                                    ))
                                ) : (
                                    <span className="text-gray-400 italic">No skills added</span>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Resume URL */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Resume URL
                        </label>
                        {isEditing ? (
                            <input
                                type="url"
                                value={editedProfile.resumeUrl || ''}
                                onChange={(e) => updateField('resumeUrl', e.target.value)}
                                placeholder="https://drive.google.com/your-resume"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div>
                                {profile?.resumeUrl ? (
                                    <a
                                        href={profile.resumeUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
                                    >
                                        View Resume <ExternalLink className="h-4 w-4" />
                                    </a>
                                ) : (
                                    <span className="text-gray-400 italic">No resume uploaded</span>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Experience Metrics */}
                    <div className="grid grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Projects Completed
                            </label>
                            {isEditing ? (
                                <input
                                    type="number"
                                    min="0"
                                    value={editedProfile.projectsCount || 0}
                                    onChange={(e) => updateField('projectsCount', parseInt(e.target.value) || 0)}
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                                />
                            ) : (
                                <div className="text-2xl font-bold text-gray-900">{profile?.projectsCount || 0}</div>
                            )}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Internship Experience (months)
                            </label>
                            {isEditing ? (
                                <input
                                    type="number"
                                    min="0"
                                    value={editedProfile.internshipMonths || 0}
                                    onChange={(e) => updateField('internshipMonths', parseInt(e.target.value) || 0)}
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                                />
                            ) : (
                                <div className="text-2xl font-bold text-gray-900">{profile?.internshipMonths || 0}</div>
                            )}
                        </div>
                    </div>

                    {/* Certifications */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Certifications <span className="text-gray-400">(comma-separated)</span>
                        </label>
                        {isEditing ? (
                            <input
                                type="text"
                                value={editedProfile.certifications?.join(', ') || ''}
                                onChange={(e) => handleCertificationsChange(e.target.value)}
                                placeholder="e.g., AWS Certified, Google Cloud Professional"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div className="flex flex-wrap gap-2">
                                {profile?.certifications?.length > 0 ? (
                                    profile.certifications.map((cert, i) => (
                                        <Badge key={i} variant="success" size="md">
                                            <CheckCircle className="h-3 w-3 mr-1" />{cert}
                                        </Badge>
                                    ))
                                ) : (
                                    <span className="text-gray-400 italic">No certifications added</span>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Career Interests */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Career Interests <span className="text-gray-400">(comma-separated)</span>
                        </label>
                        {isEditing ? (
                            <input
                                type="text"
                                value={editedProfile.careerInterests?.join(', ') || ''}
                                onChange={(e) => handleCareerInterestsChange(e.target.value)}
                                placeholder="e.g., Backend Development, DevOps, Machine Learning"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div className="flex flex-wrap gap-2">
                                {profile?.careerInterests?.length > 0 ? (
                                    profile.careerInterests.map((interest, i) => (
                                        <Badge key={i} variant="warning" size="md">
                                            <Briefcase className="h-3 w-3 mr-1" />{interest}
                                        </Badge>
                                    ))
                                ) : (
                                    <span className="text-gray-400 italic">No career interests added</span>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </Card>

            {/* Social Links - EDITABLE */}
            <Card
                title="Social Profiles"
                subtitle="Your professional online presence (you can edit these)"
                className="bg-white/60 backdrop-blur-xl border-0 ring-1 ring-black/5"
            >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                            <Linkedin className="h-4 w-4 text-blue-600" /> LinkedIn Profile
                        </label>
                        {isEditing ? (
                            <input
                                type="url"
                                value={editedProfile.linkedinUrl || ''}
                                onChange={(e) => updateField('linkedinUrl', e.target.value)}
                                placeholder="https://linkedin.com/in/yourprofile"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div>
                                {profile?.linkedinUrl ? (
                                    <a
                                        href={profile.linkedinUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-blue-600 hover:text-blue-800 flex items-center gap-1"
                                    >
                                        View Profile <ExternalLink className="h-4 w-4" />
                                    </a>
                                ) : (
                                    <span className="text-gray-400 italic">Not provided</span>
                                )}
                            </div>
                        )}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                            <Github className="h-4 w-4" /> GitHub Profile
                        </label>
                        {isEditing ? (
                            <input
                                type="url"
                                value={editedProfile.githubUrl || ''}
                                onChange={(e) => updateField('githubUrl', e.target.value)}
                                placeholder="https://github.com/yourusername"
                                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                            />
                        ) : (
                            <div>
                                {profile?.githubUrl ? (
                                    <a
                                        href={profile.githubUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="text-gray-700 hover:text-gray-900 flex items-center gap-1"
                                    >
                                        View Profile <ExternalLink className="h-4 w-4" />
                                    </a>
                                ) : (
                                    <span className="text-gray-400 italic">Not provided</span>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </Card>
        </div>
    );
}

/**
 * Read-only field component for institution-owned data.
 * Styled with disabled appearance to clearly indicate non-editable.
 */
function ReadOnlyField({ icon: Icon, label, value }) {
    return (
        <div className="space-y-1">
            <label className="block text-xs font-medium text-gray-500 uppercase tracking-wider">
                {label}
            </label>
            <div className="flex items-center gap-2 px-4 py-3 bg-gray-50 rounded-xl border border-gray-100 text-gray-700">
                {Icon && <Icon className="h-4 w-4 text-gray-400" />}
                <span className="font-medium">{value || '—'}</span>
            </div>
        </div>
    );
}
