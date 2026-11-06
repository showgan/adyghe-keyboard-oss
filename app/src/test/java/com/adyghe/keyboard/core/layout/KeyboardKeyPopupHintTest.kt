package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome
import com.adyghe.keyboard.core.language.Languages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardKeyPopupHintTest {

    private val provider = LayoutProvider()

    @Test
    fun noExtras_returnsNull() {
        assertNull(Keys.char("d").popupHint(upper = false))
        assertNull(Keys.char("x", popup = listOf("x")).popupHint(upper = false))
    }

    @Test
    fun twoExtras_joinInHint() {
        val key = Keys.char("e", popup = listOf("e", "é", "3"))
        assertEquals("é3", key.popupHint(upper = false))
        assertEquals("É3", key.popupHint(upper = true))
    }

    @Test
    fun threeExtras_joinWhenSecondIsDigit() {
        val key = Keys.char("i", popup = listOf("i", "ı", "8", "İ"))
        assertEquals("ı8", key.popupHint(upper = false))
    }

    @Test
    fun threeExtras_keepFirstWhenSecondIsNotDigit() {
        val key = Keys.char("s", popup = listOf("s", "ş", "ś", "š"))
        assertEquals("ş", key.popupHint(upper = false))
    }

    @Test
    fun englishUs_numberHintsOnTopRow() {
        val layout = EnglishUsLayout.build()
        assertEquals("3", letterKey(layout, "e").popupHint(false))
        assertEquals("0", letterKey(layout, "p").popupHint(false))
        assertNull(letterKey(layout, "a").popupHint(false))
    }

    @Test
    fun adygheLatin2_usesLetterThenDigitHints() {
        val layout = AdygheLatinLayout.build()
        assertEquals("é3", letterKey(layout, "e").popupHint(false))
        assertEquals("ṫ5", letterKey(layout, "t").popupHint(false))
        assertEquals("ü7", letterKey(layout, "u").popupHint(false))
        assertEquals("ı8", letterKey(layout, "i").popupHint(false))
        assertEquals("ö9", letterKey(layout, "o").popupHint(false))
        assertEquals("ṕ0", letterKey(layout, "p").popupHint(false))
        assertEquals("ğǵ", letterKey(layout, "g").popupHint(false))
        assertEquals("á", letterKey(layout, "a").popupHint(false))
        assertEquals("ş", letterKey(layout, "s").popupHint(false))
        assertEquals("çć", letterKey(layout, "c").popupHint(false))
    }

    @Test
    fun adygheLatin1_usesLetterThenDigitHints() {
        val layout = AdygheLatin1Layout.build()
        assertEquals("é3", letterKey(layout, "e").popupHint(false))
        assertEquals("ṫ5", letterKey(layout, "t").popupHint(false))
        assertEquals("ṕ0", letterKey(layout, "p").popupHint(false))
        assertEquals("śš", letterKey(layout, "s").popupHint(false))
        assertEquals("ǵ", letterKey(layout, "g").popupHint(false))
        assertEquals("8", letterKey(layout, "ı").popupHint(false))
        assertEquals("7", letterKey(layout, "u").popupHint(false))
        assertEquals("á", letterKey(layout, "a").popupHint(false))
        assertEquals("ḣ", letterKey(layout, "h").popupHint(false))
        assertNull(letterKey(layout, "ü").popupHint(false))
        assertFalse(letterKey(layout, "u").popupExtras(false).contains("ü"))
    }

    @Test
    fun adygheCyrillic_usesFirstDigraphExtra() {
        val layout = AdygheCyrillicLayout.build()
        assertEquals("гъ", letterKey(layout, "г").popupHint(false))
        assertEquals("й", letterKey(layout, "и").popupHint(false))
    }

    @Test
    fun palochka_hintFollowsShiftedPopup() {
        val key = letterKey(AdygheCyrillicLayout.build(), "ӏ")
        assertEquals("Ӏ", key.popupHint(upper = false))
        assertEquals("ЪЬ", key.popupHint(upper = true))
    }

    @Test
    fun turkish_dualHintOnEAndU() {
        val layout = TurkishLayout.build()
        assertEquals("3é", letterKey(layout, "e").popupHint(false))
        assertEquals("8", letterKey(layout, "ı").popupHint(false))
        assertEquals("7û", letterKey(layout, "u").popupHint(false))
        assertEquals("â", letterKey(layout, "a").popupHint(false))
        assertEquals("î", letterKey(layout, "i").popupHint(false))
        assertTrue(letterKey(layout, "u").popupExtras(false).contains("û"))
    }

    @Test
    fun russian_numberHintThenHardSign() {
        val layout = RussianLayout.build()
        assertEquals("5ё", letterKey(layout, "е").popupHint(false))
        assertEquals("ъ", letterKey(layout, "ь").popupHint(false))
    }

    @Test
    fun arabic_usesFirstHamzaVariant() {
        assertEquals("أ", letterKey(ArabicLayout.build(), "ا").popupHint(false))
        assertEquals("ئ", letterKey(ArabicLayout.build(), "ي").popupHint(false))
    }

    @Test
    fun arabic_topRowHintsEasternIndicDigit() {
        val layout = ArabicLayout.build()
        assertEquals("١", letterKey(layout, "ض").popupHint(false))
        assertEquals("٨ه\u200D", letterKey(layout, "ه").popupHint(false))
        assertEquals("٠", letterKey(layout, "ح").popupHint(false))
        assertNull(letterKey(layout, "ج").popupHint(false))
    }

    @Test
    fun hebrew_usesFirstExtraNotLaterDigits() {
        val layout = HebrewLayout.build()
        assertEquals("1", letterKey(layout, "ק").popupHint(false))
        assertEquals("פּ8", letterKey(layout, "פ").popupHint(false))
        assertEquals("שׂשׁ", letterKey(layout, "ש").popupHint(false))
    }

    @Test
    fun hebrew_dageshAndGereshHints() {
        val layout = HebrewLayout.build()
        assertEquals("דּ", letterKey(layout, "ד").popupHint(false))
        assertEquals("תּ", letterKey(layout, "ת").popupHint(false))
        assertEquals("ח׳", letterKey(layout, "ח").popupHint(false))
        assertEquals("ךּ", letterKey(layout, "ך").popupHint(false))
        assertEquals("ףּ", letterKey(layout, "ף").popupHint(false))
    }

    @Test
    fun commaAndPeriod_useFirstPunctuationExtra() {
        val bottom = EnglishUsLayout.build().lettersRows.last().keys
        val comma = bottom.first { it.char == "," }
        val period = bottom.first { it.char == "." }
        assertEquals("?", comma.popupHint(false))
        assertEquals("'", period.popupHint(false))
        assertEquals(
            listOf("?", "!", ":", ";", "/", "@", "#", "$"),
            comma.popupExtras(false)
        )
        assertEquals(
            listOf("'", "\"", "-", "(", ")", "_"),
            period.popupExtras(false)
        )
    }

    @Test
    fun arabicComma_usesArabicQuestionAsFirstExtra() {
        val bottom = ArabicLayout.build().lettersRows.last().keys
        val comma = bottom.first { it.char == "\u060C" }
        val period = bottom.first { it.char == "." }
        assertEquals("\u061F", comma.popupHint(false))
        assertEquals("'", period.popupHint(false))
        assertEquals(
            listOf("\u061F", "!", ":", "\u061B", "/", "@", "#", "$"),
            comma.popupExtras(false)
        )
        assertEquals(
            listOf("'", "\"", "-", "(", ")", "_"),
            period.popupExtras(false)
        )
    }

    @Test
    fun emailChrome_periodHintIsFirstTld() {
        val rows = LetterChromeRows.apply(EnglishUsLayout.build().lettersRows, LetterChrome.EMAIL)
        val period = rows.last().keys.first { it.char == "." }
        assertEquals(".com", period.popupHint(false))
    }

    @Test
    fun uriChrome_slashHintIsFirstPrefix() {
        val rows = LetterChromeRows.apply(EnglishUsLayout.build().lettersRows, LetterChrome.URI)
        val slash = rows.last().keys.first { it.char == "/" }
        assertEquals("://www.", slash.popupHint(false))
    }

    @Test
    fun currencyKey_usesFirstAlternate() {
        val dollar = StandardRows.numbersPanel()
            .flatMap { it.keys }
            .first { it.char == "$" }
        assertEquals("€", dollar.popupHint(false))
    }

    @Test
    fun numbersQuestionMark_usesInvertedHint() {
        val question = StandardRows.numbersPanel()
            .flatMap { it.keys }
            .first { it.char == "?" }
        assertEquals("¿", question.popupHint(false))
    }

    @Test
    fun phoneZero_usesPlus() {
        val zero = StandardRows.phonePad()
            .flatMap { it.keys }
            .first { it.char == "0" }
        assertEquals("+", zero.popupHint(false))
    }

    @Test
    fun everyLanguageHasLetterKeysWithHints() {
        for (language in Languages.ALL) {
            val hinted = provider.layoutFor(language).lettersRows
                .flatMap { it.keys }
                .count { it.popupHint(upper = false) != null }
            assertTrue("${language.id} should show popup hints", hinted > 0)
        }
    }

    private fun letterKey(layout: KeyboardLayout, ch: String): KeyboardKey =
        layout.lettersRows.flatMap { it.keys }.first { it.char == ch }
}
