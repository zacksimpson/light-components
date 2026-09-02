package com.thelightphone.components.shakereport

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The last crash, kept until somebody has read it.
 *
 * A sideloaded app on a phone with no developer tools to hand is a black box: it either works or
 * it "just closes", and the stack trace — the one piece of information that would settle it in a
 * second — is in a logcat nobody has a cable for. So the handler writes it to a file, and the
 * next launch offers to send it.
 *
 * Nothing is sent from in here. Writing the file is the last thing a dying process does and it
 * has no business opening a socket; the report goes out on the next launch, from a healthy one.
 */
object CrashLog {

    private const val FILE = "last-crash.txt"

    /** Chain onto whatever was already installed rather than replacing it. */
    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { write(app, thread, error) }
            // Always hand on: swallowing it would leave the process wedged instead of dying,
            // which is worse than crashing and is not this object's decision to make.
            previous?.uncaughtException(thread, error)
        }
    }

    /** The stored trace, or null when the last run ended the way it was supposed to. */
    fun read(context: Context): String? =
        file(context).takeIf { it.exists() }?.runCatching { readText() }?.getOrNull()

    /**
     * The trace, but only the first time anything asks in this process.
     *
     * The offer has to appear once per *launch*, and an Activity is recreated for reasons that
     * are not launches: a theme or font-scale change, a configuration change, "don't keep
     * activities". The vendored copies this replaces guarded on `savedInstanceState == null`,
     * which a library composable cannot see — but a recreation stays in the same process and a
     * real launch does not, so a process-scoped flag draws the same line.
     *
     * Without it, the two apps that toggle the display's colour mode on their own lifecycle
     * re-raise "IT CRASHED · SEND?" every time the activity comes back.
     */
    fun readOnce(context: Context): String? {
        if (offered) return null
        offered = true
        return read(context)
    }

    @Volatile
    private var offered = false

    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }

    private fun write(context: Context, thread: Thread, error: Throwable) {
        val stack = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
        val at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        file(context).writeText(
            buildString {
                appendLine("at: $at")
                appendLine("thread: ${thread.name}")
                appendLine("screen: ${ReportContext.screen}")
                appendLine()
                append(stack)
            },
        )
    }

    private fun file(context: Context) = File(context.filesDir, FILE)
}
