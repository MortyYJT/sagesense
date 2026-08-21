package com.mortyyjt.sagesense.data

import com.mortyyjt.sagesense.risk.Redactor
import java.net.IDN
import java.net.URI
import java.util.Locale

/**
 * Minimises personal and link data before a risk event is written to Room.
 *
 * Risk analysis still happens against the original notification or caller data.
 * Only the safer display label and URL origin cross the persistence boundary.
 */
internal object EventPersistencePrivacy {
    private val url = Regex("(?i)\\b(?:https?://|www\\.)[^\\s<>]+")
    private val email = Regex("(?i)\\b[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,63}\\b")
    private val possiblePhone = Regex("(?<![a-z0-9])\\+?\\d[\\d ().-]{5,}\\d(?![a-z0-9])", RegexOption.IGNORE_CASE)
    private val password = Regex("(?i)(password|passcode|密码)(\\s*(?:is|:|：|为)?\\s*)[^\\s,;]{4,64}")
    private val storedLinkOrigin = Regex("(?i)\\[LINK ORIGIN:\\s*((?:https?://|www\\.)[^\\]\\s]+)\\]")

    fun sanitiseDisplaySender(sender: String?): String? {
        val trimmed = sender?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val withoutDirectIdentifiers = redactSecrets(trimmed)
            .replace(url, "[LINK REDACTED]")
            .replace(email, "[EMAIL REDACTED]")
            .replace(possiblePhone) { candidate ->
                if (candidate.value.count(Char::isDigit) >= 7) {
                    "[PHONE REDACTED]"
                } else {
                    candidate.value
                }
            }
            .replace(Regex("\\s+"), " ")
            .trim()
        return withoutDirectIdentifiers.take(MAX_DISPLAY_SENDER_LENGTH).takeIf(String::isNotEmpty)
    }

    fun sanitiseSnippetForStorage(snippet: String): String {
        val protectedOrigins = mutableListOf<String>()
        val withProtectedOrigins = storedLinkOrigin.replace(redactSecrets(snippet)) { match ->
            val safeOrigin = sanitiseUrlForStorage(match.groupValues[1])
                ?: return@replace "[LINK REDACTED]"
            val token = "\u0001${protectedOrigins.size}\u0002"
            protectedOrigins += "[LINK ORIGIN: $safeOrigin]"
            token
        }
        var sanitised = sanitiseDirectIdentifiers(
            withProtectedOrigins.replace(url) { match ->
                sanitiseUrlForStorage(match.value)?.let { "[LINK ORIGIN: $it]" } ?: "[LINK REDACTED]"
            },
        )
        protectedOrigins.forEachIndexed { index, origin ->
            sanitised = sanitised.replace("\u0001$index\u0002", origin)
        }
        return truncateWithoutPartialLinkMarker(sanitised)
    }

    fun sanitiseUrlsForStorage(urls: Iterable<String>): List<String> = urls
        .mapNotNull(::sanitiseUrlForStorage)
        .distinct()
        .take(MAX_STORED_URLS)

    fun sanitiseUrlForStorage(raw: String): String? = runCatching {
        val value = raw.trim().trimEnd('.', ',', ';', ')', ']').takeIf(String::isNotEmpty) ?: return null
        val normalised = if (value.startsWith("www.", ignoreCase = true)) "https://$value" else value
        val uri = URI(normalised)
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        if (scheme !in setOf("http", "https") || uri.rawUserInfo != null) return null
        val host = uri.host
            ?.trimEnd('.')
            ?.removePrefix("www.")
            ?.takeIf(String::isNotEmpty)
            ?: return null
        val asciiHost = IDN.toASCII(host).lowercase(Locale.ROOT)
        "$scheme://$asciiHost"
    }.getOrNull()

    private fun sanitiseDirectIdentifiers(value: String): String = value
        .replace(email, "[EMAIL REDACTED]")
        .replace(possiblePhone) { candidate ->
            if (candidate.value.count(Char::isDigit) >= 7) {
                "[PHONE REDACTED]"
            } else {
                candidate.value
            }
        }

    private fun redactSecrets(value: String): String = Redactor.redact(value)
        .replace(password) { match -> "${match.groupValues[1]}${match.groupValues[2]}[REDACTED]" }

    private fun truncateWithoutPartialLinkMarker(value: String): String {
        if (value.length <= MAX_STORED_SNIPPET_LENGTH) return value
        val truncated = value.take(MAX_STORED_SNIPPET_LENGTH)
        val lastOpenMarker = truncated.lastIndexOf("[LINK ORIGIN:")
        val lastClosingBracket = truncated.lastIndexOf(']')
        if (lastOpenMarker <= lastClosingBracket) return truncated
        return (truncated.take(lastOpenMarker) + "[LINK REDACTED]").take(MAX_STORED_SNIPPET_LENGTH)
    }

    private const val MAX_DISPLAY_SENDER_LENGTH = 120
    private const val MAX_STORED_SNIPPET_LENGTH = 500
    private const val MAX_STORED_URLS = 5
}
