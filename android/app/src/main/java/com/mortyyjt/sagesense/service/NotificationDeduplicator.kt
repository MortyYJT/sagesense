package com.mortyyjt.sagesense.service

internal class NotificationDeduplicator(
    private val ttlMillis: Long = 30_000,
    private val maxEntries: Int = 128,
) {
    private val seenAt = LinkedHashMap<String, Long>()

    @Synchronized
    fun shouldProcess(
        notificationKey: String,
        title: String?,
        text: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        val cutoff = nowMillis - ttlMillis
        seenAt.entries.removeAll { it.value < cutoff }
        val signature = "$notificationKey\u0000${title.orEmpty()}\u0000$text"
        val previous = seenAt[signature]
        if (previous != null && previous >= cutoff) return false
        seenAt[signature] = nowMillis
        while (seenAt.size > maxEntries) {
            seenAt.remove(seenAt.entries.first().key)
        }
        return true
    }
}
