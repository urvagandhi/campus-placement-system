"""
Student model for AI service.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class StudentModel(BaseModel):
    """
    Pydantic model representing a student's profile for AI processing.

    Attributes:
        student_id: Unique identifier for the student
        name: Student's full name
        department: Academic department (e.g., CSE, ECE, ME)
        cgpa: Cumulative Grade Point Average (0.0 - 10.0)
        skills: List of technical and soft skills
        certifications: List of professional certifications
        projects_count: Number of completed projects
        internship_months: Total months of internship experience
    """

    student_id: int = Field(..., description="Unique student identifier")
    name: str = Field(..., description="Student's full name")
    department: str = Field(..., description="Academic department")
    cgpa: float = Field(..., ge=0.0, le=10.0, description="CGPA (0.0-10.0)")
    skills: List[str] = Field(default_factory=list, description="List of skills")
    certifications: List[str] = Field(default_factory=list, description="Certifications")
    projects_count: int = Field(default=0, ge=0, description="Number of projects")
    internship_months: int = Field(default=0, ge=0, description="Internship months")

    class Config:
        json_schema_extra = {
            "example": {
                "student_id": 1,
                "name": "John Doe",
                "department": "CSE",
                "cgpa": 8.5,
                "skills": ["Python", "Java", "SQL", "Machine Learning"],
                "certifications": ["AWS Cloud Practitioner"],
                "projects_count": 5,
                "internship_months": 6
            }
        }
