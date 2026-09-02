# Light Theme

The greys that are the whole LightOS palette, and the Akkurat loader that matches the system UI. No theme object — just constants and a loader you compose into whatever theming you already use.

---

## Files

* `LightGreys.kt`: the three colours LightOS actually uses — `Dim` (secondary text), `Faint` (tertiary/placeholder), `RuleGrey` (separators). No accent, no semantic red/green.
* `LightFont.kt`: `akkuratFamilyOrDefault()`, which finds the Akkurat family the phone ships with and returns it as a Compose `FontFamily`, falling back to `FontFamily.Default`.

## Depends on

* `androidx.compose.ui.graphics.Color` (for the colour constants) and `android.graphics.fonts.SystemFonts` (for the loader) — both come with the SDK's UI stack.

## Pasting this in

1. Rename the `package` declaration in both files.
2. Reference the constants directly: `Dim`, `Faint`, `RuleGrey`.
3. Call `akkuratFamilyOrDefault()` once at the top of your content and feed it into your `MaterialTheme`/`LightTheme` `typography` so all text inherits Akkurat.

> [!NOTE]
> These are deliberately not a `MaterialTheme` extension or a `CompositionLocal`. An app that wants a different palette should use a different one — several do. Constants are easier to disagree with than a theme.

> [!NOTE]
> `akkuratFamilyOrDefault()` reads from the system font table at runtime. It is a fallback chain, not a bundler — if the phone doesn't ship Akkurat (it does), you get `FontFamily.Default` rather than an error.
