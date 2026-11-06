package com.adyghe.keyboard.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.language.canChangeLanguageEnabled
import com.adyghe.keyboard.core.layout.KeyAction
import com.adyghe.keyboard.core.layout.KeyboardRow
import com.adyghe.keyboard.core.state.EnterKeyAction
import com.adyghe.keyboard.core.state.ImeState
import com.adyghe.keyboard.core.state.ImeStore
import com.adyghe.keyboard.core.state.OverlayType
import com.adyghe.keyboard.core.state.PopupSelection
import com.adyghe.keyboard.core.state.ShiftState
import com.adyghe.keyboard.ui.settings.SettingsActivity
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The full keyboard UI: fixed toolbar + key rows + long-press popup overlay.
 *
 * A single pointer handler resolves taps, long-presses, four-direction swipes, and popup
 * drag-to-select. Key bounds
 * are recorded in root coordinates for exact popup anchoring and hit-testing.
 *
 * Geometry is always visual left-to-right: layouts store Enter on the right and letters in
 * on-screen order. An RTL system locale would otherwise reverse [Row] children and
 * [Alignment.End], flipping the whole grid.
 */
@Composable
fun KeyboardScreen(
    store: ImeStore,
    modifier: Modifier = Modifier
) {
    val state by store.state.collectAsState()
    val rows = store.currentRows()
    val spaceLabel = stringResource(Languages.fromId(state.languageId).nativeNameRes)
    val density = LocalDensity.current
    val context = LocalContext.current
    val swipeThresholdPx = with(density) { KEYBOARD_SWIPE_THRESHOLD_DP.dp.toPx() }

    val keyBounds = remember { mutableStateMapOf<Pair<Int, Int>, Rect>() }
    var rowsOrigin by remember { mutableStateOf(Offset.Zero) }
    var rowsWidth by remember { mutableStateOf(0) }
    var chromeOrigin by remember { mutableStateOf(Offset.Zero) }
    var chromeWidth by remember { mutableStateOf(0) }

    // Drop bounds for keys that no longer exist after a language/mode switch. Wider
    // layouts (e.g. Cyrillic 11-key rows) leave stale rects that otherwise steal hits
    // on the rightmost keys of shorter rows (Latin "l").
    val validKeyIndices = remember(rows) {
        buildSet {
            rows.forEachIndexed { r, row ->
                row.keys.indices.forEach { c -> add(r to c) }
            }
        }
    }
    SideEffect {
        keyBounds.keys.filter { it !in validKeyIndices }.forEach { keyBounds.remove(it) }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
        // Chrome Box hosts the toolbar + keys so the long-press popup can be positioned
        // above the top letter row into the toolbar (Gboard-style).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                )
                .onGloballyPositioned { coords ->
                    chromeOrigin = coords.boundsInRoot().topLeft
                    chromeWidth = coords.size.width
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                KeyboardToolbar(
                    clipboardHistory = state.clipboardHistory,
                    clipboardExpanded = state.overlayType == OverlayType.CLIPBOARD,
                    suggestions = state.suggestions,
                    onExpandClipboard = { store.openClipboardHistory() },
                    onPasteItem = { store.pasteClipboardItem(it) },
                    onDismissClipboard = { store.dismissOverlay() },
                    onSuggestionClick = { store.onSuggestionCommit(it) },
                    onSettingsClick = { SettingsActivity.launch(context) }
                )

                if (state.overlayType == OverlayType.EMOJI) {
                    EmojiPanel(
                        onEmoji = { store.commitText(it) },
                        onClose = { store.closePanelOverlay() },
                        onBackspace = { store.onBackspace() },
                        onBackspaceRepeatStart = { store.startBackspaceRepeat() },
                        onBackspaceRepeatStop = { store.stopBackspaceRepeat() }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                rowsOrigin = coords.boundsInRoot().topLeft
                                rowsWidth = coords.size.width
                            }
                            .pointerInput(rows, swipeThresholdPx) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val rootPos = down.position + rowsOrigin
                                    val hit = hitTestKey(rootPos, keyBounds)
                                    if (hit == null) {
                                        down.consume()
                                        return@awaitEachGesture
                                    }
                                    val (row, col) = hit
                                    store.onKeyDown(row, col)

                                    val hitKey = rows.getOrNull(row)?.keys?.getOrNull(col)
                                    var pressPosition = down.position
                                    when (
                                        val gesture = awaitKeyGesture(
                                            down = down,
                                            swipeThresholdPx = swipeThresholdPx,
                                            longPressTimeoutMs = viewConfiguration.longPressTimeoutMillis
                                        )
                                    ) {
                                        KeyGestureResult.Tap -> {
                                            val previousLang = store.state.value.languageId
                                            store.onKeyRelease(row, col)
                                            if (hitKey?.action == KeyAction.LANG_SWITCH &&
                                                store.state.value.languageId != previousLang
                                            ) {
                                                toastLanguageName(context, store.state.value.languageId)
                                            }
                                            return@awaitEachGesture
                                        }
                                        is KeyGestureResult.Swipe -> {
                                            when (gesture.swipe) {
                                                KeyboardSwipe.LanguageNext -> {
                                                    store.switchLang(+1)
                                                    toastLanguageName(
                                                        context,
                                                        store.state.value.languageId
                                                    )
                                                }
                                                KeyboardSwipe.LanguagePrev -> {
                                                    store.switchLang(-1)
                                                    toastLanguageName(
                                                        context,
                                                        store.state.value.languageId
                                                    )
                                                }
                                                KeyboardSwipe.ShiftOn -> store.setShiftEnabled(true)
                                                KeyboardSwipe.ShiftOff -> store.setShiftEnabled(false)
                                            }
                                            return@awaitEachGesture
                                        }
                                        is KeyGestureResult.LongPress -> {
                                            pressPosition = gesture.position
                                            store.onLongPress(row, col)
                                            // Fall through to the post-long-press hold loop below.
                                        }
                                    }

                                    applyAnchoredPopupVisual(
                                        store = store,
                                        keyBounds = keyBounds,
                                        chromeOrigin = chromeOrigin,
                                        chromeWidth = chromeWidth,
                                        density = density
                                    )

                                    val overlay = store.state.value.overlayType
                                    var isPopup = overlay == OverlayType.POPUP
                                    if (isPopup) {
                                        val idx = hitTestPopupChar(
                                            position = pressPosition + rowsOrigin,
                                            popupChars = store.state.value.activePopup,
                                            popupAnchorIndex = store.state.value.popupAnchorIndex,
                                            keyBounds = keyBounds,
                                            rowsOrigin = rowsOrigin,
                                            rowsWidth = rowsWidth,
                                            activeRow = row,
                                            activeCol = col,
                                            density = density
                                        )
                                        store.onPopupSelect(PopupSelection(idx, ""))
                                    }

                                    try {
                                        // Drag across the popup (or just release) to select.
                                        // Backspace repeat also runs until this loop ends on finger-up.
                                        var event = awaitPointerEvent()
                                        while (event.changes.any { it.pressed }) {
                                            if (isPopup) {
                                                val pos = event.changes.first().position + rowsOrigin
                                                val idx = hitTestPopupChar(
                                                    position = pos,
                                                    popupChars = store.state.value.activePopup,
                                                    popupAnchorIndex = store.state.value.popupAnchorIndex,
                                                    keyBounds = keyBounds,
                                                    rowsOrigin = rowsOrigin,
                                                    rowsWidth = rowsWidth,
                                                    activeRow = row,
                                                    activeCol = col,
                                                    density = density
                                                )
                                                store.onPopupSelect(PopupSelection(idx, ""))
                                            }
                                            event = awaitPointerEvent()
                                        }

                                        if (isPopup) {
                                            store.onPopupRelease()
                                        } else if (
                                            overlay != OverlayType.LANGUAGE_PICKER &&
                                            overlay != OverlayType.EMOJI &&
                                            overlay != OverlayType.CLIPBOARD
                                        ) {
                                            // Language / emoji / clipboard panels stay open after release.
                                            store.dismissOverlay()
                                        }
                                    } finally {
                                        store.stopBackspaceRepeat()
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            rows.forEachIndexed { rowIndex, row ->
                                KeyboardRowView(
                                    row = row,
                                    rowIndex = rowIndex,
                                    isUpper = isUpper(state),
                                    shiftState = state.shift,
                                    records = keyBounds,
                                    spaceLabel = spaceLabel,
                                    enterAction = state.enterAction,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (rowIndex == rows.lastIndex) 58.dp else 52.dp)
                                )
                            }
                        }

                        if (state.overlayType == OverlayType.LANGUAGE_PICKER) {
                            // Backdrop: tapping anywhere outside the picker dismisses it.
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { store.dismissOverlay() }
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 64.dp)
                            ) {
                                LanguagePicker(
                                    currentLanguageId = state.languageId,
                                    enabledLanguageIds = state.enabledLanguageIds,
                                    onSelect = { store.switchTo(it) },
                                    onEnabledChange = { id, enabled ->
                                        store.setLanguageEnabled(id, enabled)
                                    }
                                )
                            }
                        }
                    }
                } // end else (not emoji)
            }

            if (state.overlayType == OverlayType.POPUP) {
                val activeRect = keyBounds[state.overlayRow to state.overlayCol]
                if (activeRect != null) {
                    val localY = activeRect.top - chromeOrigin.y
                    val popupH = with(density) { KeyPopupMetrics.totalHeightDp().dp.toPx() }
                    val left = popupPanelLeftPx(
                        popupChars = state.activePopup,
                        popupAnchorIndex = state.popupAnchorIndex,
                        keyRect = activeRect,
                        originX = chromeOrigin.x,
                        parentWidth = chromeWidth.toFloat(),
                        density = density
                    )
                    val (top, anchorAbove) = KeyPopupMetrics.verticalPlacement(
                        localKeyTop = localY,
                        keyHeight = activeRect.height,
                        popupHeight = popupH,
                        minTop = 0f
                    )
                    Box(
                        modifier = Modifier.offset { IntOffset(left.toInt(), top.toInt()) }
                    ) {
                        KeyPopup(
                            popupChars = state.activePopup,
                            selection = state.popupSelection,
                            anchorAbove = anchorAbove,
                            stemIndex = state.popupAnchorIndex
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
        )
        }
    }
}

private fun isUpper(state: ImeState): Boolean = when (state.shift) {
    ShiftState.SHIFTED, ShiftState.CAPS_LOCK -> true
    ShiftState.UNSHIFTED -> false
}

/** Toast the native name of [languageId] after a language swipe or globe tap. */
private fun toastLanguageName(context: Context, languageId: String) {
    val name = context.getString(Languages.fromId(languageId).nativeNameRes)
    Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
}

private sealed class KeyGestureResult {
    data object Tap : KeyGestureResult()
    data class LongPress(val position: Offset) : KeyGestureResult()
    data class Swipe(val swipe: KeyboardSwipe) : KeyGestureResult()
}

/**
 * Resolve tap / long-press / four-direction swipe on any key.
 * Right → next language; left → previous; up → shift on; down → shift off.
 */
private suspend fun AwaitPointerEventScope.awaitKeyGesture(
    down: PointerInputChange,
    swipeThresholdPx: Float,
    longPressTimeoutMs: Long
): KeyGestureResult {
    val pointerId = down.id
    val start = down.position
    var lastPosition = start

    val phase: KeyPhase? = withTimeoutOrNull(longPressTimeoutMs) {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.id == pointerId }
                ?: return@withTimeoutOrNull KeyPhase.Tap
            lastPosition = change.position
            val dx = change.position.x - start.x
            val dy = change.position.y - start.y
            val swipe = classifyKeyboardSwipe(dx, dy, swipeThresholdPx)
            if (swipe != null) {
                if (change.changedToUpIgnoreConsumed()) {
                    return@withTimeoutOrNull KeyPhase.SwipeDone(swipe)
                }
                // Finish outside the timeout so a slow finger-up cannot become long-press.
                return@withTimeoutOrNull KeyPhase.SwipePending(swipe.axis)
            }
            if (change.changedToUpIgnoreConsumed()) {
                return@withTimeoutOrNull KeyPhase.Tap
            }
        }
        error("unreachable")
    }

    return when (val p = phase) {
        null -> KeyGestureResult.LongPress(lastPosition)
        KeyPhase.Tap -> KeyGestureResult.Tap
        is KeyPhase.SwipeDone -> KeyGestureResult.Swipe(p.swipe)
        is KeyPhase.SwipePending -> {
            var lastDx = lastPosition.x - start.x
            var lastDy = lastPosition.y - start.y
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                lastDx = change.position.x - start.x
                lastDy = change.position.y - start.y
                if (change.changedToUpIgnoreConsumed()) break
            }
            KeyGestureResult.Swipe(classifyOnAxis(lastDx, lastDy, p.axis))
        }
    }
}

private sealed class KeyPhase {
    data object Tap : KeyPhase()
    data class SwipeDone(val swipe: KeyboardSwipe) : KeyPhase()
    data class SwipePending(val axis: KeyboardSwipeAxis) : KeyPhase()
}

@Composable
private fun KeyboardRowView(
    row: KeyboardRow,
    rowIndex: Int,
    isUpper: Boolean,
    shiftState: ShiftState,
    records: MutableMap<Pair<Int, Int>, Rect>,
    modifier: Modifier = Modifier,
    spaceLabel: String? = null,
    enterAction: EnterKeyAction = EnterKeyAction.RETURN
) {
    val totalWeight = row.keys.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f)
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 3.dp, vertical = 3.dp)
    ) {
        row.keys.forEachIndexed { colIndex, key ->
            Box(
                modifier = Modifier
                    .weight(key.weight / totalWeight)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .onGloballyPositioned { coords ->
                        records[rowIndex to colIndex] = coords.boundsInRoot()
                    }
            ) {
                KeyView(
                    key = key,
                    upper = isUpper,
                    pressed = false,
                    highlight = false,
                    shiftState = shiftState,
                    spaceLabel = spaceLabel,
                    enterAction = enterAction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun hitTestKey(position: Offset, bounds: Map<Pair<Int, Int>, Rect>): Pair<Int, Int>? {
    for ((key, rect) in bounds) {
        if (position in rect) return key
    }
    return null
}

/** Keep extras[0] over the key; write visual order + selection into the store. */
private fun applyAnchoredPopupVisual(
    store: ImeStore,
    keyBounds: Map<Pair<Int, Int>, Rect>,
    chromeOrigin: Offset,
    chromeWidth: Int,
    density: androidx.compose.ui.unit.Density
) {
    val s = store.state.value
    if (s.overlayType != OverlayType.POPUP || s.activePopup.isEmpty() || chromeWidth <= 0) return
    val rect = keyBounds[s.overlayRow to s.overlayCol] ?: return
    val keyCenterPx = rect.left + rect.width / 2f - chromeOrigin.x
    val arranged = KeyPopupMetrics.arrangeAroundAnchor(
        extras = s.activePopup,
        keyCenterDp = with(density) { keyCenterPx.toDp().value },
        parentWidthDp = with(density) { chromeWidth.toDp().value }
    )
    store.setPopupVisual(arranged.chars, arranged.anchorIndex)
}

/** Popup panel left in the coordinate space of [originX] (chrome-local or rows-local). */
private fun popupPanelLeftPx(
    popupChars: List<String>,
    popupAnchorIndex: Int,
    keyRect: Rect,
    originX: Float,
    parentWidth: Float,
    density: androidx.compose.ui.unit.Density
): Float {
    val localX = keyRect.left - originX
    val popupW = with(density) { KeyPopupMetrics.totalWidthDp(popupChars).dp.toPx() }
    val anchorCenter = with(density) {
        KeyPopupMetrics.cellCenterFromLeftDp(popupChars, popupAnchorIndex).dp.toPx()
    }
    return KeyPopupMetrics.horizontalPlacement(
        keyLeft = localX,
        keyWidth = keyRect.width,
        parentWidth = parentWidth,
        popupWidth = popupW,
        anchorCenterFromLeft = anchorCenter
    )
}

/** Map an in-root position to the index of the popup char being touched. */
private fun hitTestPopupChar(
    position: Offset,
    popupChars: List<String>,
    popupAnchorIndex: Int,
    keyBounds: Map<Pair<Int, Int>, Rect>,
    rowsOrigin: Offset,
    rowsWidth: Int,
    activeRow: Int,
    activeCol: Int,
    density: androidx.compose.ui.unit.Density
): Int {
    if (popupChars.isEmpty()) return 0
    val anchor = keyBounds[activeRow to activeCol] ?: return 0
    // Mirror the positioning math used to place the popup so the drag hit-test lines up
    // with the (possibly edge-clamped) popup rather than the key center.
    val popupLeft = popupPanelLeftPx(
        popupChars = popupChars,
        popupAnchorIndex = popupAnchorIndex,
        keyRect = anchor,
        originX = rowsOrigin.x,
        parentWidth = rowsWidth.toFloat(),
        density = density
    ) + rowsOrigin.x
    val cellWidthsPx = FloatArray(popupChars.size) { i ->
        with(density) { KeyPopupMetrics.cellWidthDp(popupChars[i]).dp.toPx() }
    }
    val spacingPx = with(density) { KeyPopupMetrics.CELL_SPACING_DP.dp.toPx() }
    val padStartPx = with(density) { KeyPopupMetrics.PANEL_PADDING_DP.dp.toPx() }
    return KeyPopupMetrics.hitTestIndex(
        xFromPanelLeft = position.x - popupLeft,
        cellWidthsPx = cellWidthsPx,
        spacingPx = spacingPx,
        padStartPx = padStartPx
    )
}

/** Gboard-style language list shown above the space row on long-press of the space bar. */
@Composable
private fun LanguagePicker(
    currentLanguageId: String,
    enabledLanguageIds: Set<String>,
    onSelect: (String) -> Unit,
    onEnabledChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(min = 200.dp, max = 280.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Languages.ALL.forEach { lang ->
                val selected = lang.id == currentLanguageId
                val isEnabled = lang.id in enabledLanguageIds
                val canToggle = canChangeLanguageEnabled(enabledLanguageIds, lang.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            if (canToggle) onEnabledChange(lang.id, checked)
                        },
                        enabled = canToggle
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(lang.id) }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(lang.nativeNameRes),
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Text(
                                text = "✓",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
