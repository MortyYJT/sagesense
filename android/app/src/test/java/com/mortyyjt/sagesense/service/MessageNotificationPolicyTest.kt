package com.mortyyjt.sagesense.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageNotificationPolicyTest {
    @Test
    fun `accepts the platform default SMS app`() {
        assertTrue(
            MessageNotificationPolicy.isSupportedPackage(
                sourcePackage = "com.vendor.messaging",
                defaultSmsPackage = "com.vendor.messaging",
                sageSensePackage = "com.mortyyjt.sagesense",
            ),
        )
    }

    @Test
    fun `accepts known Google AOSP and Samsung SMS apps`() {
        listOf(
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
        ).forEach { sourcePackage ->
            assertTrue(
                MessageNotificationPolicy.isSupportedPackage(
                    sourcePackage = sourcePackage,
                    defaultSmsPackage = null,
                    sageSensePackage = "com.mortyyjt.sagesense",
                ),
            )
        }
    }

    @Test
    fun `accepts SageSense seeded demo notifications`() {
        assertTrue(
            MessageNotificationPolicy.isSupportedPackage(
                sourcePackage = "com.mortyyjt.sagesense",
                defaultSmsPackage = null,
                sageSensePackage = "com.mortyyjt.sagesense",
            ),
        )
    }

    @Test
    fun `rejects unrelated notification apps`() {
        assertFalse(
            MessageNotificationPolicy.isSupportedPackage(
                sourcePackage = "com.example.social",
                defaultSmsPackage = "com.vendor.messaging",
                sageSensePackage = "com.mortyyjt.sagesense",
            ),
        )
    }

    @Test
    fun `ignores a blank default package`() {
        assertFalse(
            MessageNotificationPolicy.isSupportedPackage(
                sourcePackage = "com.vendor.messaging",
                defaultSmsPackage = "",
                sageSensePackage = "com.mortyyjt.sagesense",
            ),
        )
    }
}
