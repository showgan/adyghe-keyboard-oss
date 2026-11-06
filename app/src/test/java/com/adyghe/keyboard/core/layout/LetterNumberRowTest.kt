package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome
import com.adyghe.keyboard.core.language.LanguageConfig
import com.adyghe.keyboard.core.language.Languages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterNumberRowTest {

    private val provider = LayoutProvider()

    @Test
    fun english_off_keepsQwertyFirstRowAndDigitPopups() {
        val rows = letters(Languages.ENGLISH_US, numberRow = false)
        assertEquals(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            rows.first().keys.map { it.char }
        )
        assertTrue(letterKey(rows, "e").popupExtras(false).contains("3"))
        assertTrue(letterKey(rows, "q").popupExtras(false).contains("1"))
    }

    @Test
    fun english_on_prependsWesternDigitsAndStripsDigitPopups() {
        val rows = letters(Languages.ENGLISH_US, numberRow = true)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        assertTrue(rows.first().keys.all { it.popupChars.isEmpty() })

        assertEquals(emptyList<String>(), letterKey(rows, "e").popupExtras(false))
        assertEquals(emptyList<String>(), letterKey(rows, "q").popupExtras(false))
        assertNull(letterKey(rows, "q").popupHint(false))
    }

    @Test
    fun adygheLatin2_on_keepsLetterExtrasWithoutDigits() {
        val rows = letters(Languages.ADYGHE_LATIN_2, numberRow = true)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        assertEquals(listOf("é"), letterKey(rows, "e").popupExtras(false))
        assertFalse(letterKey(rows, "e").popupExtras(false).any { it.isSingleDigit() })
        assertEquals(emptyList<String>(), letterKey(rows, "q").popupExtras(false))
    }

    @Test
    fun adygheLatin1_on_keepsLetterExtrasWithoutDigits() {
        val rows = letters(Languages.ADYGHE_LATIN_1, numberRow = true)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        assertEquals(listOf("é"), letterKey(rows, "e").popupExtras(false))
        assertEquals(listOf("ṫ"), letterKey(rows, "t").popupExtras(false))
        assertEquals(listOf("ṕ"), letterKey(rows, "p").popupExtras(false))
        assertEquals(emptyList<String>(), letterKey(rows, "ı").popupExtras(false))
        assertEquals(listOf("ś", "š"), letterKey(rows, "s").popupExtras(false))
        assertFalse(letterKey(rows, "e").popupExtras(false).any { it.isSingleDigit() })
    }

    @Test
    fun arabic_on_usesEasternIndicDigitsAndStripsThemFromPopups() {
        val rows = letters(Languages.ARABIC, numberRow = true)
        assertEquals(
            listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠"),
            rows.first().keys.map { it.char }
        )
        assertEquals(emptyList<String>(), letterKey(rows, "ض").popupExtras(false))
        assertNull(letterKey(rows, "ض").popupHint(false))
        val alefExtras = letterKey(rows, "ا").popupExtras(false)
        assertTrue(alefExtras.contains("أ"))
        assertFalse(alefExtras.any { it.isSingleDigit() })
    }

    @Test
    fun hebrew_on_keepsDageshAndDropsDigitsFromPe() {
        val rows = letters(Languages.HEBREW, numberRow = true)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        val peExtras = letterKey(rows, "פ").popupExtras(false)
        assertEquals(listOf("פּ"), peExtras)
        assertFalse(peExtras.any { it in listOf("8", "9", "0") })
    }

    @Test
    fun adygheCyrillic_on_prependsRowAndKeepsDigraphExtras() {
        val rows = letters(Languages.ADYGHE_CYRILLIC, numberRow = true)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        assertEquals(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х"),
            rows[1].keys.map { it.char }
        )
        assertTrue(letterKey(rows, "г").popupExtras(false).contains("гъ"))
    }

    @Test
    fun emailChrome_numberRowIsFirstAndAtStaysOnLastRow() {
        val rows = letters(Languages.ENGLISH_US, numberRow = true, chrome = LetterChrome.EMAIL)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        val bottom = rows.last().keys
        assertTrue(bottom.any { it.char == "@" })
        assertTrue(bottom.any { it.char == "." && it.action == KeyAction.CHAR })
    }

    @Test
    fun uriChrome_numberRowIsFirstAndSlashStaysOnLastRow() {
        val rows = letters(Languages.ENGLISH_US, numberRow = true, chrome = LetterChrome.URI)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, rows.first().keys.map { it.char })
        val bottom = rows.last().keys
        assertTrue(bottom.any { it.char == "/" })
        assertEquals(LetterChromeRows.URI_SLASH_POPUP, bottom.first { it.char == "/" }.popupChars)
    }

    @Test
    fun numbersMode_unchangedWhenNumberRowEnabled() {
        val off = provider.rows(Languages.ENGLISH_US, LayoutMode.NUMBERS, numberRowEnabled = false)
        val on = provider.rows(Languages.ENGLISH_US, LayoutMode.NUMBERS, numberRowEnabled = true)
        assertEquals(off, on)
        assertEquals(LetterNumberRow.WESTERN_DIGITS, on.first().keys.map { it.char })
        assertEquals(4, on.size)
    }

    @Test
    fun commaPopup_keepsNonDigitExtrasWhenNumberRowOn() {
        val rows = letters(Languages.ENGLISH_US, numberRow = true)
        val comma = rows.last().keys.first { it.char == "," }
        assertTrue(comma.popupExtras(false).contains("#"))
        assertTrue(comma.popupExtras(false).contains("$"))
    }

    private fun letters(
        language: LanguageConfig,
        numberRow: Boolean,
        chrome: LetterChrome = LetterChrome.NONE
    ): List<KeyboardRow> =
        provider.rows(
            language,
            LayoutMode.LETTERS,
            letterChrome = chrome,
            numberRowEnabled = numberRow
        )

    private fun letterKey(rows: List<KeyboardRow>, ch: String): KeyboardKey =
        rows.flatMap { it.keys }.first { it.char == ch }
}

private fun String.isSingleDigit(): Boolean = length == 1 && this[0].isDigit()
