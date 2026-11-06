package com.adyghe.keyboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adyghe.keyboard.R

/** Fixed chrome height above the key rows. Idle, clipboard, and suggestions share this slot. */
val KeyboardToolbarHeight = 48.dp

// Suggestion text uses the theme's primary color for proper dark/light contrast.

/**
 * Always-visible Gboard-style toolbar. Height never changes; content swaps between idle
 * (Paste + settings), expanded clipboard history, and word suggestions.
 *
 * Chrome is pinned LTR so the settings icon stays on the right when the system locale
 * is RTL. [KeyboardScreen] also pins the key grid; this provider keeps the toolbar
 * correct if it is composed on its own.
 */
@Composable
fun KeyboardToolbar(
    clipboardHistory: List<String>,
    clipboardExpanded: Boolean,
    suggestions: List<String>,
    onExpandClipboard: () -> Unit,
    onPasteItem: (String) -> Unit,
    onDismissClipboard: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(KeyboardToolbarHeight)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when {
                clipboardExpanded && clipboardHistory.isNotEmpty() -> {
                    ClipboardHistoryRow(
                        history = clipboardHistory,
                        onPasteItem = onPasteItem,
                        onDismiss = onDismissClipboard
                    )
                }
                suggestions.isNotEmpty() -> {
                    SuggestionCandidates(
                        suggestions = suggestions,
                        onSuggestionClick = onSuggestionClick
                    )
                }
                else -> {
                    IdleToolbar(
                        clipboardPreview = clipboardHistory.firstOrNull(),
                        onExpandClipboard = onExpandClipboard,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleToolbar(
    clipboardPreview: String?,
    onExpandClipboard: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (clipboardPreview != null) {
            PasteChip(
                preview = clipboardPreview,
                onClick = onExpandClipboard
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(KeyboardToolbarHeight)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.toolbar_settings),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PasteChip(
    preview: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Paste",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = preview.take(24),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.widthIn(max = 140.dp)
            )
        }
    }
}

@Composable
private fun ClipboardHistoryRow(
    history: List<String>,
    onPasteItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        history.forEach { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .widthIn(max = 160.dp)
                    .clickable { onPasteItem(item) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = item,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                }
            }
        }
        Text(
            text = "✕",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onDismiss)
                .padding(8.dp)
        )
    }
}

@Composable
private fun SuggestionCandidates(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        suggestions.take(3).forEach { s ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSuggestionClick(s) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = s,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
