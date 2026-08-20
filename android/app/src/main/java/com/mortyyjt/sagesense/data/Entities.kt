package com.mortyyjt.sagesense.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mortyyjt.sagesense.risk.RiskLevel
import kotlinx.serialization.Serializable

@Entity(tableName = "risk_events")
data class RiskEventEntity(
    @PrimaryKey val id: String,
    val sourceType: String,
    val occurredAt: Long,
    val displaySender: String?,
    val senderHash: String?,
    val redactedSnippet: String,
    val urls: List<String>,
    val domains: List<String>,
    val signalCodes: List<String>,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val relatedCampaignId: String?,
    val seededDemoData: Boolean = false,
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val value: String,
    val normalisedValue: String,
    val entityType: String,
    val reasonEn: String,
    val reasonZh: String,
    val sourceTitle: String,
    val sourceUrl: String,
    val lastSeen: Long,
    val seededDemoData: Boolean = false,
)

@Serializable
data class LearnCard(
    val id: String,
    val titleEn: String,
    val titleZh: String,
    val summaryEn: String,
    val summaryZh: String,
    val sourceTitle: String,
    val sourceUrl: String,
)
