package com.adyghe.keyboard.core.state

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeStoreRtlCommitTest {

    @Test
    fun arabicLetter_inOrdinaryEditor_usesComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, ordinaryEditor())
        store.switchTo("arabic")

        tapChar(store, "ن")

        assertTrue(ic.committed.isEmpty())
        assertEquals(1, ic.setComposingTextAtCalls)
        assertEquals(listOf("ن"), ic.composingWords)
    }

    @Test
    fun hebrewLetter_inOrdinaryEditor_usesComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, ordinaryEditor())
        store.switchTo("hebrew")

        tapChar(store, "ש")

        assertTrue(ic.committed.isEmpty())
        assertEquals(1, ic.setComposingTextAtCalls)
    }

    @Test
    fun englishLetter_inOrdinaryEditor_usesComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, ordinaryEditor())
        store.switchTo("english_us")

        tapChar(store, "a")

        assertTrue(ic.committed.isEmpty())
        assertEquals(1, ic.setComposingTextAtCalls)
        assertEquals(listOf("a"), ic.composingWords)
    }

    @Test
    fun englishLetter_inReddit_usesCommitTextNotComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())
        store.switchTo("english_us")

        tapChar(store, "h")
        tapChar(store, "i")

        store.switchTo("adyghe_latin")
        tapChar(store, "a")

        assertEquals(listOf("h", "i", "a"), ic.committed)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun arabicLetter_inReddit_usesCommitTextNotComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())
        store.switchTo("arabic")

        tapChar(store, "ن")
        tapChar(store, "ت")

        assertEquals(listOf("ن", "ت"), ic.committed)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun turkishAndRussian_inReddit_useCommitText() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())

        store.switchTo("turkish")
        tapChar(store, "a")
        store.switchTo("russian")
        tapChar(store, "а")

        assertEquals(listOf("a", "а"), ic.committed)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun redditBackspace_usesDeleteBackwardNotComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, redditEditor())
        store.switchTo("english_us")

        tapChar(store, "a")
        ic.reset()

        val (bsRow, bsCol) = findActionKey(store, KeyAction.BACKSPACE)
        store.onKeyTap(bsRow, bsCol)

        assertEquals(1, ic.deleteBackwardCalls)
        assertEquals(0, ic.setComposingTextAtCalls)
    }

    @Test
    fun cursorMismatch_disablesComposingForLaterLetters() {
        val ic = RecordingInputConnectionController().apply { cursorAfterComposing = false }
        val store = storeWith(ic, ordinaryEditor())
        store.switchTo("english_us")

        tapChar(store, "h")
        tapChar(store, "i")

        assertEquals(1, ic.setComposingTextAtCalls)
        assertEquals(listOf("i"), ic.committed)
        assertEquals(1, ic.ensureCursorAfterCalls)
    }

    @Test
    fun englishLetter_inPassword_usesCommitTextNotComposing() {
        val ic = RecordingInputConnectionController()
        val store = storeWith(ic, passwordEditor())
        store.switchTo("english_us")

        tapChar(store, "h")
        tapChar(store, "i")

        assertEquals(listOf("h", "i"), ic.committed)
        assertEquals(0, ic.setComposingTextAtCalls)
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

    private fun passwordEditor(): EditorInfo = EditorInfo().apply {
        packageName = "com.example.bank"
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    private fun tapChar(store: ImeStore, ch: String) {
        val (row, col) = findCharKey(store, ch)
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

    private class RecordingInputConnectionController : InputConnectionController() {
        val committed = mutableListOf<String>()
        val composingWords = mutableListOf<String>()
        var setComposingTextAtCalls = 0
        var deleteBackwardCalls = 0
        var ensureCursorAfterCalls = 0
        var cursorAfterComposing = true

        fun reset() {
            committed.clear()
            composingWords.clear()
            setComposingTextAtCalls = 0
            deleteBackwardCalls = 0
            ensureCursorAfterCalls = 0
        }

        override fun commitText(text: String, newCursorPosition: Int) {
            committed += text
        }

        override fun setComposingTextAt(
            word: String,
            cursorInWord: Int,
            replaceBefore: Int,
            replaceAfter: Int
        ) {
            setComposingTextAtCalls++
            composingWords += word
        }

        override fun isCursorAfter(suffix: String): Boolean = cursorAfterComposing

        override fun ensureCursorAfter(suffix: String) {
            ensureCursorAfterCalls++
        }

        override fun finishComposingText() = Unit

        override fun deleteBackward() {
            deleteBackwardCalls++
        }
    }
}
