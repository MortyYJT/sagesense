# Devpost submission draft

Replace every bracketed placeholder and attach final real-device evidence before
submission. The core phone smoke test is user-reported in `test-report.md`;
final URLs and media still require separate verification.

## Project name

**SageSense**

## Tagline

A calm, explainable Android anti-scam companion for older adults.

## Inspiration

Scam warnings often arrive at the worst possible moment: a message creates
urgency, a phone is ringing, and the person being targeted has seconds to decide
whether to trust it. Older adults do not only need a red warning. They need a
clear pause, understandable evidence, and a safe next action without losing
control of their phone.

SageSense implements and adapts the **Sixth Sense** Product-thon blueprint by
**Billy Hermawan**. We retained its point-of-risk intervention and low-density,
large-type direction, then made the prototype Android-first, warning-only,
privacy-first and bilingual. Material divergences are documented in
`docs/divergence-log.md`.

## What it does

With explicit Android permissions, SageSense:

- analyses supported message notifications locally for suspicious links,
  urgency, credential requests, payment language and impersonation signals;
- checks incoming caller numbers against a local Watchlist while always allowing
  the call to continue ringing;
- creates a bilingual Cognitive Pause explaining the evidence and safe next
  steps;
- optionally shows a short-lived, user-authorised warning shield over another
  app without reading or capturing the screen;
- stores a redacted local history with search, filters, source provenance and
  delete-all;
- uses Personal Scam Memory to relate repeated scam patterns when a sender or
  domain changes; and
- lets the user explicitly ask a constrained Agent for a plain-language
  explanation, confirmation-required actions and official citations.

Local detection, history and deterministic safety guidance continue to work
offline. The Agent explains a local risk event; it does not block calls, make
payments, contact organisations or declare a transaction safe.

## How we built it

The Android client uses Kotlin, Jetpack Compose, Material 3, Room, DataStore,
Coroutines and Flow. `NotificationListenerService` processes supported message
notifications, while `CallScreeningService` performs a bounded local number
lookup and immediately allows the call. A versioned deterministic risk engine
produces a 0–100 score and evidence codes on-device.

The Agent backend uses Python 3.12, FastAPI and Pydantic v2. It routes
`deepseek-v4-flash` through the team's OpenCode Go subscription from a Vercel
function; the provider key never enters the APK. Deterministic topic gating,
bounded schemas, read-only tools, citation allowlisting, output validation and a
safe fallback wrap the model call. The curated knowledge layer uses transparent
weighted lexical retrieval rather than a vector database because the current
corpus is small and reviewable.

Privacy boundaries include redaction before persistence and Agent use, omitted
sender identity, masked phone Watchlist values, stripped URL paths/query strings,
no cloud account or cloud history, HTTPS-only release traffic and 30-day local
retention for non-demo events.

## Challenges we ran into

- Android notification channels are immutable after creation, so an early silent
  configuration required new versioned channels to restore the intended
  real-message and call-warning behaviour.
- `CallScreeningService` can be unbound before asynchronous work finishes. We
  moved the bounded warning lookup to application scope while returning the
  allow-call response immediately.
- Australian numbers appeared as `0400…`, `614…` and `+61 400…`; canonicalising
  those forms was necessary for reliable local Watchlist matching.
- A permanently floating assistant would create distraction and privacy/policy
  concerns. We changed it to an optional event-only shield that auto-hides and
  never reads the current screen.
- Large text exposed navigation clipping, so the bottom navigation adapts to a
  two-by-two layout at high font scale.

## Accomplishments that we are proud of

- A real Google Messages notification produces one explainable local event,
  visible warning and deep-linked Cognitive Pause.
- A Watchlist call keeps ringing while SageSense presents a warning.
- The demo remains usable with permissions denied, the network unavailable or
  the model provider degraded.
- English, Simplified Chinese, 1.3x/2.0x font scale, ≥22sp app typography and
  ≥56dp primary touch targets are built into the release candidate.
- The current release candidate passes 49 Android JVM tests, 31 backend tests,
  Android lint with zero errors, APK assembly, isolated Android 17 emulator
  acceptance and GitHub CI.

## What we learned

An anti-scam product should optimise for a safe decision, not simply a confident
classification. Deterministic local evidence, explicit uncertainty, source
provenance and user control are more valuable than adding an unconstrained chat
interface. We also learned to separate competition evidence from production
claims: seeded data and emulator behaviour are useful demonstrations, but they
are not live threat intelligence or older-adult user validation.

## What's next

After the hackathon, SageSense would need authenticated regional threat feeds,
measured precision/recall and false-positive targets, moderated research with
older adults and carers, a wider OEM/device matrix, Google Play permission review,
formal privacy/security assessment, durable WAF rate limiting, monitoring,
release signing and staged production operations.

## Built with

Kotlin · Jetpack Compose · Material 3 · Room · DataStore · FastAPI · Pydantic ·
DeepSeek V4 Flash · OpenCode Go · Vercel

## Credits and source links

- Original blueprint: Sixth Sense by Billy Hermawan.
- Independently generated SageSense shield mascot: SageSense team using
  ChatGPT/OpenAI; no original Sixth Sense asset was reused.
- Knowledge references: ACCC/National Anti-Scam Centre Scamwatch, ASD Australian
  Cyber Security Centre, FTC and FBI IC3. Full URLs and supported claims are in
  `docs/sources.md`.
- Source: <https://github.com/MortyYJT/sagesense>
- Video: `[PUBLIC_OR_UNLISTED_VIDEO_URL]`
- APK or installation instructions: `[APK_URL_OR_RELEASE_URL]`

## Team

- Yu Junteng — team lead, backend, Agent, data contracts and integration
- Jiahui Zhou — UI design, Compose, bilingual and accessibility
- Yijia Sheng — product documentation, test integration and submission package
- Xiuning Gu — demo story, recording, editing, subtitles and presentation
- Junteng Hu — Android team member

## Final claim check

Before pasting this draft, confirm that the final submission does not claim:

- automatic call/SMS blocking;
- screen reading, OCR or deepfake detection;
- live/dynamic threat intelligence;
- production certification or older-adult clinical/user validation; or
- physical-device acceptance unless `docs/test-report.md` records it.
