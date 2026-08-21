# SageSense security

**Prototype security statement — last updated 22 August 2026.**

SageSense is a hackathon prototype, not a production-certified anti-fraud or
security service. The current artefact is an installable debug release
candidate; physical-device acceptance, formal penetration testing, legal
review, release signing, and store review remain pending. Do not deploy this
build for sensitive personal, financial, or business data.

## 中文摘要

这是黑客松原型，不是经过生产认证的安全产品。当前 APK 是 debug RC，不能
当作正式发布版本。发现漏洞时，请优先使用 GitHub Security Advisory 私下
报告；不要在公开 issue 中放置个人资料、密钥或可利用细节。Room 本地数据
未加密，服务端限流是单进程 best-effort，正式部署还需要 Vercel WAF。

## Reporting a vulnerability

Please report security issues privately and include:

- a short description and impact;
- the affected component, commit/build, endpoint, or permission;
- minimal reproduction steps or a harmless proof of concept;
- whether any personal data, API key, or user action was exposed; and
- a safe contact route for follow-up.

Use the repository's **Security → Advisories → Report a vulnerability** flow,
if GitHub enables private advisories for the repository:
<https://github.com/MortyYJT/sagesense/security/advisories/new>. If that flow is
unavailable, contact the maintainers through the repository without posting
exploit code or sensitive data publicly and ask for a private channel. Do not
test against another person's phone, the production endpoint, or a provider
account without explicit authorisation.

We will acknowledge receipt when a private channel is available, assess the
impact, and coordinate a fix or disclosure timeline. This is a volunteer
prototype and does not promise a response time, bounty, or legal safe harbour.

## Security controls in the prototype

- Detection and risk scoring run on-device. Persistence is redacted and keeps
  sender labels, URL origin/domain, signal codes, score, and timestamps rather
  than message bodies or full links.
- An Agent request requires an explicit user query. The client re-sanitises its
  bounded context at the network boundary. The backend does not persist the
  request body and has no user database or cloud history.
- Release/main Android networking requires HTTPS. Cleartext HTTP is enabled
  only in the debug manifest for an explicitly configured local backend. The
  provider API key is server-side and is never embedded in the APK.
- Pydantic bounds cap question and nested event/watchlist data. A deterministic
  anti-scam topic gate rejects off-topic and prompt-extraction requests before
  a provider call. Agent tools are read-only, model output is schema-checked,
  citations are allowlisted, and suggested actions require confirmation.
- The FastAPI response uses `no-store`, no-referrer, `nosniff`, frame and
  permissions headers; malformed validation errors do not echo submitted
  content. Application logging records error type/status rather than request
  bodies.
- A best-effort in-memory limiter allows at most 8 requests per minute and 2
  concurrent requests per client process. Production must add a durable
  multi-instance Vercel WAF rule and abuse monitoring; the client cannot be
  trusted to identify itself.
- Notification access, call screening, and overlay access are optional system
  controls. The overlay is event-only and does not read or capture the screen.
- The ADB temporary caller fixture is debug-only and protected by Android's
  signature-level `DUMP` permission. The release merged manifest excludes it.
- Non-demo local history is pruned after 30 days and users can delete it all.

## Known limitations and residual risk

These are deliberate disclosures, not claims that the controls make the
prototype production-ready:

1. **Unencrypted local Room.** A compromised, rooted, unlocked, or debugged
   device may expose the remaining structured history. Redaction reduces
   impact but is heuristic; it does not prevent forensic recovery.
2. **No authentication or device attestation.** A public backend caller can
   submit well-formed requests. The per-process limiter resets on restart and
   does not coordinate across Vercel instances. A WAF, durable quota, identity
   boundary, monitoring, and cost controls are still required.
3. **Provider and hosting boundary.** The backend does not save request bodies,
   but Vercel, OpenCode Go, DeepSeek, network infrastructure, and operational
   logs may process metadata or transient content under their own terms. We do
   not control provider retention or incident response.
4. **Debug is not release.** The debug APK permits local cleartext development
   and includes a QA hook. It is not signed, hardened, or suitable for a store
   or real-user rollout. Physical-device behaviour is still unverified.
5. **Detection and model error.** Local heuristics can miss scams or produce
   false alarms. A curated citation or Agent answer can be stale, incomplete,
   or wrong. Prompt gating and output validation do not remove model or source
   risk.
6. **Transport and endpoint exposure.** HTTPS protects the intended release
   path, but there is no replay-resistant user session or end-to-end identity
   binding. Debug local HTTP is intentionally a developer-only exception.
7. **Platform and legal review.** Android OEM behaviour, notification/call
   permissions, `SYSTEM_ALERT_WINDOW`, accessibility expectations, data
   protection obligations, and provider terms require review before commercial
   use.

See [PRIVACY.md](PRIVACY.md), the [threat model](docs/threat-model.md), and the
[test report](docs/test-report.md) for the data boundary and evidence limits.

## Security non-goals

This prototype does not promise malware detection, complete scam coverage,
screen reading, OCR, call blocking, account takeover prevention, secure
storage against a compromised device, anonymous network access, provider-side
deletion, or production availability/SLOs. It is a warning and education aid;
users must independently verify contacts and payments through official channels.
