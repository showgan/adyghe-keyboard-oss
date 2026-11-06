package com.adyghe.keyboard.core.state

import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.core.prefs.InMemoryKeyboardPreferences
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeStorePopupExtrasTest {

    @Test
    fun adygheLatin_iPopup_keepsDotlessIVariants() {
        val store = ImeStore(LayoutProvider(), InputConnectionController(), NoOpSuggestionEngine())
        store.switchTo("adyghe_latin")

        val (row, col) = findCharKey(store, "i")
        store.onLongPress(row, col)

        val popup = store.state.value.activePopup
        assertEquals(OverlayType.POPUP, store.state.value.overlayType)
        assertEquals(listOf("ı", "8", "İ"), popup)
        assertFalse(popup.any { it == "i" || it == "I" })

        val key = store.currentRows()[row].keys[col]
        assertEquals("ı8", key.popupHint(upper = false))
    }

    @Test
    fun adygheLatin_iPopup_omitsDigitWhenNumberRowEnabled() {
        val store = ImeStore(
            LayoutProvider(),
            InputConnectionController(),
            NoOpSuggestionEngine(),
            keyboardPreferences = InMemoryKeyboardPreferences(initialNumberRow = true)
        )
        store.switchTo("adyghe_latin")
        assertTrue(store.state.value.numberRowEnabled)

        val (row, col) = findCharKey(store, "i")
        assertEquals(1, row)
        store.onLongPress(row, col)

        val popup = store.state.value.activePopup
        assertEquals(OverlayType.POPUP, store.state.value.overlayType)
        assertEquals(listOf("ı", "İ"), popup)
        assertFalse(popup.contains("8"))

        val key = store.currentRows()[row].keys[col]
        assertEquals("ıİ", key.popupHint(upper = false))
    }

    @Test
    fun setNumberRowEnabled_stripsDigitPopupsWithoutRestart() {
        val store = ImeStore(LayoutProvider(), InputConnectionController(), NoOpSuggestionEngine())
        store.switchTo("adyghe_latin")
        val (offRow, _) = findCharKey(store, "i")
        assertEquals(0, offRow)

        store.setNumberRowEnabled(true)
        val (onRow, onCol) = findCharKey(store, "i")
        assertEquals(1, onRow)
        store.onLongPress(onRow, onCol)
        assertEquals(listOf("ı", "İ"), store.state.value.activePopup)
    }

    @Test
    fun reloadNumberRow_picksUpPrefsWrittenExternally() {
        val prefs = InMemoryKeyboardPreferences()
        val store = ImeStore(
            LayoutProvider(),
            InputConnectionController(),
            NoOpSuggestionEngine(),
            keyboardPreferences = prefs
        )
        assertFalse(store.state.value.numberRowEnabled)

        prefs.setNumberRowEnabled(true)
        assertFalse(store.state.value.numberRowEnabled)

        store.reloadNumberRow()
        assertTrue(store.state.value.numberRowEnabled)
        assertEquals(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            store.currentRows().first().keys.map { it.char }
        )
    }

    @Test
    fun setPopupVisual_selectsAnchorExtra() {
        val store = ImeStore(LayoutProvider(), InputConnectionController(), NoOpSuggestionEngine())
        val (row, col) = findCharKey(store, ".")
        store.onLongPress(row, col)
        assertEquals("'", store.state.value.popupSelection?.char)
        assertEquals(0, store.state.value.popupSelection?.keyIndex)

        store.setPopupVisual(listOf("_", "-", "'"), 2)
        val s = store.state.value
        assertEquals(listOf("_", "-", "'"), s.activePopup)
        assertEquals(2, s.popupAnchorIndex)
        assertEquals(2, s.popupSelection?.keyIndex)
        assertEquals("'", s.popupSelection?.char)
    }

    @Test
    fun longPressPopup_showsAndCommitsUppercaseWhenShifted() {
        val ic = RecordingInputConnectionController()
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.switchTo("adyghe_latin")
        tapAction(store, KeyAction.SHIFT)
        assertEquals(ShiftState.SHIFTED, store.state.value.shift)

        val (row, col) = findCharKey(store, "e")
        store.onLongPress(row, col)
        assertEquals(listOf("É", "3"), store.state.value.activePopup)
        assertEquals("É", store.state.value.popupSelection?.char)

        store.onPopupRelease()
        assertEquals(listOf("É"), ic.composingWords)
        assertEquals(ShiftState.UNSHIFTED, store.state.value.shift)
        assertEquals(OverlayType.NONE, store.state.value.overlayType)
    }

    @Test
    fun popupSelect_laterExtra_commitsUppercaseWhenShifted() {
        val ic = RecordingInputConnectionController()
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.switchTo("adyghe_latin")
        tapAction(store, KeyAction.SHIFT)

        val (row, col) = findCharKey(store, "s")
        store.onLongPress(row, col)
        assertEquals(listOf("Ş", "Ś", "Š"), store.state.value.activePopup)

        store.onPopupSelect(PopupSelection(1, ""))
        assertEquals("Ś", store.state.value.popupSelection?.char)
        store.onPopupRelease()
        assertEquals(listOf("Ś"), ic.composingWords)
    }

    @Test
    fun longPressPopup_capsLockUppercasesAndStaysLocked() {
        val ic = RecordingInputConnectionController()
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.switchTo("adyghe_cyrillic")
        tapAction(store, KeyAction.SHIFT, longPress = true)
        assertEquals(ShiftState.CAPS_LOCK, store.state.value.shift)

        val (row, col) = findCharKey(store, "г")
        store.onLongPress(row, col)
        assertEquals(listOf("ГЪ"), store.state.value.activePopup)

        store.onPopupRelease()
        assertEquals(listOf("ГЪ"), ic.composingWords)
        assertEquals(ShiftState.CAPS_LOCK, store.state.value.shift)
    }

    @Test
    fun longPressPopup_punctuationUnchangedWhenShifted() {
        val store = ImeStore(LayoutProvider(), InputConnectionController(), NoOpSuggestionEngine())
        tapAction(store, KeyAction.SHIFT)
        val (row, col) = findCharKey(store, ".")
        store.onLongPress(row, col)
        assertEquals(listOf("'", "\"", "-", "(", ")", "_"), store.state.value.activePopup)
    }

    private fun tapAction(store: ImeStore, action: KeyAction, longPress: Boolean = false) {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == action) {
                    if (longPress) store.onLongPress(row, col) else store.onKeyTap(row, col)
                    return
                }
            }
        }
        error("No key for $action")
    }

    private fun findCharKey(store: ImeStore, ch: String): Pair<Int, Int> {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == KeyAction.CHAR && key.char == ch) {
                    return row to col
                }
            }
        }
        error("No CHAR key for '$ch'")
    }

    private class RecordingInputConnectionController : InputConnectionController() {
        val composingWords = mutableListOf<String>()

        override fun setComposingTextAt(
            word: String,
            cursorInWord: Int,
            replaceBefore: Int,
            replaceAfter: Int
        ) {
            composingWords += word
        }
    }
}
