package com.adyghe.keyboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.KeyboardKey
import com.adyghe.keyboard.core.state.EnterKeyAction
import com.adyghe.keyboard.core.state.ShiftState

// Pressed-key darkening overlay (theme-independent).
private val PressedOverlay = Color(0x1F000000)

/**
 * Purely visual key. All touch handling lives in [KeyboardScreen] at the container level so
 * that cross-key drag and popup drag-to-select behave consistently with Gboard.
 */
@Composable
fun KeyView(
    key: KeyboardKey,
    upper: Boolean,
    pressed: Boolean,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    shiftState: ShiftState = if (upper) ShiftState.SHIFTED else ShiftState.UNSHIFTED,
    spaceLabel: String? = null,
    enterAction: EnterKeyAction = EnterKeyAction.RETURN
) {
    val colors = MaterialTheme.colorScheme
    val keyBg = colors.surfaceContainer
    val modifierBg = colors.secondaryContainer
    val textColor = colors.onSurface

    // Shift's active state reuses the normal key background (like Gboard), so it must win
    // over the generic modifier tint.
    val shiftActive = key.action == KeyAction.SHIFT && shiftState != ShiftState.UNSHIFTED
    val background = when {
        highlight -> colors.outline
        pressed -> keyBg.copy(alpha = 0.7f)
        shiftActive -> keyBg
        key.isModifier -> modifierBg
        else -> keyBg
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            key.action == KeyAction.ENTER -> {
                EnterKeyIcon(
                    action = enterAction,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            key.action == KeyAction.NUMBER_PAD -> {
                NumpadSwitchIcon(
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            key.label != null -> {
                val label = if (key.action == KeyAction.CHAR) {
                    key.displayChar(upper).ifEmpty { checkNotNull(key.label) }
                } else {
                    checkNotNull(key.label)
                }
                Text(
                    text = label,
                    color = textColor,
                    fontSize = if (key.action == KeyAction.CHAR && label.length > 1) 16.sp else 20.sp,
                    fontWeight = if (key.isModifier) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
            key.action == KeyAction.SPACE && spaceLabel != null -> {
                // Gboard-style: the current language's native name in a small, quiet label.
                Text(
                    text = spaceLabel,
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            else -> KeyIcon(key.action, shiftState)
        }

        // Gboard-style corner hint: long-press extras (e.g. "é3" on Adyghe Latin E).
        val hint = key.popupHint(upper)
        if (hint != null) {
            val hintSize = when {
                hint.length <= 1 -> 10.sp
                hint.length <= 3 -> 8.sp
                else -> 7.sp
            }
            Text(
                text = hint,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 3.dp),
                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = hintSize,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                lineHeight = hintSize,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun KeyIcon(action: KeyAction, shiftState: ShiftState) {
    val tint = MaterialTheme.colorScheme.onSurface
    if (action == KeyAction.SHIFT) {
        ShiftArrowIcon(
            shiftState = shiftState,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        return
    }
    if (action == KeyAction.LANG_SWITCH) {
        GlobeIcon(
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        return
    }
    Text(
        text = when (action) {
            KeyAction.BACKSPACE -> "⌫"
            KeyAction.ENTER -> "↵"
            KeyAction.EMOJI -> "☺"
            KeyAction.CURSOR_LEFT -> "←"
            KeyAction.CURSOR_RIGHT -> "→"
            else -> ""
        },
        color = tint,
        fontSize = 20.sp
    )
}

/** Outlined globe (circle, meridian, equator) matching Shift/Enter stroke icons. */
@Composable
private fun GlobeIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = size.width * 0.09f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = min(size.width, size.height) * 0.42f
        val center = Offset(cx, cy)
        drawCircle(tint, radius = r, center = center, style = stroke)
        drawOval(
            color = tint,
            topLeft = Offset(cx - r * 0.40f, cy - r),
            size = Size(r * 0.80f, r * 2f),
            style = stroke
        )
        drawOval(
            color = tint,
            topLeft = Offset(cx - r, cy - r * 0.36f),
            size = Size(r * 2f, r * 0.72f),
            style = stroke
        )
    }
}

/** Gboard-style 2×2 "1234" glyph on the numbers-panel compact-pad key. */
@Composable
private fun NumpadSwitchIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    val digitStyle = TextStyle(
        color = tint,
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 9.sp
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("1", style = digitStyle)
            Text("2", style = digitStyle)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("3", style = digitStyle)
            Text("4", style = digitStyle)
        }
    }
}

/**
 * Gboard-weight enter glyph: return arrow, search, go, send, done, or next.
 */
@Composable
private fun EnterKeyIcon(
    action: EnterKeyAction,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.10f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (action) {
            EnterKeyAction.RETURN -> {
                // Classical 90° return: vertical stem down, then left, with filled arrowhead.
                val stemX = w * 0.68f
                val topY = h * 0.18f
                val cornerY = h * 0.72f
                val tipX = w * 0.18f
                drawLine(tint, Offset(stemX, topY), Offset(stemX, cornerY), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(stemX, cornerY), Offset(tipX + w * 0.14f, cornerY), stroke.width, StrokeCap.Round)
                val head = Path().apply {
                    moveTo(tipX, cornerY)
                    lineTo(tipX + w * 0.28f, cornerY - h * 0.16f)
                    lineTo(tipX + w * 0.28f, cornerY + h * 0.16f)
                    close()
                }
                drawPath(head, tint)
            }
            EnterKeyAction.SEARCH -> {
                val cx = w * 0.42f
                val cy = h * 0.42f
                val r = w * 0.28f
                drawCircle(tint, radius = r, center = Offset(cx, cy), style = stroke)
                drawLine(
                    tint,
                    Offset(cx + r * 0.70f, cy + r * 0.70f),
                    Offset(w * 0.88f, h * 0.88f),
                    stroke.width,
                    StrokeCap.Round
                )
            }
            EnterKeyAction.GO -> {
                // Right-pointing arrow.
                val shaftY = h * 0.50f
                drawLine(tint, Offset(w * 0.12f, shaftY), Offset(w * 0.62f, shaftY), stroke.width, StrokeCap.Round)
                val head = Path().apply {
                    moveTo(w * 0.88f, shaftY)
                    lineTo(w * 0.55f, shaftY - h * 0.28f)
                    lineTo(w * 0.55f, shaftY + h * 0.28f)
                    close()
                }
                drawPath(head, tint)
            }
            EnterKeyAction.SEND -> {
                // Simple paper-plane / send silhouette pointing up-right.
                val plane = Path().apply {
                    moveTo(w * 0.12f, h * 0.55f)
                    lineTo(w * 0.88f, h * 0.18f)
                    lineTo(w * 0.48f, h * 0.52f)
                    lineTo(w * 0.58f, h * 0.82f)
                    close()
                }
                drawPath(plane, tint)
                drawLine(
                    tint,
                    Offset(w * 0.48f, h * 0.52f),
                    Offset(w * 0.12f, h * 0.55f),
                    stroke.width * 0.7f,
                    StrokeCap.Round
                )
            }
            EnterKeyAction.DONE -> {
                // Checkmark.
                val path = Path().apply {
                    moveTo(w * 0.16f, h * 0.52f)
                    lineTo(w * 0.40f, h * 0.76f)
                    lineTo(w * 0.84f, h * 0.24f)
                }
                drawPath(path, tint, style = stroke)
            }
            EnterKeyAction.NEXT -> {
                // Down chevron.
                val path = Path().apply {
                    moveTo(w * 0.22f, h * 0.34f)
                    lineTo(w * 0.50f, h * 0.66f)
                    lineTo(w * 0.78f, h * 0.34f)
                }
                drawPath(path, tint, style = stroke)
            }
        }
    }
}

/**
 * Gboard-style shift arrow: a wide upward arrow that is outlined when off, filled when
 * shift is active, and filled with an underline bar in caps lock.
 */
@Composable
private fun ShiftArrowIcon(
    shiftState: ShiftState,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Wide arrow: tip at top-center, broad head, thick shaft.
        val arrow = Path().apply {
            moveTo(w * 0.50f, h * 0.07f)  // tip
            lineTo(w * 0.94f, h * 0.44f)  // head right
            lineTo(w * 0.70f, h * 0.44f)  // into shaft
            lineTo(w * 0.70f, h * 0.80f)  // shaft bottom right
            lineTo(w * 0.30f, h * 0.80f)  // shaft bottom left
            lineTo(w * 0.30f, h * 0.44f)  // out of shaft
            lineTo(w * 0.06f, h * 0.44f)  // head left
            close()
        }

        when (shiftState) {
            ShiftState.UNSHIFTED -> drawPath(arrow, tint, style = stroke)
            ShiftState.SHIFTED, ShiftState.CAPS_LOCK -> drawPath(arrow, tint)
        }

        if (shiftState == ShiftState.CAPS_LOCK) {
            val barY = h * 0.93f
            drawLine(
                color = tint,
                start = Offset(w * 0.18f, barY),
                end = Offset(w * 0.82f, barY),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round
            )
        }
    }
}
