package com.mortyyjt.sagesense.service

import org.junit.Assert.assertEquals
import org.junit.Test

class DemoReadinessPolicyTest {
    @Test
    fun missingPermissionsAlwaysProduceAnActionInsteadOfSilentFailure() {
        assertEquals(DemoLaunchAction.REQUEST_POSTING_PERMISSION, demoLaunchAction(false, false))
        assertEquals(DemoLaunchAction.REQUEST_NOTIFICATION_ACCESS, demoLaunchAction(false, true))
        assertEquals(DemoLaunchAction.POST_DEMO, demoLaunchAction(true, true))
    }
}
