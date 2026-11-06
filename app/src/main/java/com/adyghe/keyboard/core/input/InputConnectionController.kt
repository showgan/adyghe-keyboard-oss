package com.adyghe.keyboard.core.input

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import java.text.BreakIterator

/**
 * Thin wrapper around the active [InputConnection] that abstracts the committing of text,
 * key events, and editor actions. Kept decoupled from the IME so it can be unit-tested.
 * This is a singleton so the [ImeStore] and the [InputMethodService] share the same active
 * connection reference.
 */
open class InputConnectionController {

    private var connection: InputConnection? = null

    /**
     * Last text applied with [setComposingText]. Empty when the editor has no live
     * composing span that we created. Used so later letters can update that span in
     * place instead of deleting and rewriting the word.
     */
    private var composingText: String = ""

    /**
     * Nested [InputConnection] writes currently in flight. Host [onUpdateSelection]
     * callbacks during these writes are echoes of our own edits, not caret taps.
     */
    private var editorMutationDepth: Int = 0

    fun isApplyingEditorMutation(): Boolean = editorMutationDepth > 0

    /** True while we have a live composing span that [setComposingText] would replace. */
    fun hasLiveComposingSpan(): Boolean = composingText.isNotEmpty()

    /**
     * Last caret index from [com.adyghe.keyboard.core.state.ImeStore.onSelectionUpdated].
     * Used so a stale empty [InputConnection.getTextBeforeCursor] is not treated as
     * "document start".
     */
    private var lastKnownSelStart: Int = -1

    fun setLastKnownSelection(start: Int, end: Int) {
        lastKnownSelStart = start
        // end is tracked by ImeStore; only the caret index is needed for stale-buffer guards.
    }

    /**
     * True when the live composing string is still the letter-run around the caret
     * (or immediately before it). Editors such as Keep often deliver a belated tap
     * [android.inputmethodservice.InputMethodService.onUpdateSelection] with
     * candidatesStart = -1 after we have already started a new span; finishing
     * composing in that case can jump the caret.
     */
    fun composingStillMatchesSurroundings(): Boolean {
        if (composingText.isEmpty()) return false
        val (before, after) = surroundingText()
        if (before.endsWith(composingText)) return true
        val word = before.takeLastWhile { it.isLetter() } + after.takeWhile { it.isLetter() }
        return word == composingText
    }

    private fun <T> mutatingEditor(block: () -> T): T {
        editorMutationDepth++
        try {
            return block()
        } finally {
            editorMutationDepth--
        }
    }

    fun setInputConnection(ic: InputConnection?) {
        if (connection !== ic) {
            composingText = ""
        }
        connection = ic
    }

    open fun commitText(text: String, newCursorPosition: Int = 1) {
        mutatingEditor {
            connection?.commitText(text, newCursorPosition)
            composingText = ""
        }
    }

    /**
     * Delete a non-empty editor selection, if any.
     *
     * [InputConnection.deleteSurroundingText] leaves the selection untouched and only
     * removes characters *around* it, so a selected range is replaced by [commitText]
     * (space, letters) but not by a naive backspace. Empty [commitText] is the same
     * replacement, with nothing inserted.
     *
     * [InputConnection.commitText] replaces an *active composing span* first and only
     * falls through to the selection when there isn't one. Space already calls
     * [finishComposingText] before committing; backspace must do the same, otherwise
     * a leftover composing word (e.g. "gh" in "ab cdef gh") is deleted and the
     * selected range is left alone.
     *
     * @return true if a selection was deleted.
     */
    open fun deleteSelection(): Boolean {
        val ic = connection ?: return false
        val selected = ic.getSelectedText(0)
        if (selected.isNullOrEmpty()) return false
        return mutatingEditor {
            ic.beginBatchEdit()
            try {
                finishComposingText()
                commitText("", 1)
            } finally {
                ic.endBatchEdit()
            }
            true
        }
    }

    open fun deleteBackward() {
        mutatingEditor {
            val ic = connection ?: return@mutatingEditor
            val before = ic.getTextBeforeCursor(GRAPHEME_LOOKBEHIND, 0)?.toString().orEmpty()
            val unitCount = if (before.isEmpty()) 1 else lastGraphemeUtf16Count(before)
            ic.deleteSurroundingText(unitCount, 0)
        }
    }

    /**
     * Text immediately around the cursor, used to reconstruct the composing word when the
     * user has moved the cursor into the middle of a word instead of typing at the end.
     */
    open fun surroundingText(chars: Int = 64): Pair<String, String> {
        val ic = connection ?: return "" to ""
        val before = ic.getTextBeforeCursor(chars, 0)?.toString() ?: ""
        val after = ic.getTextAfterCursor(chars, 0)?.toString() ?: ""
        return before to after
    }

    fun sendKeyEvent(keyEvent: KeyEvent) {
        mutatingEditor {
            connection?.sendKeyEvent(keyEvent)
        }
    }

    /** Send a down+up pair for a hardware key code (e.g. DPAD for cursor keys). */
    fun sendDownUpKeyEvent(keyCode: Int) {
        mutatingEditor {
            val ic = connection ?: return@mutatingEditor
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    fun handleEnter(editorInfo: EditorInfo?) {
        mutatingEditor {
            val imeOptions = editorInfo?.imeOptions ?: EditorInfo.IME_NULL
            val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
            val noEnterAction = imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0
            if (noEnterAction || actionId == EditorInfo.IME_ACTION_NONE) {
                connection?.commitText("\n", 1)
            } else {
                connection?.performEditorAction(actionId)
            }
        }
    }

    fun performEditorAction(actionId: Int) {
        connection?.performEditorAction(actionId)
    }

    fun setComposingText(text: String, newCursorPosition: Int = 1) {
        mutatingEditor {
            val ic = connection ?: return@mutatingEditor
            ic.setComposingText(text, newCursorPosition)
            composingText = text
        }
    }

    /**
     * Set the word around the cursor as a composing region.
     *
     * When the live composing span is still that word, only [setComposingText] is used
     * so the editor replaces the span in place. Deleting and rewriting on every letter
     * empties the field for a frame (visible flicker in RTL chat composers such as
     * WhatsApp).
     *
     * [setComposingText] only replaces an *existing* composing span. After
     * [finishComposingText] (e.g. language switch) the letters around the cursor are
     * ordinary committed text, so calling [setComposingText] alone would *insert* a
     * duplicate of the whole word. In that case we finish any leftover composing span,
     * delete the [replaceBefore]/[replaceAfter] letter-run we are about to rebuild,
     * then set the new composing text.
     *
     * Some editors ignore the relative newCursorPosition of [setComposingText] when the
     * replacement changes length, so the caret is re-asserted with [setSelection] only
     * when it must sit *inside* the word. When it belongs at the end we trust
     * [setComposingText] `(…, 1)` and skip [InputConnection.setSelection]: snapping from
     * [InputConnection.getTextBeforeCursor] is what reverses typing in editors that
     * report an empty or inverted before-cursor buffer.
     */
    open fun setComposingTextAt(
        word: String,
        cursorInWord: Int,
        replaceBefore: Int,
        replaceAfter: Int
    ) {
        val ic = connection ?: return
        mutatingEditor {
            ic.beginBatchEdit()
            try {
                if (canUpdateComposingInPlace(ic, replaceBefore, replaceAfter)) {
                    applyComposingText(ic, word, cursorInWord)
                    return@mutatingEditor
                }
                ic.finishComposingText()
                composingText = ""
                if (replaceBefore > 0 || replaceAfter > 0) {
                    ic.deleteSurroundingText(replaceBefore, replaceAfter)
                }
                applyComposingText(ic, word, cursorInWord)
            } finally {
                ic.endBatchEdit()
            }
        }
    }

    /**
     * True when [replaceBefore]/[replaceAfter] is still our composing span. Matching
     * surrounding text avoids treating a different word (cursor moved) as the span,
     * which would make [setComposingText] edit the old location.
     */
    private fun canUpdateComposingInPlace(
        ic: InputConnection,
        replaceBefore: Int,
        replaceAfter: Int
    ): Boolean {
        if (composingText.isEmpty()) return false
        if (replaceBefore + replaceAfter != composingText.length) return false
        val before = ic.getTextBeforeCursor(replaceBefore, 0)?.toString() ?: return false
        val after = ic.getTextAfterCursor(replaceAfter, 0)?.toString() ?: return false
        return before + after == composingText
    }

    private fun applyComposingText(ic: InputConnection, word: String, cursorInWord: Int) {
        if (word.isEmpty()) {
            ic.setComposingText("", 1)
            composingText = ""
            return
        }
        ic.setComposingText(word, 1)
        composingText = word
        if (cursorInWord != word.length) {
            val textBefore = ic.getTextBeforeCursor(1024, 0)?.toString() ?: return
            // After setComposingText(..., 1) the caret sits at the end of [word].
            // Only snap if the editor's before-cursor buffer actually ends with that
            // word. An empty or stale buffer (common after a caret tap in Keep) would
            // make wordStart=0 and jump the caret to the start of the field.
            if (!textBefore.endsWith(word)) return
            val wordEnd = textBefore.length
            val wordStart = wordEnd - word.length
            val pos = (wordStart + cursorInWord).coerceIn(wordStart, wordEnd)
            ic.setSelection(pos, pos)
        }
    }

    /**
     * Whether the caret sits immediately after [suffix]. A null connection (unit tests)
     * is treated as consistent so composing is not disabled without an editor.
     */
    open fun isCursorAfter(suffix: String): Boolean {
        if (suffix.isEmpty()) return true
        val ic = connection ?: return true
        val before = ic.getTextBeforeCursor(suffix.length, 0)?.toString() ?: return false
        return before.endsWith(suffix)
    }

    /**
     * If [suffix] is immediately *after* the caret (the editor ignored newCursorPosition
     * and left the caret at the insertion start), move the caret to after it so later
     * [commitText] calls append instead of prepend.
     */
    open fun ensureCursorAfter(suffix: String) {
        if (suffix.isEmpty()) return
        mutatingEditor {
            val ic = connection ?: return@mutatingEditor
            if (isCursorAfter(suffix)) return@mutatingEditor
            val before = ic.getTextBeforeCursor(1024, 0)?.toString() ?: ""
            val after = ic.getTextAfterCursor(suffix.length, 0)?.toString() ?: ""
            if (after.startsWith(suffix)) {
                // Empty before-cursor with a non-zero last caret means the read is
                // stale; snapping to suffix.length jumps to the start of the field.
                if (before.isEmpty() && lastKnownSelStart > 0) return@mutatingEditor
                val pos = before.length + suffix.length
                ic.setSelection(pos, pos)
            }
        }
    }

    open fun finishComposingText() {
        mutatingEditor {
            connection?.finishComposingText()
            composingText = ""
        }
    }

    private companion object {
        /** Enough UTF-16 units to cover a ZWJ / flag / skin-tone cluster. */
        const val GRAPHEME_LOOKBEHIND = 32

        fun lastGraphemeUtf16Count(before: String): Int {
            val iterator = BreakIterator.getCharacterInstance()
            iterator.setText(before)
            val end = before.length
            val start = iterator.preceding(end)
            return if (start == BreakIterator.DONE) 1 else (end - start).coerceAtLeast(1)
        }
    }
}
