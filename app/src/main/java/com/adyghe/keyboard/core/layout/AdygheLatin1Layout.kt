package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages
import java.util.Locale

/**
 * Adıǵabze 1: Latin-based ADIǴE on a Turkish-Q base.
 * Dedicated `ğ ü ş ı ö ç` (and dotted `i`) sit on the main keys; remaining ADIǴE letters
 * are long-press extras on phonetically similar hosts. The tick (') is on period long-press.
 */
object AdygheLatin1Layout {

    private val tr = Locale.forLanguageTag("tr")

    fun build(): KeyboardLayout {
        fun key(c: String, popup: List<String> = emptyList()): KeyboardKey =
            Keys.char(c, popup = popup, locale = tr)

        val letters = listOf(
            KeyboardRow(
                listOf(
                    key("q", listOf("q", "1")),
                    key("w", listOf("w", "2")),
                    key("e", listOf("e", "é", "3")),
                    key("r", listOf("r", "4")),
                    key("t", listOf("t", "ṫ", "5")),
                    key("y", listOf("y", "6")),
                    key("u", listOf("u", "7")),
                    key("ı", listOf("ı", "8")),
                    key("o", listOf("o", "9")),
                    key("p", listOf("p", "ṕ", "0")),
                    key("ğ"),
                    key("ü")
                )
            ),
            KeyboardRow(
                listOf(
                    key("a", listOf("a", "á")),
                    key("s", listOf("s", "ś", "š")),
                    key("d"),
                    key("f"),
                    key("g", listOf("g", "ǵ")),
                    key("h", listOf("h", "ḣ")),
                    key("j"),
                    key("k", listOf("k", "ḱ")),
                    key("l", listOf("l", "ĺ")),
                    key("ş"),
                    key("i")
                )
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    key("z", listOf("z", "ź")),
                    key("x"),
                    key("c", listOf("c", "ć")),
                    key("v"),
                    key("b"),
                    key("n"),
                    key("m"),
                    key("ö"),
                    key("ç"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.ADYGHE_LATIN_1,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel()
        )
    }
}
