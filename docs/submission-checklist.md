# Catalyst submission checklist

## Build freeze

- [x] Backend tests pass on Python 3.12 (31 tests, 2026-08-22).
- [x] Production Agent returns `degraded=false` through OpenCode Go without exposing its key.
- [x] Production FastAPI returns the privacy/security headers and a non-echoing
  validation error verified after deployment.
- [ ] Final release commit passes both GitHub CI backend and Android jobs (verify after push).
- [x] Current Android source compiles and 44 JVM logic tests pass with the bundled Kotlin/Compose toolchain.
- [x] Current `testDebugUnitTest`, `lintDebug` and `assembleDebug` Gradle tasks pass; fresh APK is 21,022,720 bytes with SHA-256 `83fca47501ff322d1eafdc6b4b75e2d632272718388d03f2c2afc8b39e10d6f2`.
- [x] Emulator verifies overlay grant/deny/revoke, preview, auto-hide, tap-through, notification channel v4, manual check, two-event Scam Memory and Agent online/offline.
- [ ] Fresh-install physical-device smoke test passes.
- [x] No API keys, personal data or real test phone numbers are present in the local source tree.
- [x] Seeded demo data is visibly labelled.
- [x] Public repository and raw README return successfully without authentication.
- [ ] Final commit is tagged `catalyst-2026-submission`.
- [ ] Repository remains unchanged during judging.

## Documentation and licensing

- [ ] README has current setup steps, architecture and final screenshots (setup and architecture are current; screenshots remain Pending).
- [x] Product explanation covers background, process, technical choices and innovation.
- [x] `docs/divergence-log.md` records every material blueprint change.
- [ ] Billy Hermawan and Sixth Sense are credited in README, pitch and final Devpost (credit is present in the prepared draft).
- [x] Third-party APIs, libraries, sources, fonts and assets are listed.
- [x] Prototype privacy notice, security statement and threat model disclose the
  implemented controls and residual production risks.
- [x] Supplied PDF/FIG and unlicensed original assets are absent from the tracked repository tree; recheck the final ZIP.

## Video

- [ ] 3–5 minutes, 1080p, intelligible audio and subtitles.
- [ ] Real Android build is shown; no clickable-only mockup is presented as working software.
- [ ] Optional floating warning is described accurately: event-only, user-granted, auto-hiding, and never reading/capturing the current screen.
- [ ] Call and notification fixtures are described as seeded demo data.
- [ ] Video is publicly viewable without login.
- [ ] A local backup exists.

## Devpost

- [ ] Team name and all five members are correct.
- [ ] Public GitHub URL and video URL work without login.
- [x] Prepared Devpost draft names the user problem, solution, innovation and limitations; replace placeholders before submission.
- [ ] Setup instructions or downloadable APK are attached.
- [ ] Curated ZIP contains source, docs and required assets only (generation and rejection checks are scripted; rerun at final tag).
- [ ] Submit internally by Sunday 09:30, 30 minutes before the official deadline.
