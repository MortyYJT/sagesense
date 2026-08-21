# Source manifest

Accessed 2026-08-20. SageSense stores original bilingual summaries of these sources in `knowledge/cards.json`.

| ID | Publisher | Source | Claims supported |
|---|---|---|---|
| `scamwatch-methods` | National Anti-Scam Centre / Scamwatch | <https://www.scamwatch.gov.au/stop-check-protect/help-to-spot-and-avoid-scams/methods-scammers-use> | Impersonation, urgency and emotional pressure are recurring tactics |
| `scamwatch-types` | National Anti-Scam Centre / Scamwatch | <https://www.scamwatch.gov.au/types-of-scams> | SMS, phone, email, phishing and other scam categories |
| `acsc-spotting-scams` | ASD Australian Cyber Security Centre | <https://www.cyber.gov.au/protect-yourself/spotting-scams> | Avoid unexpected links; independently open official sites and verify contact details |
| `acsc-recover-scams` | ASD Australian Cyber Security Centre | <https://www.cyber.gov.au/report-and-recover/recover-from/scams> | Contact banks promptly, stop contact, preserve information, and report |
| `ftc-older-adults-imposters` | US Federal Trade Commission | <https://www.ftc.gov/news-events/news/press-releases/2025/08/ftc-data-show-more-four-fold-increase-reports-impersonation-scammers-stealing-tens-even-hundreds> | Impersonators invent urgent account, crime or computer problems and demand transfers |
| `ftc-older-adult-principles` | US Federal Trade Commission | <https://consumer.ftc.gov/system/files/consumer_ftc_gov/pdf/Guiding%20Principles%20to%20Help%20Older%20Adults%20Spot%20Fraud.pdf> | Unexpected contact, urgency and specific payment methods are warning signs |
| `ic3-elder-fraud` | FBI Internet Crime Complaint Center | <https://www.ic3.gov/CrimeInfo/ElderFraud> | Common elder-fraud schemes and official reporting resources |

Additional background references from the original Sixth Sense deck should be listed in the final Devpost bibliography if used in the pitch. Factual claims not supported by this manifest must be removed or cited before submission.

## Platform and release references

| Publisher | Source | Claims supported |
|---|---|---|
| Android Developers | <https://developer.android.com/reference/android/Manifest.permission> | `SYSTEM_ALERT_WINDOW` is special access intended for a narrow class of cross-app interactions and requires explicit user approval |
| Android Developers | <https://developer.android.com/develop/ui/compose/notifications/channels> | Notification-channel importance and alert behaviour are user-visible and cannot be freely changed after a channel is created |
| Google Play Console Help | <https://support.google.com/googleplay/android-developer/answer/9214102?hl=en> | A future Play release may require permission declarations and policy review |
