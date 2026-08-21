from __future__ import annotations

from httpx import ASGITransport, AsyncClient

from backend.app import main
from backend.app.agent import AgentService
from backend.app.guardrails import InMemoryRateLimiter


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
    assert response.headers["cache-control"] == "no-store"
    assert response.headers["pragma"] == "no-cache"
    assert response.headers["referrer-policy"] == "no-referrer"
    assert response.headers["x-content-type-options"] == "nosniff"
    assert response.headers["x-frame-options"] == "DENY"
    assert response.headers["permissions-policy"] == "camera=(), microphone=(), geolocation=()"


def test_opencode_key_precedes_legacy_deepseek_key(monkeypatch) -> None:
    monkeypatch.setenv("OPENCODE_API_KEY", "opencode-test-key")
    monkeypatch.setenv("DEEPSEEK_API_KEY", "legacy-test-key")

    service = AgentService()

    assert service.api_key == "opencode-test-key"
    assert service.base_url == "https://opencode.ai/zen/go/v1"
    assert service.model == "deepseek-v4-flash"


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


async def test_validation_error_does_not_echo_sensitive_input() -> None:
    sensitive_marker = "private-otp-481516"
    async with AsyncClient(transport=ASGITransport(app=main.app), base_url="http://test") as client:
        response = await client.post(
            "/v1/agent/query",
            json={
                "locale": "invalid-locale",
                "message": sensitive_marker,
                "recent_events": [],
                "watchlist": [],
            },
        )

    assert response.status_code == 422
    assert response.json() == {"detail": "Request validation failed."}
    assert sensitive_marker not in response.text
    assert response.headers["cache-control"] == "no-store"


async def test_agent_endpoint_resanitizes_untrusted_client_payload(monkeypatch) -> None:
    class CapturingAgentService(AgentService):
        received = None

        async def answer(self, request):
            self.received = request
            return self.deterministic_response(request)

    service = CapturingAgentService(api_key="")
    monkeypatch.setattr(main, "agent_service", service)
    monkeypatch.setattr(main, "rate_limiter", InMemoryRateLimiter(max_requests=8))
    event = {
        "id": "event-privacy",
        "source_type": "notification",
        "occurred_at": "2026-08-20T10:30:00Z",
        "display_sender": "Alice +61 400 000 999",
        "sender_hash": "private-sender-hash",
        "redacted_snippet": "OTP: 481516 at https://evil.example/path?token=private",
        "urls": ["https://evil.example/path?token=private"],
        "domains": ["evil.example"],
        "signal_codes": ["OTP_REQUEST", "SUSPICIOUS_URL"],
        "risk_score": 85,
        "risk_level": "high",
    }
    async with AsyncClient(transport=ASGITransport(app=main.app), base_url="http://test") as client:
        response = await client.post(
            "/v1/agent/query",
            json={
                "locale": "en-AU",
                "message": "Is OTP 481516 from alice@example.com a scam?",
                "active_event": event,
                "recent_events": [event],
                "watchlist": [],
            },
        )

    assert response.status_code == 200
    assert service.received is not None
    assert "481516" not in service.received.message
    assert "alice@example.com" not in service.received.message
    assert service.received.active_event.display_sender is None
    assert service.received.active_event.sender_hash is None
    assert service.received.active_event.urls == []
    assert "private" not in service.received.active_event.redacted_snippet
