package com.adyghe.keyboard.core.input

import android.text.InputType
import android.view.inputmethod.EditorInfo

/**
 * Whether the focused editor can safely use composing-region APIs.
 *
 * [android.view.inputmethod.InputConnection.setComposingText] combined with
 * delete/rebuild and [android.view.inputmethod.InputConnection.setSelection] based
 * on [android.view.inputmethod.InputConnection.getTextBeforeCursor] is the normal
 * IME protocol, but some custom editors mishandle it and prepend each new letter
 * (typing appears reversed). That is a property of the **editor**, not of whether
 * the keyboard language is RTL or LTR.
 *
 * Reddit's fields are the known case: bypassing composing only for Arabic/Hebrew
 * fixed those scripts there and immediately produced the same reversal for
 * English, Turkish, Adyghe, and Russian.
 *
 * Password editors (native, visible, WebView, and numeric PIN) typically do not
 * keep a composing span. [android.view.inputmethod.InputConnection.setComposingText]
 * then inserts the whole word on every letter.
 * [android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS] alone is not enough to
 * opt out — many ordinary editors set that flag.
 *
 * When this returns false, the IME must
 * [android.view.inputmethod.InputConnection.commitText] each character instead.
 */
object EditorComposingPolicy {

    fun allowsComposing(info: EditorInfo?): Boolean {
        if (info == null) return true
        val pkg = info.packageName
        if (!pkg.isNullOrEmpty() && isUnsafePackage(pkg)) return false
        // TYPE_NULL (0) is a dummy/raw connection that typically does not implement
        // composing spans or extracted text (AOSP LatinIME treats these as raw).
        val inputType = info.inputType
        if (inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_NULL) {
            return false
        }
        if (isPasswordInputType(inputType)) return false
        return true
    }

    /**
     * Password / PIN variations (AOSP TextView password input-type checks plus
     * visible-password). Class and variation must both match so a text field
     * with an overlapping variation nibble is not treated as a PIN.
     */
    fun isPasswordInputType(inputType: Int): Boolean {
        val cls = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return when (cls) {
            InputType.TYPE_CLASS_TEXT ->
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            InputType.TYPE_CLASS_NUMBER ->
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }

    private fun isUnsafePackage(packageName: String): Boolean {
        if (packageName in UNSAFE_PACKAGES) return true
        return UNSAFE_PACKAGE_PREFIXES.any { packageName.startsWith(it) }
    }

    private val UNSAFE_PACKAGES = setOf(
        "com.reddit",
        "reddit.news"
    )

    private val UNSAFE_PACKAGE_PREFIXES = listOf(
        "com.reddit."
    )
}
