# Calculator

Standard four-function calculator grid: digit entry, +/-/x/÷, decimal, sign toggle, backspace. CLOSE (bottom-left) returns the current display value via `goBack`, so a caller can grab whatever result the user landed on.

---

## Files

* `CalculatorScreen.kt`: the screen itself
* `CalculatorLogic.kt`: pure state transitions for the four-function math, no app coupling
* `CalculatorLogicTest.kt`: covers the file above

## Depends on

* `com.thelightphone.sdk`: `SealedLightActivity`, `SimpleLightScreen`
* `com.thelightphone.sdk.ui`: `LightTheme`, `LightThemeController`, `LightThemeTokens`, `LightIcon`, `LightIconConfiguration`, `LightIcons`, `gridUnitsAsDp`, `lightClickable`, `designVerticalPxToSp`

## Pasting this in

1. Rename the `package` declaration in all three files.
2. `CalculatorScreen` extends `SimpleLightScreen<String>` and returns the display value as a plain string, whatever's shown when CLOSE is tapped (not necessarily the result of an `=`).
3. `CalculatorLogic` is a plain object over an immutable `CalculatorState`, one pure function per button. No ViewModel, no coroutine scope, so it drops straight into `remember { mutableStateOf(CalculatorState()) }`.

> [!NOTE]
> This follows `LightThemeController` for live light/dark. If your tool is deliberately single-theme, swap that for a fixed `LightThemeColors.Dark`/`.Light` instead.

> [!NOTE]
> The button grid's sizing (`ButtonInset`, `RightGutter`, `GridFontScale`) was measured against the stock LightOS calculator screenshot rather than derived from an SDK preset, see the comment above those constants.

> [!NOTE]
> This is trimmed down from a multi-tool calculator app: no bottom-left menu to sibling tools (CLOSE just dismisses), no calc history, no long-press copy/actions on the display. Layer those back in yourself if your tool needs them.
