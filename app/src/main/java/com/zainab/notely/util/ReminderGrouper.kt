package com.zainab.notely.util

import com.zainab.notely.data.local.Reminder
import java.util.Calendar

object ReminderGrouper {

    data class Groups(
        val overdue: List<Reminder>,
        val today: List<Reminder>,
        val upcoming: List<Reminder>,
        val completed: List<Reminder>
    )

    /** Midnight at the start of the day [offsetDays] away from the day containing [millis]. */
    fun startOfDay(millis: Long, offsetDays: Int = 0): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }.timeInMillis

    fun group(reminders: List<Reminder>, now: Long): Groups {
        val tomorrow = startOfDay(now, 1)
        val (done, active) = reminders.partition { it.isCompleted }
        return Groups(
            overdue = active.filter { it.dueAt < now }.sortedBy { it.dueAt },
            today = active.filter { it.dueAt >= now && it.dueAt < tomorrow }.sortedBy { it.dueAt },
            upcoming = active.filter { it.dueAt >= tomorrow }.sortedBy { it.dueAt },
            completed = done.sortedByDescending { it.dueAt }
        )
    }
}