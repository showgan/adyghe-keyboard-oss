package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/**
 * Adıǵabze 2: Latin-based ADIǴE on a US-QWERTY base.
 * The special ADIǴE characters are reached via long-press pop-ups on phonetically/visually
 * similar base keys. The tick (') is available via long-press on the period key.
 */
object AdygheLatinLayout {

    fun build(): KeyboardLayout {
        val letters = listOf(
            // Row 1: q w e r t y u i o p (long-press numbers 1–9, 0)
            KeyboardRow(
                listOf(
                    Keys.char("q", popup = listOf("q", "1")),
                    Keys.char("w", popup = listOf("w", "2")),
                    Keys.char("e", popup = listOf("e", "é", "3")),
                    Keys.char("r", popup = listOf("r", "4")),
                    Keys.char("t", popup = listOf("t", "ṫ", "5")),
                    Keys.char("y", popup = listOf("y", "6")),
                    Keys.char("u", popup = listOf("u", "ü", "7")),
                    Keys.char("i", popup = listOf("i", "ı", "8", "İ")),
                    Keys.char("o", popup = listOf("o", "ö", "9")),
                    Keys.char("p", popup = listOf("p", "ṕ", "0"))
                )
            ),
            // Row 2: a s d f g h j k l
            KeyboardRow(
                listOf(
                    Keys.char("a", popup = listOf("a", "á")),
                    Keys.char("s", popup = listOf("s", "ş", "ś", "š")),
                    Keys.char("d"),
                    Keys.char("f"),
                    Keys.char("g", popup = listOf("g", "ğ", "ǵ")),
                    Keys.char("h", popup = listOf("h", "ḣ")),
                    Keys.char("j"),
                    Keys.char("k", popup = listOf("k", "ḱ")),
                    Keys.char("l", popup = listOf("l", "ĺ"))
                )
            ),
            // Row 3: shift z x c v b n m backspace
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    Keys.char("z", popup = listOf("z", "ź")),
                    Keys.char("x"),
                    Keys.char("c", popup = listOf("c", "ç", "ć")),
                    Keys.char("v"),
                    Keys.char("b"),
                    Keys.char("n"),
                    Keys.char("m"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.ADYGHE_LATIN_2,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel()
        )
    }
}
