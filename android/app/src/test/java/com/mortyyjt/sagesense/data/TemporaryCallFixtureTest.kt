package com.mortyyjt.sagesense.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporaryCallFixtureTest {
    @Test
    fun equivalentAustralianFormatsProduceOneStableFixture() {
        val local = TemporaryCallFixture.create("0400 123 456", nowMillis = 10L)
        val international = TemporaryCallFixture.create("+61 400 123 456", nowMillis = 20L)
        val carrier = TemporaryCallFixture.create("61400123456", nowMillis = 30L)

        assertNotNull(local)
        assertEquals(local!!.id, international!!.id)
        assertEquals(local.id, carrier!!.id)
        assertEquals(local.normalisedValue, international.normalisedValue)
        assertEquals("••• ••• 3456", local.value)
        assertTrue(local.seededDemoData)
        assertFalse(local.id.contains("0400"))
    }

    @Test
    fun differentNumbersDoNotOverwriteEachOther() {
        val first = TemporaryCallFixture.create("+61 400 123 456")!!
        val second = TemporaryCallFixture.create("+61 411 987 654")!!

        assertNotEquals(first.id, second.id)
        assertTrue(TemporaryCallFixture.isTemporaryId(first.id))
    }

    @Test
    fun rejectsNonPhoneAndOutOfBoundsInputs() {
        assertNull(TemporaryCallFixture.create("0400 CALL ME"))
        assertNull(TemporaryCallFixture.create("1234567"))
        assertNull(TemporaryCallFixture.create("1234567890123456"))
        assertNull(TemporaryCallFixture.idForRawPhone("not a phone"))
    }
}
