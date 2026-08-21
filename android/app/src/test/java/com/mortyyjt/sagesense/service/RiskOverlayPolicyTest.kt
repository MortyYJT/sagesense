package com.mortyyjt.sagesense.service

import com.mortyyjt.sagesense.risk.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskOverlayPolicyTest {
    @Test
    fun onlyMediumHighAndExplicitPreviewAreVisible() {
        assertNull(riskOverlayPresentation(RiskLevel.LOW, "en-AU"))
        assertNull(riskOverlayPresentation(null, "zh-CN"))
        assertEquals(RiskOverlayKind.MEDIUM, riskOverlayPresentation(RiskLevel.MEDIUM, "en-AU")?.kind)
        assertEquals(RiskOverlayKind.HIGH, riskOverlayPresentation(RiskLevel.HIGH, "en-AU")?.kind)
        assertEquals(RiskOverlayKind.PREVIEW, riskOverlayPresentation(null, "en-AU", preview = true)?.kind)
    }

    @Test
    fun copyIsBilingualAndWarningsAreTransient() {
        val english = requireNotNull(riskOverlayPresentation(RiskLevel.HIGH, "en-AU"))
        val chinese = requireNotNull(riskOverlayPresentation(RiskLevel.HIGH, "zh-CN"))
        assertTrue(english.contentDescription.contains("high-risk"))
        assertTrue(chinese.contentDescription.contains("高风险"))
        assertEquals(20_000, english.visibleMillis)
        assertTrue(english.sizeDp >= 56)
    }
}
