package com.mortyyjt.sagesense.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDeduplicatorTest {
    @Test
    fun suppressesRapidIdenticalUpdatesButAllowsChangedContentAndExpiry() {
        val deduplicator = NotificationDeduplicator(ttlMillis = 1_000)
        assertTrue(deduplicator.shouldProcess("message-1", "Bank", "Check this", nowMillis = 1_000))
        assertFalse(deduplicator.shouldProcess("message-1", "Bank", "Check this", nowMillis = 1_500))
        assertTrue(deduplicator.shouldProcess("message-1", "Bank", "Updated text", nowMillis = 1_600))
        assertTrue(deduplicator.shouldProcess("message-1", "Bank", "Check this", nowMillis = 2_100))
    }
}
