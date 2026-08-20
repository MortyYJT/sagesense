package com.mortyyjt.sagesense.data

import androidx.room.TypeConverter
import com.mortyyjt.sagesense.risk.RiskLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json

    @TypeConverter
    fun stringsToJson(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun jsonToStrings(value: String): List<String> = runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

    @TypeConverter
    fun riskLevelToString(value: RiskLevel): String = value.name

    @TypeConverter
    fun stringToRiskLevel(value: String): RiskLevel = runCatching { RiskLevel.valueOf(value) }.getOrDefault(RiskLevel.LOW)
}
