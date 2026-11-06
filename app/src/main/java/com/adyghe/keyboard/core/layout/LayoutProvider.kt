package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome
import com.adyghe.keyboard.core.input.NumberPadVariant
import com.adyghe.keyboard.core.language.LanguageConfig
import com.adyghe.keyboard.core.language.Languages

/**
 * Provides the [KeyboardLayout] for a given language. Layouts are built once and cached.
 * Phone and number pads are shared (language-independent).
 */
class LayoutProvider {

    private val cache = mutableMapOf<String, KeyboardLayout>()

    fun layoutFor(language: LanguageConfig): KeyboardLayout {
        return cache.getOrPut(language.id) {
            when (language.id) {
                Languages.ADYGHE_LATIN_1.id -> AdygheLatin1Layout.build()
                Languages.ADYGHE_LATIN_2.id -> AdygheLatinLayout.build()
                Languages.ADYGHE_CYRILLIC.id -> AdygheCyrillicLayout.build()
                Languages.ARABIC.id -> ArabicLayout.build()
                Languages.HEBREW.id -> HebrewLayout.build()
                Languages.ENGLISH_US.id -> EnglishUsLayout.build()
                Languages.TURKISH.id -> TurkishLayout.build()
                Languages.RUSSIAN.id -> RussianLayout.build()
                else -> EnglishUsLayout.build()
            }
        }
    }

    fun rows(
        language: LanguageConfig,
        mode: LayoutMode,
        numberPadVariant: NumberPadVariant = NumberPadVariant.PLAIN,
        letterChrome: LetterChrome = LetterChrome.NONE,
        numberRowEnabled: Boolean = false
    ): List<KeyboardRow> {
        val layout = layoutFor(language)
        return when (mode) {
            LayoutMode.LETTERS -> {
                val letters = LetterChromeRows.apply(layout.lettersRows, letterChrome)
                if (numberRowEnabled) {
                    LetterNumberRow.apply(letters, LetterNumberRow.digitsFrom(layout))
                } else {
                    letters
                }
            }
            LayoutMode.SYMBOLS -> layout.symbolsRows
            LayoutMode.NUMBERS -> layout.numbersRows
            LayoutMode.PHONE -> StandardRows.phonePad()
            LayoutMode.NUMBER_PAD -> StandardRows.numberPad(numberPadVariant)
        }
    }
}
