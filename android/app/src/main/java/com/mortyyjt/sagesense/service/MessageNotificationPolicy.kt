package com.mortyyjt.sagesense.service

/**
 * Keeps notification access scoped to SMS apps instead of inspecting every
 * notification on the device. The platform-selected default SMS package is
 * accepted so OEM apps such as Samsung Messages work without a hard-coded
 * device-specific build.
 */
internal object MessageNotificationPolicy {
    private val knownSmsPackages = setOf(
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging",
    )

    fun isSupportedPackage(
        sourcePackage: String,
        defaultSmsPackage: String?,
        sageSensePackage: String,
    ): Boolean =
        sourcePackage == sageSensePackage ||
            sourcePackage == defaultSmsPackage?.takeIf(String::isNotBlank) ||
            sourcePackage in knownSmsPackages
}
