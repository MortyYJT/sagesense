# SageSense delivery plan

Last updated: Thursday, 20 August 2026 (Melbourne time)

## Team responsibilities

| Member | GitHub | Primary responsibility | Required hand-off |
|---|---|---|---|
| Yu Junteng | `MortyYJT` | Team lead; FastAPI, DeepSeek Agent, data contracts, knowledge structure, integration and Devpost | Stable `/v1/agent/query`, deployed URL and final submission |
| Jiahui Zhou | `jzhou612` | UI design, Compose screens, bilingual copy, accessibility and motion | Approved screen states, reusable Compose components and accessibility review |
| Yijia Sheng | `sarahkaliyah` | PRD, divergence log, source register, test set, integration QA and submission ZIP | Current docs, signed test report and clean submission archive |
| Xiuning Gu | `xiuningg` | Storyboard, screen recording, editing, subtitles, publishing and final pitch | Reusable real-device clips, final 4:30 video and presentation script |
| Junteng Hu | `H0sst` | Android architecture, system services, permissions, Room, networking, APK and physical-device testing | Installable APK and device evidence for notifications and calls |

Jiahui Zhou and Junteng Hu jointly own Compose implementation. Yu Junteng and Yijia Sheng jointly own the API contract and Agent evaluation fixtures. Xiuning Gu should collect real functional clips throughout development rather than waiting for the final edit.

## Current progress

### Complete locally

- Android project builds a 20 MiB debug APK with Kotlin, Compose, Room, DataStore and manual dependency injection.
- Notification listener, call-screening service, local risk engine, Watchlist, Personal Scam Memory and deep-linked alerts are implemented.
- Home, History, Watchlist, event detail, Agent, Learn and Settings flows are implemented in English and Chinese.
- FastAPI health and Agent endpoints, DeepSeek V4 Flash tool loop, citation allowlist and deterministic fallback are implemented.
- Curated bilingual knowledge cards, PRD, design-divergence record, source register, test report, video script and submission checklist exist.
- Backend tests pass: 9. Android JVM tests, lint and debug APK build pass.

### Still required

- Connect a physical Android phone and complete notification, call role, offline, TalkBack, large-text and delete-history checks.
- Configure a server-side `DEEPSEEK_API_KEY`, deploy FastAPI to Vercel and verify the real DeepSeek response path.
- Review and polish the Compose UI against Jiahui's final design decisions.
- Capture real-device clips, complete the video, prepare Devpost, build the clean ZIP and test every public link while logged out.

## Schedule

### Friday, 21 August

**Morning**

- Five-person sync: read the PRD, this plan, divergence log and test report; raise blockers before coding.
- Junteng Hu connects a physical phone and owns the first APK install and permission walkthrough.
- Jiahui Zhou reviews the existing Compose screens and records priority UI/accessibility changes.
- Yu Junteng deploys the backend and validates the Android-to-Agent contract.
- Yijia Sheng converts the acceptance criteria into a device test run sheet.
- Xiuning Gu finalises the storyboard and records any already-working real-device clips.

**Afternoon, with first integration from 16:00–19:00**

- Complete notification → risk detail → history → cited Agent answer on the physical phone.
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
