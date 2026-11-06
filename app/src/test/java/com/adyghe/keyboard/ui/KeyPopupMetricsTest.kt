package com.adyghe.keyboard.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyPopupMetricsTest {

    @Test
    fun cellWidth_growsWithLabelLength() {
        assertEquals(42, KeyPopupMetrics.cellWidthDp("."))
        assertEquals(54, KeyPopupMetrics.cellWidthDp(".io"))
        assertEquals(68, KeyPopupMetrics.cellWidthDp(".com"))
        assertEquals(68, KeyPopupMetrics.cellWidthDp("www."))
    }

    @Test
    fun totalWidth_includesPaddingAndSpacing() {
        val chars = listOf(".", ".com")
        val expected = KeyPopupMetrics.cellWidthDp(".") +
            KeyPopupMetrics.CELL_SPACING_DP +
            KeyPopupMetrics.cellWidthDp(".com") +
            KeyPopupMetrics.PANEL_PADDING_DP * 2
        assertEquals(expected, KeyPopupMetrics.totalWidthDp(chars))
    }

    @Test
    fun hitTestIndex_selectsCorrectVariableWidthCell() {
        val widths = floatArrayOf(42f, 68f, 54f)
        val spacing = 2f
        val pad = 4f
        // First cell (after padding).
        assertEquals(0, KeyPopupMetrics.hitTestIndex(pad + 10f, widths, spacing, pad))
        // Second cell starts after 42 + spacing.
        assertEquals(1, KeyPopupMetrics.hitTestIndex(pad + 42f + spacing + 5f, widths, spacing, pad))
        // Third cell.
        assertEquals(
            2,
            KeyPopupMetrics.hitTestIndex(pad + 42f + spacing + 68f + spacing + 5f, widths, spacing, pad)
        )
        // Past the end clamps to last.
        assertEquals(2, KeyPopupMetrics.hitTestIndex(10_000f, widths, spacing, pad))
        assertTrue(KeyPopupMetrics.hitTestIndex(-1f, widths, spacing, pad) == 0)
    }

    @Test
    fun verticalPlacement_prefersAboveWhenThereIsRoom() {
        val (top, above) = KeyPopupMetrics.verticalPlacement(
            localKeyTop = 100f,
            keyHeight = 52f,
            popupHeight = 66f
        )
        assertEquals(34f, top)
        assertTrue(above)
    }

    @Test
    fun verticalPlacement_pinsToTopEdgeWhenStripIsShorterThanPopup() {
        // Top letter row sits below a 40dp suggestion strip; popup is taller than the strip.
        val (top, above) = KeyPopupMetrics.verticalPlacement(
            localKeyTop = 40f,
            keyHeight = 52f,
            popupHeight = 66f,
            minTop = 0f
        )
        assertEquals(0f, top)
        assertTrue(above)
    }

    @Test
    fun verticalPlacement_placesBelowOnlyWhenKeyIsAtTopEdge() {
        val (top, above) = KeyPopupMetrics.verticalPlacement(
            localKeyTop = 0f,
            keyHeight = 52f,
            popupHeight = 66f,
            minTop = 0f
        )
        assertEquals(52f, top)
        assertTrue(!above)
    }

    @Test
    fun horizontalPlacement_alignsAnchorCellWithKeyCenter() {
        val chars = listOf("_", ")", "(", "-", "\"", "'")
        val popupW = KeyPopupMetrics.totalWidthDp(chars).toFloat()
        val anchorCenter = KeyPopupMetrics.cellCenterFromLeftDp(chars, 5)
        val left = KeyPopupMetrics.horizontalPlacement(
            keyLeft = 260f,
            keyWidth = 56f,
            parentWidth = 360f,
            popupWidth = popupW,
            anchorCenterFromLeft = anchorCenter
        )
        assertEquals(288f - anchorCenter, left, 0.01f)
    }

    @Test
    fun horizontalPlacement_clampsWhenAnchorWouldOverflow() {
        val chars = listOf("?", "!", ":", ";", "/", "@", "#", "$")
        val popupW = KeyPopupMetrics.totalWidthDp(chars).toFloat()
        val anchorCenter = KeyPopupMetrics.cellCenterFromLeftDp(chars, 0)
        val left = KeyPopupMetrics.horizontalPlacement(
            keyLeft = 80f,
            keyWidth = 56f,
            parentWidth = popupW,
            popupWidth = popupW,
            anchorCenterFromLeft = anchorCenter
        )
        assertEquals(0f, left, 0.01f)
    }

    @Test
    fun arrangeAroundAnchor_rightEdgeKeyPutsDefaultOnTheRight() {
        val extras = listOf("'", "\"", "-", "(", ")", "_")
        val layout = KeyPopupMetrics.arrangeAroundAnchor(
            extras = extras,
            keyCenterDp = 288f,
            parentWidthDp = 360f
        )
        assertEquals("'", layout.chars[layout.anchorIndex])
        assertEquals("'", layout.chars.last())
        assertEquals(listOf("_", ")", "(", "-", "\"", "'"), layout.chars)
        assertTrue(anchorStaysOnKey(layout, keyCenterDp = 288f, parentWidthDp = 360f))
    }

    @Test
    fun arrangeAroundAnchor_leftishKeyKeepsDefaultOnTheLeftWhenThereIsRoom() {
        val extras = listOf("?", "!", ":", ";", "/", "@", "#", "$")
        val layout = KeyPopupMetrics.arrangeAroundAnchor(
            extras = extras,
            keyCenterDp = 240f,
            parentWidthDp = 800f
        )
        assertEquals(0, layout.anchorIndex)
        assertEquals("?", layout.chars.first())
        assertEquals(extras, layout.chars)
        assertTrue(anchorStaysOnKey(layout, keyCenterDp = 240f, parentWidthDp = 800f))
    }

    @Test
    fun arrangeAroundAnchor_narrowWidthKeepsDefaultOverTheKey() {
        val extras = listOf("?", "!", ":", ";", "/", "@", "#", "$")
        val layout = KeyPopupMetrics.arrangeAroundAnchor(
            extras = extras,
            keyCenterDp = 108f,
            parentWidthDp = 360f
        )
        assertEquals("?", layout.chars[layout.anchorIndex])
        assertTrue(layout.anchorIndex > 0)
        assertTrue(anchorStaysOnKey(layout, keyCenterDp = 108f, parentWidthDp = 360f))
    }

    @Test
    fun stemOffset_centersStemOnAnchorCell() {
        val chars = listOf("_", ")", "(", "-", "\"", "'")
        val stemLeft = KeyPopupMetrics.stemOffsetDp(chars, 5)
        val cellCenter = KeyPopupMetrics.cellCenterFromLeftDp(chars, 5)
        assertEquals(cellCenter - KeyPopupMetrics.STEM_WIDTH_DP / 2f, stemLeft, 0.01f)
    }

    private fun anchorStaysOnKey(
        layout: AnchoredPopupLayout,
        keyCenterDp: Float,
        parentWidthDp: Float
    ): Boolean {
        val popupW = KeyPopupMetrics.totalWidthDp(layout.chars).toFloat()
        val anchorCenter = KeyPopupMetrics.cellCenterFromLeftDp(layout.chars, layout.anchorIndex)
        val left = KeyPopupMetrics.horizontalPlacement(
            keyLeft = keyCenterDp,
            keyWidth = 0f,
            parentWidth = parentWidthDp,
            popupWidth = popupW,
            anchorCenterFromLeft = anchorCenter
        )
        val actual = left + anchorCenter
        val cellW = KeyPopupMetrics.cellWidthDp(layout.chars[layout.anchorIndex]).toFloat()
        return kotlin.math.abs(actual - keyCenterDp) <= cellW / 2f
    }
}
