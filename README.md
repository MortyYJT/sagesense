# SageSense

SageSense is an Android anti-scam companion designed for older adults. With explicit permission, it checks supported message notifications and incoming caller numbers, creates explainable risk events on-device, and offers a bilingual, citation-backed advisor.

> Hackathon prototype: SageSense provides warnings, not legal, financial, or fraud determinations. It never automatically blocks a call or declares a payment safe.

> Release status: the Friday-night debug RC was built and automated/emulator
> checks passed on 21 August 2026. Code freeze is targeted for Saturday,
> 22 August 2026 at 12:00 AEST; physical-device checks remain pending until the
> team runs them.

## Demo story

1. A clearly labelled seeded bank-impersonation notification appears.
2. The local Kotlin risk engine finds urgency, credential requests, and a suspicious domain.
3. SageSense displays a high-priority warning and stores only a redacted structured event.
4. The user opens the risk detail, sees plain-language evidence, and asks the Agent why it is risky.
5. The FastAPI service uses DeepSeek V4 Flash through OpenCode Go, with read-only tools, and returns safe actions plus official citations.
6. Personal Scam Memory relates similar recent events even when the sender changes.

## Architecture

```text
NotificationListenerService ─┐
                             ├─> local RiskAnalyzer ─> Room history ─> Compose UI
CallScreeningService ────────┘          │                    │
                                        └─ immediate alert    └─ redacted context
                                                                    │
                                                FastAPI + OpenCode Go
                                                DeepSeek V4 Flash
                                                read-only tools + citations
```

Risk verdicts are generated locally and remain available offline. The cloud Agent explains evidence; it does not control the phone.

The interface follows the older-adult design baseline: low information density,
all app-defined visible text styles are at least 22sp, and Material/custom touch
targets are at least 56dp. Atkinson Hyperlegible is used globally for supported
Latin glyphs; Android's system CJK fallback supplies Simplified Chinese glyphs.
At 1.3x and 2.0x system font scale, the bottom navigation adapts to a two-by-two
layout so labels remain readable. The app does not request an always-on overlay,
AccessibilityService screen reading, or a full-screen intent.

Before any provider call, the backend applies a deterministic anti-scam topic
gate and rejects off-topic or prompt-extraction requests. Pydantic bounds cap
the message and nested event payloads; a process-local best-effort limiter
allows 8 requests per minute and 2 concurrent requests per client. This is not
a durable multi-instance boundary: production still needs Vercel WAF
enforcement. The knowledge layer uses a small weighted bilingual lexical
retriever over curated cards, intentionally without a vector database. A
no-match query produces no citations rather than unrelated sources.

## Repository

- `android/` — Kotlin, Jetpack Compose, Room, DataStore, notification and call-screening services.
- `backend/` — FastAPI API, constrained model tool loop, deterministic fallback, tests.
- `knowledge/` — curated bilingual summaries with source metadata.
- `docs/` — PRD, design divergence, testing, video, and submission material.

Start with the [PRD](docs/PRD.md), [delivery plan and team ownership](docs/PROJECT_PLAN.md), and [current test report](docs/test-report.md).

## Run the backend

Requires Python 3.12+.

```bash
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e '.[dev]'
cp backend/.env.example .env
uvicorn backend.server:app --reload
pytest
```

`OPENCODE_API_KEY` is optional for local development. The service defaults to OpenCode Go's OpenAI-compatible endpoint and `deepseek-v4-flash`; `AGENT_BASE_URL` and `AGENT_MODEL` can override those non-secret settings. `DEEPSEEK_API_KEY` remains a temporary local migration fallback. Without a key, `/v1/agent/query` returns a citation-backed deterministic fallback with `degraded: true`.

For Vercel, import the repository and set `OPENCODE_API_KEY` as a Sensitive environment variable. Never put the key in the Android build or share it with teammates; they call the deployed SageSense backend instead. `pyproject.toml` declares `backend.server:app` as the FastAPI entrypoint.

## Run Android

The project uses JDK 17+, Android SDK 37, AGP 9.3, Gradle 9.5, and Compose BOM 2026.08.

```bash
cd android
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The default demo backend is `https://sagesense.vercel.app/`. To use a backend running on the development computer, override the URL explicitly. The Android emulator reaches the host at `10.0.2.2`:

```bash
./gradlew assembleDebug -PSAGESENSE_API_BASE_URL=http://10.0.2.2:8000/
```

For a physical phone using a local backend, replace `10.0.2.2` with the computer's LAN address. Keep the default production URL for the competition demo.

Install `android/app/build/outputs/apk/debug/app-debug.apk`. On a fresh
installation, the app shows the protection setup prompt once and persists a
seen flag; the prompt can be reopened manually from onboarding or Settings.
Notification access and the call-screening role remain optional. Calls are
always allowed to continue ringing.

Friday-night RC evidence: 21,191,579 bytes (20.21 MiB), SHA-256
`a0ffa30305d35f4967bb08294567e516652e390ff8ae24891e92f5dbeb911b35`.
The APK is configured for `https://sagesense.vercel.app/`; Agent calls have a
15-second client deadline and never contain a provider key.

The seeded phone number `+61 400 000 999` and `.example` domain are presentation fixtures, not claims about real entities.

## Privacy and safety

- Notification access is opt-in and restricted to supported packages.
- OTP, card, and account-number patterns are redacted before persistence or Agent use.
- At most 10 recent redacted summaries and 20 Watchlist entries are sent on an explicit Agent query.
- The backend has no user database and does not persist request bodies.
- Agent requests are limited to 800 characters, bounded nested collections and
  redacted event fields; off-topic and prompt-extraction requests are stopped
  before the model provider is called.
- The prototype applies a best-effort process-local limit of 8 requests/minute
  and 2 concurrent requests per client. Multi-instance production enforcement
  requires a Vercel WAF rule.
- Non-demo history is pruned after 30 days; users can delete it immediately.
- Model outputs are validated, citation IDs are allowlisted, and safe actions require user confirmation.

## Original blueprint and credits

SageSense implements and deliberately adapts the **Sixth Sense** Product-thon blueprint created by **Billy Hermawan**. The supplied Figma/PDF files are not redistributed. See [design divergence](docs/divergence-log.md) and [third-party notices](THIRD_PARTY_NOTICES.md).

The SageSense shield mascot is independently AI-generated by the team using
ChatGPT/OpenAI; it is not an original Sixth Sense asset. The shipped asset is a
512×512 transparent WebP with no embedded text. The original creator credit to
Billy Hermawan remains part of this project.

Built for Cissa x CISSA Catalyst 2026 Track 3 by Yu Junteng, Jiahui Zhou, Yijia Sheng, Xiuning Gu, and Junteng Hu.
