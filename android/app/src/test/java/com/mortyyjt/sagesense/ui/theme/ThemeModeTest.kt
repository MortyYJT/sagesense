package com.mortyyjt.sagesense.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun storedValuesMapToExpectedThemeModes() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("system"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("dark"))
    }

    @Test
    fun unknownStoredValueFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("unexpected"))
    }
}
