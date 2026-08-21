package com.mortyyjt.sagesense.network

import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.data.WatchlistEntity
import com.mortyyjt.sagesense.risk.RiskLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentPrivacyTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun eventJsonContainsEvidenceButNoIdentifiersUrlsOrSecrets() {
        val event = RiskEventEntity(
            id = "event-1",
            sourceType = "message",
            occurredAt = 1_700_000_000_000,
            displaySender = "Alice +61 400 000 999",
            senderHash = "secret-sender-hash",
            redactedSnippet = "验证码：123456，请联系 +61 400 000 999 https://evil.example/login?otp=123456",
            urls = listOf("https://evil.example/login?otp=123456&token=secret-token"),
            domains = listOf("evil.example"),
            signalCodes = listOf("OTP_REQUEST", "SUSPICIOUS_URL"),
            riskScore = 90,
            riskLevel = RiskLevel.HIGH,
            relatedCampaignId = "campaign-7",
        ).toAgentEvent()

        val encoded = json.encodeToString(event)

        assertNull(event.displaySender)
        assertNull(event.senderHash)
        assertTrue(event.urls.isEmpty())
        assertEquals(listOf("evil.example"), event.domains)
        assertEquals(listOf("OTP_REQUEST", "SUSPICIOUS_URL"), event.signalCodes)
        assertEquals(90, event.riskScore)
        assertEquals("campaign-7", event.relatedCampaignId)
        assertFalse(encoded.contains("Alice"))
        assertFalse(encoded.contains("secret-sender-hash"))
        assertFalse(encoded.contains("https://evil.example/login?otp=123456"))
        assertFalse(encoded.contains("secret-token"))
        assertFalse(encoded.contains("123456"))
        assertFalse(encoded.contains("+61 400 000 999"))
        assertTrue(encoded.contains("evil.example"))
        assertTrue(encoded.contains("OTP_REQUEST"))
        assertTrue(encoded.contains("campaign-7"))
    }

    @Test
    fun watchlistMasksPhoneButKeepsDomainEvidence() {
        val phone = WatchlistEntity(
            id = "phone-1",
            value = "+61 400 000 999",
            normalisedValue = "61400000999",
            entityType = "phone",
            reasonEn = "Repeated impersonation pattern",
            reasonZh = "重复冒充套路",
            sourceTitle = "Local fixture",
            sourceUrl = "https://www.scamwatch.gov.au/types-of-scams",
            lastSeen = 1_700_000_000_000,
        ).toAgentWatchlist("en-AU")
        val domain = WatchlistEntity(
            id = "domain-1",
            value = "evil.example",
            normalisedValue = "evil.example",
            entityType = "domain",
            reasonEn = "Suspicious domain",
            reasonZh = "可疑域名",
            sourceTitle = "Local fixture",
            sourceUrl = "https://www.scamwatch.gov.au/types-of-scams",
            lastSeen = 1_700_000_000_000,
        ).toAgentWatchlist("en-AU")

        val encoded = json.encodeToString(
            AgentQueryBody(
                locale = "en-AU",
                message = "Is this a scam?",
                watchlist = listOf(phone, domain),
            ),
        )

        assertFalse(encoded.contains("+61 400 000 999"))
        assertFalse(encoded.contains("61400000999"))
        assertTrue(encoded.contains("[PHONE"))
        assertTrue(encoded.contains("evil.example"))
    }

    @Test
    fun userQuestionIsRedactedBeforeSerialization() {
        val body = AgentQueryBody(
            locale = "en-AU",
            message = AgentPrivacy.redactText("Is验证码 123456 from +61400000999 safe?"),
        )

        val encoded = json.encodeToString(body)

        assertFalse(encoded.contains("123456"))
        assertFalse(encoded.contains("+61400000999"))
        assertTrue(encoded.contains("[PHONE REDACTED]"))
    }
}
