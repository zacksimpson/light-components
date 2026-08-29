# Wheel

Turning the Light Phone III's hardware wheel into scrolling, or into discrete steps. Compose-native: the bus is a flow, gates nest, and dialogs keep working without any activity plumbing.

---

## Files

* `Wheel.kt`: the whole component — `WheelBus`, `WheelGate`, `WheelScroll` (Compose and WebView variants), `WheelSteps`, `WheelTurns`, `WheelInDialog`.

## Depends on

* `androidx.compose.*`: `foundation` (`ScrollableState`), `runtime`, `ui`.
* `kotlinx-coroutines`: `channels`, `flow`.
* The `hardware-keys` component for `LightKeys.of` / `LightKey` — the wheel arrives as raw key events.

## Pasting this in

1. Copy the whole `src` folder, plus the `hardware-keys` component.
2. Rename the `package` declarations.
3. Provide a `WheelBus` and put it in a `CompositionLocal` (`LocalWheelBus`). The activity's `dispatchKeyEvent` feeds it: `bus.send(...)` for `LightKey.WheelUp` / `WheelDown`.
4. Call `WheelScroll(state)` inside any scrollable screen. The notch-to-pixels and smoothing constants are at the top of the file — the comments there explain each one.
5. For a modal: call `WheelInDialog()` once inside the dialog content so the wheel keeps working while the sheet is up.

> [!NOTE]
> `LocalWheelBus` defaults to `null` and everything silently no-ops without one, so a screen that forgets to wire the wheel just doesn't scroll — it doesn't crash.

> [!NOTE]
> The wheel is not a rotary encoder. It's an optical sensor emitting a DOWN+UP pair per notch (~35–60 ms), so it arrives as `KeyEvent`s, not `AXIS_SCROLL`.
