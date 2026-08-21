package com.mortyyjt.sagesense.network

import org.junit.Assert.assertEquals
import org.junit.Test

class AgentFailureTest {
    @Test
    fun rateLimitUsesRetryAfterSeconds() {
        assertEquals(
            AgentFailure.RateLimited(retryAfterSeconds = 12),
            mapAgentFailure(statusCode = 429, retryAfterHeader = " 12 "),
        )
        assertEquals(
            "请求太频繁，请等待约 12 秒后再试；本地风险检测仍可用",
            agentFailureMessage(AgentFailure.RateLimited(12), locale = "zh-CN"),
        )
    }

    @Test
    fun invalidRetryAfterUsesDefaultAndCapsLargeValues() {
        assertEquals(
            AgentFailure.RateLimited(retryAfterSeconds = 30),
            mapAgentFailure(statusCode = 429, retryAfterHeader = "not-a-number"),
        )
        assertEquals(
            AgentFailure.RateLimited(retryAfterSeconds = 30),
            mapAgentFailure(statusCode = 429, retryAfterHeader = "0"),
        )
        assertEquals(
            AgentFailure.RateLimited(retryAfterSeconds = 300),
            mapAgentFailure(statusCode = 429, retryAfterHeader = "999999"),
        )
    }

    @Test
    fun ordinaryHttpFailureKeepsUnavailableMessage() {
        assertEquals(
            AgentFailure.Unavailable,
            mapAgentFailure(statusCode = 503, retryAfterHeader = "10"),
        )
        assertEquals(
            "The advisor could not be reached. Local risk detection is still working. Please try again.",
            agentFailureMessage(AgentFailure.Unavailable, locale = "en-AU"),
        )
    }
}
