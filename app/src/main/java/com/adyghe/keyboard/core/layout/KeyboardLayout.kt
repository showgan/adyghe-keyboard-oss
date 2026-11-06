package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.language.LanguageConfig

/**
 * A complete keyboard definition for one language, split by mode.
 */
data class KeyboardLayout(
    val language: LanguageConfig,
    val lettersRows: List<KeyboardRow>,
    val symbolsRows: List<KeyboardRow>,
    val numbersRows: List<KeyboardRow>
)
