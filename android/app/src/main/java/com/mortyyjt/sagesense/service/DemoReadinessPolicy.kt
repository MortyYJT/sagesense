package com.mortyyjt.sagesense.service

internal enum class DemoLaunchAction {
    POST_DEMO,
    REQUEST_NOTIFICATION_ACCESS,
    REQUEST_POSTING_PERMISSION,
}

internal fun demoLaunchAction(
    notificationAccess: Boolean,
    notificationPosting: Boolean,
): DemoLaunchAction = when {
    !notificationPosting -> DemoLaunchAction.REQUEST_POSTING_PERMISSION
    !notificationAccess -> DemoLaunchAction.REQUEST_NOTIFICATION_ACCESS
    else -> DemoLaunchAction.POST_DEMO
}
