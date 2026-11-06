package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.LetterChrome

/**
 * Patches the letters bottom navigation row with context-specific character keys
 * (email `@` / `.`, URI `/` / `.`) without duplicating full language layouts.
 *
 * Period keys expose long-press TLD shortcuts (iOS-style). URI slash keys expose
 * common URL prefixes.
 */
internal object LetterChromeRows {

    /** Top-level domains offered on long-press of the chrome `.` key. */
    val TLD_POPUP: List<String> = listOf(".", ".com", ".net", ".org", ".edu", ".io")

    /** URL prefixes offered on long-press of the URI `/` key. */
    val URI_SLASH_POPUP: List<String> = listOf("/", "://", "www.")

    fun apply(rows: List<KeyboardRow>, chrome: LetterChrome): List<KeyboardRow> {
        if (chrome == LetterChrome.NONE || rows.isEmpty()) return rows

        val extraChars = when (chrome) {
            LetterChrome.EMAIL -> listOf("@", ".")
            LetterChrome.URI -> listOf("/", ".")
            LetterChrome.NONE -> return rows
        }

        val lastIndex = rows.lastIndex
        val bottom = rows[lastIndex].keys
        val spaceIdx = bottom.indexOfFirst { it.action == KeyAction.SPACE }
        if (spaceIdx < 0) return rows

        // Find existing right-of-space period (or similar) so we can upgrade its popup
        // instead of inserting a duplicate `.`.
        val periodIdx = bottom.indexOfFirst {
            it.action == KeyAction.CHAR && it.char == "."
        }

        val filtered = bottom.toMutableList()

        // Slightly shrink space to make room for chrome extras that are not already present.
        val newSpaceIdx = filtered.indexOfFirst { it.action == KeyAction.SPACE }
        if (newSpaceIdx < 0) return rows

        val charsToInsert = extraChars.filter { c ->
            // Always insert `@` / `/`; for `.`, reuse/upgrade an existing period key.
            c != "." || periodIdx < 0
        }

        filtered[newSpaceIdx] = filtered[newSpaceIdx].copy(
            weight = (filtered[newSpaceIdx].weight - 0.5f * charsToInsert.size).coerceAtLeast(2.5f)
        )

        if (periodIdx >= 0 && "." in extraChars) {
            // Upgrade existing period with TLD popup (preserve other popup extras if any).
            val existing = filtered[periodIdx]
            val mergedPopup = (TLD_POPUP + existing.popupChars.filter { it !in TLD_POPUP })
                .distinct()
            filtered[periodIdx] = existing.copy(popupChars = mergedPopup)
        }

        // Insert after the period key when present, otherwise immediately after space.
        var insertAt = when {
            periodIdx >= 0 -> filtered.indexOfFirst {
                it.action == KeyAction.CHAR && it.char == "."
            }.let { if (it >= 0) it + 1 else newSpaceIdx + 1 }
            else -> newSpaceIdx + 1
        }

        for (c in charsToInsert) {
            // Skip `.` if we already upgraded an existing period.
            if (c == "." && periodIdx >= 0) continue
            filtered.add(insertAt, chromeKey(c, chrome))
            insertAt++
        }

        return rows.toMutableList().also {
            it[lastIndex] = KeyboardRow(filtered)
        }
    }

    private fun chromeKey(c: String, chrome: LetterChrome): KeyboardKey = when (c) {
        "." -> Keys.char(".", popup = TLD_POPUP)
        "/" -> if (chrome == LetterChrome.URI) {
            Keys.char("/", popup = URI_SLASH_POPUP)
        } else {
            Keys.char("/")
        }
        else -> Keys.char(c)
    }
}
