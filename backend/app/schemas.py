from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, HttpUrl, field_validator


class RiskLevel(str, Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    UNKNOWN = "unknown"


class RiskEventSummary(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: str = Field(min_length=1, max_length=80)
    source_type: Literal["notification", "call", "manual"]
    occurred_at: datetime
    display_sender: str | None = Field(default=None, max_length=120)
    sender_hash: str | None = Field(default=None, max_length=128)
    redacted_snippet: str = Field(default="", max_length=500)
    urls: list[str] = Field(default_factory=list, max_length=5)
    domains: list[str] = Field(default_factory=list, max_length=5)
    signal_codes: list[str] = Field(default_factory=list, max_length=20)
    risk_score: int = Field(ge=0, le=100)
    risk_level: RiskLevel
    related_campaign_id: str | None = Field(default=None, max_length=80)


class WatchlistSummary(BaseModel):
    model_config = ConfigDict(extra="forbid")

    value: str = Field(min_length=1, max_length=250)
    entity_type: Literal["phone", "domain", "template"]
    reason: str = Field(min_length=1, max_length=300)
    source_title: str = Field(min_length=1, max_length=200)
    source_url: HttpUrl
    last_seen: datetime


class AgentQueryRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    locale: Literal["en-AU", "zh-CN"] = "en-AU"
    message: str = Field(min_length=1, max_length=800)
    active_event: RiskEventSummary | None = None
    recent_events: list[RiskEventSummary] = Field(default_factory=list, max_length=10)
    watchlist: list[WatchlistSummary] = Field(default_factory=list, max_length=20)

    @field_validator("message")
    @classmethod
    def message_must_not_be_blank(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("message must not be blank")
        return value


class Citation(BaseModel):
    id: str
    title: str
    publisher: str
    url: HttpUrl


class SuggestedAction(BaseModel):
    code: str
    label: str
    requires_confirmation: bool = True


class AgentQueryResponse(BaseModel):
    answer: str
    risk_level: RiskLevel
    related_event_ids: list[str] = Field(default_factory=list)
    suggested_actions: list[SuggestedAction] = Field(default_factory=list)
    citations: list[Citation] = Field(default_factory=list)
    degraded: bool = False


class HealthResponse(BaseModel):
    status: Literal["ok"] = "ok"
    service: Literal["sagesense-agent"] = "sagesense-agent"
    model: str
    model_configured: bool


class ModelAnswer(BaseModel):
    """Strict model output before safe actions and citations are hydrated."""

    answer: str = Field(min_length=1, max_length=3000)
    risk_level: RiskLevel = RiskLevel.UNKNOWN
    related_event_ids: list[str] = Field(default_factory=list, max_length=10)
    suggested_action_codes: list[str] = Field(default_factory=list, max_length=6)
    citation_ids: list[str] = Field(default_factory=list, max_length=6)
