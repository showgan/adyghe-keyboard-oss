package com.adyghe.keyboard.core.language

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguagePreferencesTest {

    @Test
    fun applyLanguageEnabled_cannotDisableLastAdygheLatin() {
        val onlyOne = setOf(Languages.ADYGHE_LATIN_1.id, Languages.ENGLISH_US.id)
        assertNull(applyLanguageEnabled(onlyOne, Languages.ADYGHE_LATIN_1.id, false))

        val onlyTwo = setOf(Languages.ADYGHE_LATIN_2.id)
        assertNull(applyLanguageEnabled(onlyTwo, Languages.ADYGHE_LATIN_2.id, false))
    }

    @Test
    fun applyLanguageEnabled_canDisableAdygheWhenTheOtherRemains() {
        val current = setOf(Languages.ADYGHE_LATIN_1.id, Languages.ADYGHE_LATIN_2.id)
        assertEquals(
            setOf(Languages.ADYGHE_LATIN_2.id),
            applyLanguageEnabled(current, Languages.ADYGHE_LATIN_1.id, false)
        )
        assertEquals(
            setOf(Languages.ADYGHE_LATIN_1.id),
            applyLanguageEnabled(current, Languages.ADYGHE_LATIN_2.id, false)
        )
    }

    @Test
    fun applyLanguageEnabled_disablesKnownLanguageAndPersists() {
        val prefs = InMemoryLanguagePreferences()
        val next = applyLanguageEnabled(
            prefs.loadEnabledIds(),
            Languages.RUSSIAN.id,
            false
        )
        requireNotNull(next)
        prefs.saveEnabledIds(next)

        assertFalse(Languages.RUSSIAN.id in prefs.loadEnabledIds())
        assertTrue(Languages.ADYGHE_LATIN_1.id in prefs.loadEnabledIds())
        assertTrue(Languages.ADYGHE_LATIN_2.id in prefs.loadEnabledIds())
    }

    @Test
    fun applyLanguageEnabled_ignoresUnknownId() {
        val current = Languages.ALL.map { it.id }.toSet()
        assertNull(applyLanguageEnabled(current, "klingon", true))
        assertNull(applyLanguageEnabled(current, "klingon", false))
    }

    @Test
    fun applyLanguageEnabled_reenablesLanguage() {
        val prefs = InMemoryLanguagePreferences(setOf(Languages.ADYGHE_LATIN_2.id))
        val next = applyLanguageEnabled(
            prefs.loadEnabledIds(),
            Languages.ENGLISH_US.id,
            true
        )
        requireNotNull(next)
        prefs.saveEnabledIds(next)

        assertEquals(
            setOf(Languages.ADYGHE_LATIN_2.id, Languages.ENGLISH_US.id),
            prefs.loadEnabledIds()
        )
    }

    @Test
    fun normalize_emptyReturnsAllIncludingBothAdygheLatin() {
        val all = Languages.ALL.map { it.id }.toSet()
        assertEquals(all, normalizeEnabledLanguageIds(null))
        assertEquals(all, normalizeEnabledLanguageIds(emptySet()))
        assertTrue(Languages.ADYGHE_LATIN_1.id in all)
        assertTrue(Languages.ADYGHE_LATIN_2.id in all)
    }

    @Test
    fun normalize_neitherAdygheAddsDefaultOnly() {
        val next = normalizeEnabledLanguageIds(setOf(Languages.ENGLISH_US.id))
        assertEquals(
            setOf(Languages.ADYGHE_LATIN_1.id, Languages.ENGLISH_US.id),
            next
        )
        assertFalse(Languages.ADYGHE_LATIN_2.id in next)
    }

    @Test
    fun canChangeLanguageEnabled_lastAdygheCannotUncheck() {
        val only1 = setOf(Languages.ADYGHE_LATIN_1.id)
        assertFalse(canChangeLanguageEnabled(only1, Languages.ADYGHE_LATIN_1.id))
        assertTrue(canChangeLanguageEnabled(only1, Languages.ADYGHE_LATIN_2.id))
        assertTrue(canChangeLanguageEnabled(only1, Languages.ENGLISH_US.id))
    }

    @Test
    fun canChangeLanguageEnabled_bothAdygheCanUncheck() {
        val both = setOf(Languages.ADYGHE_LATIN_1.id, Languages.ADYGHE_LATIN_2.id)
        assertTrue(canChangeLanguageEnabled(both, Languages.ADYGHE_LATIN_1.id))
        assertTrue(canChangeLanguageEnabled(both, Languages.ADYGHE_LATIN_2.id))
    }

    @Test
    fun preferredEnabledLanguageId_prefersDefaultWhenEnabled() {
        assertEquals(
            Languages.ADYGHE_LATIN_1.id,
            preferredEnabledLanguageId(
                setOf(Languages.ADYGHE_LATIN_1.id, Languages.ADYGHE_LATIN_2.id)
            )
        )
    }

    @Test
    fun preferredEnabledLanguageId_fallsBackToFirstEnabledInAllOrder() {
        assertEquals(
            Languages.ADYGHE_LATIN_2.id,
            preferredEnabledLanguageId(
                setOf(Languages.ENGLISH_US.id, Languages.ADYGHE_LATIN_2.id)
            )
        )
    }
}
