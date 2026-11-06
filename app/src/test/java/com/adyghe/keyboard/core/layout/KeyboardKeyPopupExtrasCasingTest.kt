package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class KeyboardKeyPopupExtrasCasingTest {

    @Test
    fun latinAccents_uppercaseWithShift() {
        val key = Keys.char("e", popup = listOf("e", "é", "3"))
        assertEquals(listOf("é", "3"), key.popupExtras(false))
        assertEquals(listOf("É", "3"), key.popupExtras(true))
    }

    @Test
    fun digitsAndPunctuation_unchanged() {
        val comma = Keys.char(",", popup = StandardRows.LETTER_COMMA_POPUP)
        val period = Keys.char(".", popup = StandardRows.LETTER_PERIOD_POPUP)
        assertEquals(comma.popupExtras(false), comma.popupExtras(true))
        assertEquals(period.popupExtras(false), period.popupExtras(true))
    }

    @Test
    fun urlTokens_notUppercased() {
        val period = Keys.char(".", popup = LetterChromeRows.TLD_POPUP)
        val slash = Keys.char("/", popup = LetterChromeRows.URI_SLASH_POPUP)
        assertEquals(listOf(".com", ".net", ".org", ".edu", ".io"), period.popupExtras(true))
        assertEquals(listOf("://", "www."), slash.popupExtras(true))
    }

    @Test
    fun cyrillicDigraphs_uppercaseAllLetters() {
        val key = letterKey(AdygheCyrillicLayout.build(), "г")
        assertEquals(listOf("гъ"), key.popupExtras(false))
        assertEquals(listOf("ГЪ"), key.popupExtras(true))
        assertEquals(listOf("КЪ", "КӀ"), letterKey(AdygheCyrillicLayout.build(), "к").popupExtras(true))
    }

    @Test
    fun palochka_dropsUppercaseDuplicateAndCasesSigns() {
        val key = letterKey(AdygheCyrillicLayout.build(), "ӏ")
        assertEquals(listOf("Ӏ", "ъ", "ь"), key.popupExtras(false))
        assertEquals(listOf("Ъ", "Ь"), key.popupExtras(true))
    }

    @Test
    fun turkishCircumflex_usesTurkishLocale() {
        val i = letterKey(TurkishLayout.build(), "i")
        val a = letterKey(TurkishLayout.build(), "a")
        assertEquals(listOf("î"), i.popupExtras(false))
        assertEquals(listOf("Î"), i.popupExtras(true))
        assertEquals(listOf("Â"), a.popupExtras(true))
    }

    @Test
    fun adygheLatin2_iPopup_shiftDropsDotlessIThatCasesToPrimary() {
        val key = letterKey(AdygheLatinLayout.build(), "i")
        assertEquals(listOf("ı", "8", "İ"), key.popupExtras(false))
        assertEquals(listOf("8", "İ"), key.popupExtras(true))
    }

    @Test
    fun adygheLatin1_dotlessI_usesTurkishLocale() {
        val tr = Locale.forLanguageTag("tr")
        assertEquals("I", Keys.char("ı", popup = listOf("ı", "8"), locale = tr).displayChar(true))
        val e = letterKey(AdygheLatin1Layout.build(), "e")
        assertEquals(listOf("É", "3"), e.popupExtras(true))
    }

    @Test
    fun hebrewCombining_unchanged() {
        val pe = letterKey(HebrewLayout.build(), "פ")
        assertEquals(pe.popupExtras(false), pe.popupExtras(true))
    }

    @Test
    fun arabicLetters_unchanged() {
        val alef = letterKey(ArabicLayout.build(), "ا")
        assertEquals(alef.popupExtras(false), alef.popupExtras(true))
    }

    @Test
    fun alreadyUpperExtra_staysUpper() {
        val key = Keys.char("i", popup = listOf("i", "ı", "İ"))
        assertEquals(listOf("ı", "İ"), key.popupExtras(false))
        assertEquals(listOf("İ"), key.popupExtras(true))
    }

    private fun letterKey(layout: KeyboardLayout, ch: String): KeyboardKey =
        layout.lettersRows.flatMap { it.keys }.first { it.char == ch }
}
