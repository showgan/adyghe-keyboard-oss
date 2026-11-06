package com.adyghe.keyboard.core.language

import android.content.Context
import android.content.SharedPreferences

/**
 * Loads and saves which languages are included in swipe / globe cycling.
 * At least one Adıǵabze Latin layout ([Languages.ADYGHE_LATIN_1] or
 * [Languages.ADYGHE_LATIN_2]) is always kept enabled.
 */
interface LanguagePreferences {
    fun loadEnabledIds(): Set<String>
    fun saveEnabledIds(ids: Set<String>)
}

fun normalizeEnabledLanguageIds(ids: Set<String>?): Set<String> {
    val allIds = Languages.ALL.map { it.id }.toSet()
    if (ids.isNullOrEmpty()) return allIds
    val known = ids.filter { it in allIds }.toMutableSet()
    if (known.none { Languages.isAdygheLatin(it) }) {
        known.add(Languages.default().id)
    }
    return if (known.isEmpty()) allIds else known
}

/**
 * Enable or disable [languageId] in the swipe / globe cycle set.
 * Returns null when the id is unknown or when the last Adıǵabze Latin layout would be disabled.
 */
fun applyLanguageEnabled(
    current: Set<String>,
    languageId: String,
    enabled: Boolean
): Set<String>? {
    if (Languages.ALL.none { it.id == languageId }) return null
    if (!enabled && Languages.isAdygheLatin(languageId)) {
        val remainingAdyghe = current.count { Languages.isAdygheLatin(it) && it != languageId }
        if (remainingAdyghe == 0) return null
    }
    val next = current.toMutableSet()
    if (enabled) next.add(languageId) else next.remove(languageId)
    return normalizeEnabledLanguageIds(next)
}

/**
 * Whether the settings / picker checkbox for [languageId] should accept a tap.
 * Unchecked rows can always be turned on; a checked Adıǵabze row cannot be turned off
 * when it is the last remaining Adıǵabze Latin layout.
 */
fun canChangeLanguageEnabled(enabledIds: Set<String>, languageId: String): Boolean {
    if (Languages.ALL.none { it.id == languageId }) return false
    if (languageId !in enabledIds) return true
    return applyLanguageEnabled(enabledIds, languageId, enabled = false) != null
}

/** Language to show when [enabledIds] is the cycle set: default if enabled, else first in [Languages.ALL]. */
fun preferredEnabledLanguageId(enabledIds: Set<String>): String {
    val defaultId = Languages.default().id
    if (defaultId in enabledIds) return defaultId
    return Languages.ALL.firstOrNull { it.id in enabledIds }?.id ?: defaultId
}

/** SharedPreferences-backed [LanguagePreferences]. */
class SharedLanguagePreferences(context: Context) : LanguagePreferences {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun loadEnabledIds(): Set<String> {
        val stored = prefs.getStringSet(KEY_ENABLED_IDS, null)
        val schema = prefs.getInt(KEY_SCHEMA, 0)
        var ids = normalizeEnabledLanguageIds(stored)
        if (stored != null && schema < SCHEMA) {
            ids = ids + Languages.ADYGHE_LATIN_1.id
            saveEnabledIds(ids)
        }
        return ids
    }

    override fun saveEnabledIds(ids: Set<String>) {
        prefs.edit()
            .putStringSet(KEY_ENABLED_IDS, normalizeEnabledLanguageIds(ids))
            .putInt(KEY_SCHEMA, SCHEMA)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_ENABLED_IDS = "enabled_language_ids"
        private const val KEY_SCHEMA = "language_prefs_schema"
        private const val SCHEMA = 2
    }
}

/** In-memory prefs for unit tests. */
class InMemoryLanguagePreferences(
    initial: Set<String> = Languages.ALL.map { it.id }.toSet()
) : LanguagePreferences {
    private var enabled: Set<String> = normalizeEnabledLanguageIds(initial)

    override fun loadEnabledIds(): Set<String> = enabled

    override fun saveEnabledIds(ids: Set<String>) {
        enabled = normalizeEnabledLanguageIds(ids)
    }
}
