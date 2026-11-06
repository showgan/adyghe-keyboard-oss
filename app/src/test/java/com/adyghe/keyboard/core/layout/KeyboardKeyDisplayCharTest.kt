package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardKeyDisplayCharTest {

    @Test
    fun latinLetter_respectsUpperFlag() {
        val key = Keys.char("q")
        assertEquals("Q", key.displayChar(upper = true))
        assertEquals("q", key.displayChar(upper = false))
    }

    @Test
    fun latinI_usesRootLocaleNotTurkish() {
        val key = Keys.char("i")
        assertEquals("I", key.displayChar(upper = true))
        assertEquals("i", key.displayChar(upper = false))
    }

    @Test
    fun turkishDottedAndDotlessI_respectTurkishLocale() {
        val dotted = letterKey(TurkishLayout.build(), "i")
        val dotless = letterKey(TurkishLayout.build(), "ı")
        assertEquals("İ", dotted.displayChar(upper = true))
        assertEquals("i", dotted.displayChar(upper = false))
        assertEquals("I", dotless.displayChar(upper = true))
        assertEquals("ı", dotless.displayChar(upper = false))
    }

    @Test
    fun adygheLatin1_dottedAndDotlessI_respectTurkishLocale() {
        val dotted = letterKey(AdygheLatin1Layout.build(), "i")
        val dotless = letterKey(AdygheLatin1Layout.build(), "ı")
        assertEquals("İ", dotted.displayChar(upper = true))
        assertEquals("i", dotted.displayChar(upper = false))
        assertEquals("I", dotless.displayChar(upper = true))
        assertEquals("ı", dotless.displayChar(upper = false))
    }

    @Test
    fun adygheLatin2_i_usesRootLocaleNotTurkish() {
        val key = letterKey(AdygheLatinLayout.build(), "i")
        assertEquals("I", key.displayChar(upper = true))
        assertEquals("i", key.displayChar(upper = false))
    }

    @Test
    fun cyrillicLetter_respectsUpperFlag() {
        val key = Keys.char("й")
        assertEquals("Й", key.displayChar(upper = true))
        assertEquals("й", key.displayChar(upper = false))
    }

    @Test
    fun nonLetter_unchanged() {
        val key = Keys.char(".")
        assertEquals(".", key.displayChar(upper = true))
        assertEquals(".", key.displayChar(upper = false))
    }

    @Test
    fun multiCharDigraph_unchanged() {
        val key = Keys.char("дж")
        assertEquals("дж", key.displayChar(upper = true))
        assertEquals("дж", key.displayChar(upper = false))
    }

    @Test
    fun nonCharKey_returnsEmptyWithoutInventingLabel() {
        val key = Keys.modifier(KeyAction.NUMBERS, "?123")
        assertEquals("", key.displayChar(upper = true))
        assertEquals("", key.displayChar(upper = false))
    }

    private fun letterKey(layout: KeyboardLayout, ch: String): KeyboardKey =
        layout.lettersRows.flatMap { it.keys }.first { it.char == ch }
}
