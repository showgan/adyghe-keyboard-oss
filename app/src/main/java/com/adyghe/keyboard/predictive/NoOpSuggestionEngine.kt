package com.adyghe.keyboard.predictive

import com.adyghe.keyboard.core.language.LanguageConfig

/**
 * A placeholder engine that returns no suggestions. The keyboard is fully functional without
 * a dictionary; swapping in a real implementation later is a drop-in change.
 */
class NoOpSuggestionEngine : SuggestionEngine {
    override fun suggestions(
        composingWord: String,
        language: LanguageConfig
    ): SuggestionResult = SuggestionResult(emptyList(), false)

    override fun supports(language: LanguageConfig): Boolean = false
}
