# Yijia Sheng - first-launch permission setup prompt

Copy the prompt below into Codex from a clean clone of
`https://github.com/MortyYJT/sagesense`.

```text
You are working on SageSense as Yijia Sheng (`sarahkaliyah`). Your delivery
responsibility is the first-launch Android permission setup, its acceptance
tests, and truthful device evidence. Junteng Hu is unavailable and has no task.

Critical sequencing
-------------------
Jiahui Zhou is currently changing `ui/Screens.kt` and `ui/theme/`. Do not edit
those files until her branch has been reviewed and merged into `main`. While
waiting, read the requirements, prepare the test matrix, connect the emulator or
phone, and inspect the current permission behaviour. After her merge, fetch the
latest `main` and create `codex/yijia-permission-onboarding`.

Outcome
-------
On a genuinely fresh install, SageSense immediately shows one calm, bilingual,
large-text in-app permission explanation. It must not silently throw an older
user into several system settings screens. The explanation has two clear
actions: `Start setup` and `Not now`. Starting setup walks through each Android
capability one at a time; declining any step never blocks the rest of the app.

Required behaviour
------------------
1. First-launch explanation
   - Show exactly once before the normal home screen on a fresh install.
   - Explain local notification analysis, visible risk warnings, warning-only
     call screening, and what still works without permission.
   - Provide English and Simplified Chinese before asking for access.
   - Use at least 22sp for primary copy and 56dp actions; support TalkBack.
2. Sequential setup
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
3. Denial and recovery
   - `Not now`, denial, back and swipe-away states all leave Home, Learn,
     History, manual demo and Agent usable.
   - Do not nag on every resume or app launch after the user finishes or skips
     the introduction.
   - Settings must continue to show the live state and allow retrying each
     capability later.
4. State and testability
   - Keep persistent intro/completion flags in DataStore; do not infer completion
     from one permission alone.
   - Prefer a small pure Kotlin `PermissionSetupStep` state machine with unit
     tests, then keep Compose launchers as a thin integration layer.
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

1. `feat(android): add first-launch permission setup`
   Body: explain the one-time state, sequential system requests and denial path.
2. `test(android): verify permission onboarding states`
   Body: list automated cases, device evidence and remaining limitations.

Push the branch and open a draft PR to `main`. Do not merge it yourself. Include
screenshots of the fresh-install explanation, exact test output, touched files,
known limitations and the commit from Jiahui's merged UI that you based on.
```
