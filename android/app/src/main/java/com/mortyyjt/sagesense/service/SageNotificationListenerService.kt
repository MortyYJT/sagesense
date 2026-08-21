package com.mortyyjt.sagesense.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mortyyjt.sagesense.SageSenseApplication
import com.mortyyjt.sagesense.risk.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SageNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        RiskOverlayController.showResting(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (
            sbn == null ||
            AlertNotifier.isSageSenseRiskChannel(sbn.notification.channelId) ||
            sbn.notification.extras.getBoolean(AlertNotifier.RISK_OUTPUT_EXTRA, false)
        ) return
        val allowedPackages = setOf(
            "com.google.android.apps.messaging",
            "com.android.mms",
            packageName,
        )
        if (sbn.packageName !in allowedPackages) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = sequenceOf(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            extras.getCharSequence(Notification.EXTRA_TEXT),
        ).filterNotNull().map(CharSequence::toString).firstOrNull { it.isNotBlank() } ?: return
        if (sbn.packageName == packageName && sbn.notification.channelId != AlertNotifier.DEMO_CHANNEL) return

        val app = application as SageSenseApplication
        serviceScope.launch {
            val event = app.container.riskRepository.analyseAndStore(
                sourceType = "notification",
                sender = title,
                text = text,
                seededDemoData = sbn.notification.channelId == AlertNotifier.DEMO_CHANNEL,
            )
            if (event.riskLevel != RiskLevel.LOW) {
                val locale = app.container.preferences.language.first()
                AlertNotifier.showRisk(this@SageNotificationListenerService, event, locale = locale)
            }
        }
    }
}
