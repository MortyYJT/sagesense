# Jiahui Zhou - UI and accessibility implementation prompt

Copy the prompt below into Codex from a clean clone of
`https://github.com/MortyYJT/sagesense`.

```text
You are working on SageSense as Jiahui Zhou (`jzhou612`). You are the sole
active owner of Android UI design, Jetpack Compose screens, bilingual
presentation, accessibility and restrained motion for this sprint.

Outcome
-------
Polish the existing Android prototype into a low-density safety interface for
older adults. Continue from the current implementation; do not rebuild the app
or replace working data, service, navigation or Agent integrations.

Before editing
--------------
1. Fetch `origin/main`, inspect `git status`, and preserve all existing and
   unrelated work. Stop and report any overlapping uncommitted changes.
2. Create branch `codex/jiahui-ui-accessibility`.
3. Read, in order:
   - `docs/PRD.md`
   - `docs/PROJECT_PLAN.md`
   - `docs/divergence-log.md`
   - `docs/test-report.md`
4. Inspect the current `ui/Screens.kt`, `ui/theme/Theme.kt`, strings and
   navigation before proposing changes.

Source-design requirements
--------------------------
- Low-density layouts that reduce visual clutter and cognitive load.
- Atkinson Hyperlegible for supported Latin text.
- Main readable/action text targets at least 22sp.
- Every interactive target is at least 56dp on Android.
- English and Simplified Chinese must remain usable with system large text and
  TalkBack.

P0 tasks
--------
1. Typography foundation
   - Obtain Atkinson Hyperlegible only from the Braille Institute's official
     distribution and include the applicable font licence.
   - Use it for English/Latin text. Use a reliable system CJK fallback for
     Chinese; do not render unsupported Chinese glyphs with Atkinson.
   - Record the font, source and licence in `THIRD_PARTY_NOTICES.md`.
   - Centralise typography in the theme. Remove scattered font-size overrides
     from core screens where possible.
   - Audit the current 13sp, 16sp, 17sp, 18sp, 19sp and 20sp usages. Important
     content and action labels should target 22sp or larger. Document any
     justified exception in `docs/divergence-log.md`.
2. Interaction and semantics
   - Ensure Button, TextButton, FilterChip, Tab, NavigationBarItem, clickable
     Card and IconButton targets are at least 56dp.
   - Add useful bilingual content descriptions to meaningful icons; mark purely
     decorative icons as such.
   - Do not communicate risk with colour alone. Keep a visible risk label and
     icon in addition to colour.
3. Core-flow polish
   - Prioritise Onboarding, Home, Risk Detail, Agent and History.
   - Keep one main idea and one primary action per card.
   - On Risk Detail, preserve this order: risk level, evidence, safer actions,
     Ask Agent.
   - Retain seeded-demo labels and all privacy/safety caveats.
   - Do not add complex or distracting animation.
4. Large-text resilience
   - Verify en-AU and zh-CN at font scales 1.0, 1.3 and 2.0.
   - Fix clipping, overlap, fixed-height text containers and inaccessible
     reading order.

Boundaries
----------
- Do not change the backend, risk scoring, Room schema, notification listener or
  call-screening behaviour.
- Do not implement the permission launch sequence. Yijia Sheng owns that task
  after this UI branch is merged; keep callbacks and permission status cards
  easy to integrate.
- Coordinate necessary cross-package changes with Yu Junteng.
- Do not add third-party assets without source and licence records.

Verification
------------
Run:
- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`

Capture emulator screenshots for the five core screens in English and Chinese,
including fontScale 2.0. Perform a TalkBack pass. Add only genuinely verified
results to `docs/test-report.md`; leave untested rows pending.

Commit in reviewable blocks using a standard title plus body:

1. `feat(android-ui): add accessible typography foundation`
   Body: describe Atkinson integration, Chinese fallback, type scale and licence.
2. `feat(android-ui): polish the core safety flow`
   Body: describe low-density layout, 56dp targets and semantic improvements.
3. `test(android-ui): record accessibility verification`
   Body: list commands, devices/emulators, languages, font scales and remaining
   limitations.

Push the branch and open a PR. Do not merge it yourself. The PR must contain
screenshots, exact test results, modified paths, known limitations and a short
handoff for Yu Junteng and the permission-flow implementer.
```
