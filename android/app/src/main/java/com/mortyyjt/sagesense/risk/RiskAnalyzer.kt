package com.mortyyjt.sagesense.risk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.security.MessageDigest
import java.util.Locale

enum class RiskLevel { LOW, MEDIUM, HIGH }

data class RiskWeights(
    val version: Int = 1,
    val mediumThreshold: Int = 30,
    val highThreshold: Int = 60,
    val values: Map<String, Int> = DEFAULT_WEIGHTS,
) {
    companion object {
        private val JSON = Json { ignoreUnknownKeys = true }

        val DEFAULT_WEIGHTS = mapOf(
            "URGENCY" to 15,
            "PAYMENT_REQUEST" to 20,
            "CREDENTIAL_REQUEST" to 25,
            "OTP_REQUEST" to 30,
            "SUSPICIOUS_URL" to 25,
            "MISSPELLED_DOMAIN" to 20,
            "BRAND_IMPERSONATION" to 15,
            "WATCHLIST_MATCH" to 60,
        )

        fun fromJson(raw: String): RiskWeights {
            val document = JSON.decodeFromString<RiskWeightsDocument>(raw)
            return RiskWeights(
                version = document.version,
                mediumThreshold = document.thresholds.medium,
                highThreshold = document.thresholds.high,
                values = DEFAULT_WEIGHTS + document.weights,
            )
        }
    }
}

@Serializable
private data class RiskWeightsDocument(
    val version: Int,
    val thresholds: RiskThresholdsDocument,
    val weights: Map<String, Int>,
)

@Serializable
private data class RiskThresholdsDocument(val medium: Int, val high: Int)

data class RiskAnalysis(
    val score: Int,
    val level: RiskLevel,
    val signals: List<String>,
    val urls: List<String>,
    val domains: List<String>,
    val redactedText: String,
    val campaignId: String,
)

class RiskAnalyzer(private val weights: RiskWeights = RiskWeights()) {
    private val urlPattern = Regex("(?i)\\b(?:https?://|www\\.)[^\\s<>]+")
    private val urgencyPattern = Regex(
        "(?i)(?:\\b(?:urgent|immediately|now|within \\d+ hours?|suspended|final warning|act fast)\\b|立即|马上|紧急|限时|停用|冻结|最后通知)",
    )
    private val paymentPattern = Regex(
        "(?i)(?:\\b(?:pay|payment|transfer|wire|gift card|bitcoin|crypto|refund|fee)\\b|转账|付款|汇款|礼品卡|比特币|退款|手续费)",
    )
    private val credentialPattern = Regex(
        "(?i)(?:\\b(?:password|passcode|login|bank details?|card number|account number)\\b|密码|登录|银行卡|账户信息|个人资料)",
    )
    private val otpPattern = Regex(
        "(?i)(?:\\b(?:otp|one[- ]time (?:code|password)|verification code|security code)\\b|验证码|动态码|安全码)",
    )
    private val brands = mapOf(
        "commbank" to setOf("commbank.com.au", "commbank.com"),
        "commonwealth bank" to setOf("commbank.com.au", "commbank.com"),
        "anz" to setOf("anz.com.au"),
        "westpac" to setOf("westpac.com.au"),
        "nab" to setOf("nab.com.au"),
        "mygov" to setOf("my.gov.au"),
        "australia post" to setOf("auspost.com.au"),
        "澳洲联邦银行" to setOf("commbank.com.au"),
    )

    fun analyse(text: String, sender: String? = null, watchlist: Set<String> = emptySet()): RiskAnalysis {
        val combined = listOfNotNull(sender, text).joinToString(" ")
        val urls = urlPattern.findAll(combined).map { it.value.trimEnd('.', ',', ';', ')', ']') }.distinct().take(5).toList()
        val domains = urls.mapNotNull(::domainFromUrl).distinct()
        val signals = linkedSetOf<String>()

        if (urgencyPattern.containsMatchIn(combined)) signals += "URGENCY"
        if (paymentPattern.containsMatchIn(combined)) signals += "PAYMENT_REQUEST"
        if (credentialPattern.containsMatchIn(combined)) signals += "CREDENTIAL_REQUEST"
        if (otpPattern.containsMatchIn(combined)) signals += "OTP_REQUEST"
        if (domains.any(::isSuspiciousDomain)) signals += "SUSPICIOUS_URL"
        if (domains.any { domain -> looksMisspelledBrand(domain) }) signals += "MISSPELLED_DOMAIN"
        if (containsBrandOutsideOfficialDomain(combined, domains)) signals += "BRAND_IMPERSONATION"
        val normalisedWatchlist = watchlist.mapTo(hashSetOf(), ::normaliseEntity)
        if ((domains + listOfNotNull(sender)).any { normaliseEntity(it) in normalisedWatchlist }) {
            signals += "WATCHLIST_MATCH"
        }

        val score = signals.sumOf { weights.values[it] ?: 0 }.coerceIn(0, 100)
        val level = when {
            score >= weights.highThreshold -> RiskLevel.HIGH
            score >= weights.mediumThreshold -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        val redacted = Redactor.redact(text)
        return RiskAnalysis(
            score = score,
            level = level,
            signals = signals.toList(),
            urls = urls,
            domains = domains,
            redactedText = redacted.take(500),
            campaignId = fingerprint(domains, signals, redacted),
        )
    }

    private fun domainFromUrl(raw: String): String? = runCatching {
        val normalised = if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw
        URI(normalised).host?.lowercase(Locale.ROOT)?.removePrefix("www.")
    }.getOrNull()

    private fun isSuspiciousDomain(domain: String): Boolean {
        val riskySuffixes = listOf(".example", ".click", ".top", ".xyz", ".work", ".support")
        return riskySuffixes.any(domain::endsWith) || domain.count { it == '-' } >= 2 || domain.length > 45
    }

    private fun looksMisspelledBrand(domain: String): Boolean {
        val compact = domain.substringBefore('.').replace("-", "")
        return brands.keys.any { brand ->
            val token = brand.replace(" ", "")
            compact != token && editDistance(compact, token) in 1..2
        }
    }

    private fun containsBrandOutsideOfficialDomain(text: String, domains: List<String>): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return brands.any { (brand, official) ->
            lower.contains(brand) && domains.isNotEmpty() && domains.none { domain -> official.any { domain == it || domain.endsWith(".$it") } }
        }
    }

    private fun editDistance(left: String, right: String): Int {
        if (left.isEmpty()) return right.length
        if (right.isEmpty()) return left.length
        var previous = IntArray(right.length + 1) { it }
        left.forEachIndexed { leftIndex, leftChar ->
            val current = IntArray(right.length + 1)
            current[0] = leftIndex + 1
            right.forEachIndexed { rightIndex, rightChar ->
                current[rightIndex + 1] = minOf(
                    current[rightIndex] + 1,
                    previous[rightIndex + 1] + 1,
                    previous[rightIndex] + if (leftChar == rightChar) 0 else 1,
                )
            }
            previous = current
        }
        return previous.last()
    }

    private fun fingerprint(domains: List<String>, signals: Set<String>, text: String): String {
        val stableTerms = text.lowercase(Locale.ROOT)
            .replace(Regex("\\d+"), "#")
            .split(Regex("\\W+"))
            .filter { it.length >= 4 }
            .distinct()
            .sorted()
            .take(8)
        val raw = (domains.sorted() + signals.sorted() + stableTerms).joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun normaliseEntity(value: String): String {
            val digits = value.filter(Char::isDigit)
            val phoneLike = digits.length >= 7 && (
                value.none(Char::isLetter) || value.startsWith("phone:", ignoreCase = true)
            )
            if (phoneLike) {
                // Android can surface the same Australian caller as 0400...,
                // 61400..., or +61 400.... Use one stable Watchlist key.
                val canonicalDigits = if (digits.length == 10 && digits.startsWith('0')) {
                    "61${digits.drop(1)}"
                } else {
                    digits
                }
                return "phone:$canonicalDigits"
            }
            return value.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9+]"), "")
        }
    }
}
