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

    private class CapturingRiskEventDao : RiskEventDao {
        var stored: RiskEventEntity? = null

        override fun observeAll(): Flow<List<RiskEventEntity>> = flowOf(emptyList())
        override suspend fun findById(id: String): RiskEventEntity? = stored?.takeIf { it.id == id }
        override suspend fun recent(limit: Int): List<RiskEventEntity> = listOfNotNull(stored).take(limit)
        override suspend fun related(campaignId: String, eventId: String): List<RiskEventEntity> = emptyList()
        override suspend fun upsert(event: RiskEventEntity) {
            stored = event
        }
        override suspend fun deleteAll() {
            stored = null
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
