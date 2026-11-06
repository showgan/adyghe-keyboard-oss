package com.adyghe.keyboard.core.layout

/** The action performed when a key is committed. */
enum class KeyAction {
    CHAR,
    SHIFT,
    BACKSPACE,
    SPACE,
    ENTER,
    LANG_SWITCH,
    SYMBOLS,
    NUMBERS,
    /** Compact 3×4 pad, Gboard's "1234" key on the numbers panel. */
    NUMBER_PAD,
    ABC,
    EMOJI,
    CURSOR_LEFT,
    CURSOR_RIGHT,
    COMMIT_SUGGESTION,
    NONE
}
