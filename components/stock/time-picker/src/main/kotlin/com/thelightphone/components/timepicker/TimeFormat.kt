package com.thelightphone.components.timepicker

/**
 * "HH:MM" 24h -> "h:mm AM/PM" (or unchanged when use24Hour is true).
 * TODO: use24Hour should read the device's clock format, but the SDK has no sanctioned
 * API for it yet (android.text.format.DateFormat.is24HourFormat needs a Context, and
 * android.content.Context is a blocked import), defaults to 12-hour until then.
 */
fun formatTime(time24: String, use24Hour: Boolean = false): String {
    if (use24Hour) return time24
    val (hStr, mStr) = time24.split(":")
    val h = hStr.toInt()
    val ampm = if (h >= 12) "PM" else "AM"
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$h12:$mStr $ampm"
}

/**
 * TimePicker digits ("HMM" or "HHMM") + AM/PM -> "HH:MM" 24h for storage. Always applies
 * the 12h->24h hour adjustment when [use24Hour] is false, regardless of digit count.
 */
fun digitsToTime(digits: String, ampm: String, use24Hour: Boolean = false): String {
    var h: Int
    val m: String
    if (digits.length == 3) {
        h = digits[0].digitToInt()
        m = digits.substring(1)
    } else {
        h = digits.substring(0, 2).toInt()
        m = digits.substring(2, 4)
    }
    if (!use24Hour) {
        if (ampm == "PM" && h != 12) h += 12
        if (ampm == "AM" && h == 12) h = 0
    }
    return "${h.toString().padStart(2, '0')}:$m"
}

/** "HH:MM" 24h -> TimePicker digits + AM/PM, for seeding the picker from an existing time. */
fun timeToDisplayParts(time24: String, use24Hour: Boolean = false): Pair<String, String> {
    val (hStr, mStr) = time24.split(":")
    if (use24Hour) return Pair("$hStr$mStr", "AM")
    var h = hStr.toInt()
    val ampm = if (h >= 12) "PM" else "AM"
    if (h > 12) h -= 12
    if (h == 0) h = 12
    return Pair("${h.toString().padStart(2, '0')}$mStr", ampm)
}
