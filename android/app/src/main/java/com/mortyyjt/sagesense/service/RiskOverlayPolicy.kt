package com.mortyyjt.sagesense.service

import com.mortyyjt.sagesense.risk.RiskLevel

internal enum class RiskOverlayKind {
    PREVIEW,
    MEDIUM,
    HIGH,
}

internal data class RiskOverlayPresentation(
    val kind: RiskOverlayKind,
    val sizeDp: Int,
    val visibleMillis: Long,
    val contentDescription: String,
    val windowTitle: String,
)

internal fun riskOverlayPresentation(
    riskLevel: RiskLevel?,
    locale: String,
    preview: Boolean = false,
): RiskOverlayPresentation? {
    val chinese = locale == "zh-CN"
    if (preview) {
        return RiskOverlayPresentation(
            kind = RiskOverlayKind.PREVIEW,
            sizeDp = 64,
            visibleMillis = 8_000,
            contentDescription = if (chinese) {
                "SageSense 悬浮风险警告预览。轻点返回应用。"
            } else {
                "SageSense floating risk-warning preview. Tap to return to the app."
            },
            windowTitle = if (chinese) "SageSense 风险警告预览" else "SageSense risk-warning preview",
        )
    }
    return when (riskLevel) {
        RiskLevel.MEDIUM -> RiskOverlayPresentation(
            kind = RiskOverlayKind.MEDIUM,
            sizeDp = 72,
            visibleMillis = 20_000,
            contentDescription = if (chinese) {
                "SageSense 发现中风险内容。轻点暂停并查看证据。"
            } else {
                "SageSense found medium-risk content. Tap to pause and review the evidence."
            },
            windowTitle = if (chinese) "SageSense 中风险警告" else "SageSense medium-risk warning",
        )
        RiskLevel.HIGH -> RiskOverlayPresentation(
            kind = RiskOverlayKind.HIGH,
            sizeDp = 80,
            visibleMillis = 20_000,
            contentDescription = if (chinese) {
                "SageSense 发现高风险内容。轻点暂停并查看证据。"
            } else {
                "SageSense found high-risk content. Tap to pause and review the evidence."
            },
            windowTitle = if (chinese) "SageSense 高风险警告" else "SageSense high-risk warning",
        )
        RiskLevel.LOW, null -> null
    }
}
