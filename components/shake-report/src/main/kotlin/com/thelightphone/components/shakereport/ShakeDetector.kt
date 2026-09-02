package com.thelightphone.components.shakereport

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * The accelerometer, on only while you are looking at the app.
 *
 * Registered from `onResume` and dropped in `onPause`, which is what keeps this from being a
 * battery question at all: a 50Hz stream costs real power, and there is no case where shaking a
 * phone that is showing something else should file a report against this app.
 *
 * The decision of what counts as a shake is in [ShakeGesture] and is plain arithmetic; this
 * class only turns three floats into a magnitude and hands it over.
 */
class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private companion object {
        /** How long the peak stays on screen before it decays — long enough to look down. */
        const val PEAK_HOLD_MS = 2_000L
    }

    private val sensors = context.getSystemService(SensorManager::class.java)
    private val accelerometer = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gesture = ShakeGesture()

    /** False on a phone with no accelerometer, where the whole feature quietly does not exist. */
    val available: Boolean get() = accelerometer != null

    fun start() {
        val sensor = accelerometer ?: return
        gesture.reset()
        // GAME is 50Hz. NORMAL is 5Hz, which is slower than the gesture it has to see.
        sensors?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensors?.unregisterListener(this)
        gesture.reset()
    }

    /** Called when the sheet opens, so the shake that opened it cannot open a second one. */
    fun forget() = gesture.reset()

    // Readout state, all of it only meaningful while ShakeMonitor has a watcher.
    private var fires = 0
    private var peak = 0f
    private var peakAt = 0L
    private var sample = 0

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        // The event's own timestamp is nanoseconds since boot from the sensor hub, which is
        // the right clock here: it does not drift with the main thread being busy, and being
        // busy is exactly the state a freeze report is filed in.
        val at = event.timestamp / 1_000_000L
        val fired = gesture.sample(at, magnitude)
        if (fired) {
            fires++
            onShake()
        }

        // Nothing below this line runs unless a settings screen is displaying the readout.
        if (ShakeMonitor.watchers == 0) return
        val deviation = abs(magnitude - 1f)
        if (deviation > peak || at - peakAt > PEAK_HOLD_MS) {
            peak = deviation
            peakAt = at
        }
        // Every third sample is about 16Hz, which is faster than anyone can read and a third
        // of the recompositions.
        if (++sample % 3 != 0 && !fired) return
        ShakeMonitor.publish(
            ShakeReading(
                magnitudeG = magnitude,
                peakG = 1f + peak,
                turns = gesture.turns,
                turnsNeeded = gesture.turnsNeeded,
                fires = fires,
            ),
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
