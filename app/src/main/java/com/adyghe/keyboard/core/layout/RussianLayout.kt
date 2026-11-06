package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/**
 * Russian (ЙЦУКЕН) layout. Base letters match a standard Russian keyboard, with a few
 * long-press pop-ups for common variants.
 */
object RussianLayout {

    private val popups: Map<String, List<String>> = mapOf(
        "й" to listOf("й", "1"),
        "ц" to listOf("ц", "2"),
        "у" to listOf("у", "3"),
        "к" to listOf("к", "4"),
        "е" to listOf("е", "5", "ё"),
        "н" to listOf("н", "6"),
        "г" to listOf("г", "7"),
        "ш" to listOf("ш", "8"),
        "щ" to listOf("щ", "9"),
        "з" to listOf("з", "0"),
        "ь" to listOf("ь", "ъ")
    )

    fun build(): KeyboardLayout {
        fun rc(c: String): KeyboardKey = Keys.char(c, popup = popups[c] ?: listOf(c))

        val letters = listOf(
            KeyboardRow(listOf("й","ц","у","к","е","н","г","ш","щ","з","х").map { rc(it) }),
            KeyboardRow(listOf("ф","ы","в","а","п","р","о","л","д","ж","э").map { rc(it) }),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    rc("я"), rc("ч"), rc("с"), rc("м"), rc("и"), rc("т"), rc("ь"), rc("б"), rc("ю"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.RUSSIAN,
            lettersRows = letters,
            symbolsRows = RussianSymbols.rows(),
            numbersRows = RussianNumbers.rows()
        )
    }
}

private object RussianSymbols {
    fun rows(): List<KeyboardRow> = StandardRows.symbolsPanel()
}

private object RussianNumbers {
    fun rows(): List<KeyboardRow> = StandardRows.numbersPanel()
}
