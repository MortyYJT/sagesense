# SageSense delivery plan

Last updated: Friday, 21 August 2026, 15:15 AEST

## Team responsibilities

| Member | GitHub | Primary responsibility | Required hand-off |
|---|---|---|---|
| Yu Junteng | `MortyYJT` | Team lead; FastAPI, OpenCode Go Agent, data contracts, Android integration, release APK and Devpost | Stable `/v1/agent/query`, merged build, deployed URL and final submission |
| Jiahui Zhou | `jzhou612` | Sole active UI owner: Compose screens, bilingual copy, accessibility and restrained motion | Reviewable UI branch, screenshots and accessibility evidence |
| Yijia Sheng | `sarahkaliyah` | First-launch permission setup, device QA, PRD, divergence log, source register and submission ZIP | Permission flow PR, signed device test report and clean submission archive |
| Xiuning Gu | `xiuningg` | Storyboard, screen recording, editing, subtitles, publishing and final pitch | Reusable real-device clips, final 4:30 video and presentation script |
| Junteng Hu | `H0sst` | Unavailable for the current delivery sprint | No implementation or review task assigned |

Jiahui Zhou owns UI files while her branch is active. Yijia Sheng must not edit `Screens.kt` until Jiahui's UI work is merged; she can prepare permission acceptance cases meanwhile. Yu Junteng owns integration and resolves the small permission/UI seam after both hand-offs. Xiuning Gu should collect working clips throughout development rather than waiting for the final edit.

## Current progress

### Complete locally

- Android project builds a 20 MiB debug APK with Kotlin, Compose, Room, DataStore and manual dependency injection.
- Notification listener, call-screening service, local risk engine, Watchlist, Personal Scam Memory and deep-linked alerts are implemented.
- Home, History, Watchlist, event detail, Agent, Learn and Settings flows are implemented in English and Chinese.
- FastAPI health and Agent endpoints, DeepSeek V4 Flash tool loop, citation allowlist and deterministic fallback are implemented.
- The backend is deployed at `https://sagesense.vercel.app`; a Sensitive `OPENCODE_API_KEY` routes DeepSeek V4 Flash through OpenCode Go. A production query returned `degraded=false` with allowlisted citations.
- An Android emulator is connected as `emulator-5554`. No physical phone has been verified yet.
- Yijia Sheng's `ba2eae5` adds a unified permission setup dialog on `main`. Unit tests, lint and APK assembly pass, but repeat-prompt behaviour, first-launch persistence and accessibility still need acceptance testing.
- Curated bilingual knowledge cards, PRD, design-divergence record, source register, test report, video script and submission checklist exist.
- Deterministic anti-scam topic gating, bounded request schemas, and prompt-extraction rejection run before any provider call.
- The prototype has a process-local best-effort limiter of 8 requests/minute and 2 concurrent requests per client; durable multi-instance enforcement remains a Vercel WAF responsibility.
- Curated knowledge retrieval now uses weighted bilingual lexical matching with stable ordering; no-match queries return no citations. No vector database is intentionally in scope for this small curated corpus.
- Backend tests pass: 22 on the current local tree. Current Android verification passes with 9 tests and 0 failures, lint 0 errors (7 dependency-version warnings only), and a successful debug APK build.

### Still required

- Harden the first-launch permission prompt and verify its allow, deny, back, skip, no-repeat and Settings re-entry states.
- Connect a physical Android phone and complete notification, call role, offline, TalkBack, large-text and delete-history checks.
- Build the APK with `https://sagesense.vercel.app/` and verify the Android-to-Agent path on-device.
- Review and polish the Compose UI against Jiahui's final design decisions.
- Keep the backend scope constrained to curated local knowledge: it has no arbitrary live web browsing and no vector database by design. A future production deployment still needs WAF-level multi-instance rate enforcement.
- Capture real-device clips, complete the video, prepare Devpost, build the clean ZIP and test every public link while logged out.

## Schedule

### Friday, 21 August

**15:15–16:00 — parallel preparation**

- Jiahui Zhou continues the UI/accessibility branch without changing permission launch behaviour.
- Yijia Sheng reviews her existing permission dialog against the fresh-install, no-repeat and accessibility criteria and prepares the device run sheet.
- Yu Junteng freezes the deployed Agent contract, prepares the production-base-URL APK and connects a physical phone.
- Xiuning Gu locks the 4:30 storyboard, file naming and subtitle template; emulator clips may be used as temporary edit placeholders only.

**16:00–19:00 — first physical-device integration**

- Complete first launch → permissions → notification → risk detail → history → cited Agent answer on the physical phone.
- Validate bilingual copy, source citations, offline/degraded behaviour and Personal Scam Memory.
- Save the first usable screen recordings before the end of the day.

**Friday exit condition:** the complete core demo path works on a real phone. Every failure is recorded with an owner.

### Saturday, 22 August

- 09:00–12:00: call warning, Watchlist and Personal Scam Memory device verification.
- 12:00–15:00: Learn, Settings, accessibility, test completion and release APK.
- 15:00: feature freeze. Remove unstable optional work rather than risking the core flow.
- 15:00–17:00: final QA, screenshots, README, attribution and divergence review.
- 17:00–21:00: recording, edit, subtitles and Devpost.
- 21:00–23:00: release candidate, clean ZIP, public-link checks and backups.

### Sunday, 23 August

- 07:00: fresh-install physical-device smoke test.
- 08:00: upload the public/unlisted video and verify it while logged out.
- 09:15: code freeze and tag `catalyst-2026-submission`.
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
5. [`demo-script.md`](demo-script.md)
