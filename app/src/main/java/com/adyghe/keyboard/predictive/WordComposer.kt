package com.adyghe.keyboard.predictive

import com.adyghe.keyboard.core.language.LanguageConfig

/**
 * Tracks the current composing word and its language so the [SuggestionEngine] can produce
 * candidates. This is the foundation the future dictionary/spell-checker will consume.
 */
class WordComposer {

    var language: LanguageConfig? = null
        private set

    var composing: String = ""
        private set

    val isComposing: Boolean get() = composing.isNotEmpty()

    fun startComposing(lang: LanguageConfig) {
        if (language != lang) {
            language = lang
            composing = ""
        }
    }

    fun append(char: Char): String {
        composing += char
        return composing
    }

    fun append(string: String): String {
        composing += string
        return composing
    }

    fun deleteLast(): String {
        if (composing.isNotEmpty()) {
            composing = composing.dropLast(1)
        }
        return composing
    }

    /** Replace the tracked word (e.g. after re-syncing with the editor's cursor). */
    fun set(word: String) {
        composing = word
    }

    /** Insert [string] at [index] (clamped), supporting edits in the middle of a word. */
    fun insert(index: Int, string: String): String {
        val i = index.coerceIn(0, composing.length)
        composing = composing.substring(0, i) + string + composing.substring(i)
        return composing
    }

    /** Delete the char at [index] if present, supporting edits in the middle of a word. */
    fun deleteAt(index: Int): String {
        if (index in composing.indices) {
            composing = composing.removeRange(index, index + 1)
        }
        return composing
    }

    fun commit(): String {
        val word = composing
        composing = ""
        return word
    }

    fun reset() {
        composing = ""
    }
}
