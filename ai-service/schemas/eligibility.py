"""
Schemas for eligibility API endpoints.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class EligibilityRequest(BaseModel):
    """Request schema for eligibility scoring."""

    # Student data
    student_id: int = Field(..., description="Student ID")
    student_name: Optional[str] = Field(None, description="Student name")
    department: Optional[str] = Field(None, description="Student's department")
    cgpa: float = Field(..., ge=0.0, le=10.0, description="Student's CGPA")
    skills: List[str] = Field(default_factory=list, description="Student's skills")
    certifications: List[str] = Field(default_factory=list, description="Certifications")
    projects_count: int = Field(default=0, ge=0, description="Number of projects")
    internship_months: int = Field(default=0, ge=0, description="Internship months")

    # Job requirements
    min_cgpa: float = Field(default=0.0, ge=0.0, le=10.0, description="Minimum CGPA required")
    required_skills: List[str] = Field(default_factory=list, description="Required skills")
    preferred_skills: List[str] = Field(default_factory=list, description="Preferred skills")
    eligible_departments: List[str] = Field(default_factory=list, description="Eligible departments")

    class Config:
        json_schema_extra = {
            "example": {
                "student_id": 1,
                "student_name": "John Doe",
                "department": "CSE",
                "cgpa": 8.5,
                "skills": ["Python", "Java", "SQL"],
                "certifications": ["AWS Cloud Practitioner"],
                "projects_count": 5,
                "internship_months": 6,
                "min_cgpa": 7.0,
                "required_skills": ["Python", "SQL"],
                "preferred_skills": ["Machine Learning"],
                "eligible_departments": ["CSE", "IT", "ECE"]
            }
        }


class EligibilityResponse(BaseModel):
    """Response schema for eligibility scoring."""

    success: bool = Field(..., description="Whether the calculation was successful")
    score: float = Field(..., ge=0.0, le=100.0, description="Overall eligibility score")
    is_eligible: bool = Field(..., description="Whether the student is eligible")
    reasons: List[str] = Field(default_factory=list, description="Reasons for eligibility decision")

    # Score breakdown
    cgpa_score: Optional[float] = Field(None, description="CGPA component score")
    skills_score: Optional[float] = Field(None, description="Skills match score")
    experience_score: Optional[float] = Field(None, description="Experience score")
    certifications_score: Optional[float] = Field(None, description="Certifications score")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "score": 75.5,
                "is_eligible": True,
                "reasons": ["Student meets all eligibility criteria"],
                "cgpa_score": 85.0,
                "skills_score": 70.0,
                "experience_score": 80.0,
                "certifications_score": 50.0
            }
        }
