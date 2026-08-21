# Catalyst submission checklist

## Build freeze

- [x] Backend tests pass on Python 3.12 (10 tests, 2026-08-21).
- [x] Production Agent returns `degraded=false` through OpenCode Go without exposing its key.
- [x] Android unit tests, lint and debug APK build pass (2026-08-20).
- [ ] Fresh-install physical-device smoke test passes.
- [x] No API keys, personal data or real test phone numbers are present in the local source tree.
- [x] Seeded demo data is visibly labelled.
- [ ] Public repository is readable while logged out.
- [ ] Final commit is tagged `catalyst-2026-submission`.
- [ ] Repository remains unchanged during judging.

## Documentation and licensing

- [ ] README has current setup steps, screenshots and architecture.
- [ ] Product explanation covers background, process, technical choices and innovation.
- [ ] `docs/divergence-log.md` records every material blueprint change.
- [ ] Billy Hermawan and Sixth Sense are credited in README, pitch and Devpost.
- [ ] Third-party APIs, libraries, sources, fonts and assets are listed.
- [ ] Supplied PDF/FIG and unlicensed original assets are absent from the repository ZIP.

## Video

- [ ] 3–5 minutes, 1080p, intelligible audio and subtitles.
- [ ] Real Android build is shown; no clickable-only mockup is presented as working software.
- [ ] Call and notification fixtures are described as seeded demo data.
- [ ] Video is publicly viewable without login.
- [ ] A local backup exists.

## Devpost

- [ ] Team name and all five members are correct.
- [ ] Public GitHub URL and video URL work without login.
- [ ] Project description names the user problem, solution, innovation and limitations.
- [ ] Setup instructions or downloadable APK are attached.
- [ ] Curated ZIP contains source, docs and required assets only.
- [ ] Submit internally by Sunday 09:30, 30 minutes before the official deadline.
