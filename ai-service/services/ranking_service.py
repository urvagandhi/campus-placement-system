"""
Ranking and similarity service.

Provides similarity-based ranking of students against drive requirements.

IMPORTANT: This service is "dumb" - it does NOT know about:
- Roles
- Scope
- College/tenant
- Eligibility rules

All context-aware filtering happens in the Java backend.
This service only receives pre-filtered, sanitized data.
"""

from typing import List, Optional
from pydantic import BaseModel, Field


class RankingRequest(BaseModel):
    """Request schema for ranking."""
    drive_id: int = Field(..., description="Drive ID for context")
    student_ids: List[int] = Field(..., description="Student IDs to rank")
    required_skills: List[str] = Field(default_factory=list)
    preferred_skills: List[str] = Field(default_factory=list)
    job_role: Optional[str] = None

    # Student profiles (sent by backend)
    student_profiles: List[dict] = Field(default_factory=list)


class AIExplanation(BaseModel):
    """Standardized explanation for AI outputs."""
    factors: List[str] = Field(default_factory=list)
    breakdown: dict = Field(default_factory=dict)
    human_readable: str = Field(default="")


class StudentRanking(BaseModel):
    """Individual student ranking."""
    student_id: int
    similarity_score: float
    rank: int
    matched_skills: List[str] = Field(default_factory=list)
    missing_skills: List[str] = Field(default_factory=list)
    reason: str


class RankingResponse(BaseModel):
    """Response schema for ranking."""
    success: bool = Field(default=True)
    rankings: List[StudentRanking] = Field(default_factory=list)
    explanation: AIExplanation


class RankingService:
    """
    Service for ranking students based on similarity to requirements.

    Uses skill matching, experience scoring, and other factors.
    Provides RANKING only, not decisions.
    """

    def rank_students(self, request: RankingRequest) -> RankingResponse:
        """
        Rank students by similarity to drive requirements.

        Args:
            request: Ranking request with student profiles

        Returns:
            Ranked list with similarity scores
        """
        if not request.student_profiles:
            return RankingResponse(
                success=True,
                rankings=[],
                explanation=AIExplanation(
                    factors=["No student profiles provided"],
                    breakdown={},
                    human_readable="No students to rank."
                )
            )

        rankings = []

        for profile in request.student_profiles:
            student_id = profile.get("student_id", 0)
            student_skills = [s.lower() for s in profile.get("skills", [])]

            # Calculate similarity
            result = self._calculate_similarity(
                student_skills,
                [s.lower() for s in request.required_skills],
                [s.lower() for s in request.preferred_skills]
            )

            rankings.append(StudentRanking(
                student_id=student_id,
                similarity_score=result["score"],
                rank=0,  # Will be set after sorting
                matched_skills=result["matched"],
                missing_skills=result["missing"],
                reason=result["reason"]
            ))

        # Sort by score descending and assign ranks
        rankings.sort(key=lambda x: x.similarity_score, reverse=True)
        for i, ranking in enumerate(rankings, 1):
            ranking.rank = i

        # Build overall explanation
        explanation = self._build_explanation(rankings, request)

        return RankingResponse(
            success=True,
            rankings=rankings,
            explanation=explanation
        )

    def _calculate_similarity(
        self,
        student_skills: List[str],
        required_skills: List[str],
        preferred_skills: List[str]
    ) -> dict:
        """Calculate similarity score between student and requirements."""
        matched = []
        missing = []

        # Required skills (70% weight)
        required_matched = 0
        for skill in required_skills:
            if self._skill_matches(skill, student_skills):
                matched.append(skill.title())
                required_matched += 1
            else:
                missing.append(skill.title())

        required_score = 0.0
        if required_skills:
            required_score = (required_matched / len(required_skills)) * 70
        else:
            required_score = 70  # No requirements = full score

        # Preferred skills (30% weight)
        preferred_matched = 0
        for skill in preferred_skills:
            if self._skill_matches(skill, student_skills):
                if skill.title() not in matched:
                    matched.append(skill.title())
                preferred_matched += 1

        preferred_score = 0.0
        if preferred_skills:
            preferred_score = (preferred_matched / len(preferred_skills)) * 30
        else:
            preferred_score = 30  # No preferences = full bonus

        total_score = round(required_score + preferred_score, 1)

        # Generate reason
        if total_score >= 80:
            reason = "Excellent match with strong skill alignment"
        elif total_score >= 60:
            reason = "Good match with most required skills"
        elif total_score >= 40:
            reason = "Partial match, some skill gaps"
        else:
            reason = "Limited match, significant skill gaps"

        return {
            "score": total_score,
            "matched": matched,
            "missing": missing,
            "reason": reason
        }

    def _skill_matches(self, skill: str, student_skills: List[str]) -> bool:
        """Check if student has a matching skill (with fuzzy matching)."""
        skill_lower = skill.lower()

        for student_skill in student_skills:
            # Exact match
            if skill_lower == student_skill:
                return True

            # Containment match
            if skill_lower in student_skill or student_skill in skill_lower:
                return True

            # Common alternatives
            alternatives = {
                "javascript": ["js", "ecmascript"],
                "typescript": ["ts"],
                "python": ["py"],
                "machine learning": ["ml"],
                "artificial intelligence": ["ai"],
                "postgresql": ["postgres"],
                "mongodb": ["mongo"],
                "amazon web services": ["aws"],
                "microsoft azure": ["azure"],
                "google cloud platform": ["gcp"],
                "kubernetes": ["k8s"],
                "continuous integration": ["ci/cd", "cicd"]
            }

            if skill_lower in alternatives:
                for alt in alternatives[skill_lower]:
                    if alt in student_skill:
                        return True

        return False

    def _build_explanation(self, rankings: List[StudentRanking], request: RankingRequest) -> AIExplanation:
        """Build overall explanation for the ranking."""
        factors = []
        breakdown = {}

        factors.append(f"Ranked {len(rankings)} students")

        if request.required_skills:
            factors.append(f"Required skills: {', '.join(request.required_skills[:5])}")
            breakdown["required_skills_count"] = len(request.required_skills)

        if request.preferred_skills:
            factors.append(f"Preferred skills: {', '.join(request.preferred_skills[:3])}")
            breakdown["preferred_skills_count"] = len(request.preferred_skills)

        # Calculate average score
        if rankings:
            avg_score = sum(r.similarity_score for r in rankings) / len(rankings)
            breakdown["average_score"] = round(avg_score, 1)

            high_matches = len([r for r in rankings if r.similarity_score >= 70])
            breakdown["high_match_count"] = high_matches

        human_readable = f"Ranked {len(rankings)} students based on skill similarity. "
        if rankings and rankings[0].similarity_score >= 70:
            human_readable += "Strong candidates available."
        elif rankings and rankings[0].similarity_score >= 50:
            human_readable += "Moderate matches found."
        else:
            human_readable += "Consider broadening requirements."

        return AIExplanation(
            factors=factors,
            breakdown=breakdown,
            human_readable=human_readable
        )


# Singleton instance
ranking_service = RankingService()
