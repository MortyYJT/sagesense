package com.mortyyjt.sagesense.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mortyyjt.sagesense.MainActivity
import com.mortyyjt.sagesense.R
import com.mortyyjt.sagesense.data.RiskEventEntity
import java.util.concurrent.atomic.AtomicInteger

object AlertNotifier {
    // Channel behaviour is immutable after creation. v4 repairs the teammate
    // v3 regression that made real warnings silent on existing installations.
    const val MESSAGE_RISK_CHANNEL = "sagesense_message_risk_v4"
    const val CALL_RISK_CHANNEL = "sagesense_call_risk_v4"
    const val DEMO_CHANNEL = "sagesense_demo_inputs_v2"
    internal const val RISK_OUTPUT_EXTRA = "com.mortyyjt.sagesense.extra.RISK_OUTPUT"
    private val demoSequence = AtomicInteger(0)

    internal fun isSageSenseRiskChannel(channelId: String?): Boolean =
        channelId == MESSAGE_RISK_CHANNEL || channelId == CALL_RISK_CHANNEL

    fun createChannels(context: Context, locale: String = "en-AU") {
        val manager = context.getSystemService(NotificationManager::class.java)
        val messagePolicy = alertDeliveryPolicy(AlertChannelKind.MESSAGE_RISK)
        val messageCopy = alertChannelCopy(AlertChannelKind.MESSAGE_RISK, locale)
        manager.createNotificationChannel(
            NotificationChannel(MESSAGE_RISK_CHANNEL, messageCopy.name, messagePolicy.importance).apply {
                description = messageCopy.description
                enableVibration(messagePolicy.vibrationEnabled)
                vibrationPattern = longArrayOf(0, 250)
            },
        )
        val callPolicy = alertDeliveryPolicy(AlertChannelKind.CALL_RISK)
        val callCopy = alertChannelCopy(AlertChannelKind.CALL_RISK, locale)
        manager.createNotificationChannel(
            NotificationChannel(CALL_RISK_CHANNEL, callCopy.name, callPolicy.importance).apply {
                description = callCopy.description
                enableVibration(callPolicy.vibrationEnabled)
                vibrationPattern = longArrayOf(0, 250)
                setSound(null, null)
            },
        )
        val demoPolicy = alertDeliveryPolicy(AlertChannelKind.SEEDED_DEMO)
        val demoCopy = alertChannelCopy(AlertChannelKind.SEEDED_DEMO, locale)
        manager.createNotificationChannel(
            NotificationChannel(DEMO_CHANNEL, demoCopy.name, demoPolicy.importance).apply {
                description = demoCopy.description
                enableVibration(demoPolicy.vibrationEnabled)
                setSound(null, null)
            },
        )
    }

    fun showRisk(context: Context, event: RiskEventEntity, titleOverride: String? = null, locale: String = "en-AU") {
        RiskOverlayController.showRisk(context, event, locale)
        if (!canPost(context)) return
        val channelKind = alertChannelKind(event)
        val deliveryPolicy = alertDeliveryPolicy(channelKind)
        val channelId = when (channelKind) {
            AlertChannelKind.MESSAGE_RISK -> MESSAGE_RISK_CHANNEL
            AlertChannelKind.CALL_RISK -> CALL_RISK_CHANNEL
            AlertChannelKind.SEEDED_DEMO -> DEMO_CHANNEL
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            data = "sagesense://event/${event.id}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val copy = alertCopy(event, locale)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(titleOverride ?: copy.title)
            .setContentText(copy.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(copy.body))
            .setPriority(deliveryPolicy.compatPriority)
            .setCategory(
                if (channelKind == AlertChannelKind.SEEDED_DEMO) {
                    NotificationCompat.CATEGORY_STATUS
                } else {
                    NotificationCompat.CATEGORY_ALARM
                },
            )
            .setOnlyAlertOnce(true)
            // Call alerts use a channel with no notification sound but one vibration.
            // Seeded demo alerts are the only completely silent risk output.
            .setSilent(deliveryPolicy.silent)
            .addExtras(Bundle().apply { putBoolean(RISK_OUTPUT_EXTRA, true) })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notifyAfterPermissionCheck(context, event.id.hashCode(), notification)
    }

    fun postDemoScam(context: Context, locale: String = "en-AU") {
        if (!canPost(context)) return
        val chinese = locale == "zh-CN"
        val alternate = demoSequence.getAndIncrement() % 2 == 1
        val title = when {
            chinese && alternate -> "预置演示 · 银行账户复核"
            chinese -> "预置演示 · 银行安全中心"
            alternate -> "Seeded demo · Bank Account Review"
            else -> "Seeded demo · CommBank Security"
        }
        val body = when {
            chinese && alternate -> "最后通知：立即登录并提供账户资料，否则退款将取消。"
            chinese -> "紧急：账户即将停用。请使用演示链接验证密码。"
            alternate -> "FINAL WARNING: Sign in and provide account details now or your refund will be cancelled."
            else -> "URGENT: Your account will be suspended. Verify your password using the demo link."
        }
        val expanded = when {
            chinese && alternate -> "预置演示数据——最后通知：立即登录并提供账户资料：https://commbank-account-review.click"
            chinese -> "预置演示数据——紧急：账户即将停用。请立即验证密码：https://commbank-secure-login.example"
            alternate -> "SEEDED DEMO — FINAL WARNING: Sign in and provide account details now: https://commbank-account-review.click"
            else -> "SEEDED DEMO — URGENT: Your account will be suspended. Verify your password now: https://commbank-secure-login.example"
        }
        val notification = NotificationCompat.Builder(context, DEMO_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expanded))
            .setPriority(alertDeliveryPolicy(AlertChannelKind.SEEDED_DEMO).compatPriority)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(true)
            .build()
        notifyAfterPermissionCheck(context, if (alternate) 88002 else 88001, notification)
    }

    @SuppressLint("MissingPermission")
    private fun notifyAfterPermissionCheck(
        context: Context,
        id: Int,
        notification: android.app.Notification,
    ) {
        // Both public entry points return before this method unless runtime permission is granted.
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    private fun canPost(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
