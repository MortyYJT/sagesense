package com.mortyyjt.sagesense.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionSetupPolicyTest {
    @Test
    fun showsOnceWhenReadyAndOnboardingIsIncomplete() {
        assertTrue(shouldShowPermissionSetupPrompt(ready = true, onboardingComplete = false, promptSeen = false))
    }

    @Test
    fun doesNotShowBeforeReadyOrAfterCompletionOrDismissal() {
        assertFalse(shouldShowPermissionSetupPrompt(ready = false, onboardingComplete = false, promptSeen = false))
        assertFalse(shouldShowPermissionSetupPrompt(ready = true, onboardingComplete = true, promptSeen = false))
        assertFalse(shouldShowPermissionSetupPrompt(ready = true, onboardingComplete = false, promptSeen = true))
    }
}
