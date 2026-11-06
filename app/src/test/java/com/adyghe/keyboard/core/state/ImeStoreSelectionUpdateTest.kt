package com.adyghe.keyboard.core.state

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.input.RecordingEditorConnection
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeStoreSelectionUpdateTest {

    @Test
    fun hostCaretMove_backspaceDeletesAtNewLocationNotOldComposingSpan() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hello")
        tapSpace(store)
        type(store, "hello")
        assertEquals("hello hello", editor.text)
        assertEquals(6, editor.composingStart)
        assertEquals(11, editor.composingEnd)

        // Tap the end of the first "hello" while the composing span stays on the second.
        simulateHostCaretMove(store, editor, to = 5)

        tapBackspace(store)

        assertEquals("hell hello", editor.text)
        assertEquals(4, editor.cursor)
    }

    @Test
    fun hostCaretMove_intoEarlierWord_backspaceEditsThatWord() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hello")
        tapSpace(store)
        type(store, "hello")

        simulateHostCaretMove(store, editor, to = 2)

        tapBackspace(store)

        assertEquals("hllo hello", editor.text)
        assertEquals(1, editor.cursor)
    }

    @Test
    fun hostCaretMove_nextLetterInsertsAtNewLocation() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "ab")
        tapSpace(store)
        type(store, "cd")

        simulateHostCaretMove(store, editor, to = 1)

        tapChar(store, "x")

        assertEquals("axb cd", editor.text)
        assertEquals(2, editor.cursor)
    }

    @Test
    fun hostCaretMove_clearsComposingWordAndSpan() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hi")
        tapSpace(store)
        type(store, "ab")
        assertEquals("ab", store.state.value.composingWord)
        assertTrue(editor.composingStart >= 0)

        simulateHostCaretMove(store, editor, to = 1)

        assertEquals("", store.state.value.composingWord)
        assertEquals(-1, editor.composingStart)
        assertEquals(-1, editor.composingEnd)
    }

    @Test
    fun hostCaretMove_whenEditorAlreadyClearedSpan_stillResetsComposer() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hello")
        tapSpace(store)
        type(store, "world")
        val old = editor.cursor
        editor.moveCaret(2)
        editor.clearComposingSpan()
        store.onSelectionUpdated(old, old, 2, 2, -1, -1)
        assertEquals("", store.state.value.composingWord)

        tapBackspace(store)

        assertEquals("hllo world", editor.text)
    }

    @Test
    fun composingEcho_doesNotAbandonInPlaceUpdates() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        tapChar(store, "h")
        store.onSelectionUpdated(
            oldSelStart = 0,
            oldSelEnd = 0,
            newSelStart = 1,
            newSelEnd = 1,
            candidatesStart = 0,
            candidatesEnd = 1
        )
        editor.ops.clear()

        tapChar(store, "i")

        assertEquals("hi", editor.text)
        assertEquals(listOf("setComposingText(hi)"), editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
        assertFalse("finishComposing" in editor.editorOps())
        assertEquals("hi", store.state.value.composingWord)
    }

    @Test
    fun duplicateSelectionCallback_doesNotFinishComposing() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hi")
        store.onSelectionUpdated(0, 0, 2, 2, 0, 2)
        editor.ops.clear()

        store.onSelectionUpdated(2, 2, 2, 2, 0, 2)

        assertFalse("finishComposing" in editor.editorOps())
        assertEquals("hi", store.state.value.composingWord)
        assertEquals(0, editor.composingStart)
        assertEquals(2, editor.composingEnd)
    }

    @Test
    fun hostCaretMove_firstLetterCommitsAtNewCaret() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hello")
        tapSpace(store)
        type(store, "world")
        simulateHostCaretMove(store, editor, to = 2)
        editor.ops.clear()

        tapChar(store, "x")

        assertEquals("hexllo world", editor.text)
        assertEquals(3, editor.cursor)
        assertTrue("commitText(x)" in editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("setComposingText") })
        assertFalse(editor.editorOps().any { it.startsWith("setSelection") })
    }

    @Test
    fun hostCaretMove_delayedDuplicateTapAfterFirstLetter_doesNotJumpCaret() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hello")
        tapSpace(store)
        type(store, "world")
        simulateHostCaretMove(store, editor, to = 2)
        tapChar(store, "x")
        val cursorAfterFirst = editor.cursor
        editor.ops.clear()

        // Keep often re-delivers the tap with candidatesStart=-1 after the first key.
        store.onSelectionUpdated(11, 11, 2, 2, -1, -1)

        assertEquals(cursorAfterFirst, editor.cursor)
        assertEquals("hexllo world", editor.text)
        assertFalse("finishComposing" in editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("setSelection") })
    }

    @Test
    fun belatedTapWithNoCandidates_doesNotFinishMatchingComposingSpan() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        type(store, "hi")
        store.onSelectionUpdated(0, 0, 2, 2, 0, 2)
        editor.ops.clear()

        store.onSelectionUpdated(2, 2, 3, 3, -1, -1)

        assertFalse("finishComposing" in editor.editorOps())
        assertEquals("hi", editor.text)
        assertEquals("hi", store.state.value.composingWord)
    }

    private fun storeWith(editor: RecordingEditorConnection): ImeStore {
        val ic = InputConnectionController().apply { setInputConnection(editor) }
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.setEditorInfo(
            EditorInfo().apply {
                packageName = "com.example.notes"
                inputType = InputType.TYPE_CLASS_TEXT
                initialSelStart = 0
                initialSelEnd = 0
            }
        )
        store.onInputStarted()
        store.switchTo("english_us")
        return store
    }

    private fun type(store: ImeStore, text: String) {
        text.forEach { tapChar(store, it.toString()) }
    }

    private fun simulateHostCaretMove(
        store: ImeStore,
        editor: RecordingEditorConnection,
        to: Int
    ) {
        val old = editor.cursor
        val candidatesStart = editor.composingStart
        val candidatesEnd = editor.composingEnd
        editor.moveCaret(to)
        store.onSelectionUpdated(
            oldSelStart = old,
            oldSelEnd = old,
            newSelStart = to,
            newSelEnd = to,
            candidatesStart = candidatesStart,
            candidatesEnd = candidatesEnd
        )
    }

    private fun tapChar(store: ImeStore, ch: String) {
        val (row, col) = findCharKey(store, ch)
        store.onKeyTap(row, col)
    }

    private fun tapSpace(store: ImeStore) {
        val (row, col) = findActionKey(store, KeyAction.SPACE)
        store.onKeyTap(row, col)
    }

    private fun tapBackspace(store: ImeStore) {
        val (row, col) = findActionKey(store, KeyAction.BACKSPACE)
        store.onKeyTap(row, col)
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

    private fun findActionKey(store: ImeStore, action: KeyAction): Pair<Int, Int> {
        store.currentRows().forEachIndexed { row, keyboardRow ->
            keyboardRow.keys.forEachIndexed { col, key ->
                if (key.action == action) return row to col
            }
        }
        error("No key for $action")
    }
}
