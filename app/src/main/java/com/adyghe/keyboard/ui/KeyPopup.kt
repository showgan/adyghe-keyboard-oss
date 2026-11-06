package com.adyghe.keyboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adyghe.keyboard.core.state.PopupSelection

/**
 * The floating long-press popup anchored to a key. Renders the available popup characters,
 * highlighting the currently selected one. Position (x, y) is provided by the host.
 *
 * @param anchorAbove when true (default), the stem points down toward the key below;
 *   when false the stem is on top (popup placed under a top-row key).
 * @param stemIndex cell the stem sits under (the extra that stays over the key).
 */
@Composable
fun KeyPopup(
    popupChars: List<String>,
    selection: PopupSelection?,
    modifier: Modifier = Modifier,
    anchorAbove: Boolean = true,
    stemIndex: Int = 0
) {
    val colors = MaterialTheme.colorScheme
    val popupBg = colors.surfaceContainerHigh
    val border = colors.outline
    val cellBg = colors.surfaceContainer
    val unselectedText = colors.onSurface
    val shape = RoundedCornerShape(10.dp)
    val panelWidthDp = KeyPopupMetrics.totalWidthDp(popupChars)
    val stemOffset = KeyPopupMetrics.stemOffsetDp(popupChars, stemIndex).dp

    val stem = @Composable {
        Box(
            modifier = Modifier
                .offset(x = stemOffset)
                .size(width = KeyPopupMetrics.STEM_WIDTH_DP.dp, height = KeyPopupMetrics.STEM_HEIGHT_DP.dp)
                .background(popupBg)
        )
    }

    val panel = @Composable {
        Surface(
            shape = shape,
            color = popupBg,
            shadowElevation = 10.dp,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, border)
        ) {
            Row(
                modifier = Modifier.padding(KeyPopupMetrics.PANEL_PADDING_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(KeyPopupMetrics.CELL_SPACING_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                popupChars.forEachIndexed { index, ch ->
                    val isSel = selection?.keyIndex == index
                    Box(
                        modifier = Modifier
                            .size(
                                width = KeyPopupMetrics.cellWidthDp(ch).dp,
                                height = KeyPopupMetrics.CELL_HEIGHT_DP.dp
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) colors.primary else cellBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ch,
                            fontSize = KeyPopupMetrics.fontSizeSp(ch).sp,
                            color = if (isSel) colors.onPrimary else unselectedText,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.width(panelWidthDp.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (anchorAbove) {
            panel()
            stem()
        } else {
            stem()
            panel()
        }
    }
}
