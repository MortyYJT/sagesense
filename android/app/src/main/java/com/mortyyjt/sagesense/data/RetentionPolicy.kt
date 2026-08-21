package com.mortyyjt.sagesense.data

internal object RetentionPolicy {
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1_000L

    fun cutoffMillis(nowMillis: Long, retentionDays: Int): Long {
        require(retentionDays >= 0) { "retentionDays must not be negative" }
        return nowMillis - retentionDays * MILLIS_PER_DAY
    }
}
