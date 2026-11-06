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

/**
 * Minimal editor that records [InputConnection] calls and maintains text, caret, and
 * composing span the way [android.view.inputmethod.BaseInputConnection] does.
 */
class RecordingEditorConnection(
    var text: String = "",
    var cursor: Int = 0
) : InputConnection {

    val ops = mutableListOf<String>()
    var composingStart: Int = -1
        private set
    var composingEnd: Int = -1
        private set

    /**
     * When non-null, [getTextBeforeCursor] returns this instead of the real prefix.
     * Models editors (Keep, some WebViews) that report an empty/stale before-cursor
     * buffer after a caret tap while writes still go to the real caret.
     */
    var staleTextBefore: String? = null

    fun editorOps(): List<String> = ops.filter { it != "beginBatch" && it != "endBatch" }

    fun clearComposingSpan() {
        composingStart = -1
        composingEnd = -1
    }

    fun moveCaret(pos: Int) {
        cursor = pos.coerceIn(0, text.length)
    }

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        staleTextBefore?.let { return it.takeLast(n.coerceAtLeast(0)) }
        val start = (cursor - n).coerceAtLeast(0)
        return text.substring(start, cursor)
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
        val end = (cursor + n).coerceAtMost(text.length)
        return text.substring(cursor, end)
    }

    override fun getSelectedText(flags: Int): CharSequence = ""

    override fun finishComposingText(): Boolean {
        ops += "finishComposing"
        composingStart = -1
        composingEnd = -1
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        ops += "deleteSurrounding($beforeLength,$afterLength)"
        val start = (cursor - beforeLength).coerceAtLeast(0)
        val end = (cursor + afterLength).coerceAtMost(text.length)
        text = text.removeRange(start, end)
        cursor = start
        return true
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        val insert = text?.toString().orEmpty()
        ops += "setComposingText($insert)"
        val start: Int
        val end: Int
        if (composingStart >= 0 && composingEnd >= composingStart) {
            start = composingStart
            end = composingEnd
        } else {
            start = cursor
            end = cursor
        }
        this.text = this.text.substring(0, start) + insert + this.text.substring(end)
        if (insert.isEmpty()) {
            composingStart = -1
            composingEnd = -1
            cursor = start
        } else {
            composingStart = start
            composingEnd = start + insert.length
            cursor = start + insert.length
        }
        return true
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        ops += "setSelection($start,$end)"
        cursor = start.coerceIn(0, text.length)
        return true
    }

    override fun beginBatchEdit(): Boolean {
        ops += "beginBatch"
        return true
    }

    override fun endBatchEdit(): Boolean {
        ops += "endBatch"
        return true
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        val insert = text?.toString().orEmpty()
        ops += "commitText($insert)"
        val start: Int
        val end: Int
        if (composingStart >= 0 && composingEnd >= composingStart) {
            start = composingStart
            end = composingEnd
        } else {
            start = cursor
            end = cursor
        }
        this.text = this.text.substring(0, start) + insert + this.text.substring(end)
        composingStart = -1
        composingEnd = -1
        cursor = start + insert.length
        return true
    }

    override fun getCursorCapsMode(reqModes: Int): Int = 0
    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = null
    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean = false
    override fun setComposingRegion(start: Int, end: Int): Boolean = false
    override fun commitCompletion(text: CompletionInfo?): Boolean = false
    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = false
    override fun performEditorAction(editorAction: Int): Boolean = false
    override fun performContextMenuAction(id: Int): Boolean = false
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
