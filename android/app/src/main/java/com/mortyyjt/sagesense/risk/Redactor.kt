package com.mortyyjt.sagesense.risk

object Redactor {
    private val labelledOtp = Regex("(?i)(otp|verification code|security code|验证码|动态码)(\\s*(?:is|:|为)?\\s*)\\d{4,8}")
    private val cardNumber = Regex("(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)")
    private val accountNumber = Regex("(?i)(account|acct|账户)(\\s*(?:number|no\\.?|号码|:)?\\s*)\\d{5,16}")

    fun redact(value: String): String = value
        .replace(labelledOtp) { match -> "${match.groupValues[1]}${match.groupValues[2]}[REDACTED]" }
        .replace(cardNumber, "[CARD REDACTED]")
        .replace(accountNumber) { match -> "${match.groupValues[1]}${match.groupValues[2]}[REDACTED]" }
}
