# Product readiness audit

Last updated: 2026-08-22 (Australia/Melbourne).

SageSense is a feature-complete, installable competition release candidate. It
demonstrates a credible end-to-end safety product, but it is not represented as
a production-certified anti-fraud service. The evidence boundary is deliberate:
automated and Android-emulator acceptance pass, and the team lead reported the
core physical-device smoke test passing. Submission media remain incomplete.

## Current product completeness

| Capability | Current state | Evidence boundary |
|---|---|---|
| Local message risk detection | Implemented | Emulator path passed; real SMS warning and alert sensation user-confirmed on phone |
| Watchlist call warning | Implemented; never blocks the call | Emulator verifies `RINGING`; phone sound interaction user-confirmed |
| Cognitive Pause and optional transient overlay | Implemented | Grant, revoke, preview, auto-hide and event deep link passed on emulator |
| History, search, filters and delete-all | Implemented | Emulator plus JVM policy tests pass |
| Watchlist and source provenance | Implemented with labelled fixtures | Seeded entries are not claimed as live threat intelligence |
| Personal Scam Memory | Implemented conservatively | Requires a stable campaign/domain or at least two meaningful signals |
| Constrained Agent | Implemented | Production query passed; offline deterministic fallback passed |
| Bilingual and older-adult UI | Implemented | English/Chinese, 1.3x/2.0x and TalkBack user-confirmed on phone |
| Privacy controls | Implemented for prototype | Minimized Room writes and legacy-row migration, backend re-sanitisation, HTTPS release policy, bounded Agent context, public notice/threat model and no cloud history |
| Release build | Debug RC accepted for competition | Debug prototype; not Play-signed or production-certified |

## Must finish to maximise competition score

These are release and evidence tasks, not reasons to add another feature before
the code freeze.

1. Capture phone model, Android version and short clips for the already accepted
   physical-device story where useful to the video; retain the exact evidence
   boundary in `test-report.md`.
2. Capture a concise research-and-iteration story: original Sixth Sense blueprint,
   older-adult accessibility decisions, failed/changed ideas, privacy trade-offs,
   and one or two pieces of real tester feedback with resulting changes.
3. Record the 4:30 demo from a real phone. Clearly label seeded data, show that a
   call keeps ringing, and distinguish deterministic local detection from the
   explanatory Agent.
4. Add final screenshots and the architecture graphic to README, finish subtitles,
   Devpost copy and downloadable APK/setup instructions.
5. Verify repository, video, APK and source ZIP in a logged-out browser and keep
   the tagged source unchanged during judging.

## Physical release test points

### P0 — blocks the core demonstration

- Fresh install can allow, deny or back out of every permission without a loop.
- A supported message received with the app foregrounded and backgrounded creates
  exactly one event, a warning and a working deep link.
- A Watchlist call continues ringing while the warning remains visible.
- The production Agent answers; airplane mode retains local evidence and safe
  fallback guidance.
- Delete-all removes events without removing seeded Watchlist fixtures.

### P1 — blocks a credible accessible demonstration

- Real message alert is audible/vibrating, call adds no second notification sound,
  and seeded demo remains silent.
- Overlay grant/revoke/tap/auto-hide works on the chosen recording phone.
- Chinese, 1.3x/2.0x fonts and TalkBack have no clipped core actions or misleading
  labels; reading order reaches risk, evidence and actions coherently.
- Repeated notification updates do not create a visible event storm.

### P2 — record as known limitations rather than rush before freeze

- OEM-specific behaviour outside the recording phone.
- Play Store review of call-screening and draw-over-other-apps declarations.
- Long-duration soak, battery and memory profiling.

## What a production commercial product still needs

1. **Threat intelligence:** authenticated, versioned and reviewable number/domain
   feeds, expiry and appeal handling, provenance SLAs, regional coverage and an
   emergency rollback path. Current Watchlist rows are labelled fixtures.
2. **Measured detection quality:** a consented representative corpus, precision,
   recall, false-positive/false-negative targets, threshold calibration, IDN and
   Unicode adversarial cases, and continuous regression evaluation.
3. **User research:** moderated testing with older adults and carers, accessibility
   specialists, comprehension/time-to-safe-action metrics, and documented design
   iterations. Emulator acceptance is not user validation.
4. **Platform compliance:** Google Play policy review for notification access,
   call-screening and `SYSTEM_ALERT_WINDOW`; OEM/device matrix testing; release
   signing, staged rollout and rollback.
5. **Security and privacy:** turn the current prototype threat model/privacy notice
   into a legally reviewed data-protection assessment and production policy;
   add penetration testing, dependency/SBOM scanning, encrypted local storage,
   secret rotation, abuse monitoring and retention verification.
6. **Reliable backend controls:** durable distributed rate limiting/WAF, service
   authentication or attestation, observability without sensitive payloads,
   SLOs, alerting, provider failover, cost ceilings and incident response.
7. **Production operations:** crash/ANR monitoring with consent, CI/CD release
   gates, signed artifacts, support/escalation flows, localisation review and a
   maintained content/update process.

These items should be presented as the post-hackathon roadmap. Adding RAG, a
vector database, arbitrary web browsing, OCR or an always-on floating assistant
would not substitute for these safety and product-validation requirements.
