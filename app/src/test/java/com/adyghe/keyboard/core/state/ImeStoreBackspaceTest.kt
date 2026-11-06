package com.adyghe.keyboard.core.state

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ImeStoreBackspaceTest {

    @Test
    fun backspace_withSelection_deletesSelectionNotSurrounding() {
        val ic = RecordingInputConnectionController().apply { selected = "hello" }
        val store = storeWith(ic, ordinaryEditor())

        tapBackspace(store)

        assertEquals(1, ic.deleteSelectionCalls)
        assertEquals(0, ic.deleteBackwardCalls)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun backspace_withSelection_skipsComposingRebuildAroundIt() {
        val ic = RecordingInputConnectionController().apply {
            selected = "xx"
            textBefore = "hel"
            textAfter = "lo"
        }
        val store = storeWith(ic, ordinaryEditor())

        tapBackspace(store)

        assertEquals(1, ic.deleteSelectionCalls)
        assertEquals(0, ic.deleteBackwardCalls)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun backspace_withoutSelection_stillDeletesBackward() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())
        store.switchTo("english_us")

        tapBackspace(store)

        assertEquals(0, ic.deleteSelectionCalls)
        assertEquals(1, ic.deleteBackwardCalls)
    }

    @Test
    fun onBackspace_withEmojiOverlay_deletesWithoutClosingPanel() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())
        store.switchTo("english_us")
        val (row, col) = findActionKey(store, KeyAction.EMOJI)
        store.onKeyTap(row, col)
        assertEquals(OverlayType.EMOJI, store.state.value.overlayType)

        store.onBackspace()

        assertEquals(1, ic.deleteBackwardCalls)
        assertEquals(OverlayType.EMOJI, store.state.value.overlayType)
    }

    private fun storeWith(
        ic: RecordingInputConnectionController,
        info: EditorInfo
    ): ImeStore {
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.setEditorInfo(info)
        store.onInputStarted()
        return store
    }

    private fun ordinaryEditor(): EditorInfo = EditorInfo().apply {
        packageName = "com.example.notes"
        inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun redditEditor(): EditorInfo = EditorInfo().apply {
        packageName = "com.reddit.frontpage"
        inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun tapBackspace(store: ImeStore) {
        val (row, col) = findActionKey(store, KeyAction.BACKSPACE)
        store.onKeyTap(row, col)
    }

    private fun findActionKey(store: ImeStore, action: KeyAction): Pair<Int, Int> {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == action) return row to col
            }
        }
        error("No key for $action")
    }

    private class RecordingInputConnectionController : InputConnectionController() {
        var selected: CharSequence? = null
        var textBefore: String = ""
        var textAfter: String = ""
        var deleteSelectionCalls = 0
        var deleteBackwardCalls = 0
        var setComposingTextAtCalls = 0

        override fun deleteSelection(): Boolean {
            if (selected.isNullOrEmpty()) return false
            deleteSelectionCalls++
            selected = null
            return true
        }

        override fun deleteBackward() {
            deleteBackwardCalls++
        }

        override fun setComposingTextAt(
            word: String,
            cursorInWord: Int,
            replaceBefore: Int,
            replaceAfter: Int
        ) {
            setComposingTextAtCalls++
        }

        override fun surroundingText(chars: Int): Pair<String, String> = textBefore to textAfter
    }
}
