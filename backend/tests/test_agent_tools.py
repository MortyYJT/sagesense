from __future__ import annotations

from backend.app.knowledge import KnowledgeRepository
from backend.app.schemas import AgentQueryRequest
from backend.app.tools import AgentTools, default_action_codes


def make_request() -> AgentQueryRequest:
    return AgentQueryRequest.model_validate(
        {
            "locale": "zh-CN",
            "message": "为什么这像银行诈骗？",
            "active_event": {
                "id": "new",
                "source_type": "notification",
                "occurred_at": "2026-08-20T10:30:00Z",
                "redacted_snippet": "立即验证账户",
                "domains": ["secure-bank.example"],
                "signal_codes": ["URGENCY", "CREDENTIAL_REQUEST", "SUSPICIOUS_URL"],
                "risk_score": 82,
                "risk_level": "high",
                "related_campaign_id": "campaign-a",
            },
            "recent_events": [
                {
                    "id": "old-campaign",
                    "source_type": "notification",
                    "occurred_at": "2026-08-19T10:30:00Z",
                    "redacted_snippet": "账户将被停用",
                    "domains": ["other-domain.example"],
                    "signal_codes": ["URGENCY", "CREDENTIAL_REQUEST"],
                    "risk_score": 75,
                    "risk_level": "high",
                    "related_campaign_id": "campaign-a",
                },
                {
                    "id": "benign",
                    "source_type": "notification",
                    "occurred_at": "2026-08-18T10:30:00Z",
                    "redacted_snippet": "Your parcel arrived",
                    "signal_codes": [],
                    "risk_score": 5,
                    "risk_level": "low",
                },
            ],
            "watchlist": [
                {
                    "value": "+61 400 000 999",
                    "entity_type": "phone",
                    "reason": "Seeded demo number",
                    "source_title": "SageSense demo fixture",
                    "source_url": "https://example.com/demo",
                    "last_seen": "2026-08-20T08:00:00Z",
                }
            ],
        }
    )


def test_personal_scam_memory_relates_campaigns() -> None:
    tools = AgentTools(make_request(), KnowledgeRepository())
    result = tools.compare_recent_events()

    assert result["related_event_ids"] == ["old-campaign"]
    assert result["matches"][0]["same_campaign"] is True


def test_watchlist_normalises_phone_numbers() -> None:
    tools = AgentTools(make_request(), KnowledgeRepository())

    assert tools.lookup_watchlist_entity("+61400000999")["reason"] == "Seeded demo number"


def test_high_risk_link_actions_are_safe_and_confirmed() -> None:
    codes = default_action_codes(
        make_request().active_event.risk_level,
        make_request().active_event.signal_codes,
    )

    assert "DO_NOT_CLICK" in codes
    assert "CONTACT_BANK" in codes
    assert "VERIFY_OFFICIAL_CHANNEL" in codes


def test_knowledge_search_returns_source_ids() -> None:
    results = KnowledgeRepository().search("urgent bank phishing link", "en-AU")

    assert results
    assert all(result["id"] and result["url"].startswith("https://") for result in results)
