package com.adyghe.keyboard.core.layout

/** The top-level keyboard panel mode. */
enum class LayoutMode {
    LETTERS,
    SYMBOLS,
    /** Gboard-style ?123 punctuation/number panel. */
    NUMBERS,
    /** Compact phone dial pad for [android.text.InputType.TYPE_CLASS_PHONE]. */
    PHONE,
    /** Compact number pad for number / datetime fields. */
    NUMBER_PAD
}
