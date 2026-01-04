"""
Eligibility scoring service.

Uses rule-based scoring with the following weights:
- CGPA: 30%
- Skills match: 40%
- Experience: 20%
- Certifications: 10%

IMPORTANT: This AI module acts strictly as a decision-support system
and does not autonomously make placement decisions.
"""

from typing import List
from schemas.eligibility import EligibilityRequest, EligibilityResponse


class EligibilityService:
    """
    Service for calculating student eligibility scores.

    The scoring algorithm is rule-based and deterministic,
    meaning identical inputs will always produce identical outputs.
    """

    # Scoring weights (must sum to 1.0)
    CGPA_WEIGHT = 0.30
    SKILLS_WEIGHT = 0.40
    EXPERIENCE_WEIGHT = 0.20
    CERTIFICATIONS_WEIGHT = 0.10

    # Thresholds
    ELIGIBILITY_THRESHOLD = 50.0  # Minimum score to be eligible

    def calculate_score(self, request: EligibilityRequest) -> EligibilityResponse:
        """
        Calculate eligibility score for a student against job requirements.

        Args:
            request: EligibilityRequest containing student and job data

        Returns:
            EligibilityResponse with score breakdown and eligibility status
        """
        # TODO: Implement actual scoring logic
        # For now, return placeholder response

        reasons = []

        # Calculate CGPA score (0-100)
        cgpa_score = self._calculate_cgpa_score(
            request.cgpa,
            request.min_cgpa
        )
        if cgpa_score < 50:
            reasons.append(f"CGPA {request.cgpa} is below minimum {request.min_cgpa}")

        # Calculate skills score (0-100)
        skills_score = self._calculate_skills_score(
            request.skills,
            request.required_skills,
            request.preferred_skills
        )
        if skills_score < 50:
            reasons.append("Missing critical required skills")

        # Calculate experience score (0-100)
        experience_score = self._calculate_experience_score(
            request.projects_count,
            request.internship_months
        )

        # Calculate certifications score (0-100)
        certifications_score = self._calculate_certifications_score(
            request.certifications
        )

        # Calculate weighted total score
        total_score = (
            cgpa_score * self.CGPA_WEIGHT +
            skills_score * self.SKILLS_WEIGHT +
            experience_score * self.EXPERIENCE_WEIGHT +
            certifications_score * self.CERTIFICATIONS_WEIGHT
        )

        # Determine eligibility
        is_eligible = total_score >= self.ELIGIBILITY_THRESHOLD

        # Check department eligibility
        if request.eligible_departments and request.department:
            if request.department not in request.eligible_departments:
                is_eligible = False
                reasons.append(f"Department {request.department} is not eligible")

        if not reasons and is_eligible:
            reasons.append("Student meets all eligibility criteria")

        return EligibilityResponse(
            success=True,
            score=round(total_score, 2),
            is_eligible=is_eligible,
            reasons=reasons,
            cgpa_score=round(cgpa_score, 2),
            skills_score=round(skills_score, 2),
            experience_score=round(experience_score, 2),
            certifications_score=round(certifications_score, 2)
        )

    def _calculate_cgpa_score(self, cgpa: float, min_cgpa: float) -> float:
        """Calculate CGPA component score (0-100)."""
        # TODO: Implement actual CGPA scoring logic
        if cgpa < min_cgpa:
            # Penalize if below minimum
            return (cgpa / min_cgpa) * 50 if min_cgpa > 0 else 0
        else:
            # Scale between min and max (10.0)
            return 50 + ((cgpa - min_cgpa) / (10.0 - min_cgpa)) * 50 if min_cgpa < 10 else 100

    def _calculate_skills_score(
        self,
        student_skills: List[str],
        required_skills: List[str],
        preferred_skills: List[str]
    ) -> float:
        """Calculate skills match score (0-100)."""
        # TODO: Implement actual skills matching logic
        if not required_skills:
            return 75.0  # No requirements = decent score

        # Normalize skills for comparison
        student_skills_lower = [s.lower() for s in student_skills]

        # Count required skills match
        required_matches = sum(
            1 for skill in required_skills
            if skill.lower() in student_skills_lower
        )
        required_score = (required_matches / len(required_skills)) * 70

        # Count preferred skills match
        preferred_matches = 0
        if preferred_skills:
            preferred_matches = sum(
                1 for skill in preferred_skills
                if skill.lower() in student_skills_lower
            )
            preferred_score = (preferred_matches / len(preferred_skills)) * 30
        else:
            preferred_score = 30  # Full bonus if no preferred skills specified

        return required_score + preferred_score

    def _calculate_experience_score(
        self,
        projects_count: int,
        internship_months: int
    ) -> float:
        """Calculate experience score (0-100)."""
        # TODO: Implement actual experience scoring logic
        # Projects: Max 50 points (10 points per project, max 5)
        project_score = min(projects_count * 10, 50)

        # Internship: Max 50 points (8 points per month, max ~6 months)
        internship_score = min(internship_months * 8, 50)

        return project_score + internship_score

    def _calculate_certifications_score(
        self,
        certifications: List[str]
    ) -> float:
        """Calculate certifications score (0-100)."""
        # TODO: Implement actual certifications scoring logic
        # 25 points per certification, max 4
        return min(len(certifications) * 25, 100)
