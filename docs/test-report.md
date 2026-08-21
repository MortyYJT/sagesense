# Test report

Last automated run: 2026-08-21 (Australia/Melbourne)

## Verified build evidence

| Check | Command | Result |
|---|---|---|
| Backend | `.venv/bin/pytest -q backend/tests` | Pass: 22 tests (2026-08-21 local run) |
| Backend syntax | `.venv/bin/python -m compileall -q backend` | Pass |
| OpenCode Go catalog | Authenticated `GET /zen/go/v1/models` | Pass: `deepseek-v4-flash` available; secret not printed |
| Production Agent | `POST https://sagesense.vercel.app/v1/agent/query` with seeded redacted event | Pass: HTTP 200, `degraded=false`, high risk, actions and 4 allowlisted citations |
| Knowledge and weight JSON | `python -m json.tool ...` | Pass |
| Android JVM tests | `./gradlew testDebugUnitTest` | Pass: 17 tests, 0 failures, including permission-prompt policy, alert channel/copy policy, typography minimums, Agent failure mapping, theme-mode fallback, redaction and bilingual risk fixtures |
| Android lint | `./gradlew lintDebug` | Pass: 0 errors, 11 non-blocking warnings |
| Android APK | `./gradlew assembleDebug` | Pass: 21,191,579-byte (20.21 MiB) production-URL debug APK |
| Mascot asset | `sips`, `file`, visual alpha check | Pass: 512×512, alpha channel, 98,972-byte WebP, no embedded banner text |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`

SHA-256: `a0ffa30305d35f4967bb08294567e516652e390ff8ae24891e92f5dbeb911b35`

The generated `BuildConfig` points to `https://sagesense.vercel.app/`. ADB
37.0.1 reports the Android 17 `sdk_gphone64_arm64` emulator as authorised.
Emulator evidence is recorded separately; no physical Android phone has been
connected.

## Automated coverage

- Backend health response and secret-safe configuration status.
- Backend request bounds and deterministic offline response.
- Deterministic topic gate rejects off-topic and prompt-extraction requests before a provider call.
- Process-local rate limiting returns `429` with `Retry-After`, and bounds/evicts client buckets.
- Weighted bilingual lexical knowledge search is stable, applies limits, and returns no citations on no-match.
- Agent JSON recovery and allowlisted citation hydration.
- Personal Scam Memory campaign comparison and Watchlist normalisation.
- Kotlin risk engine: 10 scam and 10 benign English/Chinese fixtures.
- Kotlin Watchlist high-risk result, stable campaign fingerprints, OTP/card/account redaction.
- Theme storage parsing for System, Light, Dark and unknown-value fallback to System.
- One-time permission-prompt decision policy, bilingual alert copy/channel selection and all Material typography styles at or above 22sp.

## FAQ and appearance emulator checks

| Scenario | Evidence | Status |
|---|---|---|
| Default/System mode | Missing or unknown stored value maps to System; System followed Android `uiMode` light/dark changes | Pass |
| Fixed Light and Dark | Light stayed light while Android was dark; Dark stayed dark while Android was light | Pass |
| Theme persistence | Dark remained selected and rendered after force-stop and relaunch | Pass |
| FAQ entry and bilingual copy | Settings entry opened the independent page; English and Chinese content switched without restart | Pass |
| FAQ accordion | Shared expandable component exposed all four questions; first answer expanded/collapsed with arrow rotation | Pass |
| Large-text layout | At Android font scale 1.3 and 2.0, English/Chinese content remained scrollable; bottom navigation adapted to a two-by-two layout instead of clipping labels | Pass |
| Permission revocation | Disabled visible-warning notifications in Android Settings; SageSense returned normally, showed OFF, and Learn/navigation remained available | Pass |
| First-launch permission prompt | After `pm clear`, the unified setup prompt appeared once; Done followed by force-stop/relaunch did not reopen it | Pass |

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
| Seeded-demo system notification | `dumpsys notification --noredact` retained a clearly labelled risk entry with `SILENT`/`ONLY_ALERT_ONCE`, `vibrate=null` and `sound=null` | Pass |
| Real alert channel policy | System channel state shows message risk at importance 4 with default sound and `[0,250]` vibration; call risk at importance 4 with no notification sound and `[0,250]` vibration | Pass |
| Notification self-loop prevention | After one seeded notification, Room contained exactly one high-risk notification event; the SageSense risk output was not re-ingested | Pass |
| Overlay boundary | Manifest contains no `SYSTEM_ALERT_WINDOW`; the experience is an in-app demo simulation, not a cross-app overlay | Pass |

## Additional release-hardening emulator checks

| Scenario | Evidence | Status |
|---|---|---|
| Seeded Watchlist call | `adb emu gsm call +61400000999` created a high-risk `call` event; `dumpsys telecom` remained `state=RINGING`; the bilingual call alert used `sagesense_call_risk_v2` and explicitly said the call was not blocked | Pass emulator |
| Agent online | Android called the local FastAPI through `adb reverse`; OpenCode Go/DeepSeek returned `degraded=false`, a Chinese explanation, five safe actions and three allowlisted citations | Pass emulator |
| Agent offline | After stopping FastAPI, Android showed the safe bilingual connection failure while the local 100/100 risk event and evidence remained available | Pass emulator |
| Production endpoint | Host health and seeded production query returned HTTP 200 and `degraded=false`; the current emulator could not resolve the production hostname, so physical-device Android-to-production verification remains Pending | Partial: host pass, device pending |

## Required device checks

| Scenario | Expected | Status |
|---|---|---|
| Fresh install and optional onboarding permissions | User can continue without enabling every protection; unavailable protections show OFF | Pass emulator for display/no-repeat; allow/deny/back pending physical device |
| Permission dialog no-repeat | Closing setup does not auto-open it on later launch; onboarding and Settings retain manual entry | Pass emulator; manual Settings re-entry pending physical device |
| Seeded notification | Medium/high event appears with deep-linked warning | Pass emulator; pending physical device |
| Notification access disabled | Notification protection shows OFF while Home, History and Learn remain available | Pending physical device |
| Seeded Watchlist call | Call rings; warning appears within system deadline | Pass virtual GSM emulator; pending physical device |
| Call role denied with “Don’t ask again” | Allow opens Default apps recovery; copy directs the user to Caller ID & spam app and SageSense | Pending physical device |
| Backend production direct | Agent returns answer, actions and official citations | Pass on 2026-08-21; OpenCode Go / DeepSeek V4 Flash; `degraded=false` |
| Android → backend online | Installed app returns answer, actions and official citations | Pass emulator through local FastAPI/ADB reverse; production URL pending physical device |
| Backend offline/timeout | Local detection remains; UI shows connection-safe failure | Pass emulator; pending physical device |
| English ↔ Chinese | Primary UI and Agent locale switch without restart | Pass emulator; pending physical device |
| TalkBack and system large text | Controls remain labelled, readable and tappable | 1.3×/2.0× pass emulator; TalkBack pending physical device |
| Delete history | Events disappear; seeded Watchlist remains | Pending physical device |

Do not mark the submission complete until all pending rows are run and replaced with pass/fail evidence, device model, Android version and tester initials.
