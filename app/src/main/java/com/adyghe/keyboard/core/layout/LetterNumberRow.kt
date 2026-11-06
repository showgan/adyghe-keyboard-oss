package com.adyghe.keyboard.core.layout

/**
 * Optional dedicated digit row prepended above letter layouts. Digit extras are stripped
 * from letter-key popups so numbers are not duplicated on long-press.
 */
internal object LetterNumberRow {

    val WESTERN_DIGITS: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    fun numberRow(digits: List<String> = WESTERN_DIGITS): KeyboardRow =
        KeyboardRow(digits.map { KeyboardKey(label = it, char = it) })

    /** Digits shown on this language's `?123` top row, or Western 1–9–0 if that row is empty. */
    fun digitsFrom(layout: KeyboardLayout): List<String> {
        val top = layout.numbersRows.firstOrNull()?.keys?.mapNotNull { it.char }.orEmpty()
        return top.ifEmpty { WESTERN_DIGITS }
    }

    fun apply(letterRows: List<KeyboardRow>, digits: List<String> = WESTERN_DIGITS): List<KeyboardRow> {
        val stripped = letterRows.map { row ->
            KeyboardRow(row.keys.map { it.withoutDigitPopupExtras() })
        }
        return listOf(numberRow(digits)) + stripped
    }
}

private fun KeyboardKey.withoutDigitPopupExtras(): KeyboardKey {
    if (popupChars.isEmpty()) return this
    val kept = popupChars.filterNot { it.isSingleDigit() }
    return if (kept == popupChars) this else copy(popupChars = kept)
}

private fun String.isSingleDigit(): Boolean = length == 1 && this[0].isDigit()
