package com.adyghe.keyboard.core.state

import com.adyghe.keyboard.core.input.LetterChrome
import com.adyghe.keyboard.core.input.NumberPadVariant
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.layout.LayoutMode

/** The shift/caps state of the keyboard. */
enum class ShiftState { UNSHIFTED, SHIFTED, CAPS_LOCK }

/** Whether a long-press popup / key preview / language picker / emoji / clipboard is shown. */
enum class OverlayType { NONE, PREVIEW, POPUP, LANGUAGE_PICKER, EMOJI, CLIPBOARD }

/**
 * Visual/semantic meaning of the enter key, derived from the focused field's
 * [android.view.inputmethod.EditorInfo.imeOptions].
 */
enum class EnterKeyAction { RETURN, SEARCH, GO, SEND, DONE, NEXT }

/** A single popup (the character being previewed above the dragged key). */
data class PopupSelection(
    val keyIndex: Int,     // index within the active key's popupChars list
    val char: String
)

/** The complete UI state of the keyboard, exposed via [ImeStore.state]. */
data class ImeState(
    val languageId: String = "adyghe_latin_1",
    /** Language ids included in keyboard swipe / globe cycling. At least one Adıǵabze Latin is present. */
    val enabledLanguageIds: Set<String> = Languages.ALL.map { it.id }.toSet(),
    val shift: ShiftState = ShiftState.UNSHIFTED,
    val mode: LayoutMode = LayoutMode.LETTERS,
    val overlayType: OverlayType = OverlayType.NONE,
    // Identifies the key (row,index) currently showing a preview or popup.
    val overlayRow: Int = -1,
    val overlayCol: Int = -1,
    val activePopup: List<String> = emptyList(),
    /** Index in [activePopup] of the extra that stays over the long-pressed key (popup stem). */
    val popupAnchorIndex: Int = 0,
    val popupSelection: PopupSelection? = null,
    val suggestions: List<String> = emptyList(),
    val composingWord: String = "",
    val hasExactSuggestion: Boolean = false,
    /** Timestamp of the most recent shift tap, used to detect a double-tap for caps lock. */
    val lastShiftTapMs: Long = 0L,
    /** Icon / semantics for the enter key based on the current editor's IME action. */
    val enterAction: EnterKeyAction = EnterKeyAction.RETURN,
    /** Whether the suggestion strip should update for this field. */
    val suggestionsEnabled: Boolean = true,
    /** Extra keys on the compact number pad for the focused field. */
    val numberPadVariant: NumberPadVariant = NumberPadVariant.PLAIN,
    /** Extra bottom-row chars for email / URI letter layouts. */
    val letterChrome: LetterChrome = LetterChrome.NONE,
    /** Dedicated digit row above letters; when true, letter popups omit digits. */
    val numberRowEnabled: Boolean = false,
    /** Recent clipboard text clips (newest first), capped in [ImeStore]. */
    val clipboardHistory: List<String> = emptyList(),
    /** True when there is pasteable clipboard text to show a Paste chip for. */
    val showPasteChip: Boolean = false
)
