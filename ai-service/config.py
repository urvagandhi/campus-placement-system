"""
Configuration module for PlacementPro AI Service.

Centralizes all environment variable loading and configuration management.
Uses pydantic-settings for type-safe configuration with validation.
"""

import os
from functools import lru_cache
from typing import Optional

from dotenv import load_dotenv

# Load .env file at module import
load_dotenv()


class Settings:
    """Application settings loaded from environment variables."""

    # Server Configuration
    host: str = os.getenv("HOST", "0.0.0.0")
    port: int = int(os.getenv("PORT", "8000"))
    debug: bool = os.getenv("DEBUG", "false").lower() == "true"
    log_level: str = os.getenv("LOG_LEVEL", "INFO")

    # CORS Configuration
    allowed_origins: list[str] = os.getenv(
        "ALLOWED_ORIGINS",
        "http://localhost:8080,http://localhost:3000"
    ).split(",")

    # Google Gemini API Configuration
    gemini_api_key: Optional[str] = os.getenv("GEMINI_API_KEY")
    gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
    gemini_base_url: str = os.getenv(
        "GEMINI_BASE_URL",
        "https://generativelanguage.googleapis.com/v1beta/models"
    )

    # Feature Flags
    use_gemini_enhancement: bool = os.getenv("USE_GEMINI_ENHANCEMENT", "false").lower() == "true"

    # Rate Limiting
    rate_limit_requests: int = int(os.getenv("RATE_LIMIT_REQUESTS", "100"))
    rate_limit_period_seconds: int = int(os.getenv("RATE_LIMIT_PERIOD_SECONDS", "60"))

    @property
    def gemini_enabled(self) -> bool:
        """Check if Gemini API is properly configured."""
        return bool(self.gemini_api_key) and self.use_gemini_enhancement

    @property
    def gemini_endpoint(self) -> str:
        """Get the full Gemini API endpoint URL."""
        return f"{self.gemini_base_url}/{self.gemini_model}:generateContent"


@lru_cache()
def get_settings() -> Settings:
    """
    Get cached application settings.

    Uses lru_cache to ensure settings are only loaded once.
    """
    return Settings()


# Export settings instance for convenience
settings = get_settings()
