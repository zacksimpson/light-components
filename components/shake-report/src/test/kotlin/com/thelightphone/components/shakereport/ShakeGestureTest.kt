package com.thelightphone.components.shakereport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The shake gesture, on the JVM.
 *
 * This is the half of shake-to-report that decides whether anything happens at all, and it is
 * the half that cannot be tested on the phone in any repeatable way — you cannot shake a device
 * the same way twice. Keeping it free of Android imports is what makes these possible, and is
 * the reason the class is shaped the way it is.
 *
 * Samples arrive at 50Hz on the real device, so the 20ms steps below are the real cadence.
 */
class ShakeGestureTest {

    /** One throw of the arm: several samples all on the same side of rest. */
    private fun ShakeGesture.throwOnce(from: Long, sign: Int, magnitude: Float = 0.7f): Boolean {
        var fired = false
        for (i in 0 until 3) {
            if (sample(from + i * 20L, 1f + sign * magnitude)) fired = true
        }
        return fired
    }

    @Test
    fun `a deliberate rattle fires once`() {
        val g = ShakeGesture()
        var fired = 0
        var at = 1_000L
        for (i in 0 until 4) {
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) fired++
            at += 60
        }
        assertEquals(1, fired, "four alternations is one report")
    }

    @Test
    fun `walking never fires`() {
        val g = ShakeGesture()
        // A brisk walk peaks around 0.3g, under the 0.46g floor, and bobs steadily.
        var at = 0L
        repeat(200) { i ->
            assertFalse(g.sample(at, 1f + (if (i % 10 < 5) 0.28f else -0.28f)))
            at += 20
        }
    }

    @Test
    fun `a hard single knock never fires`() {
        val g = ShakeGesture()
        // Setting the phone down hard clears the threshold easily, but only once, and does
        // not reverse. This is the case force alone cannot tell from a shake.
        assertFalse(g.throwOnce(0, 1, magnitude = 3.5f))
    }

    @Test
    fun `slow waves do not accumulate`() {
        val g = ShakeGesture()
        var fired = false
        // Same four reversals, but spread past the 500ms gap so each starts a new gesture.
        var at = 0L
        repeat(8) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) fired = true
            at += 900
        }
        assertFalse(fired, "reversals more than 500ms apart are not a rattle")
    }

    @Test
    fun `the cooldown stops one shake becoming three reports`() {
        val g = ShakeGesture()
        var fired = 0
        var at = 1_000L
        // Keep rattling well past the point of firing; the cooldown should swallow the rest.
        repeat(12) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) fired++
            at += 60
        }
        assertEquals(1, fired, "one continuous rattle is one report")
    }

    /**
     * The three boundary cases that live testing turned up and a first pass missed. They matter
     * because the numbers below are the whole feature: under them nothing happens and the phone
     * looks broken, over them a pocket files issues — and none of them can be checked on a
     * device, only on the JVM with these exact values.
     */
    @Test
    fun `one turn short of the count does not fire`() {
        val g = ShakeGesture()
        var fired = false
        var at = 1_000L
        // Three alternations where four are needed. Deliberately not "almost": this is the case
        // that reads as "I shake it and nothing happens".
        repeat(3) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) fired = true
            at += 60
        }
        assertFalse(fired, "three turns is not the gesture")
        assertTrue(g.turns > 0, "but they were counted")
    }

    @Test
    fun `just under the threshold is not a turn`() {
        val g = ShakeGesture()
        var fired = false
        var at = 1_000L
        // 0.45g against a 0.46g threshold, rattled for far longer than a real shake lasts.
        repeat(12) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1, magnitude = 0.45f)) fired = true
            at += 60
        }
        assertFalse(fired, "a whole rattle below the threshold counts nothing")
        assertEquals(0, g.turns)
    }

    @Test
    fun `a second shake after the cooldown fires again`() {
        val g = ShakeGesture()
        var at = 1_000L
        var first = false
        repeat(6) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) first = true
            at += 60
        }
        assertTrue(first, "the first rattle fires")
        // Past the 3s cooldown. Someone who shakes, ignores the offer, and shakes again a few
        // seconds later means it — and used to get nothing.
        at += 4_000
        var second = false
        repeat(6) { i ->
            if (g.throwOnce(at, if (i % 2 == 0) 1 else -1)) second = true
            at += 60
        }
        assertTrue(second, "and so does the next one")
    }

    @Test
    fun `reset abandons a half finished gesture`() {
        val g = ShakeGesture()
        g.throwOnce(1_000, 1)
        g.throwOnce(1_060, -1)
        assertTrue(g.turns in 1..3, "two of four counted")
        g.reset()
        assertEquals(0, g.turns)
    }
}
