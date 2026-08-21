"""Deterministic request guardrails and best-effort process-local limiting.

The topic decision is deliberately made before the model client is touched.  It
is a product boundary, not a prompt-level instruction, so an off-topic or
prompt-extraction request cannot spend a model call.
"""

from __future__ import annotations

import ipaddress
import math
import re
import threading
import time
from collections import deque
from dataclasses import dataclass
from enum import StrEnum
from typing import TYPE_CHECKING

from fastapi import Request

if TYPE_CHECKING:
    from backend.app.schemas import AgentQueryRequest


class TopicDecision(StrEnum):
    ALLOW = "allow"
    OFF_TOPIC = "off_topic"
    PROMPT_INJECTION = "prompt_injection"


# These are intentionally broad signals.  A real active event is sufficient
# evidence to allow analysis, while a request without an event must mention a
# scam-related concept before it can reach a model.
EN_STRONG_SCAM_TERMS = re.compile(
    r"\b(?:"
    r"scam|scammer|fraud|phish(?:ing)?|impersonat(?:e|ion)|suspicious|fake|hacked"
    r")\b",
    re.IGNORECASE,
)
EN_CONTEXT_TERMS = re.compile(
    r"\b(?:bank|phone|caller|call|sms|text|message|link|url|account|security|"
    r"payment|pay|transfer|refund|invoice|tax|police|parcel|package|investment|urgent|"
    r"otp|one[- ]?time\s+pass(?:word|code)|verification\s+code|password|credential|"
    r"gift\s*card|crypto(?:currency)?)\b",
    re.IGNORECASE,
)
EN_RISK_INTENT_TERMS = re.compile(
    r"\b(?:risk|safe|unsafe|dangerous|genuine|real|verify|check|ask(?:ed)?|request(?:ed)?|"
    r"require(?:d)?|receive(?:d)?|strange|unknown|unexpected|urgent|safety|suspicious|fake)\b",
    re.IGNORECASE,
)
ZH_STRONG_SCAM_TERMS = re.compile(
    r"(?:"
    r"诈骗|欺诈|骗子|钓鱼|冒充|可疑|虚假|被盗"
    r")",
)
ZH_CONTEXT_TERMS = re.compile(
    r"(?:银行|电话|来电|短信|消息|链接|网址|账户|账号|付款|支付|转账|退款|发票|"
    r"税务|警察|包裹|快递|投资|安全|验证码|密码|凭证|加密货币|礼品卡)",
)
ZH_RISK_INTENT_TERMS = re.compile(
    r"(?:风险|安全|危险|真假|真的|是否|吗|核实|验证|要求|收到|陌生|异常|紧急|可疑|"
    r"不安全|不对|奇怪|意外)",
)
EN_EXPLICIT_CREATIVE = re.compile(
    r"\b(?:write|create|generate)\b.{0,50}\b(?:poem|essay|story|joke|code|homework|recipe)\b",
    re.IGNORECASE,
)
ZH_EXPLICIT_CREATIVE = re.compile(
    r"(?:写|生成).{0,30}(?:诗|作文|故事|笑话|代码|作业|菜谱)",
)

PROMPT_INJECTION_TERMS = re.compile(
    r"(?:"
    r"(?:ignore|disregard|forget)\s+(?:all\s+)?(?:previous|prior|above)\s+instructions?|"
    r"(?:reveal|show|print|tell|泄露|显示|告诉我).{0,30}(?:system|developer|hidden|internal|prompt|instruction|"
    r"系统|开发者|隐藏|内部|提示词|指令)|"
    r"(?:system|developer|hidden|internal)\s+(?:prompt|message|instruction)|"
    r"(?:系统|开发者|隐藏|内部)(?:提示词|消息|指令)|"
    r"jailbreak|prompt\s* injection|越狱|绕过(?:限制|安全)|(?:忽略|不(?:要)?遵循)(?:之前|上面|所有|上述)(?:的)?(?:所有)?(?:指令|要求)"
    r")",
    re.IGNORECASE,
)


def classify_topic(request: AgentQueryRequest) -> TopicDecision:
    """Return the deterministic allow/reject decision for an agent request."""

    message = request.message.strip()
    nested_client_text = [message]
    for event in ([request.active_event] if request.active_event else []) + request.recent_events:
        if event is not None:
            nested_client_text.extend(
                [event.id, event.redacted_snippet, event.related_campaign_id or "", *event.domains]
            )
    for item in request.watchlist:
        nested_client_text.extend(
            [item.value, item.reason, item.source_title, str(item.source_url)]
        )
    if any(PROMPT_INJECTION_TERMS.search(value) for value in nested_client_text):
        return TopicDecision.PROMPT_INJECTION
    if EN_EXPLICIT_CREATIVE.search(message) or ZH_EXPLICIT_CREATIVE.search(message):
        return TopicDecision.OFF_TOPIC
    if request.active_event is not None:
        return TopicDecision.ALLOW
    if EN_STRONG_SCAM_TERMS.search(message) or ZH_STRONG_SCAM_TERMS.search(message):
        return TopicDecision.ALLOW
    if (
        EN_CONTEXT_TERMS.search(message)
        and EN_RISK_INTENT_TERMS.search(message)
    ) or (
        ZH_CONTEXT_TERMS.search(message)
        and ZH_RISK_INTENT_TERMS.search(message)
    ):
        return TopicDecision.ALLOW
    return TopicDecision.OFF_TOPIC


def safety_boundary_response(request: AgentQueryRequest, decision: TopicDecision):
    """Build a bilingual, model-free response for requests outside the product boundary."""

    from backend.app.schemas import AgentQueryResponse, RiskLevel

    if request.locale == "zh-CN":
        if decision == TopicDecision.PROMPT_INJECTION:
            answer = "我不能提供系统提示词或内部指令。请描述你收到的可疑消息、电话、链接或付款要求，我可以帮你核对风险。"
        else:
            answer = "我是防诈骗助手，只能协助分析可疑消息、电话、链接、付款、验证码或账户安全问题。请描述相关内容，我会给出安全的核验建议。"
    else:
        if decision == TopicDecision.PROMPT_INJECTION:
            answer = "I can’t provide system prompts or internal instructions. Describe the suspicious message, call, link, or payment request and I can help assess it."
        else:
            answer = "I’m an anti-scam assistant. I can only help assess suspicious messages, calls, links, payments, verification codes, or account-security concerns."
    return AgentQueryResponse(
        answer=answer,
        risk_level=RiskLevel.UNKNOWN,
        related_event_ids=[],
        suggested_actions=[],
        citations=[],
        # This is an intentional product-boundary response, not a provider
        # outage. Keep Android from displaying the offline/degraded label.
        degraded=False,
    )


@dataclass
class _LimitBucket:
    timestamps: deque[float]
    in_flight: int = 0
    last_seen: float = 0.0


@dataclass(frozen=True)
class RateReservation:
    key: str
    retry_after: int = 0

    @property
    def allowed(self) -> bool:
        return self.retry_after == 0


class InMemoryRateLimiter:
    """Small rolling-window limiter for the FastAPI/Vercel prototype.

    This is process-local best effort: multiple server instances do not share
    counters.  Production deployment should enforce the durable boundary in a
    Vercel WAF rule as well.  No client-provided install ID is used as a sole
    security boundary.
    """

    def __init__(
        self,
        *,
        max_requests: int = 8,
        window_seconds: float = 60.0,
        max_concurrent: int = 2,
        max_buckets: int = 2048,
    ) -> None:
        if max_requests < 1 or window_seconds <= 0 or max_concurrent < 1 or max_buckets < 1:
            raise ValueError("rate limiter limits must be positive")
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        self.max_concurrent = max_concurrent
        self.max_buckets = max_buckets
        self._buckets: dict[str, _LimitBucket] = {}
        self._lock = threading.Lock()

    def acquire(self, key: str, now: float | None = None) -> RateReservation:
        current = time.monotonic() if now is None else now
        with self._lock:
            cutoff = current - self.window_seconds
            # Expire idle buckets on the hot path. This keeps spoofed forwarded
            # addresses from making the process dictionary grow forever.
            for bucket_key, existing in list(self._buckets.items()):
                while existing.timestamps and existing.timestamps[0] <= cutoff:
                    existing.timestamps.popleft()
                if not existing.timestamps and existing.in_flight == 0:
                    del self._buckets[bucket_key]

            bucket = self._buckets.get(key)
            if bucket is None:
                if len(self._buckets) >= self.max_buckets:
                    idle = [
                        (existing.last_seen, bucket_key)
                        for bucket_key, existing in self._buckets.items()
                        if existing.in_flight == 0
                    ]
                    if not idle:
                        return RateReservation(key, 1)
                    _, evicted_key = min(idle)
                    del self._buckets[evicted_key]
                bucket = _LimitBucket(deque())
                self._buckets[key] = bucket
            bucket.last_seen = current
            while bucket.timestamps and bucket.timestamps[0] <= cutoff:
                bucket.timestamps.popleft()
            if bucket.in_flight >= self.max_concurrent:
                return RateReservation(key, 1)
            if len(bucket.timestamps) >= self.max_requests:
                wait = max(1, math.ceil(bucket.timestamps[0] + self.window_seconds - current))
                return RateReservation(key, wait)
            bucket.timestamps.append(current)
            bucket.in_flight += 1
            return RateReservation(key)

    def release(self, reservation: RateReservation) -> None:
        if not reservation.allowed:
            return
        with self._lock:
            bucket = self._buckets.get(reservation.key)
            if bucket is not None:
                bucket.in_flight = max(0, bucket.in_flight - 1)


def request_client_key(request: Request) -> str:
    """Use Vercel's forwarded client address, safely falling back to ASGI peer."""

    forwarded = request.headers.get("x-vercel-forwarded-for", "")
    candidate = forwarded.split(",", 1)[0].strip()
    try:
        # Normalize valid addresses so equivalent IPv6 forms share a bucket.
        return f"ip:{ipaddress.ip_address(candidate)}"
    except ValueError:
        peer = request.client.host if request.client else "unknown"
        return f"ip:{peer}"
