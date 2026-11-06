package com.adyghe.keyboard.core.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.adyghe.keyboard.core.layout.LayoutMode

/** Extra keys shown on the compact number pad for a given field. */
enum class NumberPadVariant {
    PLAIN,
    DECIMAL,
    SIGNED,
    SIGNED_DECIMAL,
    DATETIME
}

/** Extra character keys injected into the letters bottom row for email/URI fields. */
enum class LetterChrome {
    NONE,
    EMAIL,
    URI
}

/**
 * Resolved keyboard defaults for a focused editor, derived from [EditorInfo.inputType].
 */
data class ResolvedInputContext(
    val preferredMode: LayoutMode,
    val suggestionsEnabled: Boolean,
    val numberPadVariant: NumberPadVariant = NumberPadVariant.PLAIN,
    val letterChrome: LetterChrome = LetterChrome.NONE
)

/**
 * Maps [EditorInfo.inputType] to the most appropriate default keyboard panel and chrome.
 */
object InputTypeResolver {

    fun resolve(info: EditorInfo?): ResolvedInputContext {
        if (info == null) {
            return ResolvedInputContext(
                preferredMode = LayoutMode.LETTERS,
                suggestionsEnabled = true
            )
        }

        val inputType = info.inputType
        val cls = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val flags = inputType and InputType.TYPE_MASK_FLAGS

        return when (cls) {
            InputType.TYPE_CLASS_PHONE -> ResolvedInputContext(
                preferredMode = LayoutMode.PHONE,
                suggestionsEnabled = false
            )

            InputType.TYPE_CLASS_NUMBER -> {
                val signed = flags and InputType.TYPE_NUMBER_FLAG_SIGNED != 0
                val decimal = flags and InputType.TYPE_NUMBER_FLAG_DECIMAL != 0
                val variant = when {
                    signed && decimal -> NumberPadVariant.SIGNED_DECIMAL
                    signed -> NumberPadVariant.SIGNED
                    decimal -> NumberPadVariant.DECIMAL
                    else -> NumberPadVariant.PLAIN
                }
                ResolvedInputContext(
                    preferredMode = LayoutMode.NUMBER_PAD,
                    suggestionsEnabled = false,
                    numberPadVariant = variant
                )
            }

            InputType.TYPE_CLASS_DATETIME -> ResolvedInputContext(
                preferredMode = LayoutMode.NUMBER_PAD,
                suggestionsEnabled = false,
                numberPadVariant = NumberPadVariant.DATETIME
            )

            InputType.TYPE_CLASS_TEXT -> {
                val isPassword = EditorComposingPolicy.isPasswordInputType(inputType)
                val chrome = when (variation) {
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> LetterChrome.EMAIL
                    InputType.TYPE_TEXT_VARIATION_URI -> LetterChrome.URI
                    else -> LetterChrome.NONE
                }
                // Dictionary suggestions hurt email addresses and URIs (AOSP SoftKeyboard).
                val suggestions = !isPassword && chrome == LetterChrome.NONE
                ResolvedInputContext(
                    preferredMode = LayoutMode.LETTERS,
                    suggestionsEnabled = suggestions,
                    letterChrome = chrome
                )
            }

            else -> ResolvedInputContext(
                preferredMode = LayoutMode.LETTERS,
                suggestionsEnabled = true
            )
        }
    }
}
