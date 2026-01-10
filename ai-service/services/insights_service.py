"""
Insights service.

Provides aggregated analytics and trend analysis.

IMPORTANT: This service is "dumb" - it does NOT know about:
- Roles
- Scope
- College/tenant
- Access control

All data aggregation and privacy filtering happens in the Java backend.
This service only receives pre-aggregated, sanitized data.
"""

from typing import List, Dict, Optional
from pydantic import BaseModel, Field


class InsightsRequest(BaseModel):
    """Request schema for insights."""
    insight_type: str = Field(..., description="Type of insight: skill_trends, placement_patterns, etc.")
    data: dict = Field(default_factory=dict, description="Pre-aggregated data from backend")


class InsightItem(BaseModel):
    """Individual insight item."""
    category: str
    value: float
    trend: str = Field(default="stable")  # up, down, stable
    recommendation: Optional[str] = None


class InsightsResponse(BaseModel):
    """Response schema for insights."""
    success: bool = Field(default=True)
    insight_type: str
    insights: List[InsightItem] = Field(default_factory=list)
    summary: str


class InsightsService:
    """
    Service for generating insights from aggregated data.

    Analyzes patterns and generates recommendations.
    Operates on pre-aggregated data only.
    """

    def generate_insights(self, request: InsightsRequest) -> InsightsResponse:
        """
        Generate insights based on request type.

        Args:
            request: Insights request with pre-aggregated data

        Returns:
            Generated insights with recommendations
        """
        if request.insight_type == "skill_trends":
            return self._analyze_skill_trends(request.data)
        elif request.insight_type == "placement_patterns":
            return self._analyze_placement_patterns(request.data)
        elif request.insight_type == "selection_factors":
            return self._analyze_selection_factors(request.data)
        else:
            return InsightsResponse(
                success=False,
                insight_type=request.insight_type,
                insights=[],
                summary=f"Unknown insight type: {request.insight_type}"
            )

    def _analyze_skill_trends(self, data: dict) -> InsightsResponse:
        """Analyze skill demand vs supply trends."""
        insights = []

        skill_gaps = data.get("skill_gaps", {})
        for skill, gap_info in skill_gaps.items():
            demand = gap_info.get("demand", 0)
            supply = gap_info.get("supply", 0)

            gap_ratio = (demand - supply) / demand if demand > 0 else 0

            trend = "up" if gap_ratio > 0.3 else ("down" if gap_ratio < -0.1 else "stable")

            recommendation = None
            if gap_ratio > 0.3:
                recommendation = f"High demand for {skill}. Consider training programs."
            elif gap_ratio < -0.1:
                recommendation = f"Good supply of {skill}. Students well-prepared."

            insights.append(InsightItem(
                category=skill,
                value=round(gap_ratio * 100, 1),
                trend=trend,
                recommendation=recommendation
            ))

        # Sort by gap ratio descending
        insights.sort(key=lambda x: x.value, reverse=True)

        summary = f"Analyzed {len(insights)} skill trends. "
        high_gaps = len([i for i in insights if i.value > 30])
        if high_gaps > 0:
            summary += f"{high_gaps} skills have significant gaps."
        else:
            summary += "Skills are well-balanced."

        return InsightsResponse(
            success=True,
            insight_type="skill_trends",
            insights=insights[:10],  # Top 10
            summary=summary
        )

    def _analyze_placement_patterns(self, data: dict) -> InsightsResponse:
        """Analyze placement success patterns."""
        insights = []

        departments = data.get("departments", {})
        for dept, stats in departments.items():
            placement_rate = stats.get("placement_rate", 0)

            trend = "up" if stats.get("trend_direction", 0) > 0 else (
                "down" if stats.get("trend_direction", 0) < 0 else "stable"
            )

            recommendation = None
            if placement_rate < 50:
                recommendation = f"Consider targeted support for {dept} students."
            elif placement_rate > 80:
                recommendation = f"{dept} performing excellently."

            insights.append(InsightItem(
                category=dept,
                value=placement_rate,
                trend=trend,
                recommendation=recommendation
            ))

        insights.sort(key=lambda x: x.value, reverse=True)

        avg_rate = sum(i.value for i in insights) / len(insights) if insights else 0
        summary = f"Average placement rate: {avg_rate:.1f}%."

        return InsightsResponse(
            success=True,
            insight_type="placement_patterns",
            insights=insights,
            summary=summary
        )

    def _analyze_selection_factors(self, data: dict) -> InsightsResponse:
        """Analyze factors contributing to selection."""
        insights = []

        factors = data.get("selection_factors", {})
        for factor, importance in factors.items():
            insights.append(InsightItem(
                category=factor,
                value=importance,
                trend="stable",
                recommendation=None
            ))

        insights.sort(key=lambda x: x.value, reverse=True)

        if insights:
            top_factor = insights[0].category
            summary = f"Top selection factor: {top_factor}."
        else:
            summary = "No selection factors analyzed."

        return InsightsResponse(
            success=True,
            insight_type="selection_factors",
            insights=insights,
            summary=summary
        )


# Singleton instance
insights_service = InsightsService()
