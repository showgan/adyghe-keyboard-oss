package com.adyghe.keyboard.core.state

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.input.EditorComposingPolicy
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.input.InputTypeResolver
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.KeyboardKey
import com.adyghe.keyboard.core.layout.KeyboardRow
import com.adyghe.keyboard.core.layout.LayoutMode
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.core.language.LanguagePreferences
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.language.InMemoryLanguagePreferences
import com.adyghe.keyboard.core.language.applyLanguageEnabled
import com.adyghe.keyboard.core.language.preferredEnabledLanguageId
import com.adyghe.keyboard.core.prefs.InMemoryKeyboardPreferences
import com.adyghe.keyboard.core.prefs.KeyboardPreferences
import com.adyghe.keyboard.predictive.SuggestionEngine
import com.adyghe.keyboard.predictive.WordComposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central MVI store for the keyboard. Holds the observable UI state and reduces intents
 * (key presses, long-presses, popup drags, mode changes, language switching). Text output
 * is routed through [InputConnectionController] and composing word through [WordComposer],
 * which feeds the [SuggestionEngine].
 */
class ImeStore(
    private val layoutProvider: LayoutProvider,
    private val inputConnection: InputConnectionController,
    private val suggestionEngine: SuggestionEngine,
    private val languagePreferences: LanguagePreferences = InMemoryLanguagePreferences(),
    private val keyboardPreferences: KeyboardPreferences = InMemoryKeyboardPreferences()
) {

    private val _state = MutableStateFlow(
        initialImeState(languagePreferences, keyboardPreferences)
    )
    val state: StateFlow<ImeState> = _state.asStateFlow()

    private val composer = WordComposer()

    private var currentEditorInfo: EditorInfo? = null

    /**
     * When false, letters are [InputConnectionController.commitText] rather than
     * composed. Set from [EditorComposingPolicy] for the focused editor, and cleared
     * at runtime if that editor still prepends composing updates.
     */
    private var useEditorComposing: Boolean = true

    /**
     * Last caret/selection reported by the host (or by our own editor writes).
     * Used so duplicate [onSelectionUpdated] callbacks are not treated as caret taps.
     */
    private var lastSelStart: Int = -1
    private var lastSelEnd: Int = -1

    /**
     * After a host caret move, the next letter is [InputConnectionController.commitText]
     * so we do not immediately [InputConnection.setComposingText] / [isCursorAfter]
     * against a stale Keep/WebView selection cache. That cache is what jumps the
     * caret to an old location after the first character.
     */
    private var commitNextLetter: Boolean = false

    /**
     * Skip the one-shot [InputConnectionController.isCursorAfter] "correction" after
     * a host caret move; the before-cursor buffer is often empty or stale for that
     * first keystroke and [ensureCursorAfter] would snap to the wrong index.
     */
    private var suppressCursorCorrection: Boolean = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var backspaceRepeatJob: Job? = null

    /** Optional callback invoked whenever a key produces a tap (for haptic feedback). */
    var onKeyAction: (() -> Unit)? = null

    fun setEditorInfo(info: EditorInfo?) {
        currentEditorInfo = info
        useEditorComposing = EditorComposingPolicy.allowsComposing(info)
        lastSelStart = info?.initialSelStart ?: -1
        lastSelEnd = info?.initialSelEnd ?: -1
        inputConnection.setLastKnownSelection(lastSelStart, lastSelEnd)
        val context = InputTypeResolver.resolve(info)
        update {
            copy(
                enterAction = resolveEnterAction(info),
                mode = context.preferredMode,
                suggestionsEnabled = context.suggestionsEnabled,
                numberPadVariant = context.numberPadVariant,
                letterChrome = context.letterChrome,
                suggestions = if (context.suggestionsEnabled) suggestions else emptyList(),
                composingWord = if (context.suggestionsEnabled) composingWord else "",
                hasExactSuggestion = if (context.suggestionsEnabled) hasExactSuggestion else false
            )
        }
    }

    private fun resolveEnterAction(info: EditorInfo?): EnterKeyAction {
        if (info == null) return EnterKeyAction.RETURN
        val imeOptions = info.imeOptions
        if (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
            return EnterKeyAction.RETURN
        }
        return when (imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_SEARCH -> EnterKeyAction.SEARCH
            EditorInfo.IME_ACTION_GO -> EnterKeyAction.GO
            EditorInfo.IME_ACTION_SEND -> EnterKeyAction.SEND
            EditorInfo.IME_ACTION_DONE -> EnterKeyAction.DONE
            EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_PREVIOUS -> EnterKeyAction.NEXT
            else -> EnterKeyAction.RETURN
        }
    }

    /**
     * Reset per-editor state when the input target changes (new app, new field, or the
     * keyboard re-attaching to the same field). Clears the in-progress composing word and
     * suggestions so text typed in one app is never leaked into another.
     *
     * The editor keeps whatever text it already has; we only drop the IME-side composing
     * state, so no user text is lost.
     */
    fun onInputStarted() {
        stopBackspaceRepeat()
        composer.reset()
        useEditorComposing = EditorComposingPolicy.allowsComposing(currentEditorInfo)
        lastSelStart = currentEditorInfo?.initialSelStart ?: -1
        lastSelEnd = currentEditorInfo?.initialSelEnd ?: -1
        inputConnection.setLastKnownSelection(lastSelStart, lastSelEnd)
        commitNextLetter = false
        suppressCursorCorrection = false
        inputConnection.finishComposingText()
        update {
            copy(
                suggestions = emptyList(),
                composingWord = "",
                hasExactSuggestion = false,
                overlayType = OverlayType.NONE,
                overlayRow = -1,
                overlayCol = -1,
                activePopup = emptyList(),
                popupAnchorIndex = 0,
                popupSelection = null
            )
        }
    }

    /**
     * The host editor moved the caret or selection (tap, handle drag, or a
     * programmatic change). Drop the live composing span and the in-memory word so
     * later backspace or letters edit at the new caret instead of
     * [android.view.inputmethod.InputConnection.setComposingText] rewriting the old
     * span.
     *
     * Callbacks that arrive while we are writing to the editor, that repeat the
     * last known selection, or that keep the caret inside the composing region,
     * are echoes of our own typing and must be ignored.
     */
    fun onSelectionUpdated(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        if (inputConnection.isApplyingEditorMutation()) {
            rememberSelection(newSelStart, newSelEnd)
            return
        }

        // Duplicate confirmation of the caret we already have (Keep sends the tap
        // both before and after the first key). Must not finishComposingText.
        if (newSelStart == lastSelStart && newSelEnd == lastSelEnd) {
            return
        }

        rememberSelection(newSelStart, newSelEnd)

        if (isSelectionInsideComposingSpan(
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd
            )
        ) {
            return
        }

        // Belated tap with candidatesStart=-1 after we already composed at the new
        // caret: the span is still around the cursor, so finishing it jumps Keep.
        if (candidatesStart < 0 && inputConnection.composingStillMatchesSurroundings()) {
            return
        }

        if (!composer.isComposing && !inputConnection.hasLiveComposingSpan()) {
            markCaretMovedByHost()
            return
        }

        abandonComposing()
    }

    private fun rememberSelection(start: Int, end: Int) {
        lastSelStart = start
        lastSelEnd = end
        inputConnection.setLastKnownSelection(start, end)
    }

    private fun markCaretMovedByHost() {
        commitNextLetter = true
        suppressCursorCorrection = true
    }

    private fun isSelectionInsideComposingSpan(
        selStart: Int,
        selEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ): Boolean {
        if (candidatesStart < 0 || candidatesEnd < candidatesStart) return false
        val start = minOf(selStart, selEnd)
        val end = maxOf(selStart, selEnd)
        return start >= candidatesStart && end <= candidatesEnd
    }

    private fun abandonComposing() {
        composer.reset()
        inputConnection.finishComposingText()
        markCaretMovedByHost()
        refreshSuggestions()
    }

    /** Rows for the current language/mode, in visual left-to-right order. */
    fun currentRows(): List<KeyboardRow> = rowsFor(_state.value)

    fun currentLanguageId(): String = _state.value.languageId

    // ------------------------------------------------------------------ helpers

    private fun currentLang(languageId: String) = Languages.fromId(languageId)

    private fun rowsFor(s: ImeState): List<KeyboardRow> {
        val lang = currentLang(s.languageId)
        return layoutProvider.rows(
            lang,
            s.mode,
            s.numberPadVariant,
            s.letterChrome,
            s.numberRowEnabled
        )
    }

    private fun update(transform: ImeState.() -> ImeState) {
        _state.value = transform(_state.value)
    }

    private fun hideOverlay() {
        update {
            copy(
                overlayType = OverlayType.NONE, overlayRow = -1, overlayCol = -1,
                activePopup = emptyList(), popupAnchorIndex = 0, popupSelection = null
            )
        }
    }

    private fun isUpper(): Boolean = when (_state.value.shift) {
        ShiftState.SHIFTED, ShiftState.CAPS_LOCK -> true
        ShiftState.UNSHIFTED -> false
    }

    private fun keyAt(row: Int, col: Int): KeyboardKey? {
        val rows = rowsFor(_state.value)
        return rows.getOrNull(row)?.keys?.getOrNull(col)
    }

    // ------------------------------------------------------------------ input events

    /** Finger down on a character key: show a brief preview. */
    fun onKeyDown(row: Int, col: Int) {
        val key = keyAt(row, col) ?: return
        if (key.action == KeyAction.CHAR) {
            update {
                copy(
                    overlayType = OverlayType.PREVIEW, overlayRow = row, overlayCol = col,
                    activePopup = emptyList(), popupAnchorIndex = 0, popupSelection = null
                )
            }
        }
    }

    /** Quick tap: commit a character or perform the key's action. */
    fun onKeyTap(row: Int, col: Int) {
        onKeyTap(row, col, System.currentTimeMillis())
    }

    /** Visible for testing: [nowMs] lets tests simulate double-tap timing. */
    internal fun onKeyTap(row: Int, col: Int, nowMs: Long) {
        val key = keyAt(row, col) ?: return
        hideOverlay()
        onKeyAction?.invoke()
        when (key.action) {
            KeyAction.CHAR -> handleChar(key.displayChar(isUpper()))
            KeyAction.SHIFT -> onShiftTap(nowMs)
            KeyAction.BACKSPACE -> backspace()
            KeyAction.SPACE -> space()
            KeyAction.ENTER -> enter()
            KeyAction.LANG_SWITCH -> {
                val enabledCount = Languages.ALL.count { it.id in _state.value.enabledLanguageIds }
                if (enabledCount <= 1) openLanguagePicker() else switchLang()
            }
            KeyAction.NUMBERS -> update { copy(mode = LayoutMode.NUMBERS) }
            KeyAction.SYMBOLS -> update { copy(mode = LayoutMode.SYMBOLS) }
            KeyAction.NUMBER_PAD -> update { copy(mode = LayoutMode.NUMBER_PAD) }
            KeyAction.ABC -> update {
                copy(mode = LayoutMode.LETTERS, overlayType = OverlayType.NONE)
            }
            KeyAction.EMOJI -> update {
                copy(
                    overlayType = OverlayType.EMOJI,
                    overlayRow = -1,
                    overlayCol = -1,
                    activePopup = emptyList(),
                    popupAnchorIndex = 0,
                    popupSelection = null
                )
            }
            KeyAction.CURSOR_LEFT -> moveCursor(KeyEvent.KEYCODE_DPAD_LEFT)
            KeyAction.CURSOR_RIGHT -> moveCursor(KeyEvent.KEYCODE_DPAD_RIGHT)
            else -> Unit
        }
    }

    /** Long-press on a character key: open the popup. Long-press on shift toggles caps lock.
     *  Long-press on backspace starts repeating deletes until [stopBackspaceRepeat].
     *  Long-press on space or the globe opens the language picker. */
    fun onLongPress(row: Int, col: Int) {
        val key = keyAt(row, col) ?: return
        hideOverlay()
        when (key.action) {
            KeyAction.SHIFT -> update {
                copy(
                    shift = if (shift == ShiftState.CAPS_LOCK) ShiftState.UNSHIFTED else ShiftState.CAPS_LOCK
                )
            }
            KeyAction.BACKSPACE -> startBackspaceRepeat()
            KeyAction.CHAR -> {
                val extras = key.popupExtras(isUpper())
                if (extras.isNotEmpty()) {
                    update {
                        copy(
                            overlayType = OverlayType.POPUP,
                            overlayRow = row,
                            overlayCol = col,
                            activePopup = extras,
                            popupAnchorIndex = 0,
                            popupSelection = PopupSelection(0, extras.first())
                        )
                    }
                } else {
                    onKeyTap(row, col)
                }
            }
            KeyAction.SPACE, KeyAction.LANG_SWITCH -> update {
                copy(
                    overlayType = OverlayType.LANGUAGE_PICKER,
                    overlayRow = row,
                    overlayCol = col
                )
            }
            else -> onKeyTap(row, col)
        }
    }

    /**
     * Replace the open popup's extras with a geometry-aware visual order and select
     * the extra that sits over the key.
     */
    fun setPopupVisual(chars: List<String>, anchorIndex: Int) {
        val s = _state.value
        if (s.overlayType != OverlayType.POPUP || chars.isEmpty()) return
        val idx = anchorIndex.coerceIn(0, chars.lastIndex)
        val char = chars[idx]
        update {
            copy(
                activePopup = chars,
                popupAnchorIndex = idx,
                popupSelection = PopupSelection(idx, char)
            )
        }
    }

    /** Finger drags within the popup: update the highlighted selection. */
    fun onPopupSelect(selection: PopupSelection) {
        val s = _state.value
        if (s.overlayType != OverlayType.POPUP) return
        val char = s.activePopup.getOrNull(selection.keyIndex)
        if (char != null) {
            update { copy(popupSelection = PopupSelection(selection.keyIndex, char)) }
        }
    }

    /** Finger lifted from the popup: commit the current selection. */
    fun onPopupRelease() {
        val s = _state.value
        val chosen = s.popupSelection?.char ?: s.activePopup.firstOrNull() ?: ""
        hideOverlay()
        if (chosen.isNotEmpty()) {
            onKeyAction?.invoke()
            handleChar(chosen)
        }
    }

    /** Finger lifted from a previewed key without a long-press: commit the base char,
     *  or for modifier keys (shift, backspace, lang-switch, etc.) perform the action. */
    fun onKeyRelease(row: Int, col: Int) {
        val prev = _state.value
        if (prev.overlayType == OverlayType.POPUP) return
        val key = keyAt(row, col) ?: return
        if (prev.overlayType == OverlayType.PREVIEW && key.action == KeyAction.CHAR) {
            hideOverlay()
            handleChar(key.displayChar(isUpper()))
        } else {
            onKeyTap(row, col)
        }
    }

    fun dismissOverlay() {
        val s = _state.value
        if (s.overlayType != OverlayType.NONE && s.overlayType != OverlayType.PREVIEW) {
            hideOverlay()
        }
    }

    /** Close emoji / clipboard overlays and return to the letter panel. */
    fun closePanelOverlay() {
        update {
            copy(
                overlayType = OverlayType.NONE,
                mode = LayoutMode.LETTERS,
                overlayRow = -1,
                overlayCol = -1,
                activePopup = emptyList(),
                popupAnchorIndex = 0,
                popupSelection = null
            )
        }
    }

    /**
     * Delete backward without dismissing the current overlay. Used by the emoji panel so a
     * mistaken emoji can be removed without switching back to letters.
     */
    fun onBackspace() {
        onKeyAction?.invoke()
        backspace()
    }

    /** Commit arbitrary text (emoji, clipboard paste) without treating it as a composing letter. */
    fun commitText(text: String) {
        if (text.isEmpty()) return
        onKeyAction?.invoke()
        composer.reset()
        inputConnection.finishComposingText()
        inputConnection.commitText(text)
        refreshSuggestions()
    }

    fun pasteLatestClipboard() {
        val text = _state.value.clipboardHistory.firstOrNull() ?: return
        commitText(text)
        hideOverlay()
    }

    fun pasteClipboardItem(text: String) {
        if (text.isEmpty()) return
        commitText(text)
        hideOverlay()
    }

    fun openClipboardHistory() {
        if (_state.value.clipboardHistory.isEmpty()) return
        update {
            copy(
                overlayType = OverlayType.CLIPBOARD,
                overlayRow = -1,
                overlayCol = -1,
                activePopup = emptyList(),
                popupAnchorIndex = 0,
                popupSelection = null
            )
        }
    }

    /** Show the in-keyboard language picker (same overlay as a long-press on space). */
    fun openLanguagePicker() {
        if (_state.value.overlayType == OverlayType.LANGUAGE_PICKER) {
            hideOverlay()
            return
        }
        update {
            copy(
                overlayType = OverlayType.LANGUAGE_PICKER,
                overlayRow = -1,
                overlayCol = -1,
                activePopup = emptyList(),
                popupAnchorIndex = 0,
                popupSelection = null
            )
        }
    }

    /**
     * Push a newly observed clipboard string into history (newest first, unique, capped).
     * Called from [com.adyghe.keyboard.core.clipboard.ClipboardMonitor].
     */
    fun onClipboardText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            update { copy(showPasteChip = clipboardHistory.isNotEmpty()) }
            return
        }
        update {
            val next = (listOf(trimmed) + clipboardHistory.filter { it != trimmed })
                .take(CLIPBOARD_HISTORY_CAP)
            copy(clipboardHistory = next, showPasteChip = true)
        }
    }

    private fun moveCursor(keyCode: Int) {
        abandonComposing()
        inputConnection.sendDownUpKeyEvent(keyCode)
    }

    // ------------------------------------------------------------------ actions

    /**
     * Keyboard-wide swipe: up turns on one-shot shift, down turns it (and caps lock) off.
     * Does not update [ImeState.lastShiftTapMs], so a later Shift-key tap is not a double-tap.
     * Clears a CHAR preview so the down key is not committed.
     */
    fun setShiftEnabled(enabled: Boolean) {
        val nextShift = when {
            enabled && _state.value.shift == ShiftState.UNSHIFTED -> ShiftState.SHIFTED
            enabled -> _state.value.shift
            else -> ShiftState.UNSHIFTED
        }
        if (_state.value.overlayType == OverlayType.PREVIEW) hideOverlay()
        update { copy(shift = nextShift) }
    }

    /**
     * Advance shift on tap. A second tap within [SHIFT_DOUBLE_TAP_MS] while in one-shot
     * [ShiftState.SHIFTED] promotes to [ShiftState.CAPS_LOCK]; unlike one-shot shift, caps
     * lock is not cleared by typing a character.
     */
    private fun onShiftTap(nowMs: Long) {
        val s = _state.value
        val doubleTap = s.shift == ShiftState.SHIFTED &&
            nowMs - s.lastShiftTapMs <= SHIFT_DOUBLE_TAP_MS
        update {
            copy(
                shift = when {
                    doubleTap -> ShiftState.CAPS_LOCK
                    shift == ShiftState.UNSHIFTED -> ShiftState.SHIFTED
                    else -> ShiftState.UNSHIFTED
                },
                lastShiftTapMs = nowMs
            )
        }
    }

    /**
     * After the long-press timeout has already elapsed, emit backspaces on a fixed interval
     * until [stopBackspaceRepeat] (finger up, or input target change).
     */
    fun startBackspaceRepeat() {
        stopBackspaceRepeat()
        backspaceRepeatJob = scope.launch {
            while (isActive) {
                onKeyAction?.invoke()
                backspace()
                delay(BACKSPACE_REPEAT_MS)
            }
        }
    }

    /** Stop continuous backspace deletion started by a long-press. Safe to call when idle. */
    fun stopBackspaceRepeat() {
        backspaceRepeatJob?.cancel()
        backspaceRepeatJob = null
    }

    private companion object {
        const val SHIFT_DOUBLE_TAP_MS = 400L
        const val CLIPBOARD_HISTORY_CAP = 5
        /** Interval between repeated deletes while backspace is held (ViewConfiguration-like). */
        const val BACKSPACE_REPEAT_MS = 50L
    }

    /**
     * Sync the composer with the word around the cursor. Returns the cursor offset inside
     * the word, or -1 if the cursor is not inside/next to a word. This lets typing work
     * when the cursor sits in the middle of existing text instead of at the end.
     */
    private fun syncComposerWithCursor(): Int {
        val lang = currentLang(_state.value.languageId)
        composer.startComposing(lang)
        val (before, after) = inputConnection.surroundingText()
        val wordBefore = before.takeLastWhile { it.isLetter() }
        val wordAfter = after.takeWhile { it.isLetter() }
        val word = wordBefore + wordAfter
        return if (word.isEmpty()) {
            composer.reset()
            -1
        } else {
            composer.set(word)
            wordBefore.length
        }
    }

    private fun handleChar(c: String) {
        val isLetter = c.isNotEmpty() && c.all { it.isLetter() }
        // Reddit (and similar custom editors) mishandle composing rebuild/selection for
        // every script. Gate on the editor, not on whether the keyboard language is RTL.
        if (isLetter && useEditorComposing) {
            if (commitNextLetter) {
                commitNextLetter = false
                suppressCursorCorrection = false
                composer.reset()
                inputConnection.finishComposingText()
                inputConnection.commitText(c)
                syncComposerWithCursor()
                refreshSuggestions()
                if (_state.value.shift == ShiftState.SHIFTED) {
                    update { copy(shift = ShiftState.UNSHIFTED) }
                }
                return
            }
            val cursor = syncComposerWithCursor()
            // Letter-run currently around the cursor (0/0 when starting a fresh word).
            val replaceBefore = if (cursor >= 0) cursor else 0
            val replaceAfter =
                if (cursor >= 0) (composer.composing.length - cursor).coerceAtLeast(0) else 0
            val insertAt = if (cursor >= 0) cursor else 0
            val word = composer.insert(insertAt, c)
            // Cursor stays right after the inserted text (digraphs like "дж"/"къ" may be
            // more than one code unit), which is not necessarily the end of the word
            // when editing mid-text.
            val cursorInWord = insertAt + c.length
            inputConnection.setComposingTextAt(
                word,
                cursorInWord,
                replaceBefore,
                replaceAfter
            )
            val expectedBefore = word.substring(0, cursorInWord.coerceIn(0, word.length))
            val skipCorrection = suppressCursorCorrection
            suppressCursorCorrection = false
            if (!skipCorrection && !inputConnection.isCursorAfter(expectedBefore)) {
                // Editor prepended or left the caret at the start; stop composing so
                // later letters are commitText (append) instead of another rebuild.
                useEditorComposing = false
                composer.reset()
                inputConnection.finishComposingText()
                inputConnection.ensureCursorAfter(expectedBefore)
            }
            refreshSuggestions()
            if (_state.value.shift == ShiftState.SHIFTED) {
                update { copy(shift = ShiftState.UNSHIFTED) }
            }
        } else {
            composer.reset()
            inputConnection.finishComposingText()
            inputConnection.commitText(c)
            refreshSuggestions()
            if (isLetter && _state.value.shift == ShiftState.SHIFTED) {
                update { copy(shift = ShiftState.UNSHIFTED) }
            }
        }
    }

    private fun space() {
        // finishComposingText keeps the caret where it was (including mid-word). Commit
        // the space with newCursorPosition=1 so the caret ends up after it — 0 would leave
        // the caret before the space, which looks like a no-op when typing at the end.
        inputConnection.finishComposingText()
        inputConnection.commitText(" ", 1)
        composer.reset()
        refreshSuggestions()
    }

    private fun backspace() {
        // Selection must be cleared before composing-word edits: surrounding text
        // skips the selected range, so we would otherwise reshape the letters around
        // it and leave the selection in place.
        if (inputConnection.deleteSelection()) {
            composer.reset()
            refreshSuggestions()
            return
        }
        if (!useEditorComposing) {
            composer.reset()
            inputConnection.deleteBackward()
            refreshSuggestions()
            return
        }
        val cursor = syncComposerWithCursor()
        if (cursor >= 0) {
            if (cursor > 0) {
                val replaceBefore = cursor
                val replaceAfter = (composer.composing.length - cursor).coerceAtLeast(0)
                val word = composer.deleteAt(cursor - 1)
                inputConnection.setComposingTextAt(word, cursor - 1, replaceBefore, replaceAfter)
            } else {
                // Cursor at the start of the word: delete the char before the word.
                inputConnection.deleteBackward()
            }
            refreshSuggestions()
        } else {
            inputConnection.deleteBackward()
        }
    }

    private fun enter() {
        composer.reset()
        inputConnection.finishComposingText()
        inputConnection.handleEnter(currentEditorInfo)
        refreshSuggestions()
    }

    /**
     * Cycle through enabled languages (subset of [Languages.ALL]). Positive [direction]
     * advances, negative goes back; wraps at both ends. The globe key uses the default (+1).
     * No-op when fewer than two languages are enabled.
     */
    fun switchLang(direction: Int = 1) {
        if (_state.value.overlayType == OverlayType.PREVIEW) hideOverlay()
        val enabled = Languages.ALL.filter { it.id in _state.value.enabledLanguageIds }
        if (enabled.size <= 1) return
        val currentId = _state.value.languageId
        val idxInEnabled = enabled.indexOfFirst { it.id == currentId }
        if (idxInEnabled >= 0) {
            switchTo(enabled[Math.floorMod(idxInEnabled + direction, enabled.size)].id)
            return
        }
        // Current language is disabled: start from its position in ALL and find the next enabled.
        val all = Languages.ALL
        val start = all.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val step = if (direction >= 0) 1 else -1
        for (offset in 1..all.size) {
            val candidate = all[Math.floorMod(start + offset * step, all.size)]
            if (candidate.id in _state.value.enabledLanguageIds) {
                switchTo(candidate.id)
                return
            }
        }
    }

    /**
     * Enable or disable a language for keyboard swipe / globe cycling. The last Adıǵabze Latin layout
     * cannot be disabled. If the current layout is turned off, hops to the preferred enabled one.
     * Does not dismiss the language picker.
     */
    fun setLanguageEnabled(languageId: String, enabled: Boolean) {
        val next = applyLanguageEnabled(_state.value.enabledLanguageIds, languageId, enabled)
            ?: return
        languagePreferences.saveEnabledIds(next)
        applyEnabledLanguageIds(next)
    }

    /** Re-read the cycle set from prefs after an external writer (e.g. Settings) changes it. */
    fun reloadEnabledLanguages() {
        applyEnabledLanguageIds(languagePreferences.loadEnabledIds())
    }

    private fun applyEnabledLanguageIds(ids: Set<String>) {
        val currentId = _state.value.languageId
        if (currentId in ids) {
            update { copy(enabledLanguageIds = ids) }
            return
        }
        update { copy(enabledLanguageIds = ids) }
        switchTo(preferredEnabledLanguageId(ids))
    }

    fun setNumberRowEnabled(enabled: Boolean) {
        update { copy(numberRowEnabled = enabled) }
    }

    /** Re-read the number-row toggle after an external writer (e.g. Settings) changes it. */
    fun reloadNumberRow() {
        update { copy(numberRowEnabled = keyboardPreferences.isNumberRowEnabled()) }
    }

    fun switchTo(languageId: String) {
        composer.reset()
        inputConnection.finishComposingText()
        update {
            // Keep phone / number pads across language switches; letter modes reset to LETTERS.
            val nextMode = when (mode) {
                LayoutMode.PHONE, LayoutMode.NUMBER_PAD -> mode
                else -> LayoutMode.LETTERS
            }
            copy(
                languageId = languageId,
                mode = nextMode,
                overlayType = OverlayType.NONE,
                suggestions = emptyList(),
                composingWord = ""
            )
        }
        refreshSuggestions()
    }

    fun onSuggestionCommit(text: String) {
        inputConnection.finishComposingText()
        // Replace the whole word but leave the cursor where it was so a trailing
        // fragment after the cursor is not shoved to after the inserted suggestion.
        inputConnection.commitText(text, 0)
        composer.reset()
        refreshSuggestions()
    }

    private fun refreshSuggestions() {
        val s = _state.value
        val lang = currentLang(s.languageId)
        if (!s.suggestionsEnabled || !composer.isComposing || !suggestionEngine.supports(lang)) {
            update {
                copy(
                    suggestions = emptyList(),
                    composingWord = if (s.suggestionsEnabled) composer.composing else "",
                    hasExactSuggestion = false
                )
            }
            return
        }
        val result = suggestionEngine.suggestions(composer.composing, lang)
        update {
            copy(
                suggestions = result.suggestions,
                composingWord = composer.composing,
                hasExactSuggestion = result.exactMatch
            )
        }
    }
}

private fun initialImeState(
    languagePreferences: LanguagePreferences,
    keyboardPreferences: KeyboardPreferences
): ImeState {
    val enabled = languagePreferences.loadEnabledIds()
    return ImeState(
        languageId = preferredEnabledLanguageId(enabled),
        enabledLanguageIds = enabled,
        numberRowEnabled = keyboardPreferences.isNumberRowEnabled()
    )
}
