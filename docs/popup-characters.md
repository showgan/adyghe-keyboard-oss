# Long-press popup characters by language

Review document for letter-key popups. Source of truth today is the `*Layout.kt` files under `app/src/main/java/com/adyghe/keyboard/core/layout/`.

**How to edit this for a code update**

- Fill or change the **Popup extras** column (space-separated characters / digraphs).
- Leave the cell empty (or write `—`) for keys that should have no popup.
- Order in the cell is the order shown left-to-right in the popup.
- Digraphs are fine as multi-character tokens (e.g. `гъ`, `кӏ`).
- Do not list the primary key character itself in **Popup extras** — the app already strips it before showing the popup.
- Bottom-row punctuation next to space is included; modifiers (Shift, Backspace, Space, Enter, language switch, `?123`) are omitted.

**Languages covered**


| Language id       | Label                | Layout file               |
| ----------------- | -------------------- | ------------------------- |
| `adyghe_latin_1`  | Adıǵabze 1           | `AdygheLatin1Layout.kt`   |
| `adyghe_latin`    | Adıǵabze 2           | `AdygheLatinLayout.kt`    |
| `adyghe_cyrillic` | Адыгэбзэ (Кириллица) | `AdygheCyrillicLayout.kt` |
| `arabic`          | العربية              | `ArabicLayout.kt`         |
| `hebrew`          | עברית                | `HebrewLayout.kt`         |
| `english_us`      | English (US)         | `EnglishUsLayout.kt`      |
| `turkish`         | Türkçe               | `TurkishLayout.kt`        |
| `russian`         | Русский              | `RussianLayout.kt`        |


---



## 1. Adıǵabze 1 (`adyghe_latin_1`)

Layout: Turkish-Q · dedicated `ğ ü ş ı ö ç` and dotted `i` on main keys · remaining ADIǴE letters via long-press · bottom punctuation: `,` · `.` (`.` long-press includes `'`)


| Key | Popup extras (current) | Notes / proposed              |
| --- | ---------------------- | ----------------------------- |
| q   | 1                      |                               |
| w   | 2                      |                               |
| e   | é 3                    |                               |
| r   | 4                      |                               |
| t   | ṫ 5                    |                               |
| y   | 6                      |                               |
| u   | 7                      | ü is a dedicated main key     |
| ı   | 8                      | dotless i (Shift → I)         |
| o   | 9                      | ö is a dedicated main key     |
| p   | ṕ 0                    |                               |
| ğ   | —                      | dedicated                     |
| ü   | —                      | dedicated                     |
| a   | á                      |                               |
| s   | ś š                    | ş is a dedicated main key     |
| d   | —                      |                               |
| f   | —                      |                               |
| g   | ǵ                      | ğ is a dedicated main key     |
| h   | ḣ                      |                               |
| j   | —                      |                               |
| k   | ḱ                      |                               |
| l   | ĺ                      |                               |
| ş   | —                      | dedicated                     |
| i   | —                      | dotted i (Shift → İ)          |
| z   | ź                      |                               |
| x   | —                      |                               |
| c   | ć                      | ç is a dedicated main key     |
| v   | —                      |                               |
| b   | —                      |                               |
| n   | —                      |                               |
| m   | —                      |                               |
| ö   | —                      | dedicated                     |
| ç   | —                      | dedicated                     |
| ,   | ? ; : ! @ $ # /            |                               |
| .   | ' " ( ) - _            | tick moved from dedicated key |


---



## 2. Adıǵabze 2 (`adyghe_latin`)

Layout: QWERTY · bottom punctuation: `,` · `.` (`.` long-press includes `'`)


| Key | Popup extras (current) | Notes / proposed              |
| --- | ---------------------- | ----------------------------- |
| q   | 1                      |                               |
| w   | 2                      |                               |
| e   | é 3                    |                               |
| r   | 4                      |                               |
| t   | ṫ 5                    |                               |
| y   | 6                      |                               |
| u   | ü 7                    |                               |
| i   | ı 8 İ                  |                               |
| o   | ö 9                    |                               |
| p   | ṕ 0                    |                               |
| a   | á                      |                               |
| s   | ş ś š                  |                               |
| d   | —                      |                               |
| f   | —                      |                               |
| g   | ğ ǵ                    |                               |
| h   | ḣ                      |                               |
| j   | —                      |                               |
| k   | ḱ                      |                               |
| l   | ĺ                      |                               |
| z   | ź                      |                               |
| x   | —                      |                               |
| c   | ç ć                    |                               |
| v   | —                      |                               |
| b   | —                      |                               |
| n   | —                      |                               |
| m   | —                      |                               |
| ,   | ? ; : ! @ $ # /            |                               |
| .   | ' " ( ) - _            | tick moved from dedicated key |


---



## 3. Adyghe Cyrillic (`adyghe_cyrillic`)

Layout: Russian-style ЙЦУКЕН variant · bottom punctuation: `,` · `.` (`.` long-press includes `ъ` `ь`) · palochka `ӏ` on letter row


| Key | Popup extras (current) | Notes / proposed                     |
| --- | ---------------------- | ------------------------------------ |
| й   | —                      |                                      |
| ц   | цӏ                     |                                      |
| у   | —                      |                                      |
| к   | къ кӏ                  |                                      |
| е   | —                      |                                      |
| н   | —                      |                                      |
| г   | гъ                     |                                      |
| ш   | шъ шӏ                  |                                      |
| щ   | —                      |                                      |
| з   | —                      |                                      |
| х   | хъ хь                  |                                      |
| ф   | —                      |                                      |
| ы   | —                      |                                      |
| в   | —                      |                                      |
| а   | —                      |                                      |
| п   | пӏ                     |                                      |
| р   | —                      |                                      |
| о   | —                      |                                      |
| л   | лъ лӏ                  |                                      |
| д   | дж дз                  |                                      |
| ж   | жъ жь                  |                                      |
| э   | —                      |                                      |
| я   | —                      |                                      |
| ч   | чъ чӏ                  |                                      |
| с   | —                      |                                      |
| м   | —                      |                                      |
| и   | й                      |                                      |
| т   | тӏ                     |                                      |
| ӏ   | Ӏ ъ ь                  | uppercase palochka                   |
| б   | —                      |                                      |
| ю   | —                      |                                      |
| ,   | ? ; : ! @ $ # /            |                                      |
| .   | ' " ( ) - _            | hard/soft sign via period long-press |


---



## 4. Arabic (`arabic`)

Layout: Gboard-style visual LTR grid (11+11+10 letters, no Shift) · Eastern-Indic digit hints on the first 10 keys of row 1 · bottom punctuation: `،` · `.`


| Key | Popup extras (current) | Notes / proposed                        |
| --- | ---------------------- | --------------------------------------- |
| ض   | ١                      |                                         |
| ص   | ٢                      |                                         |
| ث   | ٣                      |                                         |
| ق   | ٤                      |                                         |
| ف   | ٥                      |                                         |
| غ   | ٦                      |                                         |
| ع   | ٧                      |                                         |
| ه   | ٨ ه‍                    | digit hint, then ZWJ form               |
| خ   | ٩                      |                                         |
| ح   | ٠                      |                                         |
| ج   | —                      | 11th top-row key, no digit              |
| ش   | —                      |                                         |
| س   | —                      |                                         |
| ي   | ئ                      | hamza-on-ya (not a main key)            |
| ب   | —                      |                                         |
| ل   | لا                     | lam-alef ligature (not a main key)      |
| ا   | أ إ آ ء ٱ              |                                         |
| ت   | —                      |                                         |
| ن   | —                      |                                         |
| م   | —                      |                                         |
| ك   | —                      |                                         |
| ط   | —                      | row 2 last                              |
| ذ   | —                      | row 3 first                             |
| ء   | —                      |                                         |
| ؤ   | —                      | also a main key                         |
| ر   | —                      |                                         |
| ى   | —                      |                                         |
| ة   | —                      |                                         |
| و   | ؤ                      |                                         |
| ز   | —                      |                                         |
| ظ   | —                      |                                         |
| د   | —                      | row 3 last letter, next to Backspace    |
| ،   | ؟ ؛ : ! @ $ # /            | Arabic comma left of space              |
| .   | ' " ( ) - _            |                                         |


---



## 5. Hebrew (`hebrew`)

Layout: visual LTR grid (8+10+9 letters, no Shift) · digit hints on row 1 · dagesh/geresh on long-press · bottom punctuation: `,` · `.`


| Key | Popup extras (current) | Notes / proposed |
| --- | ---------------------- | ---------------- |
| ק   | 1                      |                  |
| ר   | 2                      |                  |
| א   | 3                      |                  |
| ט   | 4                      |                  |
| ו   | 5                      |                  |
| ן   | 6                      |                  |
| ם   | 7                      |                  |
| פ   | פּ 8 9 0               | dagesh, then remaining digits |
| ש   | שׂשׁ                   |                  |
| ד   | דּ                     |                  |
| ג   | ג׳                     |                  |
| כ   | כּ                     |                  |
| ע   | —                      |                  |
| י   | —                      |                  |
| ח   | ח׳                     |                  |
| ל   | —                      |                  |
| ך   | ךּ                     | sofit dagesh     |
| ף   | ףּ                     | sofit dagesh     |
| ז   | ז׳                     |                  |
| ס   | —                      |                  |
| ב   | בּ                     |                  |
| ה   | —                      |                  |
| נ   | —                      |                  |
| מ   | —                      |                  |
| צ   | צ׳                     |                  |
| ת   | תּ                     |                  |
| ץ   | ץ׳                     |                  |
| ,   | ? ; : ! @ $ # /            |                  |
| .   | ' " ( ) - _            |                  |


---



## 6. English (US) (`english_us`)

Layout: QWERTY · bottom punctuation: `,` · `.` · top-row keys also carry digit shortcuts in popups


| Key | Popup extras (current) | Notes / proposed |
| --- | ---------------------- | ---------------- |
| q   | 1                      |                  |
| w   | 2                      |                  |
| e   | 3                      |                  |
| r   | 4                      |                  |
| t   | 5                      |                  |
| y   | 6                      |                  |
| u   | 7                      |                  |
| i   | 8                      |                  |
| o   | 9                      |                  |
| p   | 0                      |                  |
| a   | —                      |                  |
| s   | —                      |                  |
| d   | —                      |                  |
| f   | —                      |                  |
| g   | —                      |                  |
| h   | —                      |                  |
| j   | —                      |                  |
| k   | —                      |                  |
| l   | —                      |                  |
| z   | —                      |                  |
| x   | —                      |                  |
| c   | —                      |                  |
| v   | —                      |                  |
| b   | —                      |                  |
| n   | —                      |                  |
| m   | —                      |                  |
| ,   | ? ; : ! @ $ # /            |                  |
| .   | ' " ( ) - _            |                  |


---



## 7. Turkish (`turkish`)

Layout: Turkish-Q · dedicated `ğ ü ş ı ö ç` on main keys · bottom punctuation: `,` · `.`


| Key | Popup extras (current) | Notes / proposed |
| --- | ---------------------- | ---------------- |
| q   | 1                      |                  |
| w   | 2                      |                  |
| e   | 3 é                    |                  |
| r   | 4                      |                  |
| t   | 5                      |                  |
| y   | 6                      |                  |
| u   | 7 û                    | circumflex after digit hint   |
| ı   | 8                      | dotless i (Shift → I)         |
| o   | 9                      |                               |
| p   | 0                      |                               |
| ğ   | —                      | dedicated                     |
| ü   | —                      | dedicated                     |
| a   | â                      | Turkish circumflex, not á     |
| s   | —                      |                               |
| d   | —                      |                               |
| f   | —                      |                               |
| g   | —                      |                               |
| h   | —                      |                               |
| j   | —                      |                               |
| k   | —                      |                               |
| l   | —                      |                               |
| ş   | —                      | dedicated                     |
| i   | î                      | dotted i (Shift → İ)          |
| z   | —                      |                  |
| x   | —                      |                  |
| c   | —                      |                  |
| v   | —                      |                  |
| b   | —                      |                  |
| n   | —                      |                  |
| m   | —                      |                  |
| ö   | —                      | dedicated        |
| ç   | —                      | dedicated        |
| ,   | ? ; : ! @ $ # /            |                  |
| .   | ' " ( ) - _            |                  |


---



## 8. Russian (`russian`)

Layout: standard ЙЦУКЕН · bottom punctuation: `,` · `.`


| Key | Popup extras (current) | Notes / proposed |
| --- | ---------------------- | ---------------- |
| й   | 1                      |                  |
| ц   | 2                      |                  |
| у   | 3                      |                  |
| к   | 4                      |                  |
| е   | 5 ё                    |                  |
| н   | 6                      |                  |
| г   | 7                      |                  |
| ш   | 8                      |                  |
| щ   | 9                      |                  |
| з   | 0                      |                  |
| х   | —                      |                  |
| ф   | —                      |                  |
| ы   | —                      |                  |
| в   | —                      |                  |
| а   | —                      |                  |
| п   | —                      |                  |
| р   | —                      |                  |
| о   | —                      |                  |
| л   | —                      |                  |
| д   | —                      |                  |
| ж   | —                      |                  |
| э   | —                      |                  |
| я   | —                      |                  |
| ч   | —                      |                  |
| с   | —                      |                  |
| м   | —                      |                  |
| и   | —                      |                  |
| т   | —                      |                  |
| ь   | ъ                      |                  |
| б   | —                      |                  |
| ю   | —                      |                  |
| ,   | ? ; : ! @ $ # /            |                  |
| .   | ' " ( ) - _            |                  |


---



## Out of scope (shared panels)

These are not language letter layouts; listed for completeness only:


| Context            | Key | Popup extras                      |
| ------------------ | --- | --------------------------------- |
| Email / URI chrome | `.` | `.com` `.net` `.org` `.edu` `.io` |
| URI chrome         | `/` | `://` `www.`                      |
| Phone pad          | `0` | `+`                               |
| Numbers panel      | `$` | `€` `£` `¥` `₽` `₺`               |


After you edit the **Notes / proposed** (or **Popup extras**) columns, say which languages to apply and the code in the corresponding `*Layout.kt` files can be updated to match.