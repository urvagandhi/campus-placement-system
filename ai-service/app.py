"""
AI-Assisted Campus Placement System - AI Service

FastAPI application providing AI-powered decision support for:
- Resume parsing and skill extraction
- Eligibility scoring
- Student-drive similarity ranking
- Skill gap analysis
- Aggregated insights

IMPORTANT: The AI module acts strictly as a decision-support system
and does not autonomously make placement decisions.

IMPORTANT: This service is kept "dumb" by design - it does NOT know about:
- Roles or permissions
- Organization scope
- College/tenant boundaries
- Eligibility rules

All context-aware filtering happens in the Java backend.

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
from services.resume_parser import resume_parser, ResumeParseRequest, ResumeParseResponse
from services.ranking_service import ranking_service, RankingRequest, RankingResponse
from services.insights_service import insights_service, InsightsRequest, InsightsResponse
from schemas.eligibility import EligibilityRequest, EligibilityResponse
from schemas.skill_gap import SkillGapRequest, SkillGapResponse

# Initialize FastAPI app
app = FastAPI(
    title="PlacementPro AI Service",
    description="""
    AI-powered decision support for PlacementPro - The Smart Campus Placement System.

    **Note:** This AI module acts strictly as a decision-support system
    and does not autonomously make placement decisions.

    **Design Principle:** This service is kept "dumb" - it does NOT know about
    roles, scope, college, or eligibility. All filtering is done by the Java backend.

    ## Features
    - Resume parsing and skill extraction
    - Eligibility scoring based on CGPA, skills, and experience
    - Student-drive similarity ranking
    - Skill gap analysis with recommendations
    - Aggregated analytics insights
    """,
    version="2.0.0",
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
    return {"status": "healthy", "service": "ai-service", "version": "2.0.0"}


# ==================== Resume Parsing Endpoints ====================

@app.post("/api/v1/resume/parse",
          response_model=ResumeParseResponse,
          tags=["Resume Parsing"])
async def parse_resume(request: ResumeParseRequest):
    """
    Parse resume text and extract structured data.

    Extracts:
    - Technical skills (normalized)
    - Experience level (FRESHER, BEGINNER, INTERMEDIATE, ADVANCED)
    - Education signals
    - Project keywords

    **Note:** This endpoint receives sanitized data only.
    It does NOT know about roles, scope, or eligibility.
    """
    try:
        result = resume_parser.parse_resume(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== Ranking Endpoints ====================

@app.post("/api/v1/ranking/rank",
          response_model=RankingResponse,
          tags=["Ranking"])
async def rank_students(request: RankingRequest):
    """
    Rank students by similarity to drive requirements.

    **Important:** This provides RANKING only, not decisions.
    Business eligibility is checked by the Java backend before calling this.

    Scoring weights:
    - Required skills: 70%
    - Preferred skills: 30%
    """
    try:
        result = ranking_service.rank_students(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/v1/ranking/similarity",
          response_model=RankingResponse,
          tags=["Ranking"])
async def get_similarity_scores(request: RankingRequest):
    """
    Calculate similarity scores between students and drive requirements.

    Alias for /api/v1/ranking/rank for semantic clarity.
    """
    try:
        result = ranking_service.rank_students(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


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


# ==================== Insights Endpoints ====================

@app.post("/api/v1/insights/aggregate",
          response_model=InsightsResponse,
          tags=["Insights"])
async def get_aggregated_insights(request: InsightsRequest):
    """
    Generate insights from pre-aggregated data.

    **Note:** This endpoint receives pre-aggregated data only.
    All privacy filtering is done by the Java backend.

    Supported insight types:
    - skill_trends: Skill demand vs supply analysis
    - placement_patterns: Department-wise placement patterns
    - selection_factors: Factors contributing to selection
    """
    try:
        result = insights_service.generate_insights(request)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ==================== Main Entry Point ====================

if __name__ == "__main__":
    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )
