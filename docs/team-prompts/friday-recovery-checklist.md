# Friday recovery checklist

Snapshot: Friday, 21 August 2026, 15:15 AEST.

## Verified now

- [x] Backend unit tests: 10 passed.
- [x] OpenCode Go key works with `deepseek-v4-flash`.
- [x] Vercel Production and Preview contain a Sensitive `OPENCODE_API_KEY`.
- [x] `https://sagesense.vercel.app/v1/agent/query` returned `degraded=false`
  with safe actions and allowlisted citations.
- [x] Android emulator `emulator-5554` is authorised.
- [ ] Physical Android phone is connected and authorised.
- [ ] Jiahui's UI branch or draft PR is visible remotely.

## P0 — complete before ending Friday

- [ ] **Jiahui:** finish core-screen UI/accessibility pass; push branch and draft
  PR with English/Chinese screenshots.
- [ ] **Yijia:** prepare permission acceptance matrix; after Jiahui's merge,
  implement the one-time first-launch explanation and sequential permission flow.
- [ ] **Yu:** review and merge without overwriting either branch; build with
  `-PSAGESENSE_API_BASE_URL=https://sagesense.vercel.app/`.
- [ ] **Yu + Yijia:** fresh-install on a real phone and verify allow, deny, back,
  Settings recovery and no-repeat states.
- [ ] **Yu + Yijia:** verify seeded notification -> warning -> risk detail ->
  History -> online cited Agent answer.
- [ ] **Yu + Yijia:** verify offline/timeout leaves local detection and History
  usable.
- [ ] **Yu + Yijia:** verify Watchlist call keeps ringing while warning appears.
- [ ] **Xiuning:** save the first clean real-phone clips and update the retake
  list. Never show real personal notifications or credentials.
- [ ] Update `docs/test-report.md` only with evidence actually observed.

## P1 — finish before Saturday 15:00 feature freeze

- [ ] TalkBack pass and font scales 1.0, 1.3 and 2.0 in both languages.
- [ ] Important text at least 22sp, touch targets 56dp, risk never shown by
  colour alone, and supported Latin text uses licensed Atkinson Hyperlegible.
- [ ] Search/filter History, Watchlist provenance, seeded labels and Personal
  Scam Memory are visible and understandable on-device.
- [ ] Clear-history and 30-day pruning behaviour are verified.
- [ ] Agent loading, cancellation/timeout, retry, degraded response and citation
  opening states are understandable to an older user.
- [ ] Release/demo build uses HTTPS only; remove or scope broad cleartext traffic
  before the final APK.
- [ ] Put a provider spend cap or Vercel path rate limit in place, monitor usage,
  and rotate the OpenCode Go key after judging.
- [ ] README screenshots, architecture, setup, provider notice, APK hash and
  limitations are current.

## P2 — submission and evidence

- [ ] 4:30 video exported at 1080p with subtitles and original-designer credit.
- [ ] YouTube Unlisted video, repository, APK and citations work while logged out.
- [ ] Clean source ZIP excludes `.env`, local SDK/JDK, supplied FIG/PDF, caches,
  test phone data and raw videos.
- [ ] Devpost explains problem, innovation, architecture, privacy, limitations,
  seeded data and what changed from Sixth Sense.
- [ ] Fresh-install release-candidate smoke test passes Sunday morning.
- [ ] Tag the reviewed commit `catalyst-2026-submission` and submit internally by
  09:30 Sunday.

## Merge order

1. Backend/provider configuration and this task board.
2. Jiahui UI branch.
3. Yijia permission branch based on the merged UI.
4. Yu's integration/test-evidence fixes.
5. Documentation/video finalisation and release tag.

Junteng Hu has no current task. Do not wait for his branch or review.
