package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages
import java.util.Locale

/** Turkish-Q layout. Special Turkish letters appear directly on the main keys. */
object TurkishLayout {

    private val tr = Locale.forLanguageTag("tr")

    fun build(): KeyboardLayout {
        fun tu(c: String, popup: List<String> = emptyList()): KeyboardKey =
            Keys.char(c, popup = popup, locale = tr)

        val letters = listOf(
            KeyboardRow(
                listOf(
                    tu("q", listOf("q", "1")),
                    tu("w", listOf("w", "2")),
                    tu("e", listOf("e", "3", "é")),
                    tu("r", listOf("r", "4")),
                    tu("t", listOf("t", "5")),
                    tu("y", listOf("y", "6")),
                    tu("u", listOf("u", "7", "û")),
                    tu("ı", listOf("ı", "8")),
                    tu("o", listOf("o", "9")),
                    tu("p", listOf("p", "0")),
                    tu("ğ"),
                    tu("ü")
                )
            ),
            KeyboardRow(
                listOf(
                    tu("a", listOf("a", "â")),
                    tu("s"),
                    tu("d"),
                    tu("f"),
                    tu("g"),
                    tu("h"),
                    tu("j"),
                    tu("k"),
                    tu("l"),
                    tu("ş"),
                    tu("i", listOf("i", "î"))
                )
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    tu("z"),
                    tu("x"),
                    tu("c"),
                    tu("v"),
                    tu("b"),
                    tu("n"),
                    tu("m"),
                    tu("ö"),
                    tu("ç"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.TURKISH,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel()
        )
    }
}
