package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HebrewLayoutTest {

    private val layout = HebrewLayout.build()
    private val letterRows = layout.lettersRows.take(3)

    private fun letterChars(row: KeyboardRow): List<String> =
        row.keys.filter { it.action == KeyAction.CHAR }.mapNotNull { it.char }

    @Test
    fun allLettersAndSofitFormsAreMainKeys() {
        val alphabet = "אבגדהוזחטיכלמנסעפצקרשת".map { it.toString() }
        val sofit = listOf("ך", "ם", "ן", "ף", "ץ")
        val mainChars = letterRows.flatMap { letterChars(it) }.toSet()
        for (letter in alphabet + sofit) {
            assertTrue("$letter should be a main key", letter in mainChars)
        }
        assertEquals(alphabet.size + sofit.size, mainChars.size)
    }

    @Test
    fun rowShapesHaveNoShift() {
        assertEquals(8, letterChars(letterRows[0]).size)
        assertEquals(10, letterChars(letterRows[1]).size)
        assertEquals(9, letterChars(letterRows[2]).size)

        val row3 = letterRows[2].keys
        assertTrue(row3.none { it.action == KeyAction.SHIFT })
        assertEquals(KeyAction.BACKSPACE, row3.last().action)
        assertTrue(row3.dropLast(1).all { it.action == KeyAction.CHAR })
    }

    @Test
    fun rowsAreVisualLeftToRightLikePhysicalHebrew() {
        assertEquals(
            listOf("ק", "ר", "א", "ט", "ו", "ן", "ם", "פ"),
            letterChars(letterRows[0])
        )
        assertEquals("ש", letterChars(letterRows[1]).first())
        assertEquals("ז", letterChars(letterRows[2]).first())

        val bottom = layout.lettersRows.last().keys
        assertEquals(KeyAction.NUMBERS, bottom.first().action)
        assertEquals(KeyAction.ENTER, bottom.last().action)
    }
}
