package com.thelightphone.components.hardwarekeys

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The scancode fallback path, which is the only part that runs on the JVM. The keycode
 *  path needs a real [android.view.KeyEvent] and a real device name table, so it's verified
 *  on-device; this locks down the pure mapping logic. */
class LightKeysTest {

    // Wheel turns — Pixart optical sensor

    @Test
    fun `wheel up maps scancode 19 from the pixart sensor`() {
        assertEquals(LightKey.WheelUp, LightKeys.fromScanCode(19, "Pixart pat9126ja"))
    }

    @Test
    fun `wheel down maps scancode 20 from the pixart sensor`() {
        assertEquals(LightKey.WheelDown, LightKeys.fromScanCode(20, "Pixart pat9126ja"))
    }

    @Test
    fun `wheel scancodes do not map from a keyboard`() {
        // 19 is also KEY_R on a Bluetooth keyboard. Without the device gate, a keyboard
        // press would rack the zoom.
        assertNull(LightKeys.fromScanCode(19, "Bluetooth Keyboard"))
        assertNull(LightKeys.fromScanCode(20, "Bluetooth Keyboard"))
    }

    @Test
    fun `wheel click maps scancode 66 from a gpio device`() {
        assertEquals(LightKey.WheelClick, LightKeys.fromScanCode(66, "gpio-keys"))
    }

    // Camera button — gpio-keys board device

    @Test
    fun `camera stage two maps scancode 27 from gpio`() {
        assertEquals(LightKey.Camera, LightKeys.fromScanCode(27, "gpio-keys"))
    }

    @Test
    fun `camera stage one maps scancode 80 from gpio`() {
        assertEquals(LightKey.Focus, LightKeys.fromScanCode(80, "gpio-keys"))
    }

    // Device-name prefix matching is deliberate: the kernel spells it gpio-keys,
    // gpio_keys, or gpio-keys-wheel depending on the devicetree.

    @Test
    fun `gpio device names match by prefix regardless of separator`() {
        assertEquals(LightKey.WheelClick, LightKeys.fromScanCode(66, "gpio_keys"))
        assertEquals(LightKey.Camera, LightKeys.fromScanCode(27, "gpio-keys-wheel"))
    }

    @Test
    fun `unrecognised scancode from a trusted device is null`() {
        assertNull(LightKeys.fromScanCode(31, "Pixart pat9126ja")) // KEY_S
        assertNull(LightKeys.fromScanCode(999, "gpio-keys"))
    }

    @Test
    fun `untrusted device cannot claim any scancode`() {
        assertNull(LightKeys.fromScanCode(66, "Some Vendor Keyboard"))
        assertNull(LightKeys.fromScanCode(27, "Some Vendor Keyboard"))
        assertNull(LightKeys.fromScanCode(80, "Some Vendor Keyboard"))
    }

    @Test
    fun `unknown device name resolves nothing`() {
        assertNull(LightKeys.fromScanCode(19, ""))
        assertNull(LightKeys.fromScanCode(19, "unknown"))
    }
}
