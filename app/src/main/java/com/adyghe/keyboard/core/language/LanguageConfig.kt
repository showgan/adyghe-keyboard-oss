package com.adyghe.keyboard.core.language

import androidx.annotation.StringRes
import com.adyghe.keyboard.R

/**
 * A single selectable keyboard language/subtype. Each maps to a registered input-method
 * subtype in res/xml/method.xml.
 */
data class LanguageConfig(
    val id: String,
    @param:StringRes val labelRes: Int,
    /** Pure native language name shown on the space bar and in the language picker. */
    @param:StringRes val nativeNameRes: Int,
    val languageTag: String,
    /** Script direction for labels/settings. Does not mirror key positions. */
    val isRtl: Boolean = false,
    val isAsciiCapable: Boolean = false
) {
    val subtypeKey: String get() = id
}
