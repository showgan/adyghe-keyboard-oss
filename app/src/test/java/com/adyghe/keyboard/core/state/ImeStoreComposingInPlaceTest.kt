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

class ImeStoreComposingInPlaceTest {

    @Test
    fun hebrewLetters_doNotDeleteAndRewriteEachKey() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        store.switchTo("hebrew")
        editor.ops.clear()

        tapChar(store, "ש")
        assertEquals("ש", editor.text)
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
        editor.ops.clear()

        tapChar(store, "ל")
        tapChar(store, "ו")

        assertEquals("שלו", editor.text)
        assertEquals(
            listOf("setComposingText(של)", "setComposingText(שלו)"),
            editor.editorOps()
        )
        assertFalse("finishComposing" in editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
    }

    @Test
    fun afterSpace_nextWordStillUpdatesInPlace() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        store.switchTo("hebrew")
        tapChar(store, "ש")
        tapChar(store, "ל")
        tapSpace(store)
        editor.ops.clear()

        tapChar(store, "ו")
        editor.ops.clear()
        tapChar(store, "ם")

        assertEquals("של ום", editor.text)
        assertEquals(listOf("setComposingText(ום)"), editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
    }

    @Test
    fun intoCommittedWord_stillDeletesThenComposes() {
        val editor = RecordingEditorConnection()
        val store = storeWith(editor)
        store.switchTo("english_us")
        tapChar(store, "h")
        tapChar(store, "i")
        store.switchTo("english_us")
        editor.ops.clear()

        tapChar(store, "s")

        assertEquals("his", editor.text)
        assertTrue(editor.editorOps().any { it == "deleteSurrounding(2,0)" })
        assertTrue("setComposingText(his)" in editor.editorOps())
    }

    private fun storeWith(editor: RecordingEditorConnection): ImeStore {
        val ic = InputConnectionController().apply { setInputConnection(editor) }
        val store = ImeStore(LayoutProvider(), ic, NoOpSuggestionEngine())
        store.setEditorInfo(EditorInfo().apply {
            packageName = "com.whatsapp"
            inputType = InputType.TYPE_CLASS_TEXT
        })
        store.onInputStarted()
        return store
    }

    private fun tapChar(store: ImeStore, ch: String) {
        val (row, col) = findCharKey(store, ch)
        store.onKeyTap(row, col)
    }

    private fun tapSpace(store: ImeStore) {
        val (row, col) = findActionKey(store, KeyAction.SPACE)
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
