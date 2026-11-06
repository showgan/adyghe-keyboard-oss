package com.adyghe.keyboard.core.layout

/**
 * Shared builders for alphabetic layouts. The three main rows use the familiar QWERTY
 * layout, with an optional shift key prepended and backspace appended.
 */
internal object LetterRows {

    fun rows(
        row1: List<String>,
        row2: List<String>,
        row3: List<String>,
        shiftWeight: Float = 1.5f
    ): List<KeyboardRow> {
        fun toKeys(line: List<String>) = line.map { Keys.char(it) }
        return listOf(
            KeyboardRow(toKeys(row1)),
            KeyboardRow(toKeys(row2)),
            KeyboardRow(
                listOf(Keys.modifier(KeyAction.SHIFT, null, weight = shiftWeight)) +
                    toKeys(row3) +
                    listOf(Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = shiftWeight))
            )
        )
    }

    /**
     * Builds a row of character keys where each char's long-press popup list is looked up
     * in [popups]. The primary char is the base char.
     */
    fun charRowWithPopups(line: List<String>, popups: Map<String, List<String>>): List<KeyboardKey> {
        return line.map { c ->
            Keys.char(c, popup = popups[c] ?: listOf(c))
        }
    }
}
