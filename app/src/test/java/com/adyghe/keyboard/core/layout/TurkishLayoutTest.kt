package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkishLayoutTest {

    private val layout = TurkishLayout.build()
    private val letterRows = layout.lettersRows.take(3)

    private fun letterChars(row: KeyboardRow): List<String> =
        row.keys.filter { it.action == KeyAction.CHAR }.mapNotNull { it.char }

    @Test
    fun allTwentyNineLettersAreMainKeys() {
        val alphabet = "abcçdefgğhıijklmnoöprsştuüvyz".map { it.toString() }
        val mainChars = letterRows.flatMap { letterChars(it) }.toSet()
        for (letter in alphabet) {
            assertTrue("$letter should be a main key", letter in mainChars)
        }
        assertTrue("q, w, x are on Turkish-Q", setOf("q", "w", "x").all { it in mainChars })
        assertEquals(32, mainChars.size)
    }

    @Test
    fun turkishQRowShapes() {
        assertEquals(12, letterChars(letterRows[0]).size)
        assertEquals(11, letterChars(letterRows[1]).size)
        assertEquals(9, letterChars(letterRows[2]).size)
        assertEquals("ğ", letterChars(letterRows[0])[10])
        assertEquals("ü", letterChars(letterRows[0])[11])
        assertEquals("ş", letterChars(letterRows[1])[9])
        assertEquals("i", letterChars(letterRows[1])[10])
        assertEquals("ö", letterChars(letterRows[2])[7])
        assertEquals("ç", letterChars(letterRows[2])[8])
    }
}
