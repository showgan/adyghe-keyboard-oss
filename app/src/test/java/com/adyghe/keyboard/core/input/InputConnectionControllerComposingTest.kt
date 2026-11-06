package com.adyghe.keyboard.core.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputConnectionControllerComposingTest {

    @Test
    fun firstLetter_setsComposingWithoutDeleting() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)

        controller.setComposingTextAt("ש", 1, replaceBefore = 0, replaceAfter = 0)

        assertEquals("ש", editor.text)
        assertEquals(listOf("finishComposing", "setComposingText(ש)"), editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
    }

    @Test
    fun secondLetter_updatesLiveSpanInPlace() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("ש", 1, 0, 0)
        editor.ops.clear()

        controller.setComposingTextAt("של", 2, replaceBefore = 1, replaceAfter = 0)

        assertEquals("של", editor.text)
        assertEquals(listOf("setComposingText(של)"), editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
        assertFalse("finishComposing" in editor.editorOps())
    }

    @Test
    fun englishLetters_alsoUpdateInPlace() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("h", 1, 0, 0)
        editor.ops.clear()

        controller.setComposingTextAt("hi", 2, 1, 0)

        assertEquals("hi", editor.text)
        assertEquals(listOf("setComposingText(hi)"), editor.editorOps())
    }

    @Test
    fun afterFinish_rebuildsCommittedWord() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("ab", 2, 0, 0)
        controller.finishComposingText()
        editor.ops.clear()

        controller.setComposingTextAt("abc", 3, replaceBefore = 2, replaceAfter = 0)

        assertEquals("abc", editor.text)
        assertTrue(editor.editorOps().any { it == "deleteSurrounding(2,0)" })
        assertTrue("setComposingText(abc)" in editor.editorOps())
    }

    @Test
    fun surroundingMismatch_rebuildsInsteadOfEditingOldSpan() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("hello", 5, 0, 0)
        editor.text = "world"
        editor.moveCaret(5)
        editor.clearComposingSpan()
        editor.ops.clear()

        controller.setComposingTextAt("worlds", 6, replaceBefore = 5, replaceAfter = 0)

        assertEquals("worlds", editor.text)
        assertTrue(editor.editorOps().any { it == "deleteSurrounding(5,0)" })
    }

    @Test
    fun midWordInsert_updatesInPlaceAndRestoresCaret() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("hllo", 4, 0, 0)
        editor.moveCaret(1)
        editor.ops.clear()

        controller.setComposingTextAt("hello", cursorInWord = 2, replaceBefore = 1, replaceAfter = 3)

        assertEquals("hello", editor.text)
        assertEquals(listOf("setComposingText(hello)", "setSelection(2,2)"), editor.editorOps())
        assertEquals(2, editor.cursor)
    }

    @Test
    fun staleEmptyBeforeCursor_doesNotSnapSelectionToStart() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("hllo", 4, 0, 0)
        editor.moveCaret(1)
        editor.staleTextBefore = ""
        editor.ops.clear()

        controller.setComposingTextAt("hello", cursorInWord = 2, replaceBefore = 1, replaceAfter = 3)

        assertEquals("hello", editor.text)
        assertTrue("setComposingText(hello)" in editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("setSelection") })
        // setComposingText(..., 1) leaves the caret at the end of the span, not index 0.
        assertEquals(5, editor.cursor)
    }

    @Test
    fun ensureCursorAfter_staleEmptyBefore_doesNotSnapToIndexOne() {
        val editor = RecordingEditorConnection("hello world", cursor = 6)
        val controller = controller(editor)
        controller.setLastKnownSelection(6, 6)
        editor.text = "hello xworld"
        editor.moveCaret(6)
        editor.staleTextBefore = ""
        editor.ops.clear()

        controller.ensureCursorAfter("x")

        assertFalse(editor.editorOps().any { it.startsWith("setSelection") })
        assertEquals(6, editor.cursor)
    }

    @Test
    fun shrinkingToEmpty_clearsSpanWithoutDelete() {
        val editor = RecordingEditorConnection()
        val controller = controller(editor)
        controller.setComposingTextAt("ש", 1, 0, 0)
        editor.ops.clear()

        controller.setComposingTextAt("", 0, replaceBefore = 1, replaceAfter = 0)

        assertEquals("", editor.text)
        assertEquals(listOf("setComposingText()"), editor.editorOps())
        assertFalse(editor.editorOps().any { it.startsWith("deleteSurrounding") })
    }

    private fun controller(editor: RecordingEditorConnection): InputConnectionController {
        return InputConnectionController().apply { setInputConnection(editor) }
    }
}
