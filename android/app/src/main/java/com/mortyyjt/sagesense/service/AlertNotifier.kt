package com.mortyyjt.sagesense.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.mortyyjt.sagesense.MainActivity
import com.mortyyjt.sagesense.R
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.risk.RiskLevel

object AlertNotifier {
    const val ALERT_CHANNEL = "sagesense_risk_alerts"
    const val DEMO_CHANNEL = "sagesense_demo_inputs"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ALERT_CHANNEL, "Risk warnings", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Silent status-bar entry for suspicious messages and calls"
                enableVibration(false)
                setSound(null, null)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(DEMO_CHANNEL, "Seeded demo messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Clearly labelled inputs for the Catalyst demo"
                enableVibration(false)
                setSound(null, null)
            },
        )
    }

    fun showRisk(context: Context, event: RiskEventEntity, titleOverride: String? = null) {
        if (!canPost(context)) return
        val isHigh = event.riskLevel == RiskLevel.HIGH
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
        val title = titleOverride ?: if (isHigh) "This may be a scam" else "Please check this message"
        val body = when {
            event.sourceType == "call" -> "This caller appears on your local Risk Watchlist. The call was not blocked."
            isHigh -> "SageSense found ${event.signalCodes.size} warning signs. Tap to see why."
            else -> "SageSense found something unusual. Tap to review the evidence."
        }
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notifyAfterPermissionCheck(context, event.id.hashCode(), notification)
    }

    fun postDemoScam(context: Context) {
        if (!canPost(context)) return
        val notification = NotificationCompat.Builder(context, DEMO_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("CommBank Security")
            .setContentText("URGENT: Your account will be suspended. Verify your password now: https://commbank-secure-login.example")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "URGENT: Your account will be suspended. Verify your password now: https://commbank-secure-login.example",
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
