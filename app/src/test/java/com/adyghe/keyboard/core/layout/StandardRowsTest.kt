package com.adyghe.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class StandardRowsTest {

    @Test
    fun numbersPanel_matchesGboardPage1() {
        val rows = StandardRows.numbersPanel()
        assertEquals(4, rows.size)

        assertEquals(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            rows[0].keys.map { it.char }
        )
        assertEquals(
            listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
            rows[1].keys.map { it.char }
        )

        val row3 = rows[2].keys
        assertEquals(KeyAction.SYMBOLS, row3.first().action)
        assertEquals(
            listOf("*", "\"", "'", ":", ";", "!", "?"),
            row3.drop(1).dropLast(1).map { it.char }
        )
        assertEquals(KeyAction.BACKSPACE, row3.last().action)

        val bottom = rows[3].keys
        assertEquals(KeyAction.ABC, bottom[0].action)
        assertEquals(",", bottom[1].char)
        assertEquals(KeyAction.NUMBER_PAD, bottom[2].action)
        assertEquals(KeyAction.LANG_SWITCH, bottom[3].action)
        assertEquals(KeyAction.SPACE, bottom[4].action)
        assertEquals(3.0f, bottom[4].weight)
        assertEquals(".", bottom[5].char)
        assertEquals(KeyAction.ENTER, bottom[6].action)
    }

    @Test
    fun letterBottomRow_placesGlobeImmediatelyLeftOfSpace() {
        val bottom = StandardRows.letterBottomRow().keys
        assertEquals(KeyAction.NUMBERS, bottom[0].action)
        assertEquals(KeyAction.EMOJI, bottom[1].action)
        assertEquals(",", bottom[2].char)
        assertEquals(KeyAction.LANG_SWITCH, bottom[3].action)
        assertEquals(1.0f, bottom[3].weight)
        assertEquals(KeyAction.SPACE, bottom[4].action)
        assertEquals(3.0f, bottom[4].weight)
        assertEquals(".", bottom[5].char)
        assertEquals(KeyAction.ENTER, bottom[6].action)
    }

    @Test
    fun symbolsPanel_isFourRowsWithChrome() {
        val rows = StandardRows.symbolsPanel()
        assertEquals(4, rows.size)

        val row3 = rows[2].keys
        assertEquals(KeyAction.NUMBERS, row3.first().action)
        assertEquals(KeyAction.BACKSPACE, row3.last().action)

        val bottom = rows[3].keys
        assertEquals(KeyAction.ABC, bottom.first().action)
        assertEquals(",", bottom[1].char)
        assertEquals(KeyAction.CURSOR_LEFT, bottom[2].action)
        assertEquals(KeyAction.SPACE, bottom[3].action)
        assertEquals(KeyAction.CURSOR_RIGHT, bottom[4].action)
        assertEquals(".", bottom[5].char)
        assertEquals(KeyAction.ENTER, bottom.last().action)
    }
}
