package com.adyghe.keyboard.core.layout

import java.util.Locale

/**
 * A single keyboard key.
 *
 * @param label displayed text (null for keys like shift that use an icon).
 * @param popupChars characters shown in the long-press popup (first element is the primary
 *   char committed on a plain tap).
 * @param action the resulting action.
 * @param char the character to commit for [KeyAction.CHAR].
 * @param weight relative width within its row (default 1.0).
 * @param isModifier marks shift/backspace/space etc. that should use tinted styling.
 * @param casingLocale locale used for shift casing. [Locale.ROOT] so English `i`→`I`
 *   even on a Turkish device; Turkish keys pass `tr` so `i`→`İ` and `ı`→`I`.
 */
data class KeyboardKey(
    val label: String? = null,
    val popupChars: List<String> = emptyList(),
    val action: KeyAction = KeyAction.CHAR,
    val char: String? = null,
    val weight: Float = 1.0f,
    val isModifier: Boolean = false,
    val casingLocale: Locale = Locale.ROOT
) {
    /** The character actually typed when the key is tapped, honoring shift state. */
    fun displayChar(upper: Boolean): String {
        if (action != KeyAction.CHAR) return char ?: ""
        val c = char ?: label ?: return ""
        if (c.length == 1 && c[0].isLetter()) {
            return if (upper) c.uppercase(casingLocale) else c.lowercase(casingLocale)
        }
        return c
    }

    /**
     * Characters shown in the long-press popup after dropping the key's own character
     * (and its shifted form). Letter extras honor [upper] with [casingLocale] so a
     * shifted long-press commits `É` rather than `é`. Digits, punctuation, and URL
     * tokens are left unchanged. Exact match only — ignoreCase equates i/ı/İ and would
     * hide intentional Turkish/Adyghe extras.
     */
    fun popupExtras(upper: Boolean): List<String> {
        val primary = char.orEmpty()
        val base = displayChar(upper)
        return popupChars
            .map { casePopupToken(it, upper) }
            .filter { it.isNotEmpty() && it != primary && it != base }
            .distinct()
    }

    /**
     * Locale-aware casing for a popup extra. Combining marks and format characters
     * (dagesh, ZWJ) ride along so Hebrew/Arabic sequences stay intact; mixed tokens
     * such as `.com` are not cased.
     */
    internal fun casePopupToken(token: String, upper: Boolean): String {
        if (!upper || token.isEmpty() || token.any { !it.isPopupCasingChar() }) return token
        return token.uppercase(casingLocale)
    }

    /**
     * Corner hint for long-press extras. Shows both extras when there are exactly two,
     * or the first two when the second is a single digit (so top-row letter+digit keys
     * still preview the number). Otherwise the first extra only.
     * Null when the key has no long-press alternatives.
     */
    fun popupHint(upper: Boolean): String? {
        val extras = popupExtras(upper)
        if (extras.isEmpty()) return null
        val first = extras[0]
        val second = extras.getOrNull(1) ?: return first
        val joinTwo = extras.size == 2 || isSingleDigit(second)
        return if (joinTwo) first + second else first
    }
}

private fun isSingleDigit(value: String): Boolean =
    value.length == 1 && value[0].isDigit()

private fun Char.isPopupCasingChar(): Boolean {
    if (isLetter()) return true
    return when (Character.getType(this)) {
        Character.NON_SPACING_MARK.toInt(),
        Character.COMBINING_SPACING_MARK.toInt(),
        Character.ENCLOSING_MARK.toInt(),
        Character.FORMAT.toInt() -> true
        else -> false
    }
}
