# Test report

Last updated: 2026-08-22 (Australia/Melbourne).

This report separates automated and Android-emulator evidence from the physical-
device acceptance gate. A passing emulator run does not claim that a real phone's
OEM permission UI, vibration, ringtone interaction, or TalkBack behaviour passed.

## Release-candidate build evidence

| Check | Command/evidence | Result |
|---|---|---|
| Backend | `.venv/bin/pytest -q backend/tests` | Pass: 31 tests, 0 failures |
| Backend syntax | `.venv/bin/python -m compileall -q backend` | Pass |
| GitHub CI | [CI run 32554775871](https://github.com/MortyYJT/sagesense/actions/runs/32554775871) for `cd4b4d5` | Pass: backend and Android jobs completed successfully |
| Production Agent | Health plus a redacted `POST https://sagesense.vercel.app/v1/agent/query` | Pass: HTTP 200, `deepseek-v4-flash`, `degraded=false`, safe actions and three allowlisted citations |
| Production privacy boundary | Health headers plus a synthetic invalid-locale request after deployment | Pass: `no-store`, `no-referrer`, `nosniff`, frame/permissions headers; stable 422 did not echo the marker |
| Android JVM | `./gradlew testDebugUnitTest` | Pass: 49 tests, 0 failures/errors/skips |
| Android lint | `./gradlew lintDebug` | Pass: 0 errors; 7 non-blocking dependency/version-availability warnings |
| Android APK | `./gradlew assembleDebug` | Pass: 21,454,010 bytes |
| Static integrity | `git diff --check`, JSON/XML parsing, `unzip -t` | Pass |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`

SHA-256: `aabcc3d26a80d61f6db22fea051b3b1b2bffe3357dd077edcbcbf725dfad1f9a`

The generated APK targets the production demo backend at
`https://sagesense.vercel.app/`. The provider key remains server-side.
The deployed health response reports the configured `deepseek-v4-flash` model;
the synthetic 422 check contained no personal or production user data.

## Automated coverage

- Ten scam and ten benign English/Chinese risk-engine fixtures.
- Watchlist matching, including equivalent Australian `0400…`, `614…`, and
  `+61 400…` formats.
- Room-bound sender/snippet/URL minimisation, plus OTP, password, card, account,
  email, phone and Agent-context redaction boundaries.
- Idempotent pre-hardening history rewrite, including retry-safe link markers
  and truncation that cannot re-expose a URL path/query on a later launch.
- Independent FastAPI re-sanitisation, non-echoing validation errors, no-store
  security headers, deterministic anti-scam topic gating (including nested
  context), prompt-extraction rejection, schema bounds, rate limiting,
  model-output recovery and citation allowlisting.
- One-time permission-prompt policy and demo-readiness decisions.
- Real-message, call and seeded-demo notification/channel policies in both
  languages.
- Event-only overlay policy, duration and accessibility copy.
- Notification de-duplication, 30-day retention and conservative Personal Scam
  Memory matching.
- Temporary physical-call fixture validation: accepted phone shapes, stable
  canonical IDs, masked display values, duplicate replacement and invalid-input
  rejection.
- Theme parsing and the invariant that every app typography style is at least
  22sp.

## Android 17 emulator evidence

Device: `SageSense_API_37`, `sdk_gphone64_arm64`, Android 17 / API 37.

| Scenario | Evidence | Status |
|---|---|---|
| True fresh install | Uninstalled and reinstalled the final APK; unified setup appeared after UI readiness | Pass |
| Prompt no-repeat | Back-dismissed setup, force-stopped and relaunched; prompt count remained zero | Pass |
| Manual permission re-entry | Onboarding `Set up` reopened the unified setup after the one-time automatic prompt | Pass |
| Permission-free fallback | With all protections OFF, onboarding, Home and local features remained available; the demo button reopened setup instead of silently failing | Pass |
| Overlay preview | Explicit special access showed a 64dp preview system window; `dumpsys window` recorded `TYPE_APPLICATION_OVERLAY` | Pass |
| Overlay auto-hide/revocation | Preview disappeared after 8 seconds; revoking special access changed Home to `Not enabled` and produced no overlay | Pass |
| Risk overlay deep link | A 20-second red shield appeared for a high-risk event; tapping it opened the matching bilingual Cognitive Pause | Pass |
| Overlay privacy boundary | Manifest/source audit contains no `AccessibilityService`, screen capture, OCR, full-screen intent or automatic blocking path | Pass source audit |
| Real Google Messages path | Emulator SMS generated a Google Messages notification; SageSense stored one non-demo 100/100 event and showed the system overlay | Pass |
| Post-merge install smoke | Rebuilt APK installed over the emulator build; a new real SMS showed the merged vector shield and tapping it opened the matching Cognitive Pause | Pass |
| Privacy upgrade migration | Before `install -r`, a legacy row contained a sender hash, raw phone, password and URL query. Cold start rewrote it to a redacted phone, null hash, redacted password and origin-only URL; all retained rows then had 0 non-null sender hashes and 0 URL queries. A second launch was unchanged and the synthetic row was deleted. | Pass |
| Notification de-duplication | Two identical Google Messages updates one second apart produced one additional event, not two | Pass |
| Notification self-loop | SageSense output notifications did not re-enter the risk history | Pass |
| Channel policy | Message v4 is importance 4 with default sound and `[0,250]` vibration; call v4 is importance 4 with no notification sound and `[0,250]`; demo v2 has neither | Pass system state |
| Seeded demo | Notification has `SILENT`, `sound=null`, `vibrate=null`, a demo label and a deep link | Pass |
| Watchlist call | `0400000999` matched the `+61 400 000 999` fixture, stored a 60/100 high-risk call event, and `dumpsys telecom` remained `RINGING` | Pass |
| Temporary physical-call fixture | A DUMP-protected debug broadcast added masked `61411222333`; `0411222333` remained `RINGING`, produced one 60/100 high-risk event and overlay; after removal the same call produced no new event | Pass |
| Debug/release separation | Debug merged manifest contains the ADB receiver; `processReleaseMainManifest` completed and the release merged manifest contains zero receiver entries | Pass |
| Call overlay and copy | Red shield appeared over the ringing call; tap opened Cognitive Pause with `Call not blocked` / `电话未被拦截` | Pass |
| Manual local check | A pasted risky message produced the local Cognitive Pause and history event without an Agent request | Pass |
| Personal Scam Memory | Two labelled demo messages with changed sender/domain were related by multiple stable risk signals | Pass |
| Agent online | Installed app completed the production request within the 30-second client ceiling and rendered a relevant answer, safe actions and official sources. That invocation was marked `degraded=true`; the direct production check above separately returned provider-backed `degraded=false` | Pass with truthful degraded-state label |
| Agent offline | Airplane mode produced a safe local-evidence explanation; risk detail remained available | Pass |
| Delete all history | Room event count changed to zero while both seeded Watchlist rows remained | Pass |
| English/Chinese | Primary UI, Agent and warning copy changed without restart; the whole language row is tappable | Pass |
| Theme persistence | Dark mode remained selected after force-stop/relaunch | Pass |
| 1.3× and 2.0× fonts | Content remained scrollable; bottom navigation switched to a two-by-two layout instead of clipping labels | Pass visual/emulator |

## Physical-device gate — still Pending

Record phone model, Android version, tester initials and a clip/screenshot for
each row. Do not replace `Pending` with `Pass` based on emulator evidence.

| Scenario | Expected | Status |
|---|---|---|
| Fresh-install permission allow/deny/back | No permission loop; accurate ON/OFF state and Settings recovery | Pending |
| Foreground default SMS app | Real test SMS creates the risk flow while SageSense is foregrounded | Pass (user-reported after `cd4b4d5`; device metadata and clip Pending) |
| Background default SMS app and de-duplication | Local warning, history and deep link work while backgrounded without an event storm | Pending |
| Watchlist call | Phone keeps ringing; warning is visible; seeded fixture is named as demo data | Pending |
| Alert sensation | Real message is audible/vibrating; call adds no notification sound; seeded demo is silent | Pending |
| Overlay grant/revoke/tap | Optional warning appears only when granted, auto-hides and opens the matching event | Pending |
| Agent production/offline | Online citations and offline local guidance both render | Pending |
| Chinese, 1.3×/2.0× and TalkBack | No clipped core action; labels and reading order are usable | Pending |
| Delete history | Events disappear and seeded Watchlist remains | Pending |

The final `catalyst-2026-submission` tag remains blocked on this physical-device
gate. README screenshots, video, Devpost, clean ZIP and logged-out public-link
checks are separate submission deliverables and are not implied by this report.
