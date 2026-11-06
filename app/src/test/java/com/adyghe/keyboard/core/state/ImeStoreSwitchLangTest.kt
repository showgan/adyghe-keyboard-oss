package com.adyghe.keyboard.core.state

import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.language.InMemoryLanguagePreferences
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeStoreSwitchLangTest {

    @Test
    fun switchLang_forward_advancesThroughAll() {
        val store = newStore()
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)

        store.switchLang(+1)
        assertEquals(Languages.ADYGHE_LATIN_2.id, store.state.value.languageId)

        store.switchLang()
        assertEquals(Languages.ADYGHE_CYRILLIC.id, store.state.value.languageId)
    }

    @Test
    fun switchLang_backward_wrapsToLast() {
        val store = newStore()
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)

        store.switchLang(-1)
        assertEquals(Languages.ALL.last().id, store.state.value.languageId)
    }

    @Test
    fun switchLang_forward_wrapsFromLastToFirst() {
        val store = newStore()
        store.switchTo(Languages.ALL.last().id)

        store.switchLang(+1)
        assertEquals(Languages.ALL.first().id, store.state.value.languageId)
    }

    @Test
    fun switchLang_fullCycle_returnsToStart() {
        val store = newStore()
        val start = store.state.value.languageId
        repeat(Languages.ALL.size) { store.switchLang(+1) }
        assertEquals(start, store.state.value.languageId)

        repeat(Languages.ALL.size) { store.switchLang(-1) }
        assertEquals(start, store.state.value.languageId)
    }

    @Test
    fun switchLang_skipsDisabledLanguages() {
        val store = newStore()
        store.setLanguageEnabled(Languages.ADYGHE_LATIN_2.id, false)
        store.setLanguageEnabled(Languages.ADYGHE_CYRILLIC.id, false)
        store.setLanguageEnabled(Languages.ARABIC.id, false)

        store.switchLang(+1)
        assertEquals(Languages.HEBREW.id, store.state.value.languageId)

        store.switchLang(-1)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
    }

    @Test
    fun setLanguageEnabled_cannotDisableLastAdygheLatin() {
        val store = newStore()
        Languages.ALL
            .filter { it.id != Languages.ADYGHE_LATIN_1.id }
            .forEach { store.setLanguageEnabled(it.id, false) }

        store.setLanguageEnabled(Languages.ADYGHE_LATIN_1.id, false)
        assertEquals(
            setOf(Languages.ADYGHE_LATIN_1.id),
            store.state.value.enabledLanguageIds
        )
    }

    @Test
    fun setLanguageEnabled_canDisableAdygheWhenOtherRemains() {
        val store = newStore()
        store.setLanguageEnabled(Languages.ADYGHE_LATIN_2.id, false)
        assertFalse(Languages.ADYGHE_LATIN_2.id in store.state.value.enabledLanguageIds)
        assertTrue(Languages.ADYGHE_LATIN_1.id in store.state.value.enabledLanguageIds)

        store.setLanguageEnabled(Languages.ADYGHE_LATIN_2.id, true)
        store.setLanguageEnabled(Languages.ADYGHE_LATIN_1.id, false)
        assertFalse(Languages.ADYGHE_LATIN_1.id in store.state.value.enabledLanguageIds)
        assertTrue(Languages.ADYGHE_LATIN_2.id in store.state.value.enabledLanguageIds)
        assertEquals(Languages.ADYGHE_LATIN_2.id, store.state.value.languageId)
    }

    @Test
    fun switchLang_noopWhenOnlyOneAdygheLatinEnabled() {
        val store = newStore()
        Languages.ALL
            .filter { it.id != Languages.ADYGHE_LATIN_1.id }
            .forEach { store.setLanguageEnabled(it.id, false) }

        assertEquals(
            setOf(Languages.ADYGHE_LATIN_1.id),
            store.state.value.enabledLanguageIds
        )
        store.switchLang(+1)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
        store.switchLang(-1)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
    }

    @Test
    fun setLanguageEnabled_hopsWhenCurrentDisabled() {
        val store = newStore()
        store.switchTo(Languages.ARABIC.id)
        store.setLanguageEnabled(Languages.ARABIC.id, false)
        assertFalse(Languages.ARABIC.id in store.state.value.enabledLanguageIds)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
    }

    @Test
    fun openLanguagePicker_showsSameOverlayAsSpaceLongPress() {
        val store = newStore()
        store.openLanguagePicker()
        assertEquals(OverlayType.LANGUAGE_PICKER, store.state.value.overlayType)

        store.openLanguagePicker()
        assertEquals(OverlayType.NONE, store.state.value.overlayType)
    }

    @Test
    fun globeTap_withSeveralLanguages_cyclesForward() {
        val store = newStore()
        val (row, col) = findActionKey(store, KeyAction.LANG_SWITCH)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)

        store.onKeyTap(row, col)
        assertEquals(Languages.ADYGHE_LATIN_2.id, store.state.value.languageId)
        assertEquals(OverlayType.NONE, store.state.value.overlayType)
    }

    @Test
    fun globeTap_withOneLanguage_opensPicker() {
        val store = newStore()
        Languages.ALL
            .filter { it.id != Languages.ADYGHE_LATIN_1.id }
            .forEach { store.setLanguageEnabled(it.id, false) }

        val (row, col) = findActionKey(store, KeyAction.LANG_SWITCH)
        store.onKeyTap(row, col)

        assertEquals(OverlayType.LANGUAGE_PICKER, store.state.value.overlayType)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
    }

    @Test
    fun globeLongPress_opensPicker() {
        val store = newStore()
        val (row, col) = findActionKey(store, KeyAction.LANG_SWITCH)
        store.onLongPress(row, col)
        assertEquals(OverlayType.LANGUAGE_PICKER, store.state.value.overlayType)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
    }

    @Test
    fun setLanguageEnabled_persistsAcrossStoreRecreation() {
        val prefs = InMemoryLanguagePreferences()
        val store = newStore(prefs)
        store.setLanguageEnabled(Languages.RUSSIAN.id, false)

        val restored = newStore(prefs)
        assertFalse(Languages.RUSSIAN.id in restored.state.value.enabledLanguageIds)
        assertTrue(Languages.ADYGHE_LATIN_1.id in restored.state.value.enabledLanguageIds)
        assertEquals(Languages.ADYGHE_LATIN_1.id, restored.state.value.languageId)
    }

    @Test
    fun reloadEnabledLanguages_picksUpPrefsWrittenExternally() {
        val prefs = InMemoryLanguagePreferences()
        val store = newStore(prefs)
        assertTrue(Languages.RUSSIAN.id in store.state.value.enabledLanguageIds)

        prefs.saveEnabledIds(setOf(Languages.ADYGHE_LATIN_2.id, Languages.ENGLISH_US.id))
        assertTrue(Languages.RUSSIAN.id in store.state.value.enabledLanguageIds)

        store.reloadEnabledLanguages()
        assertFalse(Languages.RUSSIAN.id in store.state.value.enabledLanguageIds)
        assertTrue(Languages.ENGLISH_US.id in store.state.value.enabledLanguageIds)
        assertTrue(Languages.ADYGHE_LATIN_2.id in store.state.value.enabledLanguageIds)
        assertFalse(Languages.ADYGHE_LATIN_1.id in store.state.value.enabledLanguageIds)
        assertEquals(Languages.ADYGHE_LATIN_2.id, store.state.value.languageId)
    }

    @Test
    fun startsOnAdygheLatin2WhenLatin1NotEnabled() {
        val prefs = InMemoryLanguagePreferences(
            setOf(Languages.ADYGHE_LATIN_2.id, Languages.ENGLISH_US.id)
        )
        val store = newStore(prefs)
        assertEquals(Languages.ADYGHE_LATIN_2.id, store.state.value.languageId)
    }

    private fun newStore(
        prefs: InMemoryLanguagePreferences = InMemoryLanguagePreferences()
    ): ImeStore =
        ImeStore(
            LayoutProvider(),
            InputConnectionController(),
            NoOpSuggestionEngine(),
            prefs
        )

    private fun findActionKey(store: ImeStore, action: KeyAction): Pair<Int, Int> {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == action) return row to col
            }
        }
        error("No key for $action")
    }
}
