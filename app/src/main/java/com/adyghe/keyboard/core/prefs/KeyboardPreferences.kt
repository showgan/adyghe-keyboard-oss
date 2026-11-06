package com.adyghe.keyboard.core.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromPref(value: String?): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}

interface KeyboardPreferences {
    val themeMode: Flow<ThemeMode>
    val hapticEnabled: Flow<Boolean>
    val numberRowEnabled: Flow<Boolean>
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun isHapticEnabled(): Boolean
    fun setHapticEnabled(enabled: Boolean)
    fun isNumberRowEnabled(): Boolean
    fun setNumberRowEnabled(enabled: Boolean)
}

/** SharedPreferences-backed [KeyboardPreferences]. */
class SharedKeyboardPreferences(context: Context) : KeyboardPreferences {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getThemeMode(): ThemeMode =
        ThemeMode.fromPref(prefs.getString(KEY_THEME_MODE, null))

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    override fun isHapticEnabled(): Boolean =
        prefs.getBoolean(KEY_HAPTIC_ENABLED, true)

    override fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    override fun isNumberRowEnabled(): Boolean =
        prefs.getBoolean(KEY_NUMBER_ROW_ENABLED, false)

    override fun setNumberRowEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NUMBER_ROW_ENABLED, enabled).apply()
    }

    override val themeMode: Flow<ThemeMode> =
        prefs.observe(KEY_THEME_MODE) { getThemeMode() }

    override val hapticEnabled: Flow<Boolean> =
        prefs.observe(KEY_HAPTIC_ENABLED) { isHapticEnabled() }

    override val numberRowEnabled: Flow<Boolean> =
        prefs.observe(KEY_NUMBER_ROW_ENABLED) { isNumberRowEnabled() }

    companion object {
        private const val PREFS_NAME = "keyboard_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_NUMBER_ROW_ENABLED = "number_row_enabled"
    }
}

/** In-memory prefs for unit tests. */
class InMemoryKeyboardPreferences(
    initialTheme: ThemeMode = ThemeMode.SYSTEM,
    initialHaptic: Boolean = true,
    initialNumberRow: Boolean = false
) : KeyboardPreferences {
    private val themeModeState = MutableStateFlow(initialTheme)
    private val hapticEnabledState = MutableStateFlow(initialHaptic)
    private val numberRowEnabledState = MutableStateFlow(initialNumberRow)

    override val themeMode: Flow<ThemeMode> = themeModeState.asStateFlow()
    override val hapticEnabled: Flow<Boolean> = hapticEnabledState.asStateFlow()
    override val numberRowEnabled: Flow<Boolean> = numberRowEnabledState.asStateFlow()

    override fun getThemeMode(): ThemeMode = themeModeState.value

    override fun setThemeMode(mode: ThemeMode) {
        themeModeState.value = mode
    }

    override fun isHapticEnabled(): Boolean = hapticEnabledState.value

    override fun setHapticEnabled(enabled: Boolean) {
        hapticEnabledState.value = enabled
    }

    override fun isNumberRowEnabled(): Boolean = numberRowEnabledState.value

    override fun setNumberRowEnabled(enabled: Boolean) {
        numberRowEnabledState.value = enabled
    }
}

private fun <T> SharedPreferences.observe(key: String, read: () -> T): Flow<T> = callbackFlow {
    trySend(read())
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
        if (changed == key) trySend(read())
    }
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}.distinctUntilChanged()
