# Test report

Last automated run: 2026-08-21 (Australia/Melbourne)

## Verified build evidence

| Check | Command | Result |
|---|---|---|
| Backend | `.venv/bin/pytest -q` | Pass: 9 tests |
| Backend syntax | `.venv/bin/python -m compileall -q backend` | Pass |
| Knowledge and weight JSON | `python -m json.tool ...` | Pass |
| Android JVM tests | `./gradlew testDebugUnitTest` | Pass: 8 test methods, including theme-mode fallback plus 10 scam and 10 benign fixtures |
| Android lint | `./gradlew lintDebug` | Pass: 0 errors; 9 non-blocking version/unused-resource warnings |
| Android APK | `./gradlew assembleDebug` | Pass: 21 MiB debug APK |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`

SHA-256: `3fa8497999b08f9bf019cb3d0dc7d08b0da7341d81291488c047e73fd2e4bdaa`

ADB 37.0.1 connected to the Android 17 `sdk_gphone64_arm64` emulator. Emulator evidence is recorded separately from the physical-device checks below.

## Automated coverage

- Backend health response and secret-safe configuration status.
- Backend request bounds and deterministic offline response.
- Agent JSON recovery and allowlisted citation hydration.
- Personal Scam Memory campaign comparison and Watchlist normalisation.
- Kotlin risk engine: 10 scam and 10 benign English/Chinese fixtures.
- Kotlin Watchlist high-risk result, stable campaign fingerprints, OTP/card/account redaction.
- Theme storage parsing for System, Light, Dark and unknown-value fallback to System.

## FAQ and appearance emulator checks

| Scenario | Evidence | Status |
|---|---|---|
| Default/System mode | Missing or unknown stored value maps to System; System followed Android `uiMode` light/dark changes | Pass |
| Fixed Light and Dark | Light stayed light while Android was dark; Dark stayed dark while Android was light | Pass |
| Theme persistence | Dark remained selected and rendered after force-stop and relaunch | Pass |
| FAQ entry and bilingual copy | Settings entry opened the independent page; English and Chinese content switched without restart | Pass |
| FAQ accordion | Shared expandable component exposed all four questions; first answer expanded/collapsed with arrow rotation | Pass |
| FAQ large text | At Android font scale 1.3, title, principles and FAQ questions wrapped without clipping | Pass |
| Permission revocation | Disabled visible-warning notifications in Android Settings; SageSense returned normally, showed OFF, and Learn/navigation remained available | Pass |

## Cognitive Pause emulator checks

| Scenario | Evidence | Status |
|---|---|---|
| Resting companion | Home showed one small, low-opacity in-app shield with no flashing or obstruction | Pass |
| Seeded high-risk trigger | `Send seeded demo scam` created one Cognitive Pause layer over a dimmed Home screen and retained the status-bar warning | Pass |
| Trigger gesture isolation | Actions stayed disabled for the first 600 ms, preventing the trigger tap from clicking through to `See Why` | Pass |
| Honest demo and safety copy | Card showed `Demo simulation · Seeded demo data`, identified the result as a warning rather than proof, and stated that SageSense does not block, pay or contact organisations | Pass |
| Bilingual card | English/Chinese chips changed the title, explanation and actions in-place without restarting | Pass |
| User-controlled actions | `Not Now` dismissed the layer without another popup; `See Why` opened the matching high-risk evidence page | Pass |
| System dark mode | With theme set to System, Android night mode changed the card, scrim, warning colours, text and controls to the Material dark scheme with readable contrast | Pass |
| Quiet system notification | `dumpsys notification --noredact` retained the risk entry with `SILENT`/`ONLY_ALERT_ONCE`, `vibrate=null` and `sound=null` | Pass |
| Overlay boundary | Manifest contains no `SYSTEM_ALERT_WINDOW`; the experience is an in-app demo simulation, not a cross-app overlay | Pass |

## Required device checks

| Scenario | Expected | Status |
|---|---|---|
| Fresh install and optional onboarding permissions | User can continue without enabling every protection; unavailable protections show OFF | Pending physical device |
| Seeded notification | Medium/high event appears with deep-linked warning | Pending physical device |
| Notification access disabled | Notification protection shows OFF while Home, History and Learn remain available | Pending physical device |
| Seeded Watchlist call | Call rings; warning appears within system deadline | Pending physical device |
| Call role denied with “Don’t ask again” | Allow opens Default apps recovery; copy directs the user to Caller ID & spam app and SageSense | Pending physical device |
| Backend online | Agent returns answer, actions and official citations | Pending API key/deployment |
| Backend offline/timeout | Local detection remains; UI shows connection-safe failure | Pending physical device |
| English ↔ Chinese | Primary UI and Agent locale switch without restart | Pending physical device |
| TalkBack and system large text | Controls remain labelled, readable and tappable | Pending physical device |
| Delete history | Events disappear; seeded Watchlist remains | Pending physical device |

Do not mark the submission complete until all pending rows are run and replaced with pass/fail evidence, device model, Android version and tester initials.
