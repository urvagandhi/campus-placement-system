"""
Schemas for skill gap analysis API endpoints.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class SkillGapRequest(BaseModel):
    """Request schema for skill gap analysis."""

    student_id: int = Field(..., description="Student ID")
    current_skills: List[str] = Field(..., description="Student's current skills")
    required_skills: List[str] = Field(default_factory=list, description="Required skills for job")
    preferred_skills: List[str] = Field(default_factory=list, description="Preferred skills for job")
    target_role: Optional[str] = Field(None, description="Target job role")

    class Config:
        json_schema_extra = {
            "example": {
                "student_id": 1,
                "current_skills": ["Python", "Java", "SQL"],
                "required_skills": ["Python", "Machine Learning", "Data Analysis"],
                "preferred_skills": ["Docker", "Kubernetes"],
                "target_role": "Data Scientist"
            }
        }


class SkillGapResponse(BaseModel):
    """Response schema for skill gap analysis."""

    success: bool = Field(..., description="Whether the analysis was successful")
    missing_skills: List[str] = Field(default_factory=list, description="Missing required skills")
    missing_preferred_skills: List[str] = Field(default_factory=list, description="Missing preferred skills")
    recommendations: List[str] = Field(default_factory=list, description="Learning recommendations")
    match_percentage: float = Field(..., ge=0.0, le=100.0, description="Skill match percentage")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "missing_skills": ["Machine Learning", "Data Analysis"],
                "missing_preferred_skills": ["Docker", "Kubernetes"],
                "recommendations": [
                    "Priority: Learn these required skills - Machine Learning, Data Analysis",
                    "Complete Andrew Ng's ML course on Coursera"
                ],
                "match_percentage": 33.33
            }
        }
