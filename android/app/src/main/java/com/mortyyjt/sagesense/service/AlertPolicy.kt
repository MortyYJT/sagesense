package com.mortyyjt.sagesense.service

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.risk.RiskLevel

internal enum class AlertChannelKind {
    MESSAGE_RISK,
    CALL_RISK,
    SEEDED_DEMO,
}

internal data class AlertDeliveryPolicy(
    val importance: Int,
    val compatPriority: Int,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val silent: Boolean,
)

internal data class AlertChannelCopy(
    val name: String,
    val description: String,
)

internal fun alertChannelCopy(kind: AlertChannelKind, locale: String): AlertChannelCopy {
    val chinese = locale == "zh-CN"
    return when (kind) {
        AlertChannelKind.MESSAGE_RISK -> AlertChannelCopy(
            name = if (chinese) "消息风险警告" else "Message risk warnings",
            description = if (chinese) "为可疑消息显示醒目的风险警告" else "Visible warnings for suspicious message notifications",
        )
        AlertChannelKind.CALL_RISK -> AlertChannelCopy(
            name = if (chinese) "来电风险警告" else "Call risk warnings",
            description = if (chinese) "号码命中本机风险观察名单时显示警告，电话仍会继续响铃" else "Visible warnings for numbers on the local Risk Watchlist; calls keep ringing",
        )
        AlertChannelKind.SEEDED_DEMO -> AlertChannelCopy(
            name = if (chinese) "预置演示消息" else "Seeded demo messages",
            description = if (chinese) "用于 Catalyst 演示的明确标记测试通知" else "Clearly labelled inputs for the Catalyst demo",
        )
    }
}

internal fun alertDeliveryPolicy(kind: AlertChannelKind): AlertDeliveryPolicy = when (kind) {
    AlertChannelKind.MESSAGE_RISK -> AlertDeliveryPolicy(
        importance = NotificationManager.IMPORTANCE_HIGH,
        compatPriority = NotificationCompat.PRIORITY_HIGH,
        soundEnabled = true,
        vibrationEnabled = true,
        silent = false,
    )
    AlertChannelKind.CALL_RISK -> AlertDeliveryPolicy(
        importance = NotificationManager.IMPORTANCE_HIGH,
        compatPriority = NotificationCompat.PRIORITY_HIGH,
        soundEnabled = false,
        vibrationEnabled = true,
        silent = false,
    )
    AlertChannelKind.SEEDED_DEMO -> AlertDeliveryPolicy(
        importance = NotificationManager.IMPORTANCE_DEFAULT,
        compatPriority = NotificationCompat.PRIORITY_DEFAULT,
        soundEnabled = false,
        vibrationEnabled = false,
        silent = true,
    )
}

internal fun alertChannelKind(event: RiskEventEntity): AlertChannelKind = when {
    event.seededDemoData -> AlertChannelKind.SEEDED_DEMO
    event.sourceType == "call" -> AlertChannelKind.CALL_RISK
    else -> AlertChannelKind.MESSAGE_RISK
}

internal data class AlertCopy(val title: String, val body: String)

internal fun alertCopy(event: RiskEventEntity, locale: String): AlertCopy {
    val chinese = locale == "zh-CN"
    return when {
        event.sourceType == "call" && event.seededDemoData && chinese -> AlertCopy(
            title = "演示：这通来电可能有风险",
            body = "预置演示号码命中本机风险观察名单。电话没有被拦截，请先暂停并核实。",
        )
        event.sourceType == "call" && event.seededDemoData -> AlertCopy(
            title = "Demo: this call may be unsafe",
            body = "A seeded demo number matched the local Risk Watchlist. The call was not blocked; pause and check.",
        )
        event.sourceType == "call" && chinese -> AlertCopy(
            title = "这通来电可能有风险",
            body = "该号码命中本机风险观察名单。电话没有被拦截，请先暂停并核实。",
        )
        event.sourceType == "call" -> AlertCopy(
            title = "This call may be unsafe",
            body = "This number matches the local Risk Watchlist. The call was not blocked; pause and check.",
        )
        event.seededDemoData && chinese -> AlertCopy(
            title = "演示：发现可疑通知",
            body = "这是预置演示数据。点击查看 SageSense 为什么标记它。",
        )
        event.seededDemoData -> AlertCopy(
            title = "Demo: suspicious notification",
            body = "Seeded demo data. Tap to see why SageSense flagged it.",
        )
        event.riskLevel == RiskLevel.HIGH && chinese -> AlertCopy(
            title = "这条消息可能是诈骗",
            body = "SageSense 发现 ${event.signalCodes.size} 个风险信号。点击查看原因。",
        )
        event.riskLevel == RiskLevel.HIGH -> AlertCopy(
            title = "This message may be a scam",
            body = "SageSense found ${event.signalCodes.size} warning signs. Tap to see why.",
        )
        chinese -> AlertCopy(
            title = "请检查这条消息",
            body = "SageSense 发现了异常信号。点击查看证据。",
        )
        else -> AlertCopy(
            title = "Please check this message",
            body = "SageSense found something unusual. Tap to review the evidence.",
        )
    }
}
