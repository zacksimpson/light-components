# Hardware Keys

Recognising the Light Phone III's wheel and two-stage camera button from raw key events — no root, no accessibility service.

---

## Files

* `LightKeys.kt`: the whole component. Resolves the wheel's optical-sensor notches and the camera's two stages into `LightKey` values, first through Light's keylayout labels and then by Linux scancode.

## Depends on

* Nothing but the Android framework (`android.view.KeyEvent`). No SDK dependency — this works in any tool, SDK-based or sideloaded.

## Pasting this in

1. Rename the `package` declaration.
2. Override `dispatchKeyEvent` in your activity and hand each event to `LightKeys.of(event)` — it returns `null` for anything that isn't one of the five controls.
3. The scancode fallback is gated per device on purpose (a paired Bluetooth keyboard must not drive the app). Keep that gating; the `fromScanCode` path exists so it can be tested on the JVM.
4. `wheelLabelsPresent()` is a readout for a settings screen: it tells you whether this build's keylayout carries Light's labels at all.

> [!NOTE]
> The wheel is a `Pixart pat9126ja` optical sensor, **not** a rotary encoder. Each notch arrives as a discrete DOWN+UP key pair roughly 35–60 ms apart, so this is key handling, not `AXIS_SCROLL` / `onRotaryScrollEvent`.

> [!NOTE]
> `WHEEL_CCW`, `WHEEL_CW` and `WHEEL_CLICK` are not AOSP keycodes — Light added them to the kernel keylayout. Resolving by label first, then falling back to the fixed Linux scancode, covers both a build that has them and one that does not.
