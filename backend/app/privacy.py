"""Server-side privacy boundary for agent requests.

The Android client already removes most direct identifiers before making a
request.  The backend repeats that boundary on the validated Pydantic model so
that a future client cannot accidentally pass raw message content to an agent
provider.  This module is deliberately pure: it does not perform I/O, log
values, or mutate the request passed by the caller.
"""

from __future__ import annotations

import re
from urllib.parse import urlsplit

from backend.app.schemas import (
    AgentQueryRequest,
    RiskEventSummary,
    WatchlistSummary,
)


# Keep these replacements aligned with the Android wire-boundary redactor.
_URL = re.compile(r"(?i)\b(?:https?://|www\.)[^\s<>]+")
_EMAIL = re.compile(r"(?i)\b[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,63}\b")
_PHONE = re.compile(r"(?<![\d])\+?\d[\d\s().-]{5,}\d(?![\d])")
_LABELLED_OTP = re.compile(
    r"(?i)(?:\botp\b|\bone[- ]time (?:code|password)\b|\bverification code\b|"
    r"\bsecurity code\b|验证码|动态码|安全码)\s*(?:is|:|为|：|-)?\s*\d{4,8}\b"
)
_CARD_NUMBER = re.compile(r"(?<!\d)(?:\d[ -]?){13,19}(?!\d)")
_ACCOUNT_NUMBER = re.compile(
    r"(?i)(account|acct|账户)(\s*(?:number|no\.?|号码)?\s*[:：]?\s*)\d{5,16}"
)
_PASSWORD = re.compile(
    r"(?i)(password|passcode|密码)(\s*(?:is|:|：|为)?\s*)[^\s,;]{4,64}"
)
_DOMAIN = re.compile(
    r"(?i)^(?=.{1,253}\.?$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}\.?$"
)
_ALLOWED_SIGNALS = {
    "URGENCY",
    "PAYMENT_REQUEST",
    "CREDENTIAL_REQUEST",
    "OTP_REQUEST",
    "SUSPICIOUS_URL",
    "MISSPELLED_DOMAIN",
    "BRAND_IMPERSONATION",
    "WATCHLIST_MATCH",
}


def redact_text(value: str) -> str:
    """Remove links and common secret/identifier patterns from text."""

    # URL removal comes first so secrets in query strings are not retained.
    redacted = _URL.sub("[LINK REDACTED]", value)
    redacted = _EMAIL.sub("[EMAIL REDACTED]", redacted)
    # Preserve the Android redactor's labels while replacing their values.
    redacted = _LABELLED_OTP.sub("[OTP REDACTED]", redacted)
    redacted = _CARD_NUMBER.sub("[CARD REDACTED]", redacted)
    redacted = _ACCOUNT_NUMBER.sub(
        lambda match: f"{match.group(1)}{match.group(2)}[REDACTED]",
        redacted,
    )
    redacted = _PASSWORD.sub(
        lambda match: f"{match.group(1)}{match.group(2)}[REDACTED]",
        redacted,
    )
    return _PHONE.sub("[PHONE REDACTED]", redacted)


def mask_phone(value: str) -> str:
    """Keep only a short suffix of a watchlist phone value."""

    digits = "".join(character for character in value if character.isdigit())
    if not digits:
        return "[PHONE REDACTED]"
    masked_prefix = "•" * max(len(digits) - 2, 0)
    return f"[PHONE {masked_prefix}{digits[-2:]}]"


def sanitize_event(event: RiskEventSummary) -> RiskEventSummary:
    """Return an event with only non-identifying evidence retained."""

    payload = event.model_dump(mode="python")
    payload.update(
        display_sender=None,
        sender_hash=None,
        redacted_snippet=redact_text(event.redacted_snippet),
        # URLs can contain paths, query strings, and tokens. A strictly parsed
        # domain remains enough to explain link-based evidence.
        urls=[],
        domains=[item for item in map(_sanitize_domain, event.domains) if item],
        signal_codes=[item for item in event.signal_codes if item in _ALLOWED_SIGNALS],
    )
    return RiskEventSummary.model_validate(payload)


def sanitize_watchlist_item(item: WatchlistSummary) -> WatchlistSummary:
    """Minimise all client-controlled Watchlist values and metadata."""

    if item.entity_type == "phone":
        value = mask_phone(item.value)
    elif item.entity_type == "domain":
        value = _sanitize_domain(item.value) or "[DOMAIN REDACTED]"
    else:
        value = redact_text(item.value)
    payload = item.model_dump(mode="python")
    payload.update(
        value=value,
        reason=redact_text(item.reason),
        source_title=redact_text(item.source_title),
        source_url=_sanitize_web_origin(str(item.source_url)),
    )
    return WatchlistSummary.model_validate(payload)


def sanitize_agent_request(request: AgentQueryRequest) -> AgentQueryRequest:
    """Sanitize all request text and nested evidence before agent processing."""

    return AgentQueryRequest.model_validate(
        {
            "locale": request.locale,
            "message": redact_text(request.message),
            "active_event": sanitize_event(request.active_event) if request.active_event else None,
            "recent_events": [sanitize_event(event) for event in request.recent_events],
            "watchlist": [sanitize_watchlist_item(item) for item in request.watchlist],
        }
    )


def _sanitize_domain(value: str) -> str | None:
    candidate = value.strip().lower().removeprefix("www.").rstrip(".")
    if not _DOMAIN.fullmatch(candidate):
        return None
    try:
        ascii_domain = candidate.encode("idna").decode("ascii")
    except UnicodeError:
        return None
    return ascii_domain if _DOMAIN.fullmatch(ascii_domain) else None


def _sanitize_web_origin(value: str) -> str:
    parsed = urlsplit(value)
    scheme = parsed.scheme.lower()
    host = (parsed.hostname or "").rstrip(".").lower()
    if scheme not in {"http", "https"} or not _sanitize_domain(host):
        # Pydantic requires a URL on this compatibility schema. This inert
        # project-owned origin contains no client-provided path or query data.
        return "https://sagesense.vercel.app/"
    return f"{scheme}://{host}/"


# A short alias makes the integration call site read naturally while keeping
# the explicit name useful to callers and tests.
sanitize_request = sanitize_agent_request
