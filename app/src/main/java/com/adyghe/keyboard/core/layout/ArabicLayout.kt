package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/**
 * Standard Arabic layout matching Gboard. Rows are stored in visual left-to-right order
 * (the same order shown on screen). Do not reverse for RTL locales; the IME pins layout
 * direction LTR. Standard Arabic letters on the main keys, with common variants and
 * Eastern-Indic digit shortcuts via long-press.
 */
object ArabicLayout {

    private val popups: Map<String, List<String>> = mapOf(
        "ض" to listOf("ض", "١"),
        "ص" to listOf("ص", "٢"),
        "ث" to listOf("ث", "٣"),
        "ق" to listOf("ق", "٤"),
        "ف" to listOf("ف", "٥"),
        "غ" to listOf("غ", "٦"),
        "ع" to listOf("ع", "٧"),
        "ه" to listOf("ه", "٨", "ه\u200D"),
        "خ" to listOf("خ", "٩"),
        "ح" to listOf("ح", "٠"),
        "ي" to listOf("ي", "ئ"),
        "ل" to listOf("ل", "لا"),
        "ا" to listOf("ا", "أ", "إ", "آ", "ء", "ٱ"),
        "و" to listOf("و", "ؤ")
    )

    fun build(): KeyboardLayout {
        val row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج")
        val row2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
        val row3 = listOf("ذ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ", "د")

        fun ar(c: String): KeyboardKey = Keys.char(c, popup = popups[c] ?: listOf(c))

        val letters = listOf(
            KeyboardRow(row1.map { ar(it) }),
            KeyboardRow(row2.map { ar(it) }),
            KeyboardRow(
                row3.map { ar(it) } +
                    listOf(Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f))
            ),
            StandardRows.letterBottomRow(
                leftPunct = "\u060C", // Arabic comma ،
                leftPopup = listOf("\u060C", "\u061F", "!", ":", "\u061B", "/", "@", "#", "$")
            )
        )

        return KeyboardLayout(
            language = Languages.ARABIC,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel(
                topRow = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠")
            )
        )
    }
}
