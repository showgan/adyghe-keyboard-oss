package com.adyghe.keyboard.core.layout

import com.adyghe.keyboard.core.input.NumberPadVariant

/**
 * Shared row builders. Bottom navigation rows (spacebar, enter, language switch) and
 * symbol/control panels are shared across languages. Keys are stored in visual left-to-right
 * order (Enter on the right); the IME UI keeps layout direction LTR so Compose does not
 * mirror rows under an RTL system locale.
 */
internal object StandardRows {

    /** Letters-row comma long-press list; primary first, then extras by usage frequency. */
    val LETTER_COMMA_POPUP: List<String> = listOf(",", "?", "!", ":", ";", "/", "@", "#", "$")

    /** Letters-row period long-press list; primary first, then extras by usage frequency. */
    val LETTER_PERIOD_POPUP: List<String> = listOf(".", "'", "\"", "-", "(", ")", "_")

    /**
     * Letters-mode bottom row: `?123 | emoji | leftPunct | globe | SPACE | rightPunct | ENTER`.
     * Globe tap cycles languages; long-press opens the picker. Keyboard swipe / space long-press still work.
     */
    fun letterBottomRow(
        leftPunct: String = ",",
        rightPunct: String = ".",
        leftPopup: List<String> = LETTER_COMMA_POPUP,
        rightPopup: List<String> = LETTER_PERIOD_POPUP
    ): KeyboardRow = KeyboardRow(
        listOf(
            Keys.modifier(KeyAction.NUMBERS, ";?123", weight = 1.5f),
            Keys.modifier(KeyAction.EMOJI, null, weight = 1.0f),
            Keys.char(leftPunct, popup = leftPopup, weight = 1.0f),
            Keys.modifier(KeyAction.LANG_SWITCH, null, weight = 1.0f),
            Keys.modifier(KeyAction.SPACE, null, weight = 3.0f),
            Keys.char(rightPunct, popup = rightPopup, weight = 1.0f),
            Keys.modifier(KeyAction.ENTER, null, weight = 1.5f)
        )
    )

    private val currencyPopup = listOf("$", "€", "£", "¥", "₽", "₺")

    /** First "numbers" panel (Gboard ?123 page 1). */
    fun numbersPanel(topRow: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")): List<KeyboardRow> {
        return listOf(
            KeyboardRow(topRow.map { KeyboardKey(label = it, char = it) }),
            KeyboardRow(
                listOf(
                    Keys.char("@"),
                    Keys.char("#"),
                    Keys.char("$", popup = currencyPopup),
                    Keys.char("_"),
                    Keys.char("&"),
                    Keys.char("-", popup = listOf("-", "\u2013", "\u2014", "\u2022")),
                    Keys.char("+"),
                    Keys.char("("),
                    Keys.char(")"),
                    Keys.char("/", popup = listOf("/", "\\"))
                )
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.SYMBOLS, "=\\<", weight = 1.5f),
                    Keys.char("*", popup = listOf("*", "\u00D7")),
                    Keys.char("\"", popup = listOf("\"", "\u201C", "\u201D")),
                    Keys.char("'", popup = listOf("'", "\u2018", "\u2019", "`")),
                    Keys.char(":"),
                    Keys.char(";"),
                    Keys.char("!", popup = listOf("!", "\u00A1")),
                    Keys.char("?", popup = listOf("?", "\u00BF")),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.ABC, "ABC", weight = 1.5f),
                    Keys.char(","),
                    KeyboardKey(action = KeyAction.NUMBER_PAD, weight = 1.0f),
                    Keys.modifier(KeyAction.LANG_SWITCH, null, weight = 1.0f),
                    Keys.modifier(KeyAction.SPACE, null, weight = 3.0f),
                    Keys.char("."),
                    Keys.modifier(KeyAction.ENTER, null, weight = 1.5f)
                )
            )
        )
    }

    /** Second "symbols" panel (Gboard =\< page 2) with cursor keys flanking space. */
    fun symbolsPanel(): List<KeyboardRow> {
        return listOf(
            KeyboardRow(
                listOf("[", "]", "{", "}", "#", "%", "^", "\u00D7", "\u00F7", "=").map { c ->
                    KeyboardKey(label = c, char = c)
                }
            ),
            KeyboardRow(
                listOf("_", "\\", "|", "~", "<", ">", "\u20AC", "\u00A3", "\u20BD", "\u20BA").map { c ->
                    KeyboardKey(label = c, char = c)
                }
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.NUMBERS, "?123", weight = 1.5f),
                    Keys.char("\u00A9"),
                    Keys.char("\u00AE"),
                    Keys.char("\u2122"),
                    Keys.char("\u00B0"),
                    Keys.char("\u2022"),
                    Keys.char("\u00A1"),
                    Keys.char("\u00BF"),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f)
                )
            ),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.ABC, "ABC", weight = 1.5f),
                    Keys.char(","),
                    Keys.modifier(KeyAction.CURSOR_LEFT, null, weight = 1.0f),
                    Keys.modifier(KeyAction.SPACE, null, weight = 4.0f),
                    Keys.modifier(KeyAction.CURSOR_RIGHT, null, weight = 1.0f),
                    Keys.char("."),
                    Keys.modifier(KeyAction.ENTER, null, weight = 1.5f)
                )
            )
        )
    }

    /** Classic 3×4 phone dial pad for phone-number fields. */
    fun phonePad(): List<KeyboardRow> {
        fun digit(c: String, popup: List<String> = listOf(c)) =
            Keys.char(c, popup = popup, weight = 1.0f)

        return listOf(
            KeyboardRow(listOf(digit("1"), digit("2"), digit("3"))),
            KeyboardRow(listOf(digit("4"), digit("5"), digit("6"))),
            KeyboardRow(listOf(digit("7"), digit("8"), digit("9"))),
            KeyboardRow(listOf(digit("*"), digit("0", listOf("0", "+")), digit("#"))),
            KeyboardRow(
                listOf(
                    Keys.modifier(KeyAction.ABC, "ABC", weight = 1.5f),
                    Keys.char("+", weight = 1.0f),
                    Keys.modifier(KeyAction.SPACE, null, weight = 3.0f),
                    Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.5f),
                    Keys.modifier(KeyAction.ENTER, null, weight = 2.0f)
                )
            )
        )
    }

    /**
     * Compact number pad for [android.text.InputType.TYPE_CLASS_NUMBER] and datetime fields.
     * Left column: `+ − × ÷`. Right column: `%`, underscore/space, backspace.
     * Extra keys depend on [variant] (decimal point, sign, date/time separators).
     */
    fun numberPad(variant: NumberPadVariant): List<KeyboardRow> {
        fun d(c: String, weight: Float = 1.0f) = Keys.char(c, weight = weight)
        fun op(c: String) = Keys.char(c, weight = 1.0f).copy(isModifier = true)
        fun blank() = KeyboardKey(label = null, action = KeyAction.NONE, weight = 1.0f, isModifier = true)

        val leftOps = listOf("+", "\u2212", "\u00D7", "\u00F7")
        val rightChrome = listOf(
            d("%"),
            d("_"),
            Keys.modifier(KeyAction.BACKSPACE, "\u232B", weight = 1.0f)
        )

        val digitRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        val rows = digitRows.mapIndexed { i, digits ->
            KeyboardRow(
                listOf(op(leftOps[i])) + digits.map { d(it) } + listOf(rightChrome[i])
            )
        }.toMutableList()

        // Zero row: left op ÷ already used in row 3; use variant extras + 0 + =/. extras
        val zeroLeft: KeyboardKey
        val zeroRight: KeyboardKey
        when (variant) {
            NumberPadVariant.PLAIN -> {
                zeroLeft = blank()
                zeroRight = d("=")
            }
            NumberPadVariant.DECIMAL -> {
                zeroLeft = blank()
                zeroRight = d(".")
            }
            NumberPadVariant.SIGNED -> {
                zeroLeft = d("-")
                zeroRight = d("+")
            }
            NumberPadVariant.SIGNED_DECIMAL -> {
                zeroLeft = d("-")
                zeroRight = d(".")
            }
            NumberPadVariant.DATETIME -> {
                zeroLeft = d(":")
                zeroRight = d("/")
            }
        }

        rows.add(KeyboardRow(listOf(op(leftOps[3]), zeroLeft, d("0"), zeroRight, blank())))

        val chromeExtras: List<KeyboardKey> = when (variant) {
            NumberPadVariant.DATETIME -> listOf(d("-"), d("."))
            NumberPadVariant.SIGNED_DECIMAL -> listOf(d("+"))
            else -> emptyList()
        }

        val navKeys = mutableListOf(
            Keys.modifier(KeyAction.ABC, "ABC", weight = 1.5f),
            Keys.modifier(KeyAction.NUMBERS, "!?#", weight = 1.0f)
        )
        navKeys.addAll(chromeExtras)
        navKeys.add(Keys.modifier(KeyAction.SPACE, null, weight = 2.0f))
        navKeys.add(Keys.modifier(KeyAction.ENTER, null, weight = 2.0f))
        rows.add(KeyboardRow(navKeys))

        return rows
    }
}
