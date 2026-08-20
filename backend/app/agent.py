from __future__ import annotations

import json
import os
import re
from typing import Any

from openai import AsyncOpenAI
from pydantic import ValidationError

from backend.app.knowledge import KnowledgeRepository
from backend.app.schemas import AgentQueryRequest, AgentQueryResponse, ModelAnswer, RiskLevel
from backend.app.tools import AgentTools, TOOL_SPECS, default_action_codes, safe_actions


MODEL = "deepseek-v4-flash"
BASE_URL = "https://api.deepseek.com"


SYSTEM_PROMPT = """You are SageSense, a calm bilingual anti-scam advisor for older adults.
You may only read the sanitised event context, watchlist, and curated knowledge through the provided tools.
Never claim certainty without evidence. Never tell a user that a payment, caller, or link is definitely safe.
Never take actions, block calls, send messages, delete data, or ask for passwords, one-time codes, or banking details.
Prefer short sentences, plain language, and an explicit next step. Treat local risk-engine signals as evidence, not proof.
Use only citation IDs returned by search_scam_knowledge.
Your final response must be a single JSON object with keys: answer, risk_level, related_event_ids,
suggested_action_codes, citation_ids. Do not wrap JSON in Markdown."""


class AgentService:
    def __init__(
        self,
        knowledge: KnowledgeRepository | None = None,
        client: AsyncOpenAI | None = None,
        api_key: str | None = None,
    ) -> None:
        self.knowledge = knowledge or KnowledgeRepository()
        self.api_key = api_key if api_key is not None else os.getenv("DEEPSEEK_API_KEY", "")
        self.client = client or (
            AsyncOpenAI(api_key=self.api_key, base_url=BASE_URL, timeout=8.0, max_retries=0)
            if self.api_key
            else None
        )

    @staticmethod
    def _parse_json(content: str) -> ModelAnswer:
        cleaned = content.strip()
        if cleaned.startswith("```"):
            cleaned = re.sub(r"^```(?:json)?\s*|\s*```$", "", cleaned, flags=re.IGNORECASE)
        try:
            payload = json.loads(cleaned)
        except json.JSONDecodeError:
            start, end = cleaned.find("{"), cleaned.rfind("}")
            if start < 0 or end <= start:
                raise
            payload = json.loads(cleaned[start : end + 1])
        return ModelAnswer.model_validate(payload)

    @staticmethod
    def _context_message(request: AgentQueryRequest) -> str:
        context = {
            "locale": request.locale,
            "question": request.message,
            "active_event_id": request.active_event.id if request.active_event else None,
            "active_risk_level": request.active_event.risk_level if request.active_event else "unknown",
            "available_recent_event_ids": [item.id for item in request.recent_events],
            "watchlist_entity_count": len(request.watchlist),
        }
        return json.dumps(context, ensure_ascii=False, default=str)

    def _no_tool_context(self, request: AgentQueryRequest) -> str:
        event_terms = []
        if request.active_event:
            event_terms = [*request.active_event.signal_codes, *request.active_event.domains]
        knowledge = self.knowledge.search(
            " ".join([request.message, *event_terms]),
            request.locale,
            limit=3,
        )
        payload = {
            "instruction": "Tools are unavailable. Answer only from this sanitised context and curated knowledge.",
            "request": request.model_dump(mode="json"),
            "curated_knowledge": knowledge,
        }
        return json.dumps(payload, ensure_ascii=False)

    async def _answer_without_tools(self, request: AgentQueryRequest) -> AgentQueryResponse:
        if self.client is None:
            raise RuntimeError("DeepSeek client is not configured")
        completion = await self.client.chat.completions.create(
            model=MODEL,
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": self._no_tool_context(request)},
            ],
            response_format={"type": "json_object"},
            temperature=0.1,
            max_tokens=900,
            extra_body={"thinking": {"type": "disabled"}},
        )
        content = completion.choices[0].message.content or ""
        return self._hydrate(self._parse_json(content), request, degraded=False)

    def _hydrate(self, answer: ModelAnswer, request: AgentQueryRequest, degraded: bool) -> AgentQueryResponse:
        valid_event_ids = {item.id for item in request.recent_events}
        if request.active_event:
            valid_event_ids.add(request.active_event.id)
        related = [item for item in answer.related_event_ids if item in valid_event_ids]
        risk_level = answer.risk_level
        if risk_level == RiskLevel.UNKNOWN and request.active_event:
            risk_level = request.active_event.risk_level
        action_codes = answer.suggested_action_codes or default_action_codes(
            risk_level,
            request.active_event.signal_codes if request.active_event else [],
        )
        citations = self.knowledge.citations(answer.citation_ids)
        if not citations:
            cards = self.knowledge.search(request.message, request.locale, limit=2)
            citations = self.knowledge.citations([item["id"] for item in cards])
        return AgentQueryResponse(
            answer=answer.answer,
            risk_level=risk_level,
            related_event_ids=related,
            suggested_actions=safe_actions(action_codes, request.locale),
            citations=citations,
            degraded=degraded,
        )

    def deterministic_response(self, request: AgentQueryRequest) -> AgentQueryResponse:
        event = request.active_event
        risk_level = event.risk_level if event else RiskLevel.UNKNOWN
        signals = event.signal_codes if event else []
        cards = self.knowledge.search(
            " ".join([request.message, *(event.signal_codes if event else []), *(event.domains if event else [])]),
            request.locale,
            limit=2,
        )
        if request.locale == "zh-CN":
            if event:
                reason = "、".join(signals[:3]) or "可疑的联系模式"
                answer = f"这条内容被标记为{risk_level.value}风险，主要依据是：{reason}。这不是诈骗定论。请先暂停，不要点击链接，并通过官方网站独立核实。"
            else:
                answer = "我现在无法使用在线 Agent，也没有选中的风险事件。请不要分享验证码或银行资料，并通过机构官方网站独立核实。"
        else:
            if event:
                reason = ", ".join(signals[:3]) or "a suspicious contact pattern"
                answer = f"This was marked {risk_level.value} risk because of {reason}. That is a warning, not proof. Pause, avoid the link, and verify through the official website."
            else:
                answer = "The online advisor is unavailable and no risk event is selected. Do not share verification codes or banking details. Verify through the organisation's official website."
        related = AgentTools(request, self.knowledge).compare_recent_events()["related_event_ids"]
        return AgentQueryResponse(
            answer=answer,
            risk_level=risk_level,
            related_event_ids=related,
            suggested_actions=safe_actions(default_action_codes(risk_level, signals), request.locale),
            citations=self.knowledge.citations([item["id"] for item in cards]),
            degraded=True,
        )

    async def answer(self, request: AgentQueryRequest) -> AgentQueryResponse:
        if self.client is None:
            return self.deterministic_response(request)

        tools = AgentTools(request, self.knowledge)
        messages: list[dict[str, Any]] = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": self._context_message(request)},
        ]
        try:
            for _ in range(3):
                completion = await self.client.chat.completions.create(
                    model=MODEL,
                    messages=messages,
                    tools=TOOL_SPECS,
                    tool_choice="auto",
                    temperature=0.1,
                    max_tokens=900,
                    extra_body={"thinking": {"type": "disabled"}},
                )
                message = completion.choices[0].message
                if message.tool_calls:
                    messages.append(message.model_dump(exclude_none=True))
                    for call in message.tool_calls:
                        messages.append(
                            {
                                "role": "tool",
                                "tool_call_id": call.id,
                                "content": tools.execute(call.function.name, call.function.arguments),
                            }
                        )
                    continue
                if message.content:
                    return self._hydrate(self._parse_json(message.content), request, degraded=False)
                break
        except (Exception, ValidationError, json.JSONDecodeError):
            pass

        # Tool/API/output failures get one smaller JSON-only DeepSeek attempt with
        # deterministic knowledge already retrieved. If that also fails, the
        # local bilingual evidence template remains available.
        try:
            return await self._answer_without_tools(request)
        except (Exception, ValidationError, json.JSONDecodeError):
            return self.deterministic_response(request)
