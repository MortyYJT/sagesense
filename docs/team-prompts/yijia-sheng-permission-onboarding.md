# Yijia Sheng - first-launch permission setup prompt

Copy the prompt below into Codex from a clean clone of
`https://github.com/MortyYJT/sagesense`.

```text
You are working on SageSense as Yijia Sheng (`sarahkaliyah`). You already
committed the unified permission dialog as `ba2eae5`. Your delivery
responsibility is now to review and harden that implementation, add acceptance
tests, and record truthful device evidence. Do not recreate it. Junteng Hu is
unavailable and has no task.

Critical sequencing
-------------------
Jiahui Zhou is now rebasing her UI work onto `ba2eae5`. First prepare the test
matrix and inspect your current code. Coordinate a short editing window with Yu
Junteng before touching `ui/Screens.kt`; do not work on it simultaneously with
Jiahui. Make follow-up fixes on `codex/yijia-permission-hardening` from the
latest merged `main`.

Outcome
-------
On a genuinely fresh install, SageSense immediately shows one calm, bilingual,
large-text in-app permission explanation. It must not silently throw an older
user into several system settings screens. The existing `Set up later` and
continue actions must be clear. Each permission is opened only after its own
button is tapped; declining any step never blocks the rest of the app.

Required behaviour
------------------
1. Audit the current implementation
   - Reproduce fresh install, complete, skip and relaunch states before editing.
   - The current `LaunchedEffect` also opens the dialog whenever any available
     protection is missing. Fix it so completing or skipping onboarding does
     not automatically nag on every later app launch; Settings and protection
     cards remain the manual re-entry path.
   - Do not rely only on `rememberSaveable`; use the existing persistent
     onboarding state or a narrowly named DataStore flag where needed.
2. First-launch explanation
   - Show exactly once before the normal home screen on a fresh install.
   - Explain local notification analysis, visible risk warnings, warning-only
     call screening, and what still works without permission.
   - Provide English and Simplified Chinese before asking for access.
   - Use at least 22sp for primary copy and 56dp actions; support TalkBack.
3. Sequential setup
   - Android 13+: after `Start setup`, request `POST_NOTIFICATIONS` using the
     existing Activity Result launcher.
   - Notification-listener access is a special system settings page, not a
     normal runtime permission. Show an in-app step and open
     `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` only after the user taps
     its button.
   - On resume, re-read the actual listener state before proceeding.
   - If `ROLE_CALL_SCREENING` is available, explain that calls keep ringing and
     request the role only after the user taps its button.
   - Never open multiple system dialogs/settings screens back-to-back without a
     user action. Never block, reject or silence calls.
4. Denial and recovery
   - `Not now`, denial, back and swipe-away states all leave Home, Learn,
     History, manual demo and Agent usable.
   - Do not nag on every resume or app launch after the user finishes or skips
     the introduction.
   - Settings must continue to show the live state and allow retrying each
     capability later.
5. Accessibility and testability
   - Keep persistent intro/completion flags in DataStore; do not infer completion
     from one permission alone.
   - Change the existing 48dp permission buttons to at least 56dp and make
     primary dialog text/actions meet the 22sp target where layout allows.
   - Add useful TalkBack labels; risk/access state must not depend on colour.
   - Extract a small pure Kotlin auto-show/state decision with unit tests, then
     keep Compose launchers as a thin integration layer.
   - Preserve the current permission callbacks if Jiahui's merged UI depends on
     them. Do not redesign her visual system.

Acceptance cases
----------------
- Fresh install / Start setup / allow all.
- Fresh install / Not now.
- Deny POST_NOTIFICATIONS.
- Back out of notification-listener settings without enabling it.
- Enable listener access and return to SageSense.
- Decline call-screening role; verify calls are not blocked.
- Relaunch after completion or skip; intro does not repeat.
- Re-enable each missing capability from Settings.
- English and Chinese, font scales 1.0 and 2.0, TalkBack labels.

Verification
------------
Run `./gradlew testDebugUnitTest lintDebug assembleDebug`. Then fresh-install on
an emulator and, when available, a real Android phone. Record exact device,
Android version, commands and pass/fail evidence in `docs/test-report.md`. Never
mark an untested physical-device row as Pass.

Commit in reviewable blocks using title plus body:

1. `fix(android): harden first-launch permission setup`
   Body: explain the no-repeat state, explicit system requests, accessibility
   corrections and denial path relative to `ba2eae5`.
2. `test(android): verify permission onboarding states`
   Body: list automated cases, device evidence and remaining limitations.

Push the branch and open a draft PR to `main`. Do not merge it yourself. Include
screenshots of the fresh-install explanation, exact test output, touched files,
known limitations and the commit from Jiahui's merged UI that you based on.
```
