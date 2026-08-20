from types import SimpleNamespace

from backend.app.agent import AgentService
from backend.tests.test_agent_tools import make_request


def test_model_json_can_be_recovered_from_code_fence() -> None:
    parsed = AgentService._parse_json(
        """```json
        {
          "answer": "Pause and verify.",
          "risk_level": "medium",
          "related_event_ids": [],
          "suggested_action_codes": ["VERIFY_OFFICIAL_CHANNEL"],
          "citation_ids": ["scamwatch-methods"]
        }
        ```"""
    )

    assert parsed.risk_level.value == "medium"
    assert parsed.citation_ids == ["scamwatch-methods"]


class FailingToolsThenJsonCompletions:
    def __init__(self) -> None:
        self.calls: list[dict] = []

    async def create(self, **kwargs):
        self.calls.append(kwargs)
        if len(self.calls) == 1:
            raise RuntimeError("simulated tool-call failure")
        message = SimpleNamespace(
            content=(
                '{"answer":"Pause and verify independently.","risk_level":"high",'
                '"related_event_ids":[],"suggested_action_codes":["VERIFY_OFFICIAL_CHANNEL"],'
                '"citation_ids":["scamwatch-methods"]}'
            ),
            tool_calls=None,
        )
        return SimpleNamespace(choices=[SimpleNamespace(message=message)])


async def test_tool_failure_retries_once_without_tools() -> None:
    completions = FailingToolsThenJsonCompletions()
    fake_client = SimpleNamespace(chat=SimpleNamespace(completions=completions))
    service = AgentService(client=fake_client, api_key="test-only")

    result = await service.answer(make_request())

    assert result.degraded is False
    assert len(completions.calls) == 2
    assert "tools" in completions.calls[0]
    assert "tools" not in completions.calls[1]
    assert completions.calls[1]["response_format"] == {"type": "json_object"}
