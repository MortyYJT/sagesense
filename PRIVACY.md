# SageSense privacy notice

**Prototype notice — last updated 22 August 2026 (Australia/Melbourne).**

SageSense is a Catalyst 2026 hackathon prototype for a local, explainable
anti-scam warning. This document describes the prototype's intended data
boundary; it is not a production privacy policy, legal advice, or a promise
that a third-party provider will retain no data. Physical-device acceptance is
still pending and the distributed build is a debug release candidate.

## 中文摘要

SageSense 主要在 Android 手机上分析通知和来电。它不读取、截图、录屏或
OCR 当前屏幕，也不使用 AccessibilityService。风险历史只保留脱敏后的发
送方标签、URL 的 origin/domain、信号代码、分数和时间戳等结构化信息。
只有用户明确提问时，应用才会把重新脱敏的有限问题和上下文经 Vercel 发
送到 OpenCode Go/DeepSeek；后端不保存请求正文。非演示历史保留 30 天，
用户可以删除全部历史。原型没有账户、广告、分析 SDK 或云端历史。

## What the app can see

The app does not receive data until the relevant Android permission or role is
granted. Each is optional and can be reviewed or revoked in Android Settings.

| Permission or role | Purpose | Boundary |
| --- | --- | --- |
| Notification access | Inspect supported message notifications for local risk signals | The app does not read the current screen, take screenshots, record the screen, OCR it, or use `AccessibilityService`. |
| Call-screening role | Compare an incoming number with the local Watchlist and show a warning | Calls continue ringing; SageSense does not reject, silence, or remove calls. |
| Post notifications | Show a warning or history deep link | It is output from SageSense, not an additional source of message content. |
| Draw-over-other-apps (optional special access) | Show a short, event-triggered warning | It is never an always-on bubble and does not inspect the app or screen underneath. |

The local manual check accepts a message, URL, or phone number only when the
user supplies it. It is analysed on-device and does not call the Agent unless
the user later opens the event and asks a question.

## Data stored on the device

Risk analysis and the warning verdict are local and continue to work offline.
The local Room history is designed to persist only the minimum structured
event needed for the UI and Personal Scam Memory:

- a minimised sender label: direct phone, email, URL and credential patterns are
  removed, while an ordinary brand label may remain for a useful explanation;
- URL origin/domain only, when a domain is needed to explain a signal (not the
  full URL, query string, path, or message link);
- signal codes, risk score, risk level/source type where needed by the UI, and
  event timestamps;
- a short redacted evidence excerpt only where the current UI needs to show
  why an event was flagged (never the original notification body); and
- a stable local event identifier and the labelled seeded-demo state.

The original notification body, OTPs, card details, account numbers, passwords,
raw phone numbers, and full URLs are not intended to be persisted. A short
filtered excerpt may still contain an ordinary name or unusual personal detail
that the deterministic patterns do not recognise. Do not use a real secret as
a test.

The Watchlist and its source provenance are local product data. Seeded demo
rows are clearly labelled and are not live threat intelligence. Delete-all
removes risk events; it does not remove the built-in seeded Watchlist fixtures
needed for a repeatable demo.

Non-demo risk history is pruned after 30 days. The user can delete all risk
history immediately in the app. Deleting local history cannot erase a copy
that a user, Android diagnostic tool, network intermediary, or third-party
provider may already have retained outside this app.

When a pre-hardening debug installation is upgraded, SageSense runs a versioned
one-time rewrite of retained risk rows before seeding demo data. It clears old
sender hashes, re-filters sender/evidence text, and reduces stored links to their
origin. The operation is idempotent and retries on a later launch if it fails.

Room storage is not encrypted at rest in this prototype. A person who gains
access to an unlocked, rooted, debugged, or otherwise compromised device may
be able to inspect local files. Use a test device and do not enter real secrets.

## Explicit Agent query

The Agent is an optional explanation service, not the local detector. A query
is sent only after the user actively asks a question for an event. Immediately
before the network request, the Android client re-sanitises the question and
bounded context. The payload is limited to the question plus redacted event
summaries (at most 10 recent events and 20 Watchlist summaries in the current
prototype contract).

The request goes to the SageSense FastAPI backend, normally hosted on Vercel.
The backend may call OpenCode Go's OpenAI-compatible endpoint using the
`deepseek-v4-flash` model. The Android app never contains the provider key.
The persisted sender field, sender hash, URL list, and phone Watchlist values are
removed or masked again by the backend before Agent use. Message/question text
is filtered for common phone, email, password, OTP, card/account and link
patterns; a URL domain may remain where it is evidence. This is heuristic, so a
plain-language name or an unusual secret typed by the user may survive.

The backend does not persist the Agent request body and returns `Cache-Control:
no-store` responses. It has no user database and no cloud history. Hosting,
edge, network, and model-provider infrastructure can still process technical
metadata (for example, an IP address, time, error record, or abuse-control
event) and may apply their own retention terms. SageSense cannot promise or
delete provider-side transient logs. Do not type passwords, verification
codes, bank details, identity documents, or other secrets into the question.

The online Agent is constrained to curated knowledge and read-only tools; its
answer is an explanation, not a fraud determination or a guarantee that a
payment, caller, or link is safe. If the provider is unavailable, the app can
show deterministic local guidance without sending a request.

## What SageSense does not do

The prototype has no account system, advertising SDK, behavioural analytics,
cross-app tracking, cloud-synchronised history, or sale of personal data. It
does not automatically block calls, make payments, contact an organisation,
send messages, or report a person. The overlay is not a screen reader.

The debug build contains a DUMP-permission-protected, ADB-only temporary caller
fixture for QA. It is absent from the release manifest. A debug caller value is
test data stored locally for the test; any Agent Watchlist context masks the raw
phone value.

## Provider, source, and legal caveats

OpenCode Go, DeepSeek, Vercel, Android, and the publishers linked in the
knowledge cards are independent services or rights holders. Their privacy
notices, acceptable-use rules, geographic availability, retention, and data
processing terms can change. Read the applicable terms before any real-user or
commercial deployment; this prototype has not completed a data-protection
impact assessment, legal review, penetration test, or store-policy review.

Watchlist entries and citations are educational, curated sources and labelled
fixtures, not an official or complete fraud database. A warning is evidence to
help a person pause and verify through an official channel. It is not legal,
financial, medical, or law-enforcement advice, and a low-risk result is not a
safe verdict.

## Questions and user choices

Keep permissions off to use the local onboarding, learning, and manual-check
features; disable any permission or role in Android Settings; avoid Agent
queries for sensitive content; and use the in-app delete-all control for local
risk history. For a privacy or data question, use the repository maintainer
contact shown in the project repository. For a security vulnerability, follow
the private reporting process in [SECURITY.md](SECURITY.md).
