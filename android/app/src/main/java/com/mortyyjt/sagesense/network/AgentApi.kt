package com.mortyyjt.sagesense.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mortyyjt.sagesense.BuildConfig
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.data.WatchlistEntity
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

interface AgentApi {
    @POST("v1/agent/query")
    suspend fun query(@Body body: AgentQueryBody): AgentAnswer
}

class AgentClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
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
                message = message,
                activeEvent = activeEvent?.toAgentEvent(),
                recentEvents = recentEvents.take(10).map(RiskEventEntity::toAgentEvent),
                watchlist = watchlist.take(20).map { it.toAgentWatchlist(locale) },
            ),
        )
    }
}

private fun RiskEventEntity.toAgentEvent() = AgentRiskEvent(
    id = id,
    sourceType = sourceType,
    occurredAt = Instant.ofEpochMilli(occurredAt).toString(),
    displaySender = displaySender,
    senderHash = senderHash,
    redactedSnippet = redactedSnippet,
    urls = urls,
    domains = domains,
    signalCodes = signalCodes,
    riskScore = riskScore,
    riskLevel = riskLevel.name.lowercase(),
    relatedCampaignId = relatedCampaignId,
)

private fun WatchlistEntity.toAgentWatchlist(locale: String) = AgentWatchlistItem(
    value = value,
    entityType = entityType,
    reason = if (locale == "zh-CN") reasonZh else reasonEn,
    sourceTitle = sourceTitle,
    sourceUrl = sourceUrl,
    lastSeen = Instant.ofEpochMilli(lastSeen).toString(),
)
