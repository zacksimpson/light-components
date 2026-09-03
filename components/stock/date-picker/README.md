# Date Picker

Full-screen calendar month picker. Tap a day to select it and close the screen in one tap, no separate confirm step. Dismiss (X) closes without changing the value. Always opens on the current month regardless of any already-selected date (deliberate).

---

## Depends on

* `com.thelightphone.sdk`: `SealedLightActivity`, `SimpleLightScreen`
* `com.thelightphone.sdk.ui`: `LightTheme`, `LightThemeController`, `LightThemeTokens`, `LightText`, `LightIcon`, `LightIcons`, `LightBottomBar`, `LightBarButton`, `gridUnitsAsDp`, `lightClickable`, `designVerticalPxToSp`
* `java.time` (`LocalDate`, `YearMonth`), no third-party date library

## Pasting this in

1. Rename the `package` declaration.
2. `DatePickerScreen` extends `SimpleLightScreen<String?>` and returns the selected date as `"YYYY-MM-DD"` via `goBack(...)`, or `null` if dismissed. Adjust the format if your tool stores dates differently.

> [!NOTE]
> This follows `LightThemeController` for live light/dark. If your tool is deliberately single-theme, swap that for a fixed `LightThemeColors.Dark`/`.Light` instead.

> [!NOTE]
> The private `LightSizedText` helper at the bottom of the file is duplicated in `time-picker` too, not shared. If you paste both components in, you'll end up with two copies, that's expected.
