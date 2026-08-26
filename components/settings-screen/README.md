# Settings Screen

A scaffold (`LightSettingsScaffold`) plus three row types for building a settings screen out of consistently sized pieces: a top bar with an optional back button, a scrollable body, and rows for navigating to another screen, toggling a boolean, or showing/changing a current value.

---

## Row types

* `SettingsLinkRow(label, onClick, description?)`: big label, optional small description underneath. For "tap to go to another screen."
* `SettingsToggleRow(label, value, onValueChange, description?)`: toggle icon plus the same label/description sizing as the link row. For a boolean.
* `SettingsValueRow(label, value, onClick)`: small label on top, current value below in large text (label "Units", value "Fahrenheit"). For "tap to change this setting," modeled on light-sdk's own weather example. The size pairing is the opposite of the link row on purpose, the two rows serve different roles.

Text sizes are standardized on `LightTextVariant.Heading`/`Detail` throughout rather than one-off pixel sizes.

## Depends on

* `com.thelightphone.sdk.ui`: `LightTopBar`, `LightTopBarCenter`, `LightBarButton`, `LightIcon`/`LightIcons`, `LightScrollView`, `LightText`, `LightTextVariant`, `LightThemeTokens`, `gridUnitsAsDp`, `lightClickable`

## Pasting this in

1. Rename the `package` declaration in both files.
2. `LightSettingsScaffold` assumes it's already inside a `LightTheme` scope, it doesn't wrap one itself. Wrap your own screen's `Content()` in `LightTheme` first.
3. Pass `onBack = null` for a top-level tab (no back button), or a lambda for a screen pushed with `navigateTo`.

> [!NOTE]
> Horizontal padding is applied once, inside `LightScrollView`'s content, not on `LightScrollView`'s own modifier (that pulls the scrollbar in with no gutter). All three rows pad vertically only and rely on that container inset.

> [!NOTE]
> The SDK's toggle icons are `LightIcons.TOGGLE_STATE_ON`/`TOGGLE_STATE_OFF`. If you're working from an older reference that uses `TOGGLE_ON`/`TOGGLE_OFF`, that's a stale name from before an SDK rename, not a different icon.
