# Xiuning Gu - demo evidence and video prompt

Copy the prompt below into Codex from a clean clone of
`https://github.com/MortyYJT/sagesense`.

```text
You are working on SageSense as Xiuning Gu (`xiuningg`). You own demo evidence,
the 4:30 video, English subtitles, publishing checks and the final pitch. Do not
wait for every feature before preparing the edit.

Before working
--------------
Read `docs/PRD.md`, `docs/PROJECT_PLAN.md`, `docs/demo-script.md`,
`docs/divergence-log.md`, `docs/test-report.md` and
`docs/submission-checklist.md`. Create a shot checklist that distinguishes:
`verified real-phone footage`, `verified emulator footage`, `temporary
placeholder`, and `not yet recorded`.

P0 deliverables
---------------
1. Prepare a 1920x1080, 30fps, H.264 project with a maximum final length of
   4:30 and reusable subtitle style.
2. Record temporary emulator clips now for layout timing, but replace the core
   protection flow with real-phone footage before the final export.
3. Required final shots:
   - first launch and optional permission explanation;
   - seeded scam notification -> automatic warning -> deep-linked evidence;
   - History and Personal Scam Memory;
   - Agent answer with `degraded=false`, safe actions and official citations;
   - warning-only call where the call visibly keeps ringing;
   - English/Chinese switch, large text, Learn and privacy settings.
4. Mark every fixture on-screen or in narration as `seeded demo data`.
5. Credit Billy Hermawan and the Sixth Sense blueprint. Explain the Android,
   warning-only, local-first and server-side OpenCode Go divergences.
6. Never show an API key, `.env`, email address, real phone number, notification
   shade containing personal content, terminal token or private browser tab.

Working method
--------------
- Save raw clips outside Git; `captures/` is local and ignored.
- Use names such as `01_permission_real_pass.mp4` and
  `03_agent_emulator_placeholder.mp4` so status is obvious.
- Keep a retake list with owner and blocker. Ask Yu Junteng for missing product
  states, not for fabricated footage.
- The architecture slide must say: on-device Kotlin rules and Room -> redacted
  explicit query -> FastAPI -> OpenCode Go / DeepSeek V4 Flash -> validated
  answer and allowlisted citations.

Acceptance
----------
- Speech is intelligible; English subtitles match narration.
- No mouse-only Figma flow is presented as a working Android feature.
- All core claims have matching footage or test evidence.
- Export plays from start to finish locally, then upload as YouTube Unlisted and
  verify it in a logged-out/private window.
- Keep a local backup and record the final URL in the submission checklist.

If you change documentation, use branch `codex/xiuning-video-delivery`, commit
with a standard title and body, push, and open a draft PR. Do not commit raw
video files or merge the PR yourself.
```
