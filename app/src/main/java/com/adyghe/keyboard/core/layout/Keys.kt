package com.adyghe.keyboard.core.layout

import java.util.Locale

internal object Keys {
    fun modifier(action: KeyAction, label: String? = null, weight: Float = 1.5f) =
        KeyboardKey(label = label, action = action, weight = weight, isModifier = true)

    fun char(
        c: String,
        popup: List<String> = emptyList(),
        weight: Float = 1.0f,
        locale: Locale = Locale.ROOT
    ) = KeyboardKey(label = c, char = c, popupChars = popup, weight = weight, casingLocale = locale)

    fun char(popup: List<String>): KeyboardKey {
        val primary = popup.first()
        return KeyboardKey(label = primary, char = primary, popupChars = popup)
    }
}
