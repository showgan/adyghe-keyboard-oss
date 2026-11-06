package com.adyghe.keyboard.core.input

import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputConnectionControllerDeleteSelectionTest {

    @Test
    fun deleteSelection_replacesSelectedRange_notComposingWord() {
        val editor = FakeEditorConnection(text = "ab cdef gh", selStart = 3, selEnd = 7).apply {
            setComposing(8, 10)
        }
        val controller = InputConnectionController().apply { setInputConnection(editor) }

        assertTrue(controller.deleteSelection())
        assertEquals("ab  gh", editor.text)
    }

    @Test
    fun deleteSelection_withoutComposing_stillReplacesSelection() {
        val editor = FakeEditorConnection(text = "ab cdef gh", selStart = 3, selEnd = 7)
        val controller = InputConnectionController().apply { setInputConnection(editor) }

        assertTrue(controller.deleteSelection())
        assertEquals("ab  gh", editor.text)
    }

    @Test
    fun deleteSelection_withoutSelection_isNoOp() {
        val editor = FakeEditorConnection(text = "ab cdef gh", selStart = 10, selEnd = 10)
        val controller = InputConnectionController().apply { setInputConnection(editor) }

        assertFalse(controller.deleteSelection())
        assertEquals("ab cdef gh", editor.text)
    }

    /**
     * Models Android [InputConnection.commitText]: composing text is replaced when a
     * span is active; the selection is replaced only when there is no composing span.
     */
    private class FakeEditorConnection(
        var text: String,
        var selStart: Int,
        var selEnd: Int
    ) : StubInputConnection() {
        private var composingStart = -1
        private var composingEnd = -1

        fun setComposing(start: Int, end: Int) {
            composingStart = start
            composingEnd = end
        }

        override fun getSelectedText(flags: Int): CharSequence {
            if (selStart == selEnd) return ""
            val start = minOf(selStart, selEnd)
            val end = maxOf(selStart, selEnd)
            return text.substring(start, end)
        }

        override fun finishComposingText(): Boolean {
            composingStart = -1
            composingEnd = -1
            return true
        }

        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            val insert = text?.toString().orEmpty()
            if (composingStart >= 0 && composingEnd >= composingStart) {
                replace(composingStart, composingEnd, insert)
            } else {
                replace(minOf(selStart, selEnd), maxOf(selStart, selEnd), insert)
            }
            composingStart = -1
            composingEnd = -1
            return true
        }

        private fun replace(start: Int, end: Int, insert: String) {
            this.text = this.text.substring(0, start) + insert + this.text.substring(end)
            val cursor = start + insert.length
            selStart = cursor
            selEnd = cursor
        }
    }

    private open class StubInputConnection : InputConnection {
        override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? = null
        override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? = null
        override fun getSelectedText(flags: Int): CharSequence? = null
        override fun getCursorCapsMode(reqModes: Int): Int = 0
        override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = null
        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean = false
        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean = false
        override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean = false
        override fun setComposingRegion(start: Int, end: Int): Boolean = false
        override fun finishComposingText(): Boolean = false
        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean = false
        override fun commitCompletion(text: CompletionInfo?): Boolean = false
        override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = false
        override fun setSelection(start: Int, end: Int): Boolean = false
        override fun performEditorAction(editorAction: Int): Boolean = false
        override fun performContextMenuAction(id: Int): Boolean = false
        override fun beginBatchEdit(): Boolean = true
        override fun endBatchEdit(): Boolean = true
        override fun sendKeyEvent(event: KeyEvent?): Boolean = false
        override fun clearMetaKeyStates(states: Int): Boolean = false
        override fun reportFullscreenMode(enabled: Boolean): Boolean = false
        override fun performPrivateCommand(action: String?, data: Bundle?): Boolean = false
        override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = false
        override fun getHandler(): Handler? = null
        override fun closeConnection() = Unit
        override fun commitContent(
            inputContentInfo: InputContentInfo,
            flags: Int,
            opts: Bundle?
        ): Boolean = false
    }
}
