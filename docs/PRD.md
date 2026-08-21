# SageSense PRD

## Product statement

SageSense helps an older Android user notice, understand, and safely respond to suspicious digital contact without requiring them to interpret technical security language.

## Primary user and problem

The primary persona is an older adult who uses calls and messaging but may hesitate when a message looks official or urgent. Existing protection often blocks silently, uses dense security language, or expects the user to search independently. SageSense combines automatic, explainable local detection with education at the moment of risk.

## Success criteria

- A fresh install can demonstrate notification → local warning → risk detail → history → cited Agent answer on a physical Android device.
- A Watchlist caller continues ringing while a visible warning is displayed.
- Every warning names observable evidence and clearly states that it is not proof.
- Core detection works offline and no secret is embedded in the APK.
- English and Simplified Chinese cover every primary screen and Agent request.

## Functional requirements

### Onboarding and permissions

- On a fresh installation, show the protection setup prompt automatically once
  after the first screen is ready. Persist `permission_setup_prompt_seen`
  immediately when it is shown so closing, skipping, denying, or restarting
  does not cause repeated automatic prompts.
- Keep manual re-entry from onboarding and Settings so the user can review or
  enable protection later. Existing installations that have completed
  onboarding must not receive an unexpected new prompt.
- Explain notification access, visible-notification permission, call-screening role, local processing, and Agent data transfer before the relevant action.
- Notification access, visible-notification permission, and the call-screening role (when available) are optional and can be reviewed or disabled in Android Settings.
- If access is declined or revoked, only the related protection shows OFF; local features such as History and Learn remain available.
- If Android blocks a repeated call-screening request, provide a recovery path to Default apps with clear instructions to select Caller ID & spam app and then SageSense.

### Automatic detection

- Analyse supported message notifications using URL, urgency, payment, credential, OTP, brand, spelling, and Watchlist signals.
- Produce a score from 0–100: low below 30, medium 30–59, high 60 or above.
- Store a redacted event and show a deep-linked alert for medium/high risk.
- Query calls against the local Watchlist within three seconds; never reject, silence, or remove a call from the log.

### Cognitive Pause warning

- Use separate alert behaviour for real messages, Watchlist calls, and seeded
  demo events. Real-message warnings are high-priority with one audible and
  vibrating alert; call warnings are visible with one vibration while the call
  continues ringing; seeded demos are silent and clearly labelled.
- Keep one authoritative in-app Cognitive Pause layer for a newly observed
  medium/high event. Do not use an always-on overlay, AccessibilityService,
  or full-screen intent.
- Use a dimmed background, a high-contrast bilingual card, one short haptic, and clear `See Why` / `Not Now` actions; do not queue repeated overlays while one is visible.
- State that the warning is evidence, not a fraud verdict, and that SageSense does not block calls, make payments or contact organisations.
- For Watchlist calls, state `Local Watchlist match` and `Call not blocked`; for seeded scenarios, label the card `Demo simulation · Seeded demo data`.
- Keep the hackathon implementation inside the Compose app. Do not request `SYSTEM_ALERT_WINDOW` or describe the simulation as a cross-app system overlay.

### History and Personal Scam Memory

- Display, search, and risk-filter events.
- Show the local Watchlist with provenance and seeded-demo labels.
- Relate events sharing a campaign fingerprint, domain, or multiple signal codes.
- Retain non-demo events for 30 days and support delete-all.

### Agent

- Accept a question plus one selected event, at most 10 recent events, and 20 Watchlist summaries.
- Use only read-only tools and at most three tool rounds.
- Return plain-language explanation, risk level, related event IDs, confirmation-required actions, and allowlisted citations.
- Fall back to deterministic bilingual guidance on missing key, timeout, tool failure, invalid JSON, or validation failure.

### Learn and accessibility

- Present short bilingual lessons linking to official sources.
- Follow the design baseline of low information density, all app-defined
  visible text styles at least 22sp, and touch targets at least 56dp. Use Atkinson
  Hyperlegible globally for supported Latin glyphs with Android system CJK
  fallback, plus TalkBack descriptions and text as well as colour for risk.
  At 1.3× and 2.0× system font scale, use an adaptive two-by-two bottom
  navigation layout to prevent label clipping.

## Non-goals

iOS, full SMS replacement, AccessibilityService screen reading, automatic blocking, voice/deepfake detection, cloud accounts, cloud history, analytics, and an always-on overlay are outside the hackathon MVP.

## Data model

`RiskEvent`: ID, source type, time, display sender, sender hash, redacted snippet, URLs/domains, signal codes, score, level, campaign ID, seeded-demo flag.

`WatchlistEntry`: ID, raw and normalised value, entity type, bilingual reason, source title/URL, last seen, seeded-demo flag.

## Safety constraints

- Do not describe a warning as a fraud verdict.
- Do not claim a payment or contact is safe.
- Do not request passwords, OTPs, card details, or identity documents.
- Do not automatically call, message, block, delete, or report.
- Display source provenance for Watchlist and factual Agent guidance.
