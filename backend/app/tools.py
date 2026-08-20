from __future__ import annotations

import json
import re
from dataclasses import dataclass
from typing import Any, Callable

from backend.app.knowledge import KnowledgeRepository
from backend.app.schemas import AgentQueryRequest, RiskEventSummary, RiskLevel, SuggestedAction


ACTION_LABELS: dict[str, dict[str, str]] = {
    "DO_NOT_CLICK": {"en-AU": "Do not open the link", "zh-CN": "不要打开这个链接"},
    "VERIFY_OFFICIAL_CHANNEL": {
        "en-AU": "Verify using the organisation's official app or website",
        "zh-CN": "通过机构的官方应用或网站核实",
    },
    "CALL_OFFICIAL_NUMBER": {
        "en-AU": "Call using a number from the official website",
        "zh-CN": "使用官方网站上的号码回拨核实",
    },
    "DO_NOT_SHARE_CODE": {"en-AU": "Do not share passwords or verification codes", "zh-CN": "不要分享密码或验证码"},
    "CONTACT_BANK": {"en-AU": "Contact your bank immediately if money was sent", "zh-CN": "如果已经转账，请立即联系银行"},
    "REPORT_SCAM": {"en-AU": "Report the incident to Scamwatch", "zh-CN": "向 Scamwatch 报告事件"},
    "TALK_TO_TRUSTED_PERSON": {"en-AU": "Pause and talk to someone you trust", "zh-CN": "先停下来，与信任的人商量"},
}


TOOL_SPECS: list[dict[str, Any]] = [
    {
        "type": "function",
        "function": {
            "name": "search_scam_knowledge",
            "description": "Search curated official scam-safety summaries and return source IDs.",
            "parameters": {
                "type": "object",
                "properties": {"query": {"type": "string"}},
                "required": ["query"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_event_context",
            "description": "Read one sanitised risk event supplied by the user.",
            "parameters": {
                "type": "object",
                "properties": {"event_id": {"type": "string"}},
                "required": ["event_id"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "compare_recent_events",
            "description": "Find recent events with shared domains, signal patterns, or campaign IDs.",
            "parameters": {"type": "object", "properties": {}, "additionalProperties": False},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "lookup_watchlist_entity",
            "description": "Look up a phone number or domain in the user-provided watchlist.",
            "parameters": {
                "type": "object",
                "properties": {"value": {"type": "string"}},
                "required": ["value"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_safe_actions",
            "description": "Return safe action codes for a risk level and evidence signals.",
            "parameters": {
                "type": "object",
                "properties": {
                    "risk_level": {"type": "string", "enum": ["low", "medium", "high", "unknown"]},
                    "signal_codes": {"type": "array", "items": {"type": "string"}},
                },
                "required": ["risk_level", "signal_codes"],
                "additionalProperties": False,
            },
        },
    },
]


def safe_actions(codes: list[str], locale: str) -> list[SuggestedAction]:
    unique: list[SuggestedAction] = []
    seen: set[str] = set()
    for code in codes:
        if code in seen or code not in ACTION_LABELS:
            continue
        seen.add(code)
        unique.append(SuggestedAction(code=code, label=ACTION_LABELS[code][locale]))
    return unique


def default_action_codes(risk_level: RiskLevel, signals: list[str]) -> list[str]:
    codes = ["VERIFY_OFFICIAL_CHANNEL", "TALK_TO_TRUSTED_PERSON"]
    signal_set = set(signals)
    if signal_set & {"SUSPICIOUS_URL", "CREDENTIAL_REQUEST", "MISSPELLED_DOMAIN"}:
        codes.insert(0, "DO_NOT_CLICK")
    if signal_set & {"OTP_REQUEST", "CREDENTIAL_REQUEST"}:
        codes.append("DO_NOT_SHARE_CODE")
    if risk_level == RiskLevel.HIGH:
        codes.extend(["CONTACT_BANK", "REPORT_SCAM"])
    return list(dict.fromkeys(codes))[:5]


@dataclass
class AgentTools:
    request: AgentQueryRequest
    knowledge: KnowledgeRepository

    @staticmethod
    def _normalise(value: str) -> str:
        return re.sub(r"[^a-z0-9+]", "", value.lower())

    @staticmethod
    def _event_dict(event: RiskEventSummary) -> dict[str, Any]:
        return event.model_dump(mode="json")

    def search_scam_knowledge(self, query: str) -> Any:
        return self.knowledge.search(query=query, locale=self.request.locale)

    def get_event_context(self, event_id: str) -> Any:
        events = ([self.request.active_event] if self.request.active_event else []) + self.request.recent_events
        for event in events:
            if event and event.id == event_id:
                return self._event_dict(event)
        return {"error": "event_not_found"}

    def compare_recent_events(self) -> Any:
        active = self.request.active_event
        if active is None:
            return {"related_event_ids": [], "reason": "no_active_event"}
        active_domains = {self._normalise(item) for item in active.domains}
        active_signals = set(active.signal_codes)
        matches: list[dict[str, Any]] = []
        for event in self.request.recent_events:
            if event.id == active.id:
                continue
            domain_overlap = active_domains & {self._normalise(item) for item in event.domains}
            signal_overlap = active_signals & set(event.signal_codes)
            same_campaign = bool(active.related_campaign_id and active.related_campaign_id == event.related_campaign_id)
            if same_campaign or domain_overlap or len(signal_overlap) >= 2:
                matches.append(
                    {
                        "id": event.id,
                        "same_campaign": same_campaign,
                        "shared_domains": sorted(domain_overlap),
                        "shared_signals": sorted(signal_overlap),
                    }
                )
        return {"related_event_ids": [item["id"] for item in matches], "matches": matches}

    def lookup_watchlist_entity(self, value: str) -> Any:
        needle = self._normalise(value)
        for item in self.request.watchlist:
            if self._normalise(item.value) == needle:
                return item.model_dump(mode="json")
        return {"matched": False}

    def get_safe_actions(self, risk_level: str, signal_codes: list[str]) -> Any:
        level = RiskLevel(risk_level)
        codes = default_action_codes(level, signal_codes)
        return {"actions": [item.model_dump() for item in safe_actions(codes, self.request.locale)]}

    def execute(self, name: str, arguments: str) -> str:
        try:
            payload = json.loads(arguments or "{}")
            handlers: dict[str, Callable[..., Any]] = {
                "search_scam_knowledge": self.search_scam_knowledge,
                "get_event_context": self.get_event_context,
                "compare_recent_events": self.compare_recent_events,
                "lookup_watchlist_entity": self.lookup_watchlist_entity,
                "get_safe_actions": self.get_safe_actions,
            }
            if name not in handlers:
                return json.dumps({"error": "unknown_tool"})
            return json.dumps(handlers[name](**payload), ensure_ascii=False, default=str)
        except (TypeError, ValueError, json.JSONDecodeError) as exc:
            return json.dumps({"error": "invalid_tool_arguments", "detail": str(exc)})
