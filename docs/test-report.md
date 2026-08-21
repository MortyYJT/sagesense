# Test report

Last automated run: 2026-08-21 (Australia/Melbourne)

## Verified build evidence

| Check | Command | Result |
|---|---|---|
| Backend | `.venv/bin/pytest -q backend/tests` | Pass: 10 tests |
| Backend syntax | `.venv/bin/python -m compileall -q backend` | Pass |
| OpenCode Go catalog | Authenticated `GET /zen/go/v1/models` | Pass: `deepseek-v4-flash` available; secret not printed |
| Production Agent | `POST https://sagesense.vercel.app/v1/agent/query` with seeded redacted event | Pass: HTTP 200, `degraded=false`, high risk, actions and 4 allowlisted citations |
| Knowledge and weight JSON | `python -m json.tool ...` | Pass |
| Android JVM tests | `./gradlew testDebugUnitTest` | Pass: 6 test methods, including 10 scam and 10 benign fixtures |
| Android lint | `./gradlew lintDebug` | Pass: 0 errors, 7 dependency-version warnings |
| Android APK | `./gradlew assembleDebug` | Pass: 20 MiB debug APK |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`

SHA-256: `ab8692f8492227a01fc16b2c5f7b1b41abf8c6c1b5f571b53cb8f3a6c9692f94`

ADB 37.0.1 reports `emulator-5554` as an authorised emulator. No physical Android phone has been connected, so no physical-device row below is represented as passed.

## Automated coverage

- Backend health response and secret-safe configuration status.
- Backend request bounds and deterministic offline response.
- Agent JSON recovery and allowlisted citation hydration.
- Personal Scam Memory campaign comparison and Watchlist normalisation.
- Kotlin risk engine: 10 scam and 10 benign English/Chinese fixtures.
- Kotlin Watchlist high-risk result, stable campaign fingerprints, OTP/card/account redaction.

## Required device checks

| Scenario | Expected | Status |
|---|---|---|
| Fresh install and optional onboarding permissions | App remains usable when access is declined | Pending physical device |
| Seeded notification | Medium/high event appears with deep-linked warning | Pending physical device |
| Notification access disabled | Manual Learn/history/Agent remain available | Pending physical device |
| Seeded Watchlist call | Call rings; warning appears within system deadline | Pending physical device |
| Backend production direct | Agent returns answer, actions and official citations | Pass on 2026-08-21; OpenCode Go / DeepSeek V4 Flash; `degraded=false` |
| Android → backend online | Installed app returns answer, actions and official citations | Pending physical device |
| Backend offline/timeout | Local detection remains; UI shows connection-safe failure | Pending physical device |
| English ↔ Chinese | Primary UI and Agent locale switch without restart | Pending physical device |
| TalkBack and system large text | Controls remain labelled, readable and tappable | Pending physical device |
| Delete history | Events disappear; seeded Watchlist remains | Pending physical device |

Do not mark the submission complete until all pending rows are run and replaced with pass/fail evidence, device model, Android version and tester initials.
