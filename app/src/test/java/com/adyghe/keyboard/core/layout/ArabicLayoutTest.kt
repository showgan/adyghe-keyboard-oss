package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicLayoutTest {

    private val layout = ArabicLayout.build()
    private val letterRows = layout.lettersRows.take(3)

    private fun letterChars(row: KeyboardRow): List<String> =
        row.keys.filter { it.action == KeyAction.CHAR }.mapNotNull { it.char }

    @Test
    fun allTwentyEightAlphabetLettersAreMainKeys() {
        val alphabet = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي".map { it.toString() }
        val mainChars = letterRows.flatMap { letterChars(it) }.toSet()
        for (letter in alphabet) {
            assertTrue("$letter should be a main key", letter in mainChars)
        }
    }

    @Test
    fun mainKeysAreUniqueAndOmitHamzaOnYaAndLamAlef() {
        val mainChars = letterRows.flatMap { letterChars(it) }
        assertEquals(mainChars.toSet().size, mainChars.size)
        assertFalse(mainChars.contains("ئ"))
        assertFalse(mainChars.contains("لا"))
    }

    @Test
    fun rowShapesMatchGboard() {
        assertEquals(11, letterChars(letterRows[0]).size)
        assertEquals(11, letterChars(letterRows[1]).size)
        assertEquals(10, letterChars(letterRows[2]).size)

        val row3 = letterRows[2].keys
        assertTrue(row3.none { it.action == KeyAction.SHIFT })
        assertEquals(KeyAction.BACKSPACE, row3.last().action)
        assertTrue(row3.dropLast(1).all { it.action == KeyAction.CHAR })
    }

    @Test
    fun gboardLetterPositions() {
        assertEquals("ط", letterChars(letterRows[1]).last())
        assertEquals("ذ", letterChars(letterRows[2]).first())
        assertEquals("د", letterChars(letterRows[2]).last())
    }
}
