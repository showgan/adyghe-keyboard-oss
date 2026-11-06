package com.adyghe.keyboard.core.state

import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeStoreShiftSwipeTest {

    @Test
    fun setShiftEnabled_on_fromUnshifted_isOneShot() {
        val store = newStore()
        assertEquals(ShiftState.UNSHIFTED, store.state.value.shift)
        assertEquals(0L, store.state.value.lastShiftTapMs)

        store.setShiftEnabled(true)

        assertEquals(ShiftState.SHIFTED, store.state.value.shift)
        assertEquals(0L, store.state.value.lastShiftTapMs)
    }

    @Test
    fun setShiftEnabled_on_whenAlreadyShifted_staysShifted() {
        val store = newStore()
        store.setShiftEnabled(true)

        store.setShiftEnabled(true)

        assertEquals(ShiftState.SHIFTED, store.state.value.shift)
        assertEquals(0L, store.state.value.lastShiftTapMs)
    }

    @Test
    fun setShiftEnabled_on_whenCapsLock_staysLocked() {
        val store = newStore()
        val (row, col) = findActionKey(store, KeyAction.SHIFT)
        store.onLongPress(row, col)
        assertEquals(ShiftState.CAPS_LOCK, store.state.value.shift)

        store.setShiftEnabled(true)

        assertEquals(ShiftState.CAPS_LOCK, store.state.value.shift)
    }

    @Test
    fun setShiftEnabled_off_clearsCapsLock() {
        val store = newStore()
        val (row, col) = findActionKey(store, KeyAction.SHIFT)
        store.onLongPress(row, col)
        assertEquals(ShiftState.CAPS_LOCK, store.state.value.shift)

        store.setShiftEnabled(false)

        assertEquals(ShiftState.UNSHIFTED, store.state.value.shift)
    }

    @Test
    fun setShiftEnabled_off_fromOneShot_isUnshifted() {
        val store = newStore()
        store.setShiftEnabled(true)

        store.setShiftEnabled(false)

        assertEquals(ShiftState.UNSHIFTED, store.state.value.shift)
    }

    @Test
    fun setShiftEnabled_on_doesNotCountAsShiftTapForCaps() {
        val store = newStore()
        store.setShiftEnabled(true)
        val (row, col) = findActionKey(store, KeyAction.SHIFT)

        store.onKeyTap(row, col, nowMs = 10_000L)

        assertEquals(ShiftState.UNSHIFTED, store.state.value.shift)
    }

    @Test
    fun setShiftEnabled_clearsPreviewWithoutCommitting() {
        val ic = RecordingInputConnectionController()
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        val (row, col) = findCharKey(store, "a")
        store.onKeyDown(row, col)
        assertEquals(OverlayType.PREVIEW, store.state.value.overlayType)

        store.setShiftEnabled(true)

        assertEquals(OverlayType.NONE, store.state.value.overlayType)
        assertEquals(ShiftState.SHIFTED, store.state.value.shift)
        assertTrue(ic.composingWords.isEmpty())
    }

    @Test
    fun switchLang_noop_clearsPreviewWithoutCommitting() {
        val ic = RecordingInputConnectionController()
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        Languages.ALL
            .filter { it.id != Languages.ADYGHE_LATIN_1.id }
            .forEach { store.setLanguageEnabled(it.id, false) }
        val (row, col) = findCharKey(store, "a")
        store.onKeyDown(row, col)
        assertEquals(OverlayType.PREVIEW, store.state.value.overlayType)

        store.switchLang(+1)

        assertEquals(OverlayType.NONE, store.state.value.overlayType)
        assertEquals(Languages.ADYGHE_LATIN_1.id, store.state.value.languageId)
        assertTrue(ic.composingWords.isEmpty())
    }

    private fun newStore(): ImeStore =
        ImeStore(LayoutProvider(), InputConnectionController(), NoOpSuggestionEngine())

    private fun findActionKey(store: ImeStore, action: KeyAction): Pair<Int, Int> {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == action) return row to col
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
