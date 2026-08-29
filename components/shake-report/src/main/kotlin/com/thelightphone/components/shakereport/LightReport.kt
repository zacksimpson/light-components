package com.thelightphone.components.shakereport

/**
 * What this app is, and how it is allowed to file issues.
 *
 * **This is the one thing a consuming app must set up**, and it exists because `BuildConfig`
 * does not cross a library boundary. When the report code lived inside each app it could read
 * `BuildConfig.REPORT_TOKEN` directly; a library has its own BuildConfig, not the app's, so the
 * values have to be handed in. That turns out to be the better shape anyway — the token stays a
 * build-time secret in the app that owns it, and this module never has to know how it got there.
 *
 * Call [install] once, early, from `MainActivity.onCreate` or an `Application`:
 *
 * ```kotlin
 * LightReport.install(
 *     context = this,
 *     appName = "MyApp",
 *     label = "myapp",
 *     token = BuildConfig.REPORT_TOKEN,
 * )
 * ```
 *
 * [install] also arms the crash handler, so there is exactly one call to make and no way to end
 * up with reporting that works except for crashes.
 *
 * Not calling it at all is a supported state: the detector never fires a callback, the crash
 * handler is never armed, and nothing is queued. An app that has not opted in pays nothing.
 */
object LightReport {

    /** How the app names itself in an issue title — what it is called on the phone. */
    @Volatile
    var appName: String = ""
        private set

    /** The triage label — one per app, lower case. */
    @Volatile
    var label: String = ""
        private set

    /** `owner/repo` the issues are filed into. Empty until a consuming app sets it. */
    @Volatile
    var repo: String = ""
        private set

    @Volatile
    internal var token: String = ""
        private set

    /** False until [install] has been called. Everything in the module no-ops until it is. */
    @Volatile
    var installed: Boolean = false
        private set

    /**
     * @param token usually `BuildConfig.REPORT_TOKEN`. Blank is fine and normal — a build with
     *   no key still collects reports, queues them on disk, and sends them from a later build
     *   that has one.
     */
    @JvmStatic
    @JvmOverloads
    fun install(
        context: android.content.Context,
        appName: String,
        label: String,
        token: String,
        repo: String = "",
    ) {
        this.appName = appName
        this.label = label
        this.token = token
        this.repo = repo
        this.installed = true
        CrashLog.install(context)
    }
}

/**
 * Where the app was when it went wrong.
 *
 * A single field rather than anything passed down: the crash handler runs on a dying thread that
 * has no view of the composition, and a report is worth far more with "standings" on it than
 * without. Written from the navigation, read from anywhere, so it is deliberately volatile.
 */
object ReportContext {
    @Volatile
    var screen: String = "home"
}
