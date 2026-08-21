package com.mortyyjt.sagesense.ui

/** Pure decision policy so the first-run permission prompt is easy to verify. */
internal fun shouldShowPermissionSetupPrompt(
    ready: Boolean,
    onboardingComplete: Boolean,
    promptSeen: Boolean,
): Boolean = ready && !onboardingComplete && !promptSeen
