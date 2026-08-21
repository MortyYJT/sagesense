package com.mortyyjt.sagesense.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RetentionPolicyTest {
    @Test
    fun calculatesThirtyDayAndImmediateCutoffs() {
        val now = 3_000_000_000L
        assertEquals(now, RetentionPolicy.cutoffMillis(now, 0))
        assertEquals(now - 30L * 24L * 60L * 60L * 1_000L, RetentionPolicy.cutoffMillis(now, 30))
    }

    @Test
    fun rejectsNegativeRetention() {
        assertThrows(IllegalArgumentException::class.java) {
            RetentionPolicy.cutoffMillis(nowMillis = 0L, retentionDays = -1)
        }
    }
}
