package com.adyghe.keyboard.predictive

import com.adyghe.keyboard.core.language.LanguageConfig

/**
 * Interface for the predictive-text / dictionary engine. A concrete implementation backed
 * by a real dictionary+spell-checker can be added later, one per language, without rewiring
 * the IME. The IME only depends on this interface.
 */
interface SuggestionEngine {

    /**
     * Returns suggestion candidates for the current [composingWord] in [language], plus a
     * flag for whether the first suggestion is an exact dictionary match.
     */
    fun suggestions(
        composingWord: String,
        language: LanguageConfig
    ): SuggestionResult

    /** Whether this engine can contribute suggestions for [language]. */
    fun supports(language: LanguageConfig): Boolean
}

data class SuggestionResult(
    val suggestions: List<String> = emptyList(),
    val exactMatch: Boolean = false
)
