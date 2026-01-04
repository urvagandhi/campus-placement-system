"""
Job requirements model for AI service.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class JobRequirements(BaseModel):
    """
    Pydantic model representing job requirements for eligibility matching.

    Attributes:
        min_cgpa: Minimum CGPA requirement
        required_skills: List of must-have skills
        preferred_skills: List of nice-to-have skills
        eligible_departments: Departments that can apply
    """

    min_cgpa: float = Field(default=0.0, ge=0.0, le=10.0, description="Minimum CGPA")
    required_skills: List[str] = Field(default_factory=list, description="Required skills")
    preferred_skills: List[str] = Field(default_factory=list, description="Preferred skills")
    eligible_departments: List[str] = Field(default_factory=list, description="Eligible departments")

    class Config:
        json_schema_extra = {
            "example": {
                "min_cgpa": 7.0,
                "required_skills": ["Python", "SQL", "Data Analysis"],
                "preferred_skills": ["Machine Learning", "Tableau"],
                "eligible_departments": ["CSE", "IT", "ECE"]
            }
        }
