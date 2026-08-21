package com.mortyyjt.sagesense.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventPersistencePrivacyTest {
    @Test
    fun preservesNonIdentifyingBrandSender() {
        assertEquals("CommBank Alert", EventPersistencePrivacy.sanitiseDisplaySender(" CommBank Alert "))
    }

    @Test
    fun redactsDirectIdentifiersFromSenderLabel() {
        val result = EventPersistencePrivacy.sanitiseDisplaySender(
            "Alice +61 400 000 999 alice@example.com https://private.example/path OTP: 481516",
        ).orEmpty()

        assertTrue(result.contains("[PHONE REDACTED]"))
        assertTrue(result.contains("[EMAIL REDACTED]"))
        assertTrue(result.contains("[LINK REDACTED]"))
        assertTrue(result.contains("REDACTED"))
        assertFalse(result.contains("400 000 999"))
        assertFalse(result.contains("alice@example.com"))
        assertFalse(result.contains("private.example"))
        assertFalse(result.contains("481516"))
    }

    @Test
    fun storesOnlyNormalisedWebOrigin() {
        assertEquals(
            "https://evil.example",
            EventPersistencePrivacy.sanitiseUrlForStorage(
                "https://evil.example/login?otp=123456&token=secret#fragment",
            ),
        )
        assertEquals(
            "https://evil.example",
            EventPersistencePrivacy.sanitiseUrlForStorage("www.evil.example/path"),
        )
    }

    @Test
    fun removesDirectIdentifiersAndUrlSecretsFromStoredSnippet() {
        val result = EventPersistencePrivacy.sanitiseSnippetForStorage(
            "Call +61 400 000 999 or alice@example.com. Password: hunter2. Open https://evil.example/login?otp=481516&token=private",
        )

        assertTrue(result.contains("[PHONE REDACTED]"))
        assertTrue(result.contains("[EMAIL REDACTED]"))
        assertTrue(result.contains("Password: [REDACTED]"))
        assertTrue(result.contains("[LINK ORIGIN: https://evil.example]"))
        assertFalse(result.contains("400 000 999"))
        assertFalse(result.contains("alice@example.com"))
        assertFalse(result.contains("hunter2"))
        assertFalse(result.contains("481516"))
        assertFalse(result.contains("private"))
    }

    @Test
    fun rejectsUnsupportedOrIdentifiedOrigins() {
        assertNull(EventPersistencePrivacy.sanitiseUrlForStorage("ftp://evil.example/file"))
        assertNull(EventPersistencePrivacy.sanitiseUrlForStorage("https://user:secret@evil.example/path"))
        assertNull(EventPersistencePrivacy.sanitiseUrlForStorage("not a URL"))
    }

    @Test
    fun deduplicatesOriginsAndDropsSensitiveUrlComponents() {
        val result = EventPersistencePrivacy.sanitiseUrlsForStorage(
            listOf(
                "https://evil.example/a?token=one",
                "https://evil.example/b?token=two",
                "https://safe.example/welcome",
            ),
        )

        assertEquals(listOf("https://evil.example", "https://safe.example"), result)
        assertTrue(
            result.none {
                it.removePrefix("https://").removePrefix("http://").contains('/') ||
                    it.contains("token") || it.contains("one") || it.contains("two")
            },
        )
    }
}
