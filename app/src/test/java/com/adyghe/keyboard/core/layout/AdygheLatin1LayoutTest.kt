package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdygheLatin1LayoutTest {

    private val layout = AdygheLatin1Layout.build()
    private val letterRows = layout.lettersRows.take(3)

    private fun letterChars(row: KeyboardRow): List<String> =
        row.keys.filter { it.action == KeyAction.CHAR }.mapNotNull { it.char }

    private fun letterKey(ch: String): KeyboardKey =
        layout.lettersRows.flatMap { it.keys }.first { it.char == ch }

    @Test
    fun turkishQRowShapes() {
        assertEquals(12, letterChars(letterRows[0]).size)
        assertEquals(11, letterChars(letterRows[1]).size)
        assertEquals(9, letterChars(letterRows[2]).size)
        assertEquals(
            listOf("q", "w", "e", "r", "t", "y", "u", "ı", "o", "p", "ğ", "ü"),
            letterChars(letterRows[0])
        )
        assertEquals(
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i"),
            letterChars(letterRows[1])
        )
        assertEquals(
            listOf("z", "x", "c", "v", "b", "n", "m", "ö", "ç"),
            letterChars(letterRows[2])
        )
    }

    @Test
    fun adygheExtrasStayOnHostKeys() {
        assertEquals(listOf("é", "3"), letterKey("e").popupExtras(false))
        assertEquals(listOf("ṫ", "5"), letterKey("t").popupExtras(false))
        assertEquals(listOf("ṕ", "0"), letterKey("p").popupExtras(false))
        assertEquals(listOf("á"), letterKey("a").popupExtras(false))
        assertEquals(listOf("ś", "š"), letterKey("s").popupExtras(false))
        assertEquals(listOf("ǵ"), letterKey("g").popupExtras(false))
        assertEquals(listOf("ḣ"), letterKey("h").popupExtras(false))
        assertEquals(listOf("ḱ"), letterKey("k").popupExtras(false))
        assertEquals(listOf("ĺ"), letterKey("l").popupExtras(false))
        assertEquals(listOf("ź"), letterKey("z").popupExtras(false))
        assertEquals(listOf("ć"), letterKey("c").popupExtras(false))
        for (extra in listOf("ḣ", "ḱ", "ĺ", "ṕ", "é", "ṫ", "á", "ǵ", "ś", "š", "ź", "ć")) {
            assertTrue("$extra should be an extra", extra in allExtras())
        }
    }

    @Test
    fun dedicatedLettersAreNotPopupExtrasOnOtherKeys() {
        val dedicated = setOf("ğ", "ü", "ş", "ı", "ö", "ç", "i")
        val extras = allExtras()
        for (letter in dedicated) {
            assertFalse("$letter should not be a popup extra", letter in extras)
        }
    }

    private fun allExtras(): Set<String> =
        layout.lettersRows.flatMap { row ->
            row.keys.flatMap { it.popupExtras(false) }
        }.toSet()
}
