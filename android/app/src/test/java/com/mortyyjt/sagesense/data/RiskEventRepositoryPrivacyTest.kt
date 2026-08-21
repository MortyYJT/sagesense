package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.RiskAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class RiskEventRepositoryPrivacyTest {
    @Test
    fun minimisesSenderAndUrlBeforeRoomBoundary() = runTest {
        val eventDao = CapturingRiskEventDao()
        val repository = RiskEventRepository(eventDao, EmptyWatchlistDao(), RiskAnalyzer())

        repository.analyseAndStore(
            sourceType = "notification",
            sender = "+61 400 000 999",
            text = "Urgent: open https://evil.example/login?otp=481516&token=private now",
        )

        val stored = requireNotNull(eventDao.stored)
        assertEquals("[PHONE REDACTED]", stored.displaySender)
        assertNull(stored.senderHash)
        assertEquals(listOf("https://evil.example"), stored.urls)
        assertEquals("Urgent: open [LINK ORIGIN: https://evil.example] now", stored.redactedSnippet)
        assertFalse(stored.urls.single().contains("481516"))
        assertFalse(stored.urls.single().contains("private"))
    }

    @Test
    fun rewritesLegacyRowsOnceAndIsIdempotent() = runTest {
        val eventDao = CapturingRiskEventDao()
        val repository = RiskEventRepository(eventDao, EmptyWatchlistDao(), RiskAnalyzer())
        val legacy = RiskEventEntity(
            id = "legacy-event",
            sourceType = "notification",
            occurredAt = 1234L,
            displaySender = "+61 400 000 999",
            senderHash = "legacy-stable-hash",
            redactedSnippet = "Password: hunter2 at https://evil.example/login?token=private",
            urls = listOf("https://evil.example/login?token=private"),
            domains = listOf("evil.example"),
            signalCodes = listOf("SUSPICIOUS_URL", "CREDENTIAL_REQUEST"),
            riskScore = 50,
            riskLevel = com.mortyyjt.sagesense.risk.RiskLevel.MEDIUM,
            relatedCampaignId = "campaign-id",
            seededDemoData = false,
        )
        eventDao.upsert(legacy)

        assertEquals(1, repository.minimiseExistingHistory())
        val migrated = requireNotNull(eventDao.findById(legacy.id))
        assertEquals("[PHONE REDACTED]", migrated.displaySender)
        assertNull(migrated.senderHash)
        assertEquals("Password: [REDACTED] at [LINK ORIGIN: https://evil.example]", migrated.redactedSnippet)
        assertEquals(listOf("https://evil.example"), migrated.urls)
        assertEquals(legacy.occurredAt, migrated.occurredAt)
        assertEquals(legacy.signalCodes, migrated.signalCodes)
        assertEquals(0, repository.minimiseExistingHistory())
        assertEquals(migrated, eventDao.findById(legacy.id))
    }

    private class CapturingRiskEventDao : RiskEventDao {
        private val rows = linkedMapOf<String, RiskEventEntity>()
        val stored: RiskEventEntity? get() = rows.values.lastOrNull()

        override fun observeAll(): Flow<List<RiskEventEntity>> = flowOf(rows.values.toList())
        override suspend fun findById(id: String): RiskEventEntity? = rows[id]
        override suspend fun recent(limit: Int): List<RiskEventEntity> = rows.values.reversed().take(limit)
        override suspend fun allForPrivacyMigration(): List<RiskEventEntity> = rows.values.toList()
        override suspend fun related(campaignId: String, eventId: String): List<RiskEventEntity> = emptyList()
        override suspend fun upsert(event: RiskEventEntity) {
            rows[event.id] = event
        }
        override suspend fun upsertAll(events: List<RiskEventEntity>) {
            events.forEach { rows[it.id] = it }
        }
        override suspend fun deleteAll() {
            rows.clear()
        }
        override suspend fun deleteOlderThan(cutoff: Long) = Unit
    }

    private class EmptyWatchlistDao : WatchlistDao {
        override fun observeAll(): Flow<List<WatchlistEntity>> = flowOf(emptyList())
        override suspend fun all(): List<WatchlistEntity> = emptyList()
        override suspend fun findNormalised(normalised: String): WatchlistEntity? = null
        override suspend fun upsertAll(items: List<WatchlistEntity>) = Unit
        override suspend fun deleteById(id: String) = Unit
    }
}
