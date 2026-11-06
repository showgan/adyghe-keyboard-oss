package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/**
 * Standard Hebrew layout. Rows are stored in visual left-to-right order (the same order
 * shown on screen — ק on the left, like a physical Hebrew keyboard). Do not reverse for
 * RTL locales; the IME pins layout direction LTR. No Shift — Hebrew has no case. Common
 * dagesh/geresh forms and top-row digit shortcuts are on long-press.
 */
object HebrewLayout {

    private val popups: Map<String, List<String>> = mapOf(
        "ק" to listOf("ק", "1"),
        "ר" to listOf("ר", "2"),
        "א" to listOf("א", "3"),
        "ט" to listOf("ט", "4"),
        "ו" to listOf("ו", "5"),
        "ן" to listOf("ן", "6"),
        "ם" to listOf("ם", "7"),
        "פ" to listOf("פ", "פּ", "8", "9", "0"),
        "ש" to listOf("ש", "שׂ", "שׁ"),
        "ד" to listOf("ד", "דּ"),
        "ג" to listOf("ג", "ג׳"),
        "כ" to listOf("כ", "כּ"),
        "ח" to listOf("ח", "ח׳"),
        "ך" to listOf("ך", "ךּ"),
        "ף" to listOf("ף", "ףּ"),
        "ז" to listOf("ז", "ז׳"),
        "ב" to listOf("ב", "בּ"),
        "צ" to listOf("צ", "צ׳"),
        "ת" to listOf("ת", "תּ"),
        "ץ" to listOf("ץ", "ץ׳")
    )

    fun build(): KeyboardLayout {
        val row1 = listOf("ק","ר","א","ט","ו","ן","ם","פ")
        val row2 = listOf("ש","ד","ג","כ","ע","י","ח","ל","ך","ף")
        val row3 = listOf("ז","ס","ב","ה","נ","מ","צ","ת","ץ")

        fun he(c: String): KeyboardKey = Keys.char(c, popup = popups[c] ?: listOf(c))

        val letters = listOf(
            KeyboardRow(row1.map { he(it) }),
            KeyboardRow(row2.map { he(it) }),
            KeyboardRow(
                row3.map { he(it) } +
                    listOf(Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f))
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.HEBREW,
            lettersRows = letters,
            symbolsRows = HebrewSymbols.rows(),
            numbersRows = HebrewNumbers.rows()
        )
    }
}

private object HebrewSymbols {
    fun rows(): List<KeyboardRow> = StandardRows.symbolsPanel()
}

private object HebrewNumbers {
    fun rows(): List<KeyboardRow> = StandardRows.numbersPanel()
}
