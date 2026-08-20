package com.mortyyjt.sagesense.risk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactorTest {
    @Test
    fun redactsOtpCardAndAccountPatterns() {
        val result = Redactor.redact("Verification code: 739201 card 4111 1111 1111 1111 account number 12345678")

        assertFalse(result.contains("739201"))
        assertFalse(result.contains("4111 1111 1111 1111"))
        assertFalse(result.contains("12345678"))
        assertTrue(result.contains("REDACTED"))
    }
}
