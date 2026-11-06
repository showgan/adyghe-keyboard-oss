package com.adyghe.keyboard.core.language

import com.adyghe.keyboard.R

object Languages {

    val ADYGHE_LATIN_1 = LanguageConfig(
        id = "adyghe_latin_1",
        labelRes = R.string.subtype_label_adyghe_latin_1,
        nativeNameRes = R.string.lang_native_adyghe_latin_1,
        languageTag = "ady",
        isAsciiCapable = true
    )

    val ADYGHE_LATIN_2 = LanguageConfig(
        id = "adyghe_latin",
        labelRes = R.string.subtype_label_adyghe_latin,
        nativeNameRes = R.string.lang_native_adyghe_latin,
        languageTag = "ady",
        isAsciiCapable = true
    )

    val ADYGHE_CYRILLIC = LanguageConfig(
        id = "adyghe_cyrillic",
        labelRes = R.string.subtype_label_adyghe_cyrillic,
        nativeNameRes = R.string.lang_native_adyghe_cyrillic,
        languageTag = "ady-Cyrl"
    )

    val ARABIC = LanguageConfig(
        id = "arabic",
        labelRes = R.string.subtype_label_arabic,
        nativeNameRes = R.string.lang_native_arabic,
        languageTag = "ar",
        isRtl = true
    )

    val HEBREW = LanguageConfig(
        id = "hebrew",
        labelRes = R.string.subtype_label_hebrew,
        nativeNameRes = R.string.lang_native_hebrew,
        languageTag = "he",
        isRtl = true
    )

    val ENGLISH_US = LanguageConfig(
        id = "english_us",
        labelRes = R.string.subtype_label_english,
        nativeNameRes = R.string.lang_native_english,
        languageTag = "en-US",
        isAsciiCapable = true
    )

    val TURKISH = LanguageConfig(
        id = "turkish",
        labelRes = R.string.subtype_label_turkish,
        nativeNameRes = R.string.lang_native_turkish,
        languageTag = "tr",
        isAsciiCapable = true
    )

    val RUSSIAN = LanguageConfig(
        id = "russian",
        labelRes = R.string.subtype_label_russian,
        nativeNameRes = R.string.lang_native_russian,
        languageTag = "ru"
    )

    /** All supported languages, in switch order (the glob key cycles through these). */
    val ALL: List<LanguageConfig> = listOf(
        ADYGHE_LATIN_1,
        ADYGHE_LATIN_2,
        ADYGHE_CYRILLIC,
        ARABIC,
        HEBREW,
        ENGLISH_US,
        TURKISH,
        RUSSIAN
    )

    val ADYGHE_LATIN_IDS: Set<String> = setOf(ADYGHE_LATIN_1.id, ADYGHE_LATIN_2.id)

    fun isAdygheLatin(id: String): Boolean = id in ADYGHE_LATIN_IDS

    private val byId = ALL.associateBy { it.id }

    fun fromId(id: String): LanguageConfig = byId[id] ?: ADYGHE_LATIN_1

    fun default(): LanguageConfig = ADYGHE_LATIN_1
}
