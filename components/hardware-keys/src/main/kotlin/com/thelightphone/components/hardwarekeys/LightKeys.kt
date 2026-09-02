package com.thelightphone.components.hardwarekeys

import android.view.KeyEvent

/** The physical controls the LPIII sends to whichever app has focus. */
enum class LightKey {
    /** Wheel turned towards the top of the phone. */
    WheelUp,

    /** Wheel turned towards the bottom of the phone. */
    WheelDown,

    /** Wheel pressed in — the flashlight press on the home screen. */
    WheelClick,

    /** Camera button, second stage. This is the shutter. */
    Camera,

    /** Camera button, first stage. Arrives paired with [Camera], order not guaranteed. */
    Focus,
}

/**
 * Recognising the LPIII's wheel and camera button.
 *
 * The wheel is not a rotary encoder. It is a `Pixart pat9126ja` optical sensor on
 * `/dev/input/event4` that emits one discrete DOWN+UP key pair per notch, roughly 35–60 ms
 * apart, so this is key handling and not `AXIS_SCROLL` / `onRotaryScrollEvent`.
 *
 * Light patched `/system/usr/keylayout/Generic.kl` — the layout every input device on the
 * phone loads — to relabel five scancodes:
 *
 * ```
 * key 19    WHEEL_CCW      # wheel up      (Pixart, was R)
 * key 20    WHEEL_CW       # wheel down    (Pixart, was T)
 * key 66    WHEEL_CLICK    # wheel press   (gpio-keys, was F8)
 * key 80    FOCUS          # camera stage 1 (gpio-keys, was NUMPAD_2)
 * key 27    CAMERA         # camera stage 2 (gpio-keys, was RIGHT_BRACKET)
 * ```
 *
 * Nothing intercepts these in `PhoneWindowManager`; they are dispatched to the focused
 * window like any other key. For most sideloaded apps that is a nuisance. For this one it
 * is the entire point: a real two-stage shutter release arrives here as two ordinary key
 * events, with no root and no accessibility service.
 *
 * `WHEEL_CCW`, `WHEEL_CW` and `WHEEL_CLICK` are not AOSP keycodes; Light added them, so
 * their integer values are Light's to change. Hence two ways in, in order:
 *
 *  1. Resolve the label to a keycode at runtime. [KeyEvent.keyCodeFromString] reads the
 *     same native label table the keylayout parser uses, so Light's additions resolve.
 *  2. Fall back to the raw Linux scancode, which is fixed by the hardware. Scancode 19 is
 *     also `r` on a Bluetooth keyboard, so that path is gated on the device name.
 */
object LightKeys {

    // Linux scancodes, from `getevent -pl`. These are hardware, not software.
    private const val SCAN_WHEEL_UP = 19 // KEY_R
    private const val SCAN_WHEEL_DOWN = 20 // KEY_T
    private const val SCAN_WHEEL_CLICK = 66 // KEY_F8
    private const val SCAN_FOCUS = 80 // KEY_KP2
    private const val SCAN_CAMERA = 27 // KEY_RIGHTBRACE

    /**
     * Which physical device is allowed to claim which scancode.
     *
     * Per scancode rather than one shared set of trusted device names, and that is not
     * fussiness. The turns come from the optical sensor and everything else from the board's
     * button device, so a shared set lets either device claim any of the five codes — and these
     * are ordinary keyboard codes underneath: 19 is `r`, 20 is `t`, 66 is F8. One shared set
     * means a paired Bluetooth keyboard whose name happened to match could rack the zoom.
     *
     * The board's name is matched by **prefix**, which is the half that actually bites. That
     * name is the kernel's, and vendors spell it `gpio-keys`, `gpio_keys` or `gpio-keys-wheel`
     * depending on the devicetree. An exact match against `"gpio-keys"` fails on a build that
     * spells it either of the other ways, and the failure is total and silent: the wheel click
     * simply never arrives, which reads as an app ignoring the button rather than as a device
     * name not matching a string. Prefix matching was arrived at the hard way — a build that
     * matched exactly quietly dropped every wheel click.
     */
    private data class Control(val key: LightKey, val fromDevice: (String) -> Boolean)

    private val PIXART: (String) -> Boolean = { it == "Pixart pat9126ja" }

    private val GPIO: (String) -> Boolean = { it.startsWith("gpio", ignoreCase = true) }

    private val byScanCode = mapOf(
        SCAN_WHEEL_UP to Control(LightKey.WheelUp, PIXART),
        SCAN_WHEEL_DOWN to Control(LightKey.WheelDown, PIXART),
        SCAN_WHEEL_CLICK to Control(LightKey.WheelClick, GPIO),
        SCAN_FOCUS to Control(LightKey.Focus, GPIO),
        SCAN_CAMERA to Control(LightKey.Camera, GPIO),
    )

    private val byKeyCode: Map<Int, LightKey> = buildMap {
        putLabel("WHEEL_CCW", LightKey.WheelUp)
        putLabel("WHEEL_CW", LightKey.WheelDown)
        putLabel("WHEEL_CLICK", LightKey.WheelClick)
        putLabel("FOCUS", LightKey.Focus)
        putLabel("CAMERA", LightKey.Camera)
    }

    private fun MutableMap<Int, LightKey>.putLabel(label: String, key: LightKey) {
        val code = runCatching { KeyEvent.keyCodeFromString(label) }
            .getOrDefault(KeyEvent.KEYCODE_UNKNOWN)
        if (code != KeyEvent.KEYCODE_UNKNOWN) put(code, key)
    }

    /** Which control produced [event], or null if it wasn't one of ours. */
    fun of(event: KeyEvent): LightKey? {
        byKeyCode[event.keyCode]?.let { return it }
        // Either the labels moved or this build doesn't have them. Trust the scancode,
        // but only from the two devices that physically own these controls — otherwise a
        // paired keyboard's `r` would rack the zoom.
        val device = event.device?.name ?: return null
        val control = byScanCode[event.scanCode] ?: return null
        return if (control.fromDevice(device)) control.key else null
    }

    /**
     * Which control a scancode from a named device resolves to, label table aside.
     *
     * Exposed only so the device gating can be tested on the JVM: [of] needs a real [KeyEvent],
     * which needs a device, which needs a phone.
     */
    fun fromScanCode(scanCode: Int, deviceName: String): LightKey? {
        val control = byScanCode[scanCode] ?: return null
        return if (control.fromDevice(deviceName)) control.key else null
    }

    /** True if this build maps the wheel labels at all — useful for a settings readout. */
    fun wheelLabelsPresent(): Boolean = byKeyCode.containsValue(LightKey.WheelUp)
}
