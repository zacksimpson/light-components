# Shake to Report

The plumbing for "shake the phone, offer to send a report": recognising a deliberate shake (without a pocket setting it off), capturing the last crash, and a config object that ties it to the app's own reporting channel.

---

## Files

* `ShakeGesture.kt`: the pure arithmetic that decides "was that a shake, or was that a pocket?" — sign reversals in the deviation from rest, not peaks. No Android imports, so the whole thing is JVM-testable.
* `ShakeDetector.kt`: the accelerometer wiring. On only while the app is in front (registered in `onResume`, dropped in `onPause`), so it is not a battery question.
* `ShakeMonitor.kt`: a window onto the detector for a settings-screen readout (`ShakeReading`), open only while something is looking — publishing 50Hz samples into a flow nobody collects is waste.
* `CrashLog.kt`: writes the last crash to a file so the next launch can offer to send it. Nothing is sent from in here; a dying process has no business opening a socket.
* `LightReport.kt`: the one thing a consuming app must set up — app name, triage label, token, repo. Also arms the crash handler.
* `ShakeGestureTest.kt`: the JVM tests for the gesture logic.

## Depends on

* `android.hardware.SensorManager` / `Sensor` (detector), `kotlinx.coroutines.flow` (monitor), Android `Context` (crash log) — all in the SDK's Android stack.
* Nothing from `com.thelightphone.sdk.ui`; this is the non-visual half of the flow, the sheet you build yourself.

## Pasting this in

1. Rename the `package` declaration in all files.
2. `LightReport.install(context, appName, label, token, repo)` once, early, from `MainActivity.onCreate` or an `Application`. `token` is usually `BuildConfig.REPORT_TOKEN`; a blank token is fine and normal — a build with no key still collects, and your app's sender can pick the token up from a later build.
3. Call `ShakeDetector.start()`/`stop()` from your activity's `onResume`/`onPause`, passing a lambda that shows your report screen.
4. Read `CrashLog.readOnce(context)` at launch to offer the previous crash; call `CrashLog.clear(context)` after it's sent.
5. For a live readout: `ShakeMonitor.watch()` in a `DisposableEffect`, collect `ShakeMonitor.reading`, `ShakeMonitor.unwatch()` on the way out.

> [!NOTE]
> Not calling `LightReport.install` is a supported state — nothing renders, nothing appears, nothing is queued. An app that has not opted in pays nothing.

> [!NOTE]
> The sheet UI, issue filing, and the GitHub integration live in the consuming app. This component is deliberately the non-visual half — the gesture, the capture, and the config — so there is no token, no repo convention, and no HTTP in here.
