package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.RiskLevel
import java.util.Locale

/**
 * Conservative, local-only policy for grouping repeated scam patterns.
 *
 * This deliberately does not infer relationships from sender names: scammers
 * can rotate phone numbers and domains. A relationship requires a medium/high
 * event on both sides and one of the stable indicators below.
 */
internal object ScamMemoryPolicy {
    private val meaningfulSignals = setOf(
        "URGENCY",
        "PAYMENT_REQUEST",
        "CREDENTIAL_REQUEST",
        "OTP_REQUEST",
        "SUSPICIOUS_URL",
        "MISSPELLED_DOMAIN",
        "BRAND_IMPERSONATION",
        "WATCHLIST_MATCH",
    )

    fun relatedCandidates(
        event: RiskEventEntity,
        candidates: Iterable<RiskEventEntity>,
    ): List<RiskEventEntity> = candidates
        .filter { candidate -> candidate.id != event.id && areRelated(event, candidate) }

    fun areRelated(left: RiskEventEntity, right: RiskEventEntity): Boolean {
        if (left.id == right.id || !left.riskLevel.isScamMemoryEligible() || !right.riskLevel.isScamMemoryEligible()) {
            return false
        }

        val sameCampaign = left.relatedCampaignId
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.equals(right.relatedCampaignId?.trim(), ignoreCase = true) == true
        if (sameCampaign) return true

        val sharedDomain = left.domains
            .asSequence()
            .map(::normaliseDomain)
            .filter(String::isNotEmpty)
            .toSet()
            .intersect(
                right.domains.asSequence()
                    .map(::normaliseDomain)
                    .filter(String::isNotEmpty)
                    .toSet(),
            )
            .isNotEmpty()
        if (sharedDomain) return true

        val sharedSignals = left.signalCodes
            .asSequence()
            .map { it.trim().uppercase(Locale.ROOT) }
            .filter(meaningfulSignals::contains)
            .toSet()
            .intersect(
                right.signalCodes.asSequence()
                    .map { it.trim().uppercase(Locale.ROOT) }
                    .filter(meaningfulSignals::contains)
                    .toSet(),
            )
        return sharedSignals.size >= 2
    }

    private fun RiskLevel.isScamMemoryEligible(): Boolean = this == RiskLevel.MEDIUM || this == RiskLevel.HIGH

    private fun normaliseDomain(value: String): String {
        val withoutScheme = value.trim().lowercase(Locale.ROOT)
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
        return withoutScheme
            .substringBefore('/')
            .substringBefore(':')
            .removePrefix("www.")
    }
}
