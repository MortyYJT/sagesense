package com.mortyyjt.sagesense.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskEventDao {
    @Query("SELECT * FROM risk_events ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<RiskEventEntity>>

    @Query("SELECT * FROM risk_events WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): RiskEventEntity?

    @Query("SELECT * FROM risk_events ORDER BY occurredAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<RiskEventEntity>

    @Query("SELECT * FROM risk_events WHERE relatedCampaignId = :campaignId AND id != :eventId ORDER BY occurredAt DESC")
    suspend fun related(campaignId: String, eventId: String): List<RiskEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: RiskEventEntity)

    @Query("DELETE FROM risk_events")
    suspend fun deleteAll()

    @Query("DELETE FROM risk_events WHERE occurredAt < :cutoff AND seededDemoData = 0")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY lastSeen DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist")
    suspend fun all(): List<WatchlistEntity>

    @Query("SELECT * FROM watchlist WHERE normalisedValue = :normalised LIMIT 1")
    suspend fun findNormalised(normalised: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteById(id: String)
}
