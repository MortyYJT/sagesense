# Junteng Hu - Android integration implementation prompt

Copy the prompt below into Codex from a clean clone of
`https://github.com/MortyYJT/sagesense`.

```text
You are working on SageSense as Junteng Hu (`H0sst`). Your ownership is Android
architecture, system services, permissions, Room, networking, APK production
and physical-device verification.

Outcome
-------
Turn the existing buildable Android prototype into a verified real-phone demo.
Do not recreate features that already exist. Audit the actual implementation,
reproduce failures, and make only evidence-backed fixes required for the core
demo.

Before editing
--------------
1. Fetch `origin/main`, inspect `git status`, and preserve all existing and
   unrelated work. Stop and report overlapping uncommitted changes.
2. Create branch `codex/hu-android-integration`.
3. Open `android/` in Android Studio with its embedded JDK (17+) and SDK 37.
4. Read, in order:
   - `docs/PRD.md`
   - `docs/PROJECT_PLAN.md`
   - `docs/test-report.md`
   - `docs/demo-script.md`
5. Inspect the current manifest, services, repositories, Room entities,
   networking and tests before changing anything.

P0 tasks
--------
1. Physical-device baseline
   - Connect a real Android phone with USB debugging.
   - Require an `authorized` row from `adb devices -l`.
   - Record tester, phone model and Android version.
   - Install a fresh debug APK without depending on previous app data.
2. Notification flow
   - Test permission denial and approval for notification access and visible
     notifications.
   - Trigger the clearly labelled seeded scam notification.
   - Verify local analysis, high-priority warning, deep link to the correct risk
     event, History persistence and sensitive-number redaction.
   - Keep Learn, History, manual demo and Agent available when optional access is
     denied.
   - Do not add AccessibilityService or read unsupported apps.
3. Warning-only call flow
   - Request and verify the CallScreening role.
   - Test the seeded Watchlist number.
   - The call must continue ringing. SageSense may warn, but must not reject,
     silence or block it.
   - Record Android/platform limitations rather than hiding them.
4. Shared backend integration
   - Obtain the deployed SageSense backend URL from Yu Junteng; never request or
     embed the DeepSeek API key.
   - Build with
     `./gradlew assembleDebug -PSAGESENSE_API_BASE_URL=https://<backend>/`.
   - Verify notification -> risk detail -> Ask Agent -> cited answer.
   - Verify timeout, cancellation, offline and backend-unavailable behaviour.
     Local risk analysis and stored history must remain usable.
5. Privacy and storage
   - Confirm Room stores only structured/redacted event data.
   - Confirm OTP, card and account patterns are not persisted or logged in raw
     form.
   - Verify clear-history and 30-day pruning behaviour.
   - Ensure logs and BuildConfig contain neither the DeepSeek key nor raw
     sensitive messages.

Coordination boundaries
-----------------------
- Jiahui Zhou owns `ui/` and `ui/theme/`. Do not overwrite her work.
- Prefer fixes in the existing `service`, `data`, `risk` and `network` packages.
- If a UI change is unavoidable, keep it minimal and explain it in the PR.
- Do not change backend contracts without coordinating with Yu Junteng.
- Fix P0 blockers before adding visual polish or stretch features.

Verification
------------
Run:
- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`

Complete and record these device checks: fresh install, permission accept/deny,
seeded notification, detail deep link, History, online Agent, offline Agent,
warning-only call, English/Chinese switch, large text and clear history. Update
`docs/test-report.md` with pass/fail evidence; never mark an untested item Pass.
Provide the final APK path and SHA-256. Save clean real-device clips for Xiuning
Gu and label all fixtures as seeded demo data.

Commit in reviewable blocks using a standard title plus body:

1. `fix(android): stabilise the notification risk flow`
   Body: describe device, permissions, deep links, redaction and persistence.
2. `fix(android): verify warning-only call screening`
   Body: describe continued ringing, warning behaviour and platform limits.
3. `test(android): record physical-device acceptance`
   Body: list device details, commands, APK hash, results and unresolved failures.

Push the branch and open a PR. Do not merge it yourself. The PR must contain
exact test evidence, APK hash, device details, recordings/screenshots, known
limitations and a handoff for Yu Junteng and Jiahui Zhou.
```
