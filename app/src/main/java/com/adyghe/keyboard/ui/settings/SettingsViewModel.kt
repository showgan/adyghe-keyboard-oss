package com.adyghe.keyboard.ui.settings

import androidx.lifecycle.ViewModel
import com.adyghe.keyboard.core.haptics.VibrationProvider
import com.adyghe.keyboard.core.language.LanguagePreferences
import com.adyghe.keyboard.core.language.applyLanguageEnabled
import com.adyghe.keyboard.core.prefs.KeyboardPreferences
import com.adyghe.keyboard.core.prefs.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: KeyboardPreferences,
    private val languagePreferences: LanguagePreferences,
    private val vibration: VibrationProvider
) : ViewModel() {

    val themeMode: Flow<ThemeMode> = prefs.themeMode
    val hapticEnabled: Flow<Boolean> = prefs.hapticEnabled
    val numberRowEnabled: Flow<Boolean> = prefs.numberRowEnabled

    private val _enabledLanguageIds =
        MutableStateFlow(languagePreferences.loadEnabledIds())
    val enabledLanguageIds: StateFlow<Set<String>> = _enabledLanguageIds.asStateFlow()

    fun currentThemeMode(): ThemeMode = prefs.getThemeMode()

    fun isHapticEnabled(): Boolean = prefs.isHapticEnabled()

    fun isNumberRowEnabled(): Boolean = prefs.isNumberRowEnabled()

    fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.setHapticEnabled(enabled)
        if (enabled) vibration.tick()
    }

    fun setNumberRowEnabled(enabled: Boolean) {
        prefs.setNumberRowEnabled(enabled)
    }

    fun setLanguageEnabled(languageId: String, enabled: Boolean) {
        val next = applyLanguageEnabled(_enabledLanguageIds.value, languageId, enabled) ?: return
        languagePreferences.saveEnabledIds(next)
        _enabledLanguageIds.value = languagePreferences.loadEnabledIds()
    }
}
