package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterChromeRowsTest {

    @Test
    fun none_returnsRowsUnchanged() {
        val rows = EnglishUsLayout.build().lettersRows
        val result = LetterChromeRows.apply(rows, LetterChrome.NONE)
        assertEquals(rows, result)
    }

    @Test
    fun email_upgradesPeriodWithTldPopupAndInsertsAtAfterIt() {
        val rows = LetterChromeRows.apply(EnglishUsLayout.build().lettersRows, LetterChrome.EMAIL)
        val bottom = rows.last().keys
        val spaceIdx = bottom.indexOfFirst { it.action == KeyAction.SPACE }
        assertTrue(spaceIdx >= 0)
        assertEquals(KeyAction.LANG_SWITCH, bottom[spaceIdx - 1].action)

        val period = bottom[spaceIdx + 1]
        val at = bottom[spaceIdx + 2]
        assertEquals(".", period.char)
        assertEquals(mergedPeriodChromePopup(), period.popupChars)

        assertEquals("@", at.char)
        assertTrue(at.popupChars.isEmpty())
    }

    @Test
    fun uri_upgradesPeriodWithTldPopupAndInsertsSlashAfterIt() {
        val rows = LetterChromeRows.apply(EnglishUsLayout.build().lettersRows, LetterChrome.URI)
        val bottom = rows.last().keys
        val spaceIdx = bottom.indexOfFirst { it.action == KeyAction.SPACE }
        assertTrue(spaceIdx >= 0)
        assertEquals(KeyAction.LANG_SWITCH, bottom[spaceIdx - 1].action)

        val period = bottom[spaceIdx + 1]
        val slash = bottom[spaceIdx + 2]
        assertEquals(".", period.char)
        assertEquals(mergedPeriodChromePopup(), period.popupChars)
        assertEquals("/", slash.char)
        assertEquals(LetterChromeRows.URI_SLASH_POPUP, slash.popupChars)
    }

    @Test
    fun email_doesNotDuplicatePeriodOnBottomRow() {
        val rows = LetterChromeRows.apply(EnglishUsLayout.build().lettersRows, LetterChrome.EMAIL)
        val periods = rows.last().keys.filter { it.action == KeyAction.CHAR && it.char == "." }
        assertEquals(1, periods.size)
    }

    /** TLD shortcuts stay first; remaining period extras are preserved after them. */
    private fun mergedPeriodChromePopup(): List<String> =
        (LetterChromeRows.TLD_POPUP + StandardRows.LETTER_PERIOD_POPUP.filter { it !in LetterChromeRows.TLD_POPUP })
            .distinct()
}
