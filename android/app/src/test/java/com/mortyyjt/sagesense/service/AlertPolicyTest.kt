package com.mortyyjt.sagesense.service

import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.risk.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertPolicyTest {
    @Test
    fun selectsDedicatedChannelsForMessageCallAndDemo() {
        assertEquals(AlertChannelKind.MESSAGE_RISK, alertChannelKind(event(sourceType = "notification")))
        assertEquals(AlertChannelKind.CALL_RISK, alertChannelKind(event(sourceType = "call")))
        assertEquals(AlertChannelKind.SEEDED_DEMO, alertChannelKind(event(seeded = true)))
        assertEquals(AlertChannelKind.SEEDED_DEMO, alertChannelKind(event(sourceType = "call", seeded = true)))
    }

    @Test
    fun returnsBilingualCallCopy() {
        val call = event(sourceType = "call")
        assertTrue(alertCopy(call, "en-AU").body.contains("not blocked"))
        assertTrue(alertCopy(call, "zh-CN").body.contains("没有被拦截"))
        assertTrue(alertCopy(event(sourceType = "call", seeded = true), "en-AU").title.startsWith("Demo:"))
    }

    @Test
    fun riskChannelsAreRecognisedButDemoIsAnInputChannel() {
        assertTrue(AlertNotifier.isSageSenseRiskChannel(AlertNotifier.MESSAGE_RISK_CHANNEL))
        assertTrue(AlertNotifier.isSageSenseRiskChannel(AlertNotifier.CALL_RISK_CHANNEL))
        assertTrue(!AlertNotifier.isSageSenseRiskChannel(AlertNotifier.DEMO_CHANNEL))
    }

    @Test
    fun realWarningsRemainInterruptiveAndOnlyDemoIsFullySilent() {
        val message = alertDeliveryPolicy(AlertChannelKind.MESSAGE_RISK)
        val call = alertDeliveryPolicy(AlertChannelKind.CALL_RISK)
        val demo = alertDeliveryPolicy(AlertChannelKind.SEEDED_DEMO)

        assertTrue(message.soundEnabled)
        assertTrue(message.vibrationEnabled)
        assertFalse(message.silent)
        assertFalse(call.soundEnabled)
        assertTrue(call.vibrationEnabled)
        assertFalse(call.silent)
        assertFalse(demo.soundEnabled)
        assertFalse(demo.vibrationEnabled)
        assertTrue(demo.silent)
    }

    @Test
    fun channelLabelsAreBilingual() {
        assertEquals("Message risk warnings", alertChannelCopy(AlertChannelKind.MESSAGE_RISK, "en-AU").name)
        assertEquals("消息风险警告", alertChannelCopy(AlertChannelKind.MESSAGE_RISK, "zh-CN").name)
        assertTrue(alertChannelCopy(AlertChannelKind.CALL_RISK, "zh-CN").description.contains("继续响铃"))
        assertTrue(alertChannelCopy(AlertChannelKind.SEEDED_DEMO, "en-AU").description.contains("demo"))
    }

    private fun event(sourceType: String = "notification", seeded: Boolean = false) = RiskEventEntity(
        id = "test",
        sourceType = sourceType,
        occurredAt = 0L,
        displaySender = "sender",
        senderHash = null,
        redactedSnippet = "example",
        urls = emptyList(),
        domains = emptyList(),
        signalCodes = listOf("URGENCY"),
        riskScore = 70,
        riskLevel = RiskLevel.HIGH,
        relatedCampaignId = null,
        seededDemoData = seeded,
    )
}
