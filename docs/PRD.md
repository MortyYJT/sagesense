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

- Explain notification access, visible-notification permission, call-screening role, local processing, and Agent data transfer before the relevant action.
- Permissions remain optional and can be reviewed later.

### Automatic detection

- Analyse supported message notifications using URL, urgency, payment, credential, OTP, brand, spelling, and Watchlist signals.
- Produce a score from 0–100: low below 30, medium 30–59, high 60 or above.
- Store a redacted event and show a deep-linked alert for medium/high risk.
- Query calls against the local Watchlist within three seconds; never reject, silence, or remove a call from the log.

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
- Use high contrast, large type, 48–56dp targets, TalkBack descriptions, and text as well as colour for risk.

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
