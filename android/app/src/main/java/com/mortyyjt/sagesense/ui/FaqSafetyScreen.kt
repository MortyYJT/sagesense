package com.mortyyjt.sagesense.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mortyyjt.sagesense.ui.theme.AtkinsonHyperlegible

private val FaqTitleStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 41.sp,
)

private val FaqHeadingStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = FontWeight.Bold,
    fontSize = 25.sp,
    lineHeight = 32.sp,
)

private val FaqBodyStyle = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontSize = 22.sp,
    lineHeight = 31.sp,
)

private val FaqQuestionStyle = FaqBodyStyle.copy(fontWeight = FontWeight.Bold, fontSize = 23.sp)

private data class FaqCopy(
    val questionEn: String,
    val questionZh: String,
    val answerEn: String,
    val answerZh: String,
)

private val faqItems = listOf(
    FaqCopy(
        questionEn = "Is my information private?",
        questionZh = "我的信息是私密的吗？",
        answerEn = "Risk checks run locally on this phone. SageSense stores structured risk events with known sensitive patterns redacted. Only when the user asks the Agent is a limited, sanitised context sent to the backend.",
        answerZh = "基础风险判断在这台手机上完成。SageSense 保存结构化风险记录，并对已识别的敏感信息模式进行脱敏。只有当用户主动询问 Agent 时，有限的脱敏上下文才会发送到后端。",
    ),
    FaqCopy(
        questionEn = "What can the app see?",
        questionZh = "这个应用可以看到什么？",
        answerEn = "SageSense can check supported message notifications after notification access is granted. Call warnings use the local Watchlist after the call-screening role is granted. It does not read the whole screen.",
        answerZh = "获得通知读取权限后，SageSense 可以检查受支持的消息通知。获得来电筛查角色后，来电预警会使用本地观察名单。它不会读取整个手机屏幕。",
    ),
    FaqCopy(
        questionEn = "Can I turn alerts off?",
        questionZh = "我可以关闭提醒吗？",
        answerEn = "Yes. Permissions remain optional and can be reviewed or disabled in Android Settings. The app can still provide local features such as History and Learn without all protection permissions.",
        answerZh = "可以。所有权限都是可选的，你可以在 Android 系统设置中查看或关闭相关权限。即使没有开启所有防护权限，History 和 Learn 等本地功能仍然可以使用。",
    ),
    FaqCopy(
        questionEn = "How do warnings work?",
        questionZh = "风险警告如何工作？",
        answerEn = "SageSense checks local signals such as urgency, payment requests, credential requests, suspicious links, brand impersonation and Watchlist matches. A warning is evidence to help you pause and check, not proof that someone is committing fraud.",
        answerZh = "SageSense 会检查本地风险信号，例如紧迫措辞、付款请求、身份信息请求、可疑链接、品牌冒充和观察名单匹配。警告是帮助你停下来核实的证据，并不是对某人构成诈骗的定论。",
    ),
)

@Composable
fun FaqSafetyScreen(
    locale: String,
    onLanguage: (String) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = faqL(locale, "Back", "返回"),
                        modifier = Modifier.size(30.dp),
                    )
                }
                Text(
                    faqL(locale, "FAQ & Safety", "常见问题与安全"),
                    style = FaqTitleStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        item {
            Text(
                faqL(locale, "Learn how the app keeps you safe.", "了解 SageSense 如何保护你。"),
                style = FaqBodyStyle,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = locale == "en-AU",
                    onClick = { onLanguage("en-AU") },
                    label = { Text("English", style = FaqBodyStyle) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
                FilterChip(
                    selected = locale == "zh-CN",
                    onClick = { onLanguage("zh-CN") },
                    label = { Text("中文", style = FaqBodyStyle) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EyeShieldMascot(68.dp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("SageSense", style = FaqHeadingStyle)
                            Text(
                                faqL(
                                    locale,
                                    "Your safety companion",
                                    "你的数字安全伙伴",
                                ),
                                style = FaqBodyStyle,
                            )
                        }
                    }
                    SafetyPoint(locale, "Warns about risks", "提醒潜在风险")
                    SafetyPoint(locale, "Explains why something looks unsafe", "解释为什么内容看起来不安全")
                    SafetyPoint(locale, "Lets you choose what to do next", "让你选择下一步怎么做")
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    faqL(locale, "Frequently asked questions", "常见安全问题"),
                    style = FaqHeadingStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        itemsIndexed(faqItems, key = { index, _ -> index }) { _, item ->
            FaqItem(item = item, locale = locale)
        }
    }
}

@Composable
private fun SafetyPoint(locale: String, en: String, zh: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp),
        )
        Text(
            faqL(locale, en, zh),
            style = FaqBodyStyle.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun FaqItem(item: FaqCopy, locale: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "FAQ arrow")
    val question = faqL(locale, item.questionEn, item.questionZh)
    val answer = faqL(locale, item.answerEn, item.answerZh)
    val state = faqL(locale, if (expanded) "Expanded" else "Collapsed", if (expanded) "已展开" else "已收起")

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "$question, $state"
                    }
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(question, style = FaqQuestionStyle, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = state,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).rotate(arrowRotation),
                )
            }
            if (expanded) {
                Text(
                    answer,
                    style = FaqBodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
                )
            }
        }
    }
}

private fun faqL(locale: String, en: String, zh: String): String = if (locale == "zh-CN") zh else en
