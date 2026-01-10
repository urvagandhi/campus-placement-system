"""
Resume parsing service.

Extracts structured data from resume text:
- Skills extraction using pattern matching
- Experience level detection
- Education signal parsing
- Project keyword extraction

IMPORTANT: This service is "dumb" - it does NOT know about:
- Roles
- Scope
- College/tenant
- Eligibility rules

All context-aware filtering happens in the Java backend.
"""

import re
from typing import List, Optional
from pydantic import BaseModel, Field


class ResumeParseRequest(BaseModel):
    """Request schema for resume parsing."""
    student_id: int = Field(..., description="Student ID for reference")
    resume_text: str = Field(..., description="Raw text content from resume")


class AIExplanation(BaseModel):
    """Standardized explanation for AI outputs."""
    factors: List[str] = Field(default_factory=list)
    breakdown: dict = Field(default_factory=dict)
    human_readable: str = Field(default="")


class ResumeParseResponse(BaseModel):
    """Response schema for resume parsing."""
    success: bool = Field(default=True)
    student_id: int
    skills: List[str] = Field(default_factory=list)
    experience_level: str = Field(default="FRESHER")
    confidence_score: float = Field(default=0.0)
    project_keywords: List[str] = Field(default_factory=list)
    education_signals: List[str] = Field(default_factory=list)
    explanation: AIExplanation


class ResumeParserService:
    """
    Service for parsing resumes and extracting structured data.

    Uses pattern matching and keyword extraction.
    Stateless and context-agnostic by design.
    """

    # Common technical skills to detect
    KNOWN_SKILLS = {
        # Programming Languages
        "python", "java", "javascript", "typescript", "c++", "c#", "go", "rust",
        "ruby", "php", "swift", "kotlin", "scala", "r", "matlab",

        # Web Technologies
        "html", "css", "react", "angular", "vue", "node.js", "express",
        "django", "flask", "spring", "spring boot", "asp.net", "rails",

        # Databases
        "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
        "oracle", "sqlite", "cassandra", "dynamodb",

        # Cloud & DevOps
        "aws", "azure", "gcp", "docker", "kubernetes", "terraform",
        "jenkins", "ci/cd", "git", "linux", "nginx", "apache",

        # Data & ML
        "machine learning", "deep learning", "tensorflow", "pytorch",
        "pandas", "numpy", "scikit-learn", "data analysis", "big data",
        "hadoop", "spark", "tableau", "power bi",

        # Other
        "api", "rest", "graphql", "microservices", "agile", "scrum",
        "jira", "figma", "photoshop", "ux", "ui"
    }

    # Experience keywords for level detection
    EXPERIENCE_KEYWORDS = {
        "senior": 4, "lead": 4, "principal": 5, "architect": 5,
        "manager": 4, "director": 5, "years experience": 3,
        "intern": 1, "fresher": 1, "entry level": 1, "junior": 2,
        "mid-level": 3, "experienced": 3
    }

    # Education patterns
    EDUCATION_PATTERNS = [
        r"b\.?tech", r"b\.?e\.?", r"bachelor", r"master", r"m\.?tech",
        r"m\.?s\.?", r"ph\.?d", r"mba", r"bca", r"mca", r"bsc", r"msc"
    ]

    INSTITUTION_PATTERNS = [
        r"iit\s+\w+", r"nit\s+\w+", r"iiit\s+\w+", r"bits\s+\w+",
        r"university", r"college", r"institute"
    ]

    def parse_resume(self, request: ResumeParseRequest) -> ResumeParseResponse:
        """
        Parse resume text and extract structured data.

        Args:
            request: Resume parse request with text content

        Returns:
            Structured resume data with explanation
        """
        text = request.resume_text.lower()

        # Extract skills
        skills = self._extract_skills(text)

        # Detect experience level
        experience_level, exp_score = self._detect_experience_level(text)

        # Extract education signals
        education_signals = self._extract_education(request.resume_text)

        # Extract project keywords
        project_keywords = self._extract_project_keywords(text)

        # Calculate confidence score
        confidence_score = self._calculate_confidence(
            len(skills), len(education_signals), len(project_keywords)
        )

        # Build explanation
        explanation = self._build_explanation(
            skills, experience_level, education_signals, confidence_score
        )

        return ResumeParseResponse(
            success=True,
            student_id=request.student_id,
            skills=skills,
            experience_level=experience_level,
            confidence_score=confidence_score,
            project_keywords=project_keywords,
            education_signals=education_signals,
            explanation=explanation
        )

    def _extract_skills(self, text: str) -> List[str]:
        """Extract technical skills from text."""
        found_skills = []

        for skill in self.KNOWN_SKILLS:
            # Use word boundary matching
            pattern = r'\b' + re.escape(skill) + r'\b'
            if re.search(pattern, text, re.IGNORECASE):
                # Normalize skill name
                normalized = skill.title() if len(skill) > 3 else skill.upper()
                if normalized not in found_skills:
                    found_skills.append(normalized)

        return found_skills

    def _detect_experience_level(self, text: str) -> tuple:
        """Detect experience level from keywords."""
        max_score = 0
        detected_level = "FRESHER"

        for keyword, score in self.EXPERIENCE_KEYWORDS.items():
            if keyword in text:
                if score > max_score:
                    max_score = score

        # Also check for year mentions
        year_match = re.search(r'(\d+)\+?\s*years?\s*(of)?\s*experience', text)
        if year_match:
            years = int(year_match.group(1))
            if years >= 5:
                max_score = max(max_score, 4)
            elif years >= 3:
                max_score = max(max_score, 3)
            elif years >= 1:
                max_score = max(max_score, 2)

        # Map score to level
        if max_score >= 4:
            detected_level = "ADVANCED"
        elif max_score >= 3:
            detected_level = "INTERMEDIATE"
        elif max_score >= 2:
            detected_level = "BEGINNER"
        else:
            detected_level = "FRESHER"

        return detected_level, max_score

    def _extract_education(self, text: str) -> List[str]:
        """Extract education signals from text."""
        signals = []

        # Check degree patterns
        for pattern in self.EDUCATION_PATTERNS:
            matches = re.findall(pattern, text, re.IGNORECASE)
            for match in matches:
                normalized = match.upper().replace(".", "")
                if normalized not in signals:
                    signals.append(normalized)

        # Check for institutions
        for pattern in self.INSTITUTION_PATTERNS:
            matches = re.findall(pattern, text, re.IGNORECASE)
            for match in matches:
                normalized = match.title()
                if normalized not in signals and len(normalized) > 4:
                    signals.append(normalized)

        return signals[:5]  # Limit to 5 signals

    def _extract_project_keywords(self, text: str) -> List[str]:
        """Extract project-related keywords."""
        keywords = []

        # Look for project sections
        project_patterns = [
            r'project[s]?\s*[:\-]?\s*([^.]+)',
            r'built\s+([^.]+)',
            r'developed\s+([^.]+)',
            r'created\s+([^.]+)'
        ]

        for pattern in project_patterns:
            matches = re.findall(pattern, text, re.IGNORECASE)
            for match in matches:
                # Extract significant words
                words = re.findall(r'\b[a-z]{4,}\b', match.lower())
                for word in words[:3]:  # Limit per match
                    if word not in keywords and word not in ["with", "using", "that", "this"]:
                        keywords.append(word.title())

        return keywords[:10]  # Limit to 10 keywords

    def _calculate_confidence(self, skills_count: int, education_count: int, projects_count: int) -> float:
        """Calculate confidence score based on extracted data."""
        score = 0.0

        # Skills contribute up to 50%
        score += min(skills_count * 0.1, 0.5)

        # Education contributes up to 30%
        score += min(education_count * 0.15, 0.3)

        # Projects contribute up to 20%
        score += min(projects_count * 0.05, 0.2)

        return round(score, 2)

    def _build_explanation(
        self,
        skills: List[str],
        experience_level: str,
        education_signals: List[str],
        confidence_score: float
    ) -> AIExplanation:
        """Build human-readable explanation."""
        factors = []
        breakdown = {}

        if skills:
            factors.append(f"Found {len(skills)} technical skills")
            breakdown["skills_count"] = len(skills)
        else:
            factors.append("No recognized technical skills found")

        factors.append(f"Experience level: {experience_level}")
        breakdown["experience_level"] = experience_level

        if education_signals:
            factors.append(f"Education signals: {', '.join(education_signals[:3])}")
            breakdown["education_count"] = len(education_signals)

        breakdown["confidence"] = confidence_score

        # Generate human-readable summary
        if confidence_score >= 0.7:
            human_readable = f"Strong profile with {len(skills)} skills at {experience_level} level."
        elif confidence_score >= 0.4:
            human_readable = f"Good profile with {len(skills)} skills detected."
        else:
            human_readable = "Limited information extracted. Consider adding more details to resume."

        return AIExplanation(
            factors=factors,
            breakdown=breakdown,
            human_readable=human_readable
        )


# Singleton instance
resume_parser = ResumeParserService()
