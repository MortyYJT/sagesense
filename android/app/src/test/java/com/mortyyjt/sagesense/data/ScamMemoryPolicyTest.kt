package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamMemoryPolicyTest {
    @Test
    fun changedDomainWithSameScamPatternIsLinked() {
        val first = event(
            id = "first",
            domain = "commbank-login.example",
            signals = listOf("URGENCY", "CREDENTIAL_REQUEST", "BRAND_IMPERSONATION"),
            campaign = "campaign-a",
        )
        val changedDomain = event(
            id = "second",
            domain = "commbank-secure.click",
            signals = listOf("URGENCY", "CREDENTIAL_REQUEST", "BRAND_IMPERSONATION"),
            campaign = "campaign-b",
        )

        assertTrue(ScamMemoryPolicy.areRelated(first, changedDomain))
        assertEquals(listOf(changedDomain), ScamMemoryPolicy.relatedCandidates(first, listOf(first, changedDomain)))
    }

    @Test
    fun unrelatedAndLowRiskEventsAreNotLinked() {
        val current = event(
            id = "current",
            domain = "commbank-login.example",
            signals = listOf("URGENCY", "CREDENTIAL_REQUEST"),
            campaign = "campaign-a",
        )
        val unrelated = event(
            id = "unrelated",
            domain = "parcel-tracking.example",
            signals = listOf("URGENCY"),
            campaign = "campaign-b",
        )
        val lowRisk = event(
            id = "low",
            domain = "commbank-login.example",
            signals = listOf("URGENCY", "CREDENTIAL_REQUEST"),
            campaign = "campaign-a",
            level = RiskLevel.LOW,
        )

        assertFalse(ScamMemoryPolicy.areRelated(current, unrelated))
        assertFalse(ScamMemoryPolicy.areRelated(current, lowRisk))
        assertEquals(emptyList<RiskEventEntity>(), ScamMemoryPolicy.relatedCandidates(current, listOf(current, unrelated, lowRisk)))
    }

    @Test
    fun oneSharedSignalIsNotEnoughWithoutCampaignOrDomain() {
        val left = event("left", "one.example", listOf("URGENCY", "PAYMENT_REQUEST"), "a")
        val right = event("right", "two.example", listOf("URGENCY", "OTP_REQUEST"), "b")

        assertFalse(ScamMemoryPolicy.areRelated(left, right))
    }

    private fun event(
        id: String,
        domain: String,
        signals: List<String>,
        campaign: String?,
        level: RiskLevel = RiskLevel.HIGH,
    ) = RiskEventEntity(
        id = id,
        sourceType = "notification",
        occurredAt = 0L,
        displaySender = "rotating sender",
        senderHash = null,
        redactedSnippet = "redacted scam text",
        urls = listOf("https://$domain/login"),
        domains = listOf(domain),
        signalCodes = signals,
        riskScore = if (level == RiskLevel.LOW) 0 else 80,
        riskLevel = level,
        relatedCampaignId = campaign,
    )
}
