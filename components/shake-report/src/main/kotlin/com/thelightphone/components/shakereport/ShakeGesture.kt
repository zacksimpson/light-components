package com.thelightphone.components.shakereport

/**
 * Was that a shake, or was that a pocket?
 *
 * The question is harder than it looks, because the accelerometer cannot tell the two apart by
 * force alone: setting the phone down hard, a step while it is in your hand, and a jolt in a bag
 * all clear any threshold a real shake clears. What only a shake does is *reverse*. So this
 * counts sign changes in the deviation from rest rather than peaks: during a shake the magnitude
 * swings above 1g as the arm drives the phone and below 1g as it lets go at the end of each
 * throw, so a deliberate rattle alternates several times in about a second, and walking — a slow
 * bob well inside the threshold — never alternates at all.
 *
 * Deliberately free of Android imports so the arithmetic can be tested on the JVM; the sensor
 * plumbing lives in [ShakeDetector].
 */
class ShakeGesture(
    /**
     * How far from rest, in g, before a sample counts as part of a throw at all.
     *
     * A brisk walk peaks around 0.3g, which is what sets the floor. Everything above that is
     * a deliberate movement of the wrist.
     */
    private val thresholdG: Float = 0.46f,
    /**
     * Four alternations — two quick shakes, there and back and there and back.
     *
     * Six past 0.55g was something you had to *mean*, hard, twice, before anything happened;
     * three past 0.38g went off on its own. Four past 0.46g is a movement you would not make by
     * accident but would not think twice about making on purpose.
     */
    private val reversalsToFire: Int = 4,
    /** A reversal this long after the last one starts a new gesture instead of joining it. */
    private val gapMs: Long = 500,
    /** Nothing fires again for this long: one shake should not become three reports. */
    private val cooldownMs: Long = 3_000,
) {
    private var lastSign = 0
    private var lastAt = 0L
    private var reversals = 0
    private var firedAt = 0L

    val turns: Int get() = reversals
    val turnsNeeded: Int get() = reversalsToFire

    /** Forget a half-finished gesture — on resume, or once the sheet is up. */
    fun reset() {
        lastSign = 0
        lastAt = 0L
        reversals = 0
    }

    /**
     * @param magnitudeG length of the acceleration vector in g, so ~1.0 lying on a table.
     * @return true exactly once per completed shake.
     */
    fun sample(atMs: Long, magnitudeG: Float): Boolean {
        // Not `atMs - firedAt`: firedAt is 0 until the first fire, and on a device where
        // the clock starts near zero that subtraction is meaningless rather than large.
        if (firedAt != 0L && atMs - firedAt < cooldownMs) return false

        val deviation = magnitudeG - 1f
        if (deviation > -thresholdG && deviation < thresholdG) return false

        val sign = if (deviation > 0f) 1 else -1
        // One throw of the arm holds its sign across many samples at 50Hz; only the turn
        // at the end of it is news.
        if (sign == lastSign) {
            lastAt = atMs
            return false
        }
        // A slow wave of the phone reverses too, just lazily. The reversals have to come
        // one on top of the other to be a rattle.
        if (lastSign != 0 && atMs - lastAt > gapMs) reversals = 0

        lastSign = sign
        lastAt = atMs
        reversals++
        if (reversals < reversalsToFire) return false

        firedAt = atMs
        reset()
        return true
    }
}
