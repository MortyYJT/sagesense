# SageSense delivery plan

Last updated: Saturday, 22 August 2026, 01:17 AEST

Release status: the current release-hardening tree has an installable debug RC.
Backend, production-Agent, Android JVM, lint, build and isolated-emulator gates
pass. The optional event-only overlay, notification-channel v4, manual local
checking, notification de-duplication, stricter Agent redaction and conservative
Personal Scam Memory matching are included. Physical-device checks remain
Pending and must not be described as passed until recorded in the test report.

## Team responsibilities

| Member | GitHub | Primary responsibility | Required hand-off |
|---|---|---|---|
| Yu Junteng | `MortyYJT` | Team lead; FastAPI, OpenCode Go Agent, data contracts, Android integration, release APK and Devpost | Stable `/v1/agent/query`, merged build, deployed URL and final submission |
| Jiahui Zhou | `jzhou612` | Sole active UI owner: Compose screens, bilingual copy, accessibility, mascot integration and restrained motion | Reviewable UI branch, screenshots and accessibility evidence |
| Yijia Sheng | `sarahkaliyah` | First-launch permission setup, device QA, PRD, divergence log, source register and submission ZIP | Permission flow PR, signed device test report and clean submission archive |
| Xiuning Gu | `xiuningg` | Storyboard, screen recording, editing, subtitles, publishing and final pitch | Reusable real-device clips, final 4:30 video and presentation script |
| Junteng Hu | `H0sst` | Unavailable for the current delivery sprint | No implementation or review task assigned |

Jiahui Zhou owns UI files while her branch is active. Yijia Sheng must not edit `Screens.kt` until Jiahui's UI work is merged; she can prepare permission acceptance cases meanwhile. Yu Junteng owns integration and resolves the small permission/UI seam after both hand-offs. Xiuning Gu should collect working clips throughout development rather than waiting for the final edit.

## Current progress

### Complete locally

- Android project has a current 21,454,010-byte debug RC with Kotlin, Compose, Room, DataStore and manual dependency injection. Its SHA-256 is `e863c99dca00077000857610c8fb329cf7175f42cb827d7e0508050c8fd95f79`.
- Notification listener, call-screening service, local risk engine, Watchlist, Personal Scam Memory and deep-linked alerts are implemented.
- Home, History, Watchlist, event detail, Agent, Learn and Settings flows are implemented in English and Chinese.
- FastAPI health and Agent endpoints, DeepSeek V4 Flash tool loop, citation allowlist and deterministic fallback are implemented.
- The backend is deployed at `https://sagesense.vercel.app`; a Sensitive `OPENCODE_API_KEY` routes DeepSeek V4 Flash through OpenCode Go. A production query returned `degraded=false` with allowlisted citations.
- An isolated Android 17 / API 37 emulator passed a true fresh install, permission-prompt no-repeat and manual re-entry, overlay grant/revoke/preview/auto-hide/deep-link, real Google Messages notification, duplicate suppression, Watchlist call-while-ringing, manual local check, Personal Scam Memory, Agent online/offline, bilingual, 1.3x/2.0x font, theme persistence and delete-history scenarios. No physical phone has been verified yet.
- Yijia Sheng's unified permission setup dialog is integrated with a persisted one-time fresh-install prompt. Emulator clear-data, dismiss and force-stop/relaunch checks pass; physical-device allow/deny/back cases remain pending.
- Jiahui Zhou's UI contribution from `origin/ui/jzhou612-anti-scam-mascot` (`920fcc7`) is integrated on `codex/release-hardening`; the original large PNG was replaced by the provenance-recorded 512×512 transparent WebP.
- Curated bilingual knowledge cards, PRD, design-divergence record, source
  register, test report, privacy notice, security statement, threat model, video
  script and submission checklist exist.
- Deterministic anti-scam topic gating, bounded request schemas, nested-context
  prompt-extraction rejection and independent server-side re-sanitisation run
  before any provider call.
- The prototype has a process-local best-effort limiter of 8 requests/minute and 2 concurrent requests per client; durable multi-instance enforcement remains a Vercel WAF responsibility.
- Curated knowledge retrieval now uses weighted bilingual lexical matching with stable ordering; no-match queries return no citations. No vector database is intentionally in scope for this small curated corpus.
- Backend tests pass on the current tree: 31. Android `testDebugUnitTest` passes
  42 tests; `lintDebug` reports 0 errors and 7 non-blocking dependency/version
  availability warnings; `assembleDebug` succeeds. This automated and emulator
  evidence is not a substitute for the still-Pending physical-device gate.
- A debug-only, Android `DUMP`-permission-protected ADB hook can add and remove a
  masked temporary second-phone fixture for physical call QA. Add → ringing call
  → warning and remove → no new event passed on the emulator; the generated
  release manifest contains no receiver for this hook.

### Still required

- Connect a physical Android phone and complete permission allow, deny, back and Settings re-entry; foreground/background notification; call role; offline; TalkBack; large-text; and delete-history checks.
- Install the production-URL RC on a physical phone and verify the Android-to-Agent path on-device.
- Re-run optional overlay, channel sensation, manual check, duplicate suppression,
  changed-sender Personal Scam Memory and outbound Agent privacy as a physical
  smoke test only where observable; emulator and automated coverage already pass.
- Keep the backend API contract frozen during release hardening; no Agent
  request/response or provider configuration changes are planned.
- Keep the backend scope constrained to curated local knowledge: it has no arbitrary live web browsing and no vector database by design. A future production deployment still needs WAF-level multi-instance rate enforcement.
- Merge the reviewed release-hardening commits to `main` now; do not create the final submission tag until Saturday's physical-device gate passes.
- Capture real-device clips, complete the video, prepare Devpost, build the clean ZIP and test every public link while logged out.

## Schedule

### Friday, 21 August

**15:15–16:00 — parallel preparation**

- Jiahui Zhou continues the UI/accessibility branch without changing permission launch behaviour.
- Yijia Sheng reviews her existing permission dialog against the fresh-install, no-repeat and accessibility criteria and prepares the device run sheet.
- Yu Junteng freezes the deployed Agent contract, prepares the production-base-URL APK and connects a physical phone.
- Xiuning Gu locks the 4:30 storyboard, file naming and subtitle template; emulator clips may be used as temporary edit placeholders only.

**16:00–19:00 — release-candidate integration**

- Integrate the UI branch, harden permissions/alerts/accessibility, run automated tests and build the production-URL debug RC.
- Validate bilingual copy, source citations, offline/degraded behaviour, Personal Scam Memory and virtual Watchlist calls in the emulator.
- Record exact APK size/SHA and leave every unrun physical-device row Pending.

**Friday exit condition:** an installable RC exists with passing automated and emulator evidence; the team stops development for the night. Physical-device acceptance remains the first Saturday gate.

### Saturday, 22 August

- 09:00–11:00: physical-device tests for fresh-install permissions, foreground/background messages, Watchlist calls, English/Chinese, TalkBack, 1.3×/2.0× fonts, Agent online/offline and delete history.
- 11:00–11:30: fix only P0/P1 core-demo blockers; do not add features.
- 11:30–12:00: rebuild, rerun smoke checks, merge `main`, generate the final APK and create `catalyst-2026-submission` only if the physical-device gate passes.
- 13:00–17:00: record the real-device demo, architecture/privacy proof and team clips.
- 17:00–21:00: edit, subtitles, Devpost, README screenshots and attribution review.
- 21:00–23:00: clean ZIP, public-link checks and backups.

### Sunday, 23 August

- 07:00: fresh-install physical-device smoke test.
- 08:00: upload the public/unlisted video and verify it while logged out.
- 09:15: verify the existing physical-device-approved submission tag and public artifacts; do not retag unchanged code.
- 09:30: internal submission deadline, leaving 30 minutes before the official 10:00 deadline.

## Scope-cut order

If the team falls behind, cut work in this order:

1. Floating assistant/avatar.
2. Dynamic online Watchlist updates.
3. Extra call-screen visual polish.

Do not cut notification detection, explainable evidence, history, Agent guidance or citations.

## Friday reading order

1. [`PRD.md`](PRD.md)
2. This delivery plan
3. [`divergence-log.md`](divergence-log.md)
4. [`test-report.md`](test-report.md)
5. [`product-readiness-audit.md`](product-readiness-audit.md)
6. [`demo-script.md`](demo-script.md)
