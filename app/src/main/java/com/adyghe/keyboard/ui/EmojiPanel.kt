package com.adyghe.keyboard.ui

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.adyghe.keyboard.R

/**
 * Full-panel emoji chooser. Prefers [EmojiPickerView]; falls back to a small static grid
 * if the picker fails to inflate.
 */
@Composable
fun EmojiPanel(
    onEmoji: (String) -> Unit,
    onClose: () -> Unit,
    onBackspace: () -> Unit,
    onBackspaceRepeatStart: () -> Unit,
    onBackspaceRepeatStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var useFallback by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Text("ABC")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Emoji",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            EmojiBackspaceButton(
                onBackspace = onBackspace,
                onBackspaceRepeatStart = onBackspaceRepeatStart,
                onBackspaceRepeatStop = onBackspaceRepeatStop
            )
        }

        if (useFallback) {
            FallbackEmojiGrid(
                onEmoji = onEmoji,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            AndroidView(
                factory = { context ->
                    try {
                        EmojiPickerView(context).apply {
                            setOnEmojiPickedListener { item ->
                                onEmoji(item.emoji)
                            }
                        }
                    } catch (t: Throwable) {
                        Log.w("EmojiPanel", "EmojiPickerView unavailable; using fallback", t)
                        Handler(Looper.getMainLooper()).post { useFallback = true }
                        View(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun EmojiBackspaceButton(
    onBackspace: () -> Unit,
    onBackspaceRepeatStart: () -> Unit,
    onBackspaceRepeatStop: () -> Unit
) {
    val label = stringResource(R.string.emoji_backspace)
    var ignoreNextClick by remember { mutableStateOf(false) }
    val latestRepeatStart by rememberUpdatedState(onBackspaceRepeatStart)
    val latestRepeatStop by rememberUpdatedState(onBackspaceRepeatStop)
    TextButton(
        onClick = {
            if (ignoreNextClick) {
                ignoreNextClick = false
            } else {
                onBackspace()
            }
        },
        modifier = Modifier
            .width(64.dp)
            .semantics { contentDescription = label }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val longPress = awaitLongPressOrCancellation(down.id)
                    if (longPress == null) return@awaitEachGesture
                    ignoreNextClick = true
                    latestRepeatStart()
                    try {
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.pressed }) {
                            event = awaitPointerEvent()
                        }
                    } finally {
                        latestRepeatStop()
                    }
                }
            }
    ) {
        Text("⌫")
    }
}

@Composable
private fun FallbackEmojiGrid(
    onEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(FallbackEmojis.ALL) { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { onEmoji(emoji) }
                    .padding(6.dp)
            )
        }
    }
}
