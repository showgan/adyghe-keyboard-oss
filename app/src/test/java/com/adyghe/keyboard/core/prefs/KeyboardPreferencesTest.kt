package com.adyghe.keyboard.core.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPreferencesTest {

    @Test
    fun defaults_areSystemThemeHapticsOnAndNumberRowOff() {
        val prefs = InMemoryKeyboardPreferences()
        assertEquals(ThemeMode.SYSTEM, prefs.getThemeMode())
        assertTrue(prefs.isHapticEnabled())
        assertFalse(prefs.isNumberRowEnabled())
    }

    @Test
    fun setThemeMode_persistsAndReadsBack() {
        val prefs = InMemoryKeyboardPreferences()
        prefs.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, prefs.getThemeMode())
        prefs.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, prefs.getThemeMode())
        prefs.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, prefs.getThemeMode())
    }

    @Test
    fun setHapticEnabled_persistsAndReadsBack() {
        val prefs = InMemoryKeyboardPreferences()
        prefs.setHapticEnabled(false)
        assertFalse(prefs.isHapticEnabled())
        prefs.setHapticEnabled(true)
        assertTrue(prefs.isHapticEnabled())
    }

    @Test
    fun setNumberRowEnabled_persistsAndReadsBack() {
        val prefs = InMemoryKeyboardPreferences()
        prefs.setNumberRowEnabled(true)
        assertTrue(prefs.isNumberRowEnabled())
        prefs.setNumberRowEnabled(false)
        assertFalse(prefs.isNumberRowEnabled())
    }

    @Test
    fun initialValues_canOverrideDefaults() {
        val prefs = InMemoryKeyboardPreferences(
            initialTheme = ThemeMode.LIGHT,
            initialHaptic = false,
            initialNumberRow = true
        )
        assertEquals(ThemeMode.LIGHT, prefs.getThemeMode())
        assertFalse(prefs.isHapticEnabled())
        assertTrue(prefs.isNumberRowEnabled())
    }
}

class ThemeModeTest {

    @Test
    fun isDark_systemFollowsArgument() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }

    @Test
    fun isDark_lightIsAlwaysFalse() {
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = false))
    }

    @Test
    fun isDark_darkIsAlwaysTrue() {
        assertTrue(ThemeMode.DARK.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }

    @Test
    fun fromPref_knownNames() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPref("SYSTEM"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromPref("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromPref("DARK"))
    }

    @Test
    fun fromPref_nullOrUnknownFallsBackToSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPref(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPref(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromPref("midnight"))
    }
}
