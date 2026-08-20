package com.mortyyjt.sagesense.risk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskAnalyzerTest {
    private val analyzer = RiskAnalyzer()

    private data class Case(val name: String, val sender: String?, val text: String, val expected: RiskLevel)

    private val scamCases = listOf(
        Case("bank-link-en", "CommBank", "URGENT: verify your password now at https://commbank-secure-login.example", RiskLevel.HIGH),
        Case("otp-en", "Security", "Final warning. Send your OTP and account number at https://secure-help.click", RiskLevel.HIGH),
        Case("crypto-en", "Investment team", "Act fast and transfer bitcoin now at https://profit-now.top", RiskLevel.HIGH),
        Case("gift-card-en", "ATO refund", "Urgent fee due. Pay with a gift card at https://ato-refund.example", RiskLevel.HIGH),
        Case("mygov-en", "myGov", "Your login is suspended. Confirm password immediately: https://mygov-login.xyz", RiskLevel.HIGH),
        Case("bank-link-zh", "澳洲联邦银行", "紧急：账户将被停用，请立即登录并填写密码 https://commbank-secure-login.example", RiskLevel.HIGH),
        Case("otp-zh", "安全中心", "最后通知：请马上提供验证码和账户信息 https://secure-help.click", RiskLevel.HIGH),
        Case("crypto-zh", "投资顾问", "限时机会，请立即转账比特币 https://profit-now.top", RiskLevel.HIGH),
        Case("refund-zh", "退款中心", "紧急退款，请支付手续费并填写银行卡资料 https://refund-now.example", RiskLevel.HIGH),
        Case("government-zh", "myGov", "账户冻结，请立即登录并提供个人资料 https://mygov-help.xyz", RiskLevel.HIGH),
    )

    private val benignCases = listOf(
        Case("family-en", "Mum", "Dinner is at six. See you soon.", RiskLevel.LOW),
        Case("bank-app-en", "CommBank", "Your monthly statement is ready in the CommBank app.", RiskLevel.LOW),
        Case("doctor-en", "Clinic", "Reminder: your appointment is Tuesday at 10 am.", RiskLevel.LOW),
        Case("parcel-en", "Australia Post", "Your parcel was delivered to the front door.", RiskLevel.LOW),
        Case("library-en", "Library", "Your reserved book is ready for collection.", RiskLevel.LOW),
        Case("family-zh", "女儿", "今晚六点回家吃饭，不用着急。", RiskLevel.LOW),
        Case("clinic-zh", "诊所", "提醒：您的预约是周二上午十点。", RiskLevel.LOW),
        Case("parcel-zh", "邮局", "您的包裹已经放在前门。", RiskLevel.LOW),
        Case("community-zh", "社区中心", "本周五下午有免费书法活动。", RiskLevel.LOW),
        Case("weather-zh", "天气提醒", "明天可能下雨，出门请带伞。", RiskLevel.LOW),
    )

    @Test
    fun scamFixturesReachExpectedRiskLevel() {
        scamCases.forEach { case ->
            assertEquals(case.name, case.expected, analyzer.analyse(case.text, case.sender).level)
        }
    }

    @Test
    fun benignFixturesStayLowRisk() {
        benignCases.forEach { case ->
            val result = analyzer.analyse(case.text, case.sender)
            assertEquals(case.name, case.expected, result.level)
            assertTrue(case.name, result.score < 30)
        }
    }

    @Test
    fun watchlistMatchCreatesHighRiskWarning() {
        val result = analyzer.analyse(
            text = "Incoming call",
            sender = "+61 400 000 999",
            watchlist = setOf("+61400000999"),
        )

        assertEquals(RiskLevel.HIGH, result.level)
        assertTrue("WATCHLIST_MATCH" in result.signals)
    }

    @Test
    fun repeatedTemplateHasStableCampaignFingerprint() {
        val first = analyzer.analyse("Urgent: verify account 1234 at https://bank-help.example")
        val second = analyzer.analyse("Urgent: verify account 9876 at https://bank-help.example")

        assertEquals(first.campaignId, second.campaignId)
    }

    @Test
    fun versionedWeightsLoadFromJson() {
        val weights = RiskWeights.fromJson(
            """{"version":2,"thresholds":{"medium":25,"high":70},"weights":{"URGENCY":22}}""",
        )

        assertEquals(2, weights.version)
        assertEquals(25, weights.mediumThreshold)
        assertEquals(70, weights.highThreshold)
        assertEquals(22, weights.values["URGENCY"])
        assertEquals(60, weights.values["WATCHLIST_MATCH"])
    }
}
