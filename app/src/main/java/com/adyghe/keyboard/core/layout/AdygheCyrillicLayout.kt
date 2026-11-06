package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.Languages

/**
 * Cyrillic-based ADIǴE (Адыгэбзэ) layout. Built on a Russian-style base with the
 * Adyghe-specific letters placed in accessible positions. The palochka (ӏ/Ӏ) and the
 * common vowel э are placed centrally, and rare Russian-only letters (ю, ё, ъ, ь) are
 * moved to peripheral or pop-up positions, following the LING073 Adyghe keyboard approach.
 */
object AdygheCyrillicLayout {

    fun build(): KeyboardLayout {
        val popups: Map<String, List<String>> = mapOf(
            "ц" to listOf("ц", "цӏ"),
            "к" to listOf("к", "къ", "кӏ"),
            "г" to listOf("г", "гъ"),
            "ш" to listOf("ш", "шъ", "шӏ"),
            "х" to listOf("х", "хъ", "хь"),
            "п" to listOf("п", "пӏ"),
            "л" to listOf("л", "лъ", "лӏ"),
            "д" to listOf("д", "дж", "дз"),
            "ж" to listOf("ж", "жъ", "жь"),
            "ч" to listOf("ч", "чъ", "чӏ"),
            "и" to listOf("и", "й"),
            "т" to listOf("т", "тӏ"),
            "ӏ" to listOf("ӏ", "Ӏ", "ъ", "ь")
        )

        fun cy(c: String): KeyboardKey = Keys.char(c, popup = popups[c] ?: listOf(c))

        val letters = listOf(
            KeyboardRow(listOf("й","ц","у","к","е","н","г","ш","щ","з","х").map { cy(it) }),
            KeyboardRow(listOf("ф","ы","в","а","п","р","о","л","д","ж","э").map { cy(it) }),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SHIFT, null, weight = 1.5f),
                    cy("я"), cy("ч"), cy("с"), cy("м"), cy("и"), cy("т"),
                    cy("ӏ"),
                    cy("б"), cy("ю"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            StandardRows.letterBottomRow()
        )

        return KeyboardLayout(
            language = Languages.ADYGHE_CYRILLIC,
            lettersRows = letters,
            symbolsRows = StandardRows.symbolsPanel(),
            numbersRows = StandardRows.numbersPanel()
        )
    }
}
