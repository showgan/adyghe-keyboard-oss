package com.adyghe.keyboard.core.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.layout.LayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputTypeResolverTest {

    @Test
    fun emailAddress_enablesEmailChromeAndDisablesSuggestions() {
        val ctx = InputTypeResolver.resolve(
            editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        )
        assertEquals(LayoutMode.LETTERS, ctx.preferredMode)
        assertEquals(LetterChrome.EMAIL, ctx.letterChrome)
        assertFalse(ctx.suggestionsEnabled)
    }

    @Test
    fun webEmailAddress_enablesEmailChromeAndDisablesSuggestions() {
        val ctx = InputTypeResolver.resolve(
            editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
        )
        assertEquals(LetterChrome.EMAIL, ctx.letterChrome)
        assertFalse(ctx.suggestionsEnabled)
    }

    @Test
    fun uri_enablesUriChromeAndDisablesSuggestions() {
        val ctx = InputTypeResolver.resolve(
            editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        )
        assertEquals(LayoutMode.LETTERS, ctx.preferredMode)
        assertEquals(LetterChrome.URI, ctx.letterChrome)
        assertFalse(ctx.suggestionsEnabled)
    }

    @Test
    fun password_disablesSuggestionsWithoutChrome() {
        val ctx = InputTypeResolver.resolve(
            editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        )
        assertEquals(LetterChrome.NONE, ctx.letterChrome)
        assertFalse(ctx.suggestionsEnabled)
    }

    @Test
    fun normalText_keepsSuggestionsAndNoChrome() {
        val ctx = InputTypeResolver.resolve(
            editor(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL)
        )
        assertEquals(LetterChrome.NONE, ctx.letterChrome)
        assertTrue(ctx.suggestionsEnabled)
    }

    @Test
    fun phone_usesPhonePad() {
        val ctx = InputTypeResolver.resolve(editor(InputType.TYPE_CLASS_PHONE))
        assertEquals(LayoutMode.PHONE, ctx.preferredMode)
        assertFalse(ctx.suggestionsEnabled)
        assertEquals(LetterChrome.NONE, ctx.letterChrome)
    }

    @Test
    fun number_usesNumberPad() {
        val ctx = InputTypeResolver.resolve(editor(InputType.TYPE_CLASS_NUMBER))
        assertEquals(LayoutMode.NUMBER_PAD, ctx.preferredMode)
        assertEquals(NumberPadVariant.PLAIN, ctx.numberPadVariant)
        assertFalse(ctx.suggestionsEnabled)
    }

    @Test
    fun nullEditor_defaultsToLettersWithSuggestions() {
        val ctx = InputTypeResolver.resolve(null)
        assertEquals(LayoutMode.LETTERS, ctx.preferredMode)
        assertTrue(ctx.suggestionsEnabled)
        assertEquals(LetterChrome.NONE, ctx.letterChrome)
    }

    private fun editor(inputType: Int): EditorInfo =
        EditorInfo().apply { this.inputType = inputType }
}
