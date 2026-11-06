package com.adyghe.keyboard.core.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorComposingPolicyTest {

    @Test
    fun nullEditor_allowsComposing() {
        assertTrue(EditorComposingPolicy.allowsComposing(null))
    }

    @Test
    fun ordinaryTextField_allowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.whatsapp"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertTrue(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun redditFrontPage_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.reddit.frontpage"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun redditChildPackage_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.reddit.launch"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun legacyRedditNews_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "reddit.news"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun typeNull_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.example.search"
            inputType = InputType.TYPE_NULL
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun password_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.example.bank"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun visiblePassword_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.example.bank"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun webPassword_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.android.chrome"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun numberPassword_disallowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.example.bank"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        assertFalse(EditorComposingPolicy.allowsComposing(info))
    }

    @Test
    fun noSuggestionsFlagAlone_stillAllowsComposing() {
        val info = EditorInfo().apply {
            packageName = "com.whatsapp"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        assertTrue(EditorComposingPolicy.allowsComposing(info))
    }
}
