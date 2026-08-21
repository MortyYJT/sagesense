package com.mortyyjt.sagesense.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.ui.theme.AtkinsonHyperlegible
import kotlinx.coroutines.delay

private val PauseTitleStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = FontWeight.Bold,
    fontSize = 31.sp,
    lineHeight = 38.sp,
)

private val PauseHeadingStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = FontWeight.Bold,
    fontSize = 23.sp,
    lineHeight = 30.sp,
)

private val PauseBodyStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontSize = 22.sp,
    lineHeight = 31.sp,
)

@Composable
fun CognitivePauseExperience(
    event: RiskEventEntity?,
    locale: String,
    onLanguage: (String) -> Unit,
    onSeeWhy: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val active = event != null
    val indicatorSize by animateDpAsState(if (active) 68.dp else 46.dp, label = "Companion size")
    val indicatorColor by animateColorAsState(
        if (active) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
        label = "Companion colour",
    )
    val hapticFeedback = LocalHapticFeedback.current
    var actionsEnabled by remember(event?.id) { mutableStateOf(false) }

    LaunchedEffect(event?.id) {
        actionsEnabled = false
        if (event != null) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            // Prevent the gesture that triggered the warning from clicking through
            // to an action as the overlay enters the composition.
            delay(600)
            actionsEnabled = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = active,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(2f),
        ) {
            event?.let { riskEvent ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.68f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .padding(20.dp)
                        .semantics {
                            paneTitle = pauseL(locale, "Cognitive Pause", "认知暂停")
                            liveRegion = LiveRegionMode.Assertive
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CognitivePauseCard(
                        event = riskEvent,
                        locale = locale,
                        actionsEnabled = actionsEnabled,
                        onLanguage = onLanguage,
                        onSeeWhy = { onSeeWhy(riskEvent.id) },
                        onDismiss = onDismiss,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 18.dp, end = 14.dp)
                .zIndex(3f)
                .size(indicatorSize)
                .alpha(if (active) 1f else 0.42f)
                .clip(CircleShape)
                .background(indicatorColor)
                .semantics {
                    contentDescription = pauseL(
                        locale,
                        if (active) "SageSense risk warning" else "SageSense companion resting",
                        if (active) "SageSense 风险警告" else "SageSense 伙伴安静守护中",
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (active) Icons.Default.Warning else Icons.Default.Shield,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(indicatorSize * 0.58f),
            )
        }
    }
}

@Composable
private fun CognitivePauseCard(
    event: RiskEventEntity,
    locale: String,
    actionsEnabled: Boolean,
    onLanguage: (String) -> Unit,
    onSeeWhy: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 720.dp)
            .shadow(18.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(68.dp),
            )
            Text(
                pauseL(locale, "Cognitive Pause", "认知暂停"),
                style = PauseHeadingStyle,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                pauseL(locale, "Wait. This may be dangerous.", "等一下，这可能有危险。"),
                style = PauseTitleStyle,
            )

            if (event.seededDemoData) {
                PauseEvidenceRow(
                    pauseL(locale, "Demo simulation · Seeded demo data", "演示模拟 · 预置演示数据"),
                )
            }
            if (event.sourceType == "call") {
                PauseEvidenceRow(pauseL(locale, "Local Watchlist match", "本地观察名单匹配"))
                PauseEvidenceRow(pauseL(locale, "Call not blocked", "电话未被拦截"))
            }

            Text(
                pauseL(
                    locale,
                    "This is a risk warning, not proof of fraud. Review the evidence, pause, and choose what to do next.",
                    "这是风险警告，不是诈骗定论。请先暂停、查看证据，再由你决定下一步。",
                ),
                style = PauseBodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                pauseL(
                    locale,
                    "SageSense does not block calls, make payments or contact organisations for you.",
                    "SageSense 不会自动拦截来电、自动付款或替你联系任何机构。",
                ),
                style = PauseBodyStyle,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = locale == "en-AU",
                    enabled = actionsEnabled,
                    onClick = { onLanguage("en-AU") },
                    label = { Text("English", style = PauseBodyStyle) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
                FilterChip(
                    selected = locale == "zh-CN",
                    enabled = actionsEnabled,
                    onClick = { onLanguage("zh-CN") },
                    label = { Text("中文", style = PauseBodyStyle) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
            }

            Button(
                onClick = onSeeWhy,
                enabled = actionsEnabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            ) {
                Text(pauseL(locale, "See Why", "查看原因"), style = PauseHeadingStyle)
            }
            OutlinedButton(
                onClick = onDismiss,
                enabled = actionsEnabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            ) {
                Text(pauseL(locale, "Not Now", "暂时关闭"), style = PauseHeadingStyle)
            }
        }
    }
}

@Composable
private fun PauseEvidenceRow(text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = PauseBodyStyle.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
    }
}

private fun pauseL(locale: String, en: String, zh: String): String = if (locale == "zh-CN") zh else en
