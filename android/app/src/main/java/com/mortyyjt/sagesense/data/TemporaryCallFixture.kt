package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.RiskAnalyzer
import java.security.MessageDigest

/**
 * Builds a clearly labelled, local-only caller fixture for physical debug QA.
 *
 * No raw number is used in the entity ID or display value. The canonical number
 * remains only in Room's normalised lookup field so CallScreeningService can
 * exercise the same production lookup path as a seeded Watchlist match.
 */
object TemporaryCallFixture {
    private const val ID_PREFIX = "debug-call-fixture-"
    private val ALLOWED_PHONE_CHARACTERS = Regex("^[+()\\- .0-9]+$")

    fun create(rawPhone: String, nowMillis: Long = System.currentTimeMillis()): WatchlistEntity? {
        val trimmed = rawPhone.trim()
        if (!ALLOWED_PHONE_CHARACTERS.matches(trimmed)) return null
        val digits = trimmed.filter(Char::isDigit)
        if (digits.length !in 8..15) return null

        val normalised = RiskAnalyzer.normaliseEntity(trimmed)
        if (!normalised.startsWith("phone:")) return null
        return WatchlistEntity(
            id = idFor(normalised),
            value = "••• ••• ${digits.takeLast(4)}",
            normalisedValue = normalised,
            entityType = "phone",
            reasonEn = "Temporary local caller used only to verify the physical-device warning flow; not a fraud report.",
            reasonZh = "仅用于验证真机来电警告流程的本地临时号码，并非诈骗举报。",
            sourceTitle = "SageSense temporary device-test fixture",
            sourceUrl = "https://www.scamwatch.gov.au/types-of-scams",
            lastSeen = nowMillis,
            seededDemoData = true,
        )
    }

    fun idForRawPhone(rawPhone: String): String? = create(rawPhone, nowMillis = 0L)?.id

    fun isTemporaryId(id: String): Boolean = id.startsWith(ID_PREFIX)

    private fun idFor(normalised: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(normalised.toByteArray())
        val suffix = digest.take(8).joinToString("") { "%02x".format(it) }
        return "$ID_PREFIX$suffix"
    }
}
