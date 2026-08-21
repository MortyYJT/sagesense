package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.RiskAnalyzer
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class RiskEventRepository(
    private val eventDao: RiskEventDao,
    private val watchlistDao: WatchlistDao,
    private val analyzer: RiskAnalyzer,
) {
    val events: Flow<List<RiskEventEntity>> = eventDao.observeAll()
    val watchlist: Flow<List<WatchlistEntity>> = watchlistDao.observeAll()

    suspend fun analyseAndStore(
        sourceType: String,
        sender: String?,
        text: String,
        seededDemoData: Boolean = false,
    ): RiskEventEntity {
        val watchlistValues = watchlistDao.all().map { it.normalisedValue }.toSet()
        val analysis = analyzer.analyse(text, sender, watchlistValues)
        val event = RiskEventEntity(
            id = UUID.randomUUID().toString(),
            sourceType = sourceType,
            occurredAt = System.currentTimeMillis(),
            displaySender = EventPersistencePrivacy.sanitiseDisplaySender(sender),
            // A stable sender identifier is unnecessary for the current local
            // memory policy and would make cross-event tracking possible.
            senderHash = null,
            redactedSnippet = EventPersistencePrivacy.sanitiseSnippetForStorage(analysis.redactedText),
            urls = EventPersistencePrivacy.sanitiseUrlsForStorage(analysis.urls),
            domains = analysis.domains,
            signalCodes = analysis.signals,
            riskScore = analysis.score,
            riskLevel = analysis.level,
            relatedCampaignId = analysis.campaignId,
            seededDemoData = seededDemoData,
        )
        eventDao.upsert(event)
        return event
    }

    suspend fun find(id: String): RiskEventEntity? = eventDao.findById(id)
    suspend fun recent(limit: Int = 10): List<RiskEventEntity> = eventDao.recent(limit)
    suspend fun related(event: RiskEventEntity): List<RiskEventEntity> =
        ScamMemoryPolicy.relatedCandidates(event, eventDao.recent(RELATED_EVENT_SCAN_LIMIT))

    suspend fun clearHistory() = eventDao.deleteAll()

    suspend fun prune(retentionDays: Int = 30) {
        val cutoff = RetentionPolicy.cutoffMillis(System.currentTimeMillis(), retentionDays)
        eventDao.deleteOlderThan(cutoff)
    }

    /**
     * Rewrites rows created by pre-hardening builds through the current
     * persistence privacy boundary. The operation is idempotent and does not
     * alter risk evidence, timestamps, or the seeded-demo label.
     */
    suspend fun minimiseExistingHistory(): Int {
        val updates = eventDao.allForPrivacyMigration().mapNotNull { event ->
            val safe = event.copy(
                displaySender = EventPersistencePrivacy.sanitiseDisplaySender(event.displaySender),
                senderHash = null,
                redactedSnippet = EventPersistencePrivacy.sanitiseSnippetForStorage(event.redactedSnippet),
                urls = EventPersistencePrivacy.sanitiseUrlsForStorage(event.urls),
            )
            safe.takeIf { it != event }
        }
        if (updates.isNotEmpty()) eventDao.upsertAll(updates)
        return updates.size
    }

    suspend fun seedDemoData() {
        val watchlist = listOf(
            WatchlistEntity(
                id = "demo-phone-1",
                value = "+61 400 000 999",
                normalisedValue = RiskAnalyzer.normaliseEntity("+61 400 000 999"),
                entityType = "phone",
                reasonEn = "Seeded demonstration number linked to an impersonation scenario.",
                reasonZh = "用于演示冒充诈骗场景的测试号码。",
                sourceTitle = "SageSense seeded demo fixture",
                sourceUrl = "https://www.scamwatch.gov.au/types-of-scams",
                lastSeen = System.currentTimeMillis(),
                seededDemoData = true,
            ),
            WatchlistEntity(
                id = "demo-domain-1",
                value = "commbank-secure-login.example",
                normalisedValue = RiskAnalyzer.normaliseEntity("commbank-secure-login.example"),
                entityType = "domain",
                reasonEn = "Reserved example domain used in the bank impersonation demo.",
                reasonZh = "银行冒充演示中使用的保留示例域名。",
                sourceTitle = "SageSense seeded demo fixture",
                sourceUrl = "https://www.scamwatch.gov.au/stop-check-protect/help-to-spot-and-avoid-scams/methods-scammers-use",
                lastSeen = System.currentTimeMillis(),
                seededDemoData = true,
            ),
        )
        watchlistDao.upsertAll(watchlist)
    }

    private companion object {
        // Keep relationship lookup bounded while covering the recent history shown to users.
        const val RELATED_EVENT_SCAN_LIMIT = 100
    }
}
