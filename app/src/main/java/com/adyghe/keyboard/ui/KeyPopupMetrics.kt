package com.adyghe.keyboard.ui

import kotlin.math.abs

/** Visual popup extras with [anchorIndex] sitting over the long-pressed key. */
data class AnchoredPopupLayout(
    val chars: List<String>,
    val anchorIndex: Int
)

/**
 * Shared sizing for [KeyPopup] cells and the host's positioning / drag hit-testing.
 * Multi-character labels (TLDs like `.com`, URI prefixes like `www.`) need wider cells.
 */
object KeyPopupMetrics {
    const val CELL_HEIGHT_DP = 48
    const val CELL_SPACING_DP = 2
    const val PANEL_PADDING_DP = 4
    const val STEM_HEIGHT_DP = 10
    const val STEM_WIDTH_DP = 22

    fun cellWidthDp(label: String): Int = when {
        label.length <= 1 -> 42
        label.length <= 3 -> 54
        else -> 68
    }

    fun fontSizeSp(label: String): Int = when {
        label.length <= 1 -> 22
        label.length <= 3 -> 16
        else -> 14
    }

    /** Full popup panel width in dp (cells + spacing + horizontal padding). */
    fun totalWidthDp(popupChars: List<String>): Int {
        if (popupChars.isEmpty()) return PANEL_PADDING_DP * 2
        val cells = popupChars.sumOf { cellWidthDp(it) }
        val spacing = (popupChars.size - 1) * CELL_SPACING_DP
        return cells + spacing + PANEL_PADDING_DP * 2
    }

    /** Panel + stem height in dp (matches [KeyPopup] layout). */
    fun totalHeightDp(): Int =
        PANEL_PADDING_DP * 2 + CELL_HEIGHT_DP + STEM_HEIGHT_DP

    /**
     * Distance from the panel's left edge to the center of [index], in dp.
     */
    fun cellCenterFromLeftDp(chars: List<String>, index: Int): Float {
        if (chars.isEmpty()) return PANEL_PADDING_DP.toFloat()
        val i = index.coerceIn(0, chars.lastIndex)
        var x = PANEL_PADDING_DP.toFloat()
        for (j in 0 until i) {
            x += cellWidthDp(chars[j]) + CELL_SPACING_DP
        }
        return x + cellWidthDp(chars[i]) / 2f
    }

    /**
     * Left offset of the stem so it sits under the anchor cell (same units as [STEM_WIDTH_DP]).
     */
    fun stemOffsetDp(chars: List<String>, anchorIndex: Int): Float {
        val cellCenter = cellCenterFromLeftDp(chars, anchorIndex)
        val popupW = totalWidthDp(chars).toFloat()
        return (cellCenter - STEM_WIDTH_DP / 2f)
            .coerceIn(0f, (popupW - STEM_WIDTH_DP).coerceAtLeast(0f))
    }

    /**
     * Horizontal offset of the popup's left edge so [anchorCenterFromLeft] lines up with the
     * key center, then clamped to [0, parentWidth - popupWidth]. All arguments share one unit.
     */
    fun horizontalPlacement(
        keyLeft: Float,
        keyWidth: Float,
        parentWidth: Float,
        popupWidth: Float,
        anchorCenterFromLeft: Float
    ): Float {
        val keyCenter = keyLeft + keyWidth / 2f
        return (keyCenter - anchorCenterFromLeft)
            .coerceIn(0f, (parentWidth - popupWidth).coerceAtLeast(0f))
    }

    /**
     * Visual order that keeps [extras]`[0]` (the default extra) over [keyCenterDp] after
     * clamping. Remaining extras (already frequency-sorted) go nearest-to-key first on
     * each side; leftover extras fill the side with more room.
     */
    fun arrangeAroundAnchor(
        extras: List<String>,
        keyCenterDp: Float,
        parentWidthDp: Float
    ): AnchoredPopupLayout {
        if (extras.isEmpty()) return AnchoredPopupLayout(emptyList(), 0)
        if (extras.size == 1 || parentWidthDp <= 0f) return AnchoredPopupLayout(extras, 0)

        val preferred = extras.first()
        val rest = extras.drop(1)
        val preferMoreOnLeft = keyCenterDp > parentWidthDp / 2f

        var bestDist = Float.MAX_VALUE
        var bestLeftCount = 0
        var bestVisual = extras

        for (leftCount in 0..rest.size) {
            val leftExtras = rest.take(leftCount).asReversed()
            val visual = leftExtras + preferred + rest.drop(leftCount)
            val anchorIndex = leftCount
            val popupW = totalWidthDp(visual).toFloat()
            val anchorCenter = cellCenterFromLeftDp(visual, anchorIndex)
            val popupLeft = horizontalPlacement(
                keyLeft = keyCenterDp,
                keyWidth = 0f,
                parentWidth = parentWidthDp,
                popupWidth = popupW,
                anchorCenterFromLeft = anchorCenter
            )
            val dist = abs(popupLeft + anchorCenter - keyCenterDp)
            val tied = abs(dist - bestDist) <= 0.5f
            val better = dist < bestDist - 0.5f ||
                (tied && preferMoreOnLeft && leftCount > bestLeftCount)
            if (better) {
                bestDist = dist
                bestLeftCount = leftCount
                bestVisual = visual
            }
        }

        return AnchoredPopupLayout(bestVisual, bestLeftCount)
    }

    /**
     * Vertical offset of the popup top in the keyboard-chrome coordinate space (toolbar +
     * key rows). Always prefers above the key so top-row popups can sit in the toolbar.
     * Only places below when the key is already at [minTop] (no room above at all).
     *
     * @return pair of (topY, anchorAbove)
     */
    fun verticalPlacement(
        localKeyTop: Float,
        keyHeight: Float,
        popupHeight: Float,
        minTop: Float = 0f
    ): Pair<Float, Boolean> {
        val preferredAbove = localKeyTop - popupHeight
        return when {
            // Full popup fits above the key within the chrome / display.
            preferredAbove >= minTop -> preferredAbove to true
            // Key has chrome above it (e.g. toolbar) but not enough for the full
            // popup height: pin to the top edge so the panel still sits above the key row.
            localKeyTop > minTop -> minTop to true
            // Key is at the top display edge — nowhere above to go.
            else -> (localKeyTop + keyHeight) to false
        }
    }

    /**
     * Maps a horizontal offset (from the popup panel's left edge, in the same unit as
     * [cellWidthsPx]) to the selected cell index.
     */
    fun hitTestIndex(xFromPanelLeft: Float, cellWidthsPx: FloatArray, spacingPx: Float, padStartPx: Float): Int {
        if (cellWidthsPx.isEmpty()) return 0
        var x = xFromPanelLeft - padStartPx
        if (x < 0f) return 0
        for (i in cellWidthsPx.indices) {
            val w = cellWidthsPx[i] + if (i < cellWidthsPx.lastIndex) spacingPx else 0f
            if (x < w) return i
            x -= w
        }
        return cellWidthsPx.lastIndex
    }
}
