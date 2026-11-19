package com.zainab.notely.util

import com.zainab.notely.data.local.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderGrouperTest {

    private fun at(day: Int, hour: Int, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, day, hour, minute, 0)
        }.timeInMillis

    private val now = at(19, 12)   // 19 Nov 2026, 12:00

    private fun r(id: Int, due: Long, done: Boolean = false) =
        Reminder(id = id, title = "R$id", dueAt = due, isCompleted = done, createdAt = 0L)

    @Test
    fun earlierTodayIsOverdue() {
        val g = ReminderGrouper.group(listOf(r(1, at(19, 9))), now)
        assertEquals(listOf(1), g.overdue.map { it.id })
        assertTrue(g.today.isEmpty())
    }

    @Test
    fun yesterdayIsOverdueOldestFirst() {
        val g = ReminderGrouper.group(listOf(r(1, at(18, 10)), r(2, at(17, 10))), now)
        assertEquals(listOf(2, 1), g.overdue.map { it.id })
    }

    @Test
    fun laterTodayIsToday() {
        val g = ReminderGrouper.group(listOf(r(1, at(19, 18))), now)
        assertEquals(listOf(1), g.today.map { it.id })
    }

    @Test
    fun justBeforeMidnightIsTodayAndMidnightIsUpcoming() {
        val g = ReminderGrouper.group(listOf(r(1, at(19, 23, 59)), r(2, at(20, 0))), now)
        assertEquals(listOf(1), g.today.map { it.id })
        assertEquals(listOf(2), g.upcoming.map { it.id })
    }

    @Test
    fun completedGoesToCompletedWhateverItsTime() {
        val g = ReminderGrouper.group(listOf(r(1, at(18, 10), done = true)), now)
        assertEquals(listOf(1), g.completed.map { it.id })
        assertTrue(g.overdue.isEmpty())
    }
}