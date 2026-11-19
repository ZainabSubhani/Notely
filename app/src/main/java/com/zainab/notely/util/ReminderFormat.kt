package com.zainab.notely.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReminderFormat {

    fun time(millis: Long): String =
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))

    fun date(millis: Long): String =
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(millis))

    /** "18:00", "Tomorrow · 09:00", "Yesterday · 10:00", or "Sat, Nov 21 · 09:00". */
    fun meta(dueAt: Long, now: Long): String {
        val t = time(dueAt)
        val today = ReminderGrouper.startOfDay(now, 0)
        val tomorrow = ReminderGrouper.startOfDay(now, 1)
        val dayAfter = ReminderGrouper.startOfDay(now, 2)
        val yesterday = ReminderGrouper.startOfDay(now, -1)
        return when {
            dueAt in today until tomorrow -> t
            dueAt in tomorrow until dayAfter -> "Tomorrow · $t"
            dueAt in yesterday until today -> "Yesterday · $t"
            else -> "${date(dueAt)} · $t"
        }
    }
}