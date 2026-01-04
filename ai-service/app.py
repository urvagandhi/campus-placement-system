"""
AI-Assisted Campus Placement System - AI Service

FastAPI application providing AI-powered decision support for:
- Eligibility scoring
- Skill gap analysis
- Career insights (TODO)

IMPORTANT: The AI module acts strictly as a decision-support system
and does not autonomously make placement decisions.

Technology Stack:
- Python 3.10+
- FastAPI
- Pydantic for data validation
- Uvicorn for ASGI server

API Versioning: All endpoints use /api/v1 prefix for consistency.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import uvicorn

from services.eligibility_service import EligibilityService
from services.skill_gap_service import SkillGapService
from schemas.eligibility import EligibilityRequest, EligibilityResponse
from schemas.skill_gap import SkillGapRequest, SkillGapResponse

# Initialize FastAPI app
app = FastAPI(
    title="PlacementPro AI Service",
    description="""
    AI-powered decision support for PlacementPro - The Smart Campus Placement System.

    **Note:** This AI module acts strictly as a decision-support system
    and does not autonomously make placement decisions.

    ## Features
    - Eligibility scoring based on CGPA, skills, and experience
    - Skill gap analysis with recommendations
    - Career insights (coming soon)
    """,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8080",  # Java backend
        "http://localhost:3000",  # Next.js frontend (for direct calls if needed)
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize services
eligibility_service = EligibilityService()
skill_gap_service = SkillGapService()


# ==================== Health Check ====================

@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint for monitoring."""
    return {"status": "healthy", "service": "ai-service"}


# ==================== Eligibility Endpoints ====================

@app.post("/api/v1/eligibility/score",
          response_model=EligibilityResponse,
          tags=["Eligibility"])
async def calculate_eligibility_score(request: EligibilityRequest):
    """
    Calculate eligibility score for a student against job requirements.

    Uses rule-based scoring:
    - CGPA weight: 30%
    - Skills match: 40%
    - Experience: 20%
    - Certifications: 10%
    """
    try:
        result = eligibility_service.calculate_score(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== Skill Gap Endpoints ====================

@app.post("/api/v1/skills/gap-analysis",
          response_model=SkillGapResponse,
          tags=["Skills"])
async def analyze_skill_gaps(request: SkillGapRequest):
    """
    Analyze skill gaps between student skills and job requirements.
    Returns missing skills and recommendations.
    """
    try:
        result = skill_gap_service.analyze_gaps(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== Career Insights Endpoints ====================

@app.post("/api/v1/career/insights", tags=["Career"])
async def get_career_insights(student_id: int):
    """
    Get career insights and recommendations for a student.

    TODO: Implement career insights logic
    """
    # TODO: Implement career insights
    raise HTTPException(
        status_code=501,
        detail="Career insights not implemented yet"
    )


# ==================== Main Entry Point ====================

if __name__ == "__main__":
    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )
