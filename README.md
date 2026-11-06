# ADIǴE Multi-Language Keyboard

A modern Android Input Method Editor (IME) built with Kotlin and Jetpack Compose. The primary layouts are the **Latin-based ADIǴE alphabets** (Adıǵabze 1 and Adıǵabze 2),
and the keyboard also ships several additional languages as switchable IME subtypes.

## Languages

| Subtype | Layout | RTL |
| --- | --- | --- |
| Adıǵabze 1 | Turkish-Q base + remaining ADIǴE letters via long-press | |
| Adıǵabze 2 | US-QWERTY base + long-press pop-ups | |
| Адыгэбзэ (Cyrillic) | Russian-style base, palochka `ӏ`/`Ӏ` and `э` central | |
| العربية (Arabic) | Standard Arabic | yes |
| עברית (Hebrew) | Standard Hebrew | yes |
| English (US) | Standard US QWERTY | |
| Türkçe (Turkish) | Turkish-Q with `ç ğ ı ö ş ü` | |
| Русский (Russian) | ЙЦУКЕН | |

Adıǵabze 1 places `ğ ü ş ı ö ç` on the main keys (Turkish-Q). Adıǵabze 2 keeps a US-QWERTY
base and reaches special ADIǴE letters via long-press on phonetically similar keys.
The tick `'` is on period long-press.

## Architecture

- **Kotlin**, **Jetpack Compose** for both the Settings app and the keyboard UI.
- **InputMethodService** (`AdigeImeService`) hosts a ComposeView. Since an IME has no
  Activity lifecycle, a custom `ImeLifecycleOwner` (LifecycleOwner + ViewModelStoreOwner +
  SavedStateRegistryOwner) is attached to the view tree.
- **MVI** with `ImeStore` exposing `StateFlow<ImeState>` for shift state, layout mode,
  long-press popup, and suggestions.
- **Hilt** for dependency injection (`LayoutProvider`, `ImeStore`, `InputConnectionController`,
  `VibrationProvider`).
- **Predictive-text ready**: `SuggestionEngine` interface + `WordComposer` and a `SuggestionStrip`
  UI. A no-op engine is wired in today; a real per-language dictionary can be added as a drop-in
  later.

## Key packages

- `ime/` — the input method service and Compose lifecycle wrapper.
- `core/layout/` — keyboard data models and per-language layout definitions.
- `core/language/` — language configs and subtype registry.
- `core/state/` — MVI store and state.
- `core/input/` — `InputConnectionController`.
- `predictive/` — `SuggestionEngine`, `WordComposer`.
- `ui/` — Compose keyboard UI (rows, keys, popup, suggestion strip).
- `ui/settings/` — launcher Settings screen.

## Build & run

```bash
./gradlew :app:assembleDebug
```

Install and enable:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell ime enable com.adyghe.keyboard/.ime.AdigeImeService
adb shell ime set com.adyghe.keyboard/.ime.AdigeImeService
```

Switch languages by tapping the globe key (left of the space bar on the letters and
numbers panels), swiping left/right on the space bar, or long-pressing the globe or
space bar for the language picker. If only one language is enabled, tapping the globe
opens the picker so more languages can be turned on.

## License

Licensed under the [Apache License 2.0](LICENSE).
