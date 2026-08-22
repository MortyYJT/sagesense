package com.mortyyjt.sagesense.service

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mortyyjt.sagesense.SageSenseApplication
import com.mortyyjt.sagesense.risk.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SageNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val deduplicator = NotificationDeduplicator()

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (
            sbn == null ||
            AlertNotifier.isSageSenseRiskChannel(sbn.notification.channelId) ||
            sbn.notification.extras.getBoolean(AlertNotifier.RISK_OUTPUT_EXTRA, false)
        ) return
        if (
            !MessageNotificationPolicy.isSupportedPackage(
                sourcePackage = sbn.packageName,
                defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this),
                sageSensePackage = packageName,
            )
        ) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = sequenceOf(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            extras.getCharSequence(Notification.EXTRA_TEXT),
        ).filterNotNull().map(CharSequence::toString).firstOrNull { it.isNotBlank() } ?: return
        if (sbn.packageName == packageName && sbn.notification.channelId != AlertNotifier.DEMO_CHANNEL) return
        if (!deduplicator.shouldProcess(sbn.key, title, text)) return

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
