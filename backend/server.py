"""Vercel entrypoint for the SageSense FastAPI application."""

from backend.app.main import app

__all__ = ["app"]
