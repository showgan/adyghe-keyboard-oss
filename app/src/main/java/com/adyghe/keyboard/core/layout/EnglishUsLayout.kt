package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/** English (US) QWERTY layout. */
object EnglishUsLayout {

    val popups: Map<String, List<String>> = mapOf(
        "q" to listOf("q", "1"),
        "w" to listOf("w", "2"),
        "e" to listOf("e", "3"),
        "r" to listOf("r", "4"),
        "t" to listOf("t", "5"),
        "y" to listOf("y", "6"),
        "u" to listOf("u", "7"),
        "i" to listOf("i", "8"),
        "o" to listOf("o", "9"),
        "p" to listOf("p", "0")
    )

    fun build(): KeyboardLayout {
        val letters = listOf(
            KeyboardRow(
                listOf("q","w","e","r","t","y","u","i","o","p").map { c ->
                    Keys.char(c, popup = popups[c] ?: listOf(c))
                }
            ),
            KeyboardRow(
                listOf("a","s","d","f","g","h","j","k","l").map { c ->
                    Keys.char(c, popup = popups[c] ?: listOf(c))
                }
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    Keys.char("z"), Keys.char("x"), Keys.char("c"), Keys.char("v"),
                    Keys.char("b"), Keys.char("n"), Keys.char("m"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.ENGLISH_US,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel()
        )
    }
}
