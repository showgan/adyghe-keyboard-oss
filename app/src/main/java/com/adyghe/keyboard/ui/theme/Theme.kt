package com.adyghe.keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.adyghe.keyboard.core.prefs.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF000000),
    background = Color(0xFF202124),
    surface = Color(0xFF202124),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF303134),
    onSurfaceVariant = Color(0xFF9AA0A6),
    secondaryContainer = Color(0xFF3C4043),
    onSecondaryContainer = Color(0xFFE8EAED),
    surfaceContainerHigh = Color(0xFF3C4043),
    surfaceContainer = Color(0xFF303134),
    outline = Color(0xFF5F6368),
    outlineVariant = Color(0xFF444746)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF1F3F4),
    surface = Color(0xFFF1F3F4),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF444746),
    secondaryContainer = Color(0xFFD3E3FD),
    onSecondaryContainer = Color(0xFF1F1F1F),
    surfaceContainerHigh = Color(0xFFEEF1F5),
    surfaceContainer = Color(0xFFFFFFFF),
    outline = Color(0xFFBDC1C6),
    outlineVariant = Color(0xFFDADCE0)
)

@Composable
fun AdygheKeyboardTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
