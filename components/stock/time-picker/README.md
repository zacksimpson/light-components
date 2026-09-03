# Time Picker

Numpad time entry. Digits fill right to left into an "H:MM" / "HH:MM" display. SAVE confirms once there are 3-4 valid digits. Dismiss (X) is only available with zero digits typed. Supports 12-hour (with AM/PM tap targets) and 24-hour modes via the `use24Hour` constructor param.

---

## Files

* `TimePickerScreen.kt`: the screen itself
* `TimePickerLogic.kt`: pure digit-validation state machine, no app coupling
* `TimeFormat.kt`: the formatting functions the screen calls (`formatTime`, `digitsToTime`, `timeToDisplayParts`), kept narrow to just those
* `TimePickerLogicTest.kt`: covers the two files above

## Depends on

* `com.thelightphone.sdk`: `SealedLightActivity`, `SimpleLightScreen`
* `com.thelightphone.sdk.ui`: `LightTheme`, `LightThemeController`, `LightThemeTokens`, `LightText`, `LightIcon`, `LightIcons`, `gridUnitsAsDp`, `lightClickable`, `designVerticalPxToSp`

## Pasting this in

1. Rename the `package` declaration in all four files.
2. `TimePickerScreen` extends `SimpleLightScreen<String?>` and returns the selected time as 24-hour `"HH:MM"` via `goBack(...)`, or `null` if dismissed.

> [!NOTE]
> This follows `LightThemeController` for live light/dark. If your tool is deliberately single-theme, swap that for a fixed `LightThemeColors.Dark`/`.Light` instead.

> [!NOTE]
> `use24Hour` can't read the device's actual clock-format setting yet, the SDK has no sanctioned API for it (see the TODO in `TimeFormat.kt`). Defaults to 12-hour, pass `use24Hour = true` explicitly if you need it.

> [!NOTE]
> The private `LightSizedText` helper at the bottom of `TimePickerScreen.kt` is duplicated in `date-picker` too, not shared.
