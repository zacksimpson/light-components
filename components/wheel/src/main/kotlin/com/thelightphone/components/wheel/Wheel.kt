package com.thelightphone.components.wheel

import android.view.KeyEvent
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.webkit.WebView
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.thelightphone.components.hardwarekeys.LightKey
import com.thelightphone.components.hardwarekeys.LightKeys
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Wheel notches on their way from the activity to whatever is on screen.
 *
 * One notch per event, positive for up. The activity is the only thing that can see the
 * key — a `dispatchKeyEvent` override is what lets it win against a focused WebView — but
 * only the current screen knows what scrolling means, so the two are joined by a flow
 * rather than by the activity reaching into the UI.
 *
 * A [SharedFlow] with no replay, deliberately: a notch that arrives while nothing is
 * listening is gone, which is what you want. Buffered generously because the sensor emits
 * bursts far faster than a frame.
 */
class WheelBus {
    private val _notches = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    private val _pressedNotches = MutableSharedFlow<Int>(extraBufferCapacity = 64)

    /** Bare turns. */
    val notches: SharedFlow<Int> = _notches.asSharedFlow()

    /**
     * Turns made with the wheel held in.
     *
     * A separate flow rather than a flag on the same one, because the two are different
     * controls to the screen receiving them: in a camera viewfinder a bare turn is zoom and a
     * pressed turn is exposure compensation, and a screen that only wants one of them should
     * not have to filter the other out of its own callback.
     */
    val pressedNotches: SharedFlow<Int> = _pressedNotches.asSharedFlow()

    fun send(notches: Int, pressed: Boolean = false) {
        if (pressed) _pressedNotches.tryEmit(notches) else _notches.tryEmit(notches)
    }
}

/**
 * Turn the wheel on or off for everything composed inside.
 *
 * Nests: an inner value replaces an outer one for its own subtree, so a modal can kill the
 * wheel without knowing which screen is underneath it. Composition only — no layout node, so
 * it cannot affect measurement.
 *
 * Defaults to on, so an app that never calls this is unaffected.
 */
@Composable
fun WheelGate(active: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWheelActive provides active, content = content)
}

private val LocalWheelActive = compositionLocalOf { true }

val LocalWheelBus = staticCompositionLocalOf<WheelBus?> { null }

/**
 * Distance per notch. About six notches to a screenful on the LPIII panel — enough that a
 * flick of the wheel moves you somewhere, short enough that you can land on a paragraph.
 */
private val NOTCH = 64.dp

/**
 * Which way a notch moves the page.
 *
 * `1` means turning the wheel up moves you *down* the document — the wheel drags the page
 * the way a finger flick does, rather than moving a viewport over it. Flip to `-1` for the
 * mouse-wheel convention.
 */
private const val DIRECTION = 1

/**
 * Fraction of the remaining distance applied per frame.
 *
 * This is the whole reason scrolling feels like scrolling rather than like a slide
 * projector. The sensor fires a notch every ~35 ms, which is faster than a frame, so
 * applying each one on arrival produces a stack of instant jumps — nothing to follow with
 * your eye. Instead every notch adds to a debt, and each frame pays off a share of it, so
 * one notch glides and a fast spin becomes a single continuous sweep that keeps moving
 * slightly after your thumb stops.
 *
 * 0.28 settles ~90% inside seven frames: quick enough to feel direct, slow enough to read.
 */
private const val SMOOTHING = 0.28f

/**
 * Notches needed to start scrolling, and how long a turn stays live.
 *
 * The wheel sits under a thumb and catches stray brushes, and one stray notch used to be a
 * scroll. So the first notch after a pause buys nothing on its own: it is remembered, and
 * only a second notch releases both. Once turning, everything applies immediately until
 * [IDLE_MS] passes with the wheel still, at which point the guard comes back.
 *
 * 1.5 s is deliberately long. It has to cover deliberate-but-slow turning, and the cost of
 * it being too long is nil — you are turning the wheel, so the next notch re-arms it.
 */
private const val ARM_NOTCHES = 2
private const val IDLE_MS = 1_500L

/**
 * Point the wheel at a Compose scroller. Works for both `ScrollState` and `LazyListState`.
 */
@Composable
fun WheelScroll(state: ScrollableState, active: Boolean = true, reverse: Boolean = false) {
    val step = with(LocalDensity.current) { NOTCH.toPx() }
    val debt = remember { Debt() }
    val direction = if (reverse) -DIRECTION else DIRECTION
    val wake = remember { Channel<Unit>(Channel.CONFLATED) }

    ArmedNotches(active) { notches ->
        debt.px += notches * step * direction
        wake.trySend(Unit)
    }

    LaunchedEffect(state, wake) {
        while (true) {
            // Suspends while the wheel is still, so an idle screen costs nothing.
            wake.receive()
            // One scroll session for the whole glide. A finger on the screen takes priority
            // and cancels this, which is the right outcome.
            state.scroll {
                while (abs(debt.px) > 0.5f) {
                    withFrameNanos { }
                    val wanted = (debt.px * SMOOTHING).let {
                        // Never stall a notch out in sub-pixel increments.
                        if (abs(it) < 1f) debt.px else it
                    }
                    debt.px -= wanted
                    val consumed = scrollBy(wanted)
                    // At the top or bottom the rest of the debt is unpayable, and keeping
                    // it would mean the next turn back spends its first notches on nothing.
                    if (abs(consumed) < abs(wanted) - 0.5f) debt.px = 0f
                }
            }
        }
    }
}

/** The same, for the reader's WebView, which Compose knows nothing about. */
@Composable
fun WheelScroll(web: WebView?, active: Boolean = true, reverse: Boolean = false) {
    val step = with(LocalDensity.current) { NOTCH.toPx() }
    val debt = remember { Debt() }
    val direction = if (reverse) -DIRECTION else DIRECTION
    val wake = remember { Channel<Unit>(Channel.CONFLATED) }

    ArmedNotches(active && web != null) { notches ->
        debt.px += notches * step * direction
        wake.trySend(Unit)
    }

    LaunchedEffect(web, wake) {
        val target = web ?: return@LaunchedEffect
        while (true) {
            wake.receive()
            while (abs(debt.px) > 0.5f) {
                withFrameNanos { }
                val wanted = (debt.px * SMOOTHING).let {
                    if (abs(it) < 1f) debt.px else it
                }
                debt.px -= wanted
                if (!target.wheelScrollBy(wanted.toInt())) debt.px = 0f
            }
        }
    }
}

/**
 * Point the wheel at discrete targets instead of at a list: one step, one action.
 *
 * Used where turning the wheel walks a focus up and down — a remote control, a filter
 * carousel, a settings list. The sensor fires every 35–60 ms, which is far quicker than
 * anyone can read a moving highlight, so notches are banked and paid out no faster than
 * [minIntervalMs]. Without that a flick of the thumb sends a dozen presses and the selection
 * ends up somewhere nobody chose.
 *
 * The bank is clamped for the same reason: after a hard spin the tail of unpaid notches would
 * otherwise keep walking the focus for a second after the thumb stopped.
 */
@Composable
fun WheelSteps(
    active: Boolean = true,
    notchesPerStep: Int = NOTCHES_PER_STEP,
    minIntervalMs: Long = 110,
    onStep: (Int) -> Unit,
) {
    val handler by rememberUpdatedState(onStep)
    val bank = remember { Debt() }
    val wake = remember { Channel<Unit>(Channel.CONFLATED) }

    ArmedNotches(active) { notches ->
        bank.px = (bank.px + notches).coerceIn(-MAX_BANKED_NOTCHES, MAX_BANKED_NOTCHES)
        wake.trySend(Unit)
    }

    LaunchedEffect(wake, notchesPerStep, minIntervalMs) {
        while (true) {
            wake.receive()
            while (abs(bank.px) >= notchesPerStep) {
                val direction = bank.px.sign
                bank.px -= direction * notchesPerStep
                handler(direction.toInt())
                delay(minIntervalMs)
            }
            // The remainder is kept, not discarded. Zeroing it would mean a wheel turned one
            // notch at a time never moved anything at all, since no single notch reaches the
            // threshold on its own.
        }
    }
}

/**
 * Notches the wheel has to travel to move the focus one row.
 *
 * One-to-one was the obvious first guess and it overshot by exactly double: the sensor is
 * optical, so a "notch" is a sampling artefact rather than a detent you can feel, and what
 * reads as one flick of the thumb is two of them.
 */
private const val NOTCHES_PER_STEP = 2

/**
 * Notches worth banking mid-spin — four rows' worth. Beyond this the focus keeps travelling
 * after the thumb has stopped, which feels like the remote arguing with you.
 */
private const val MAX_BANKED_NOTCHES = 8f

/**
 * The wheel inside a dialog or a bottom sheet.
 *
 * A Compose `Dialog` — and a Material `ModalBottomSheet`, which is one underneath — is a
 * window of its own, with its own `ViewRootImpl`. Keys go to whichever window has focus, so
 * while a sheet is up the activity's `dispatchKeyEvent` is never called and the bus falls
 * silent. Call this once inside any dialog that holds a scroller: it borrows that window's
 * callback for as long as the dialog is on screen and feeds the same bus, then puts the
 * original callback back.
 *
 * If the window can't be found the wheel simply does nothing there, which is the same as
 * not calling this at all.
 */
@Composable
fun WheelInDialog() {
    val bus = LocalWheelBus.current ?: return
    val window = LocalView.current.dialogWindow() ?: return
    DisposableEffect(window, bus) {
        val original = window.callback ?: return@DisposableEffect onDispose { }
        window.callback = object : Window.Callback by original {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                when (LightKeys.of(event)) {
                    LightKey.WheelUp -> {
                        if (event.action == KeyEvent.ACTION_DOWN) bus.send(1)
                        return true
                    }

                    LightKey.WheelDown -> {
                        if (event.action == KeyEvent.ACTION_DOWN) bus.send(-1)
                        return true
                    }

                    else -> Unit
                }
                return original.dispatchKeyEvent(event)
            }
        }
        onDispose { window.callback = original }
    }
}

/**
 * The window a dialog's content is hosted in, or null in the activity's own window.
 *
 * Compose marks the host view of a dialog window with [DialogWindowProvider]; the whole
 * parent chain is walked rather than just the immediate parent, because how many views sit
 * between the composition and that host is an implementation detail.
 */
private fun View.dialogWindow(): Window? {
    var parent: ViewParent? = this.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) return parent.window
        parent = (parent as? View)?.parent
    }
    return null
}

/** Notches, minus the stray ones — [WheelTurns] with the guard on. See [ARM_NOTCHES]. */
@Composable
private fun ArmedNotches(active: Boolean, onNotch: (Int) -> Unit) =
    WheelTurns(active = active, armed = true, onNotch = onNotch)

/**
 * Raw notches, for a control that is not a scroller.
 *
 * [armed] false is for controls where every notch has to count and a stray one is harmless —
 * stepping through filters, say, where the worst case is one wrong name on screen. Zoom and
 * exposure use the guard, because a stray notch there changes the photo.
 *
 * [pressed] listens to turns made with the wheel held in instead of bare ones.
 *
 * Armed state lives in the effect rather than in composition state: it is a property of the
 * turn in progress, and a recomposition mid-turn should not disarm the wheel.
 */
@Composable
fun WheelTurns(
    active: Boolean = true,
    armed: Boolean = false,
    pressed: Boolean = false,
    onNotch: (Int) -> Unit,
) {
    val handler by rememberUpdatedState(onNotch)
    val bus = LocalWheelBus.current ?: return
    // A gate anywhere above this point wins. Read in composition, not in the effect, so
    // closing the gate restarts the effect rather than leaving a live collector behind.
    val live = active && LocalWheelActive.current
    LaunchedEffect(bus, live, armed, pressed) {
        if (!live) return@LaunchedEffect
        val flow = if (pressed) bus.pressedNotches else bus.notches
        if (!armed) {
            flow.collect { handler(it) }
            return@LaunchedEffect
        }
        var isArmed = false
        var held = 0
        var count = 0
        var last = 0L
        flow.collect { notches ->
            val now = System.nanoTime() / 1_000_000
            if (now - last > IDLE_MS) {
                isArmed = false
                held = 0
                count = 0
            }
            last = now
            if (isArmed) {
                handler(notches)
                return@collect
            }
            held += notches
            count++
            if (count >= ARM_NOTCHES) {
                isArmed = true
                // Release what the guard was holding, so nothing deliberate is lost.
                if (held != 0) handler(held) else handler(notches.sign)
                held = 0
            }
        }
    }
}

/**
 * Scrolling the document, bounded at both ends. Returns false at an edge, so the caller can
 * drop the rest of the debt instead of pushing against it.
 *
 * `canScrollVertically` is the public way to ask — `computeVerticalScrollRange` is protected
 * on View, and the content height is only available in CSS pixels that would have to be
 * scaled back by hand.
 */
private fun WebView.wheelScrollBy(px: Int): Boolean {
    if (px == 0) return true
    if (!canScrollVertically(if (px > 0) 1 else -1)) return false
    scrollBy(0, px)
    return true
}

/**
 * Distance still owed to the scroller.
 *
 * Deliberately not Compose state: nothing in composition reads it, and making it observable
 * would restart the glide on every recomposition it caused.
 */
private class Debt {
    @Volatile
    var px: Float = 0f
}
