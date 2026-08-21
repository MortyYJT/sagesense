package com.mortyyjt.sagesense.service

import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.risk.RiskLevel

internal enum class AlertChannelKind {
    MESSAGE_RISK,
    CALL_RISK,
    SEEDED_DEMO,
}

internal fun alertChannelKind(event: RiskEventEntity): AlertChannelKind = when {
    event.sourceType == "call" -> AlertChannelKind.CALL_RISK
    event.seededDemoData -> AlertChannelKind.SEEDED_DEMO
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
