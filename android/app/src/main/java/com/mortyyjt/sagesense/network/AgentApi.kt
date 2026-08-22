package com.mortyyjt.sagesense.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mortyyjt.sagesense.BuildConfig
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.data.WatchlistEntity
import com.mortyyjt.sagesense.risk.Redactor
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class AgentRiskEvent(
    val id: String,
    @SerialName("source_type") val sourceType: String,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("display_sender") val displaySender: String? = null,
    @SerialName("sender_hash") val senderHash: String? = null,
    @SerialName("redacted_snippet") val redactedSnippet: String,
    val urls: List<String>,
    val domains: List<String>,
    @SerialName("signal_codes") val signalCodes: List<String>,
    @SerialName("risk_score") val riskScore: Int,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("related_campaign_id") val relatedCampaignId: String? = null,
)

@Serializable
data class AgentWatchlistItem(
    val value: String,
    @SerialName("entity_type") val entityType: String,
    val reason: String,
    @SerialName("source_title") val sourceTitle: String,
    @SerialName("source_url") val sourceUrl: String,
    @SerialName("last_seen") val lastSeen: String,
)

@Serializable
data class AgentQueryBody(
    val locale: String,
    val message: String,
    @SerialName("active_event") val activeEvent: AgentRiskEvent? = null,
    @SerialName("recent_events") val recentEvents: List<AgentRiskEvent> = emptyList(),
    val watchlist: List<AgentWatchlistItem> = emptyList(),
)

@Serializable
data class AgentCitation(
    val id: String,
    val title: String,
    val publisher: String,
    val url: String,
)

@Serializable
data class AgentAction(
    val code: String,
    val label: String,
    @SerialName("requires_confirmation") val requiresConfirmation: Boolean,
)

@Serializable
data class AgentAnswer(
    val answer: String,
    @SerialName("risk_level") val riskLevel: String,
    @SerialName("related_event_ids") val relatedEventIds: List<String>,
    @SerialName("suggested_actions") val suggestedActions: List<AgentAction>,
    val citations: List<AgentCitation>,
    val degraded: Boolean,
)

/**
 * The Agent receives evidence, not identifiers or secrets. Keep this boundary
 * next to the wire models so future callers cannot accidentally serialize the
 * Room representation directly.
 */
internal object AgentPrivacy {
    private val url = Regex("(?i)\\b(?:https?://|www\\.)[^\\s<>]+")
    private val phoneNumber = Regex("(?<![\\d])\\+?\\d[\\d\\s().-]{7,}\\d(?![\\d])")
    private val labelledOtp = Regex(
        "(?i)(?:\\botp\\b|\\bone[- ]time (?:code|password)\\b|\\bverification code\\b|\\bsecurity code\\b|验证码|动态码|安全码)\\s*(?:is|:|为|：|-)?\\s*\\d{4,8}\\b",
    )

    fun redactText(value: String): String = value
        .replace(url, "[LINK REDACTED]")
        .let(Redactor::redact)
        .replace(labelledOtp, "[OTP REDACTED]")
        .replace(phoneNumber, "[PHONE REDACTED]")

    fun maskPhone(value: String): String {
        val digits = value.filter(Char::isDigit)
        if (digits.isEmpty()) return "[PHONE REDACTED]"
        val maskedPrefix = "•".repeat((digits.length - 2).coerceAtLeast(0))
        return "[PHONE $maskedPrefix${digits.takeLast(2)}]"
    }
}

interface AgentApi {
    @POST("v1/agent/query")
    suspend fun query(@Body body: AgentQueryBody): AgentAnswer
}

class AgentClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        // Production model responses can exceed 15 seconds during a cold start.
        // Keep a finite ceiling so offline fallback remains predictable while
        // allowing the deployed Agent enough time to return useful guidance.
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.SAGESENSE_API_BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AgentApi::class.java)

    suspend fun ask(
        locale: String,
        message: String,
        activeEvent: RiskEventEntity?,
        recentEvents: List<RiskEventEntity>,
        watchlist: List<WatchlistEntity>,
    ): Result<AgentAnswer> = runCatching {
        api.query(
            AgentQueryBody(
                locale = locale,
                message = AgentPrivacy.redactText(message),
                activeEvent = activeEvent?.toAgentEvent(),
                recentEvents = recentEvents.take(10).map(RiskEventEntity::toAgentEvent),
                watchlist = watchlist.take(20).map { it.toAgentWatchlist(locale) },
            ),
        )
    }
}

internal fun RiskEventEntity.toAgentEvent() = AgentRiskEvent(
    id = id,
    sourceType = sourceType,
    occurredAt = Instant.ofEpochMilli(occurredAt).toString(),
    // Sender labels and hashes are not needed for explanation and are still
    // identifying data. Domains and signal codes below provide the evidence.
    displaySender = null,
    senderHash = null,
    redactedSnippet = AgentPrivacy.redactText(redactedSnippet),
    // Full URLs can contain paths, tokens, or query parameters. The extracted
    // domains remain available as a stable, non-secret evidence signal.
    urls = emptyList(),
    domains = domains,
    signalCodes = signalCodes,
    riskScore = riskScore,
    riskLevel = riskLevel.name.lowercase(),
    relatedCampaignId = relatedCampaignId,
)

internal fun WatchlistEntity.toAgentWatchlist(locale: String) = AgentWatchlistItem(
    value = if (entityType.equals("phone", ignoreCase = true)) AgentPrivacy.maskPhone(value) else value,
    entityType = entityType,
    reason = if (locale == "zh-CN") reasonZh else reasonEn,
    sourceTitle = sourceTitle,
    sourceUrl = sourceUrl,
    lastSeen = Instant.ofEpochMilli(lastSeen).toString(),
)
