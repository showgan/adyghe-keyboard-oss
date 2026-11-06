package com.adyghe.keyboard.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardSwipeTest {

    private val threshold = KEYBOARD_SWIPE_THRESHOLD_DP

    @Test
    fun underThreshold_isNone() {
        assertNull(classifyKeyboardSwipe(0f, 0f, threshold))
        assertNull(classifyKeyboardSwipe(threshold - 1f, 0f, threshold))
        assertNull(classifyKeyboardSwipe(0f, -(threshold - 1f), threshold))
        assertNull(classifyKeyboardSwipe(30f, 30f, threshold))
    }

    @Test
    fun right_isLanguageNext() {
        assertEquals(KeyboardSwipe.LanguageNext, classifyKeyboardSwipe(threshold, 0f, threshold))
        assertEquals(KeyboardSwipe.LanguageNext, classifyKeyboardSwipe(80f, 10f, threshold))
    }

    @Test
    fun left_isLanguagePrev() {
        assertEquals(KeyboardSwipe.LanguagePrev, classifyKeyboardSwipe(-threshold, 0f, threshold))
        assertEquals(KeyboardSwipe.LanguagePrev, classifyKeyboardSwipe(-80f, 10f, threshold))
    }

    @Test
    fun up_isShiftOn() {
        assertEquals(KeyboardSwipe.ShiftOn, classifyKeyboardSwipe(0f, -threshold, threshold))
        assertEquals(KeyboardSwipe.ShiftOn, classifyKeyboardSwipe(10f, -80f, threshold))
    }

    @Test
    fun down_isShiftOff() {
        assertEquals(KeyboardSwipe.ShiftOff, classifyKeyboardSwipe(0f, threshold, threshold))
        assertEquals(KeyboardSwipe.ShiftOff, classifyKeyboardSwipe(10f, 80f, threshold))
    }

    @Test
    fun fortyFiveDegreeTie_isHorizontalLanguage() {
        assertEquals(
            KeyboardSwipe.LanguageNext,
            classifyKeyboardSwipe(threshold, threshold, threshold)
        )
        assertEquals(
            KeyboardSwipe.LanguagePrev,
            classifyKeyboardSwipe(-threshold, threshold, threshold)
        )
    }

    @Test
    fun dominantVerticalWinsOverHorizontalDrift() {
        assertEquals(
            KeyboardSwipe.ShiftOn,
            classifyKeyboardSwipe(threshold, -(threshold + 1f), threshold)
        )
    }

    @Test
    fun classifyOnAxis_followsReleaseSign() {
        assertEquals(
            KeyboardSwipe.LanguageNext,
            classifyOnAxis(12f, -90f, KeyboardSwipeAxis.Horizontal)
        )
        assertEquals(
            KeyboardSwipe.LanguagePrev,
            classifyOnAxis(-1f, 0f, KeyboardSwipeAxis.Horizontal)
        )
        assertEquals(
            KeyboardSwipe.ShiftOn,
            classifyOnAxis(90f, -1f, KeyboardSwipeAxis.Vertical)
        )
        assertEquals(
            KeyboardSwipe.ShiftOff,
            classifyOnAxis(0f, 0f, KeyboardSwipeAxis.Vertical)
        )
    }

    @Test
    fun swipeAxis_matchesKind() {
        assertEquals(KeyboardSwipeAxis.Horizontal, KeyboardSwipe.LanguageNext.axis)
        assertEquals(KeyboardSwipeAxis.Horizontal, KeyboardSwipe.LanguagePrev.axis)
        assertEquals(KeyboardSwipeAxis.Vertical, KeyboardSwipe.ShiftOn.axis)
        assertEquals(KeyboardSwipeAxis.Vertical, KeyboardSwipe.ShiftOff.axis)
    }
}
