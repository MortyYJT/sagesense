from __future__ import annotations

from httpx import ASGITransport, AsyncClient

from backend.app import main
from backend.app.agent import AgentService


async def test_health_reports_model_without_exposing_secrets(monkeypatch) -> None:
    monkeypatch.setattr(main, "agent_service", AgentService(api_key=""))
    async with AsyncClient(transport=ASGITransport(app=main.app), base_url="http://test") as client:
        response = await client.get("/v1/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "ok",
        "service": "sagesense-agent",
        "model": "deepseek-v4-flash",
        "model_configured": False,
    }


async def test_agent_endpoint_has_deterministic_offline_fallback(monkeypatch) -> None:
    monkeypatch.setattr(main, "agent_service", AgentService(api_key=""))
    async with AsyncClient(transport=ASGITransport(app=main.app), base_url="http://test") as client:
        response = await client.post(
            "/v1/agent/query",
            json={
                "locale": "en-AU",
                "message": "Why is this bank message unsafe?",
                "active_event": {
                    "id": "event-1",
                    "source_type": "notification",
                    "occurred_at": "2026-08-20T10:30:00Z",
                    "display_sender": "CommBank Alert",
                    "redacted_snippet": "Verify immediately at https://commbank-secure-login.example",
                    "urls": ["https://commbank-secure-login.example"],
                    "domains": ["commbank-secure-login.example"],
                    "signal_codes": ["URGENCY", "SUSPICIOUS_URL", "CREDENTIAL_REQUEST"],
                    "risk_score": 85,
                    "risk_level": "high",
                },
                "recent_events": [],
                "watchlist": [],
            },
        )

    assert response.status_code == 200
    body = response.json()
    assert body["degraded"] is True
    assert body["risk_level"] == "high"
    assert {item["code"] for item in body["suggested_actions"]} >= {
        "DO_NOT_CLICK",
        "VERIFY_OFFICIAL_CHANNEL",
    }
    assert body["citations"]
    assert all(item["url"].startswith("https://") for item in body["citations"])


async def test_rejects_more_than_ten_recent_events() -> None:
    event = {
        "id": "event",
        "source_type": "manual",
        "occurred_at": "2026-08-20T10:30:00Z",
        "redacted_snippet": "hello",
        "risk_score": 10,
        "risk_level": "low",
    }
    async with AsyncClient(transport=ASGITransport(app=main.app), base_url="http://test") as client:
        response = await client.post(
            "/v1/agent/query",
            json={
                "locale": "en-AU",
                "message": "Check these",
                "recent_events": [{**event, "id": f"event-{index}"} for index in range(11)],
                "watchlist": [],
            },
        )

    assert response.status_code == 422
