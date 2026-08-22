# Catalyst submission checklist

## Build freeze

- [x] Backend tests pass on Python 3.12 (31 tests, 2026-08-22).
- [x] Production Agent returns `degraded=false` through OpenCode Go without exposing its key.
- [x] Production FastAPI returns the privacy/security headers and a non-echoing
  validation error verified after deployment.
- [x] GitHub CI run 32554775871 passed both backend and Android jobs for `cd4b4d5`.
- [x] Final documentation CI run 32593850128 passed both backend and Android jobs for `970f79d`.
- [x] Final video-link documentation CI run 32597323923 passed both backend and Android jobs for `9f1757b`.
- [x] Current Android source compiles and 49 JVM logic tests pass with the bundled Kotlin/Compose toolchain.
- [x] Current `testDebugUnitTest`, `lintDebug` and `assembleDebug` Gradle tasks pass; fresh APK is 20,994,464 bytes with SHA-256 `4e83da80e3353657145fa7dc5436acfb2d55d9f2c21ca7447d16728de0065bbf`.
- [x] Emulator upgrade test rewrites pre-hardening sender hashes, direct identifiers
  and full URL components; restart remains idempotent.
- [x] Emulator verifies overlay grant/deny/revoke, preview, auto-hide, tap-through, notification channel v4, manual check, two-event Scam Memory and Agent online/offline.
- [x] A real foreground SMS through the phone's default SMS app was user-confirmed after `cd4b4d5`.
- [x] Core physical-device smoke test is user-confirmed: permission allow/deny/back/no-repeat, real-message sound/vibration, call sound interaction, Agent online/airplane-mode fallback, Chinese, 1.3x/2.0x fonts and TalkBack. Device metadata and clips were not recorded.
- [x] No API keys, personal data or real test phone numbers are present in the local source tree.
- [x] Seeded demo data is visibly labelled.
- [x] Public repository and raw README return successfully without authentication.
- [x] The audited Android release commit is preserved by the annotated `catalyst-2026-final` tag; the later `main` commits update documentation only.
- [ ] Repository remains unchanged during judging.

## Documentation and licensing

- [x] README has current setup steps, architecture and the verified demo-video link.
- [ ] Optional static gallery screenshots remain to be uploaded if the Devpost video gallery item is not sufficient.
- [x] Product explanation covers background, process, technical choices and innovation.
- [x] `docs/divergence-log.md` records every material blueprint change.
- [x] Billy Hermawan and Sixth Sense are credited in README and the final Devpost draft. The video itself does not state the credit, so retain it prominently in the submitted text.
- [x] Third-party APIs, libraries, sources, fonts and assets are listed.
- [x] Prototype privacy notice, security statement and threat model disclose the
  implemented controls and residual production risks.
- [x] Supplied PDF/FIG and unlicensed original assets are absent from the tracked repository tree; recheck the final ZIP.

## Video

- [x] YouTube video is 4:52, has captions and is publicly retrievable without login.
- [x] Video narration moves from architecture slides to the real Android build and demonstrates the non-blocking call flow.
- [x] Optional floating warning is not overclaimed in the narration; repository documentation defines its event-only boundary.
- [ ] Video narration does not explicitly call its number/message fixtures seeded demo data. The final Devpost draft now states this; add the same sentence to the YouTube description if time allows.
- [x] Video is publicly viewable without login: `https://youtu.be/GRfb4cCK7PQ`.
- [ ] A local backup exists.

## Devpost

- [x] All five members are listed; three emailed Devpost invitations remain unconfirmed and are not treated as a submission blocker by the team lead.
- [x] Public GitHub URL and video URL work without login.
- [x] Final Devpost draft names the user problem, solution, innovation and limitations and contains no URL placeholders.
- [x] Public Android setup/build instructions are linked from the Devpost draft; the local debug RC APK is ready to attach if the form allows it.
- [x] Curated `sagesense-source-9f1757b.zip` was regenerated from the final link-audit commit, checksum-verified and uploaded to Devpost.
- [ ] Submit internally by Sunday 09:30, 30 minutes before the official deadline.
