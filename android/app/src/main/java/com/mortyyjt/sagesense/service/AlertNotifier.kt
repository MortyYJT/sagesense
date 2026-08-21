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
import com.mortyyjt.sagesense.risk.RiskLevel

object AlertNotifier {
    const val MESSAGE_RISK_CHANNEL = "sagesense_message_risk_v3"
    const val CALL_RISK_CHANNEL = "sagesense_call_risk_v3"
    const val DEMO_CHANNEL = "sagesense_demo_inputs_v2"
    internal const val RISK_OUTPUT_EXTRA = "com.mortyyjt.sagesense.extra.RISK_OUTPUT"

    internal fun isSageSenseRiskChannel(channelId: String?): Boolean =
        channelId == MESSAGE_RISK_CHANNEL || channelId == CALL_RISK_CHANNEL

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(MESSAGE_RISK_CHANNEL, "Message risk warnings", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Silent status-bar entries for suspicious message notifications"
                enableVibration(false)
                setSound(null, null)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(CALL_RISK_CHANNEL, "Call risk warnings", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Silent status-bar entries for local Risk Watchlist matches"
                enableVibration(false)
                setSound(null, null)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(DEMO_CHANNEL, "Seeded demo messages", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Clearly labelled inputs for the Catalyst demo"
                enableVibration(false)
                setSound(null, null)
            },
        )
    }

    fun showRisk(context: Context, event: RiskEventEntity, titleOverride: String? = null, locale: String = "en-AU") {
        if (event.riskLevel == RiskLevel.LOW) return
        RiskOverlayController.showRisk(context, event)
        if (!canPost(context)) return
        val channelId = when (alertChannelKind(event)) {
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addExtras(Bundle().apply { putBoolean(RISK_OUTPUT_EXTRA, true) })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notifyAfterPermissionCheck(context, event.id.hashCode(), notification)
    }

    fun postDemoScam(context: Context) {
        if (!canPost(context)) return
        val notification = NotificationCompat.Builder(context, DEMO_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Seeded demo · CommBank Security")
            .setContentText("URGENT: Your account will be suspended. Verify your password using the demo link.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "SEEDED DEMO — URGENT: Your account will be suspended. Verify your password now: https://commbank-secure-login.example",
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(true)
            .build()
        notifyAfterPermissionCheck(context, 88001, notification)
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
