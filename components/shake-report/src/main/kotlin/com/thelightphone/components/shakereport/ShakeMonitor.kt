package com.thelightphone.components.shakereport

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** What the accelerometer is saying right now, for a readout on a settings screen. */
data class ShakeReading(
    /** Length of the acceleration vector in g. About 1.0 lying still. */
    val magnitudeG: Float = 1f,
    /** The hardest sample of the last couple of seconds, so a flick leaves a mark to read. */
    val peakG: Float = 1f,
    /** Turns counted towards the gesture so far. */
    val turns: Int = 0,
    /** How many it takes. */
    val turnsNeeded: Int = 4,
    /** Rising count of completed shakes, so the screen can say one just landed. */
    val fires: Int = 0,
)

/**
 * A window onto the shake detector, open only while something is looking through it.
 *
 * This exists because "I shook it and nothing happened" is unanswerable from the outside: the
 * gesture either cleared a threshold or it did not, and there is no way to tell which from a
 * phone with no logcat attached. With the numbers on screen the question becomes "it peaked at
 * 1.2g and needs 1.38g", which is a thing you can act on.
 *
 * [watchers] gates the writes. The detector runs at 50Hz whenever the app is in front, and
 * publishing every one of those samples into a flow nobody collects is pure waste — so a screen
 * showing the readout calls [watch] in a `DisposableEffect` and [unwatch] on the way out.
 */
object ShakeMonitor {

    private val _reading = MutableStateFlow(ShakeReading())
    val reading: StateFlow<ShakeReading> = _reading

    @Volatile
    var watchers: Int = 0
        private set

    fun watch() {
        watchers++
    }

    fun unwatch() {
        if (watchers > 0) watchers--
    }

    fun publish(reading: ShakeReading) {
        _reading.value = reading
    }
}
