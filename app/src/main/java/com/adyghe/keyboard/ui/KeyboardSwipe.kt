package com.adyghe.keyboard.ui

import kotlin.math.abs

/** Travel on the key grid that commits a swipe (screen-absolute). */
const val KEYBOARD_SWIPE_THRESHOLD_DP = 48f

/** Four-direction keyboard swipe after travel reaches [KEYBOARD_SWIPE_THRESHOLD_DP]. */
enum class KeyboardSwipe {
    LanguageNext,
    LanguagePrev,
    ShiftOn,
    ShiftOff
}

enum class KeyboardSwipeAxis {
    Horizontal,
    Vertical
}

val KeyboardSwipe.axis: KeyboardSwipeAxis
    get() = when (this) {
        KeyboardSwipe.LanguageNext, KeyboardSwipe.LanguagePrev -> KeyboardSwipeAxis.Horizontal
        KeyboardSwipe.ShiftOn, KeyboardSwipe.ShiftOff -> KeyboardSwipeAxis.Vertical
    }

/**
 * Classify a drag from finger-down. Returns null until [thresholdPx] is reached on the
 * dominant axis. A 45° tie is treated as horizontal.
 */
fun classifyKeyboardSwipe(dx: Float, dy: Float, thresholdPx: Float): KeyboardSwipe? {
    val absDx = abs(dx)
    val absDy = abs(dy)
    if (maxOf(absDx, absDy) < thresholdPx) return null
    val axis = if (absDx >= absDy) KeyboardSwipeAxis.Horizontal else KeyboardSwipeAxis.Vertical
    return classifyOnAxis(dx, dy, axis)
}

/** Sign of a swipe after the axis has already been locked. */
fun classifyOnAxis(dx: Float, dy: Float, axis: KeyboardSwipeAxis): KeyboardSwipe = when (axis) {
    KeyboardSwipeAxis.Horizontal ->
        if (dx > 0f) KeyboardSwipe.LanguageNext else KeyboardSwipe.LanguagePrev
    KeyboardSwipeAxis.Vertical ->
        if (dy < 0f) KeyboardSwipe.ShiftOn else KeyboardSwipe.ShiftOff
}
