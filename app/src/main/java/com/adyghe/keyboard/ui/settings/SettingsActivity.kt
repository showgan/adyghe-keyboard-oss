package com.adyghe.keyboard.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyghe.keyboard.R
import com.adyghe.keyboard.core.language.Languages
import com.adyghe.keyboard.core.language.canChangeLanguageEnabled
import com.adyghe.keyboard.core.prefs.ThemeMode
import com.adyghe.keyboard.ui.configureEdgeToEdge
import com.adyghe.keyboard.ui.theme.AdygheKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        configureEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(
                initialValue = viewModel.currentThemeMode()
            )
            AdygheKeyboardTheme(themeMode = themeMode) {
                ApplySystemBarContrast(themeMode)
                SettingsScreen(viewModel)
            }
        }
    }

    companion object {
        fun launch(context: Context) {
            val appContext = context.applicationContext
            appContext.startActivity(
                Intent(appContext, SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }
}

@Composable
private fun ApplySystemBarContrast(themeMode: ThemeMode) {
    val view = LocalView.current
    val dark = themeMode.isDark(isSystemInDarkTheme())
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(viewModel: SettingsViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(
        initialValue = viewModel.currentThemeMode()
    )
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle(
        initialValue = viewModel.isHapticEnabled()
    )
    val numberRowEnabled by viewModel.numberRowEnabled.collectAsStateWithLifecycle(
        initialValue = viewModel.isNumberRowEnabled()
    )
    val enabledLanguageIds by viewModel.enabledLanguageIds.collectAsStateWithLifecycle()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { innerPadding ->
        SettingsContent(
            themeMode = themeMode,
            hapticEnabled = hapticEnabled,
            numberRowEnabled = numberRowEnabled,
            enabledLanguageIds = enabledLanguageIds,
            onThemeModeChange = viewModel::setThemeMode,
            onHapticEnabledChange = viewModel::setHapticEnabled,
            onNumberRowEnabledChange = viewModel::setNumberRowEnabled,
            onLanguageEnabledChange = viewModel::setLanguageEnabled,
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    hapticEnabled: Boolean,
    numberRowEnabled: Boolean,
    enabledLanguageIds: Set<String>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onNumberRowEnabledChange: (Boolean) -> Unit,
    onLanguageEnabledChange: (String, Boolean) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Button(
                onClick = { openInputMethodSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.settings_enable_ime))
            }
        }
        item {
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }
        item {
            val modes = ThemeMode.entries
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = modes.size
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(mode.labelRes))
                    }
                }
            }
        }
        item {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.settings_number_row))
                },
                supportingContent = {
                    Text(stringResource(R.string.settings_number_row_desc))
                },
                trailingContent = {
                    Switch(
                        checked = numberRowEnabled,
                        onCheckedChange = null,
                        modifier = Modifier.clearAndSetSemantics { }
                    )
                },
                modifier = Modifier.toggleable(
                    value = numberRowEnabled,
                    onValueChange = onNumberRowEnabledChange,
                    role = Role.Switch
                )
            )
        }
        item {
            Text(
                text = stringResource(R.string.settings_feedback),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp)
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.settings_haptic))
                },
                supportingContent = {
                    Text(stringResource(R.string.settings_haptic_desc))
                },
                trailingContent = {
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = null,
                        modifier = Modifier.clearAndSetSemantics { }
                    )
                },
                modifier = Modifier.toggleable(
                    value = hapticEnabled,
                    onValueChange = onHapticEnabledChange,
                    role = Role.Switch
                )
            )
        }
        item {
            Text(
                text = stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp)
            )
        }
        item {
            Text(
                text = stringResource(R.string.settings_language_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
            )
        }
        items(Languages.ALL) { lang ->
            val isEnabled = lang.id in enabledLanguageIds
            val canToggle = canChangeLanguageEnabled(enabledLanguageIds, lang.id)
            ListItem(
                headlineContent = {
                    Text(stringResource(lang.labelRes))
                },
                supportingContent = {
                    Text("${lang.languageTag} \u2022 ${if (lang.isRtl) "RTL" else "LTR"}")
                },
                trailingContent = {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = null,
                        enabled = canToggle,
                        modifier = Modifier.clearAndSetSemantics { }
                    )
                },
                modifier = if (canToggle) {
                    Modifier.toggleable(
                        value = isEnabled,
                        onValueChange = { onLanguageEnabledChange(lang.id, it) },
                        role = Role.Checkbox
                    )
                } else {
                    Modifier
                }
            )
        }
    }
}

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

private fun openInputMethodSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
