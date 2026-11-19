package com.zainab.notely.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Reminder
import com.zainab.notely.util.ReminderFormat
import com.zainab.notely.util.ReminderGrouper
import kotlinx.coroutines.launch

sealed class ReminderListItem {
    enum class Status { OVERDUE, TODAY, UPCOMING, COMPLETED }
    data class Header(val title: String, val isOverdue: Boolean) : ReminderListItem()
    data class Row(val reminder: Reminder, val meta: String, val status: Status) : ReminderListItem()
}

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).reminderDao()
    private val reminders: LiveData<List<Reminder>> = dao.observeAll()
    private val now = MutableLiveData(System.currentTimeMillis())

    // Regroups whenever the rows change OR the clock moves on
    val items: LiveData<List<ReminderListItem>> =
        MediatorLiveData<List<ReminderListItem>>().also { mediator ->
            fun recompute() {
                val list = reminders.value ?: return
                mediator.value = buildItems(list, now.value ?: System.currentTimeMillis())
            }
            mediator.addSource(reminders) { recompute() }
            mediator.addSource(now) { recompute() }
        }

    fun refreshNow() {
        now.value = System.currentTimeMillis()
    }

    fun setCompleted(reminder: Reminder, completed: Boolean) {
        viewModelScope.launch { dao.setCompleted(reminder.id, completed) }
    }

    private fun buildItems(list: List<Reminder>, now: Long): List<ReminderListItem> {
        val groups = ReminderGrouper.group(list, now)
        return buildList {
            fun section(title: String, overdue: Boolean, status: ReminderListItem.Status, rows: List<Reminder>) {
                if (rows.isEmpty()) return
                add(ReminderListItem.Header(title, overdue))
                rows.forEach { add(ReminderListItem.Row(it, ReminderFormat.meta(it.dueAt, now), status)) }
            }
            section("OVERDUE", true, ReminderListItem.Status.OVERDUE, groups.overdue)
            section("TODAY", false, ReminderListItem.Status.TODAY, groups.today)
            section("UPCOMING", false, ReminderListItem.Status.UPCOMING, groups.upcoming)
            section("COMPLETED", false, ReminderListItem.Status.COMPLETED, groups.completed)
        }
    }
}