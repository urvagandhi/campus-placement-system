"""
Skill gap analysis service.

Analyzes the gap between a student's current skills and job requirements,
providing recommendations for skill development.

IMPORTANT: This AI module acts strictly as a decision-support system
and does not autonomously make placement decisions.
"""

from typing import List
from schemas.skill_gap import SkillGapRequest, SkillGapResponse


class SkillGapService:
    """
    Service for analyzing skill gaps and providing recommendations.
    """

    def analyze_gaps(self, request: SkillGapRequest) -> SkillGapResponse:
        """
        Analyze skill gaps between student skills and job requirements.

        Args:
            request: SkillGapRequest with current and required skills

        Returns:
            SkillGapResponse with missing skills and recommendations
        """
        # TODO: Implement actual skill gap analysis logic

        # Normalize skills for comparison
        current_skills_lower = set(s.lower() for s in request.current_skills)

        # Find missing required skills
        missing_skills = [
            skill for skill in request.required_skills
            if skill.lower() not in current_skills_lower
        ]

        # Find missing preferred skills
        missing_preferred = [
            skill for skill in request.preferred_skills
            if skill.lower() not in current_skills_lower
        ]

        # Generate recommendations
        recommendations = self._generate_recommendations(
            missing_skills,
            missing_preferred,
            request.target_role
        )

        # Calculate match percentage
        total_required = len(request.required_skills)
        matched_required = total_required - len(missing_skills)
        match_percentage = (matched_required / total_required * 100) if total_required > 0 else 100

        return SkillGapResponse(
            success=True,
            missing_skills=missing_skills,
            missing_preferred_skills=missing_preferred,
            recommendations=recommendations,
            match_percentage=round(match_percentage, 2)
        )

    def _generate_recommendations(
        self,
        missing_skills: List[str],
        missing_preferred: List[str],
        target_role: str = None
    ) -> List[str]:
        """
        Generate learning recommendations based on skill gaps.

        Args:
            missing_skills: List of missing required skills
            missing_preferred: List of missing preferred skills
            target_role: Target job role (optional)

        Returns:
            List of recommendation strings
        """
        # TODO: Implement actual recommendation logic
        # Could integrate with learning platforms, courses, etc.

        recommendations = []

        if missing_skills:
            recommendations.append(
                f"Priority: Learn these required skills - {', '.join(missing_skills)}"
            )

            # Add specific recommendations for common skills
            skill_recommendations = {
                "python": "Take Python fundamentals course on Coursera or Udemy",
                "java": "Complete Java certification on Oracle Academy",
                "sql": "Practice SQL on LeetCode and HackerRank",
                "machine learning": "Complete Andrew Ng's ML course on Coursera",
                "data analysis": "Learn pandas and numpy through DataCamp",
            }

            for skill in missing_skills:
                skill_lower = skill.lower()
                if skill_lower in skill_recommendations:
                    recommendations.append(skill_recommendations[skill_lower])

        if missing_preferred:
            recommendations.append(
                f"Optional: Consider learning - {', '.join(missing_preferred)}"
            )

        if not missing_skills and not missing_preferred:
            recommendations.append("Great job! Your skills match the requirements.")

        return recommendations

    def get_recommendations(self, skill_gaps: List[str]) -> List[str]:
        """
        Get learning recommendations for skill gaps.

        Args:
            skill_gaps: List of skills to learn

        Returns:
            List of recommendation strings
        """
        # TODO: Implement more sophisticated recommendation engine
        return self._generate_recommendations(skill_gaps, [], None)
