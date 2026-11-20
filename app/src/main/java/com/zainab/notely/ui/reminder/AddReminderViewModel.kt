package com.zainab.notely.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Reminder
import kotlinx.coroutines.launch
import java.util.Calendar
import com.zainab.notely.worker.ReminderScheduler

class AddReminderViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).reminderDao()
    private val reminderId: Int = savedStateHandle.get<Int>("reminderId") ?: -1
    val isEditing: Boolean = reminderId != -1

    private var originalDueAt: Long? = null
    private var saving = false
    var deleted = false                 // so the screen knows not to ask for permission after a delete
    var askedForPermission = false
    var permissionAnswered = false
    private val _dueAt = MutableLiveData(defaultDueAt())
    val dueAt: LiveData<Long> = _dueAt

    private val _loaded = MutableLiveData<Reminder?>()
    val loadedReminder: LiveData<Reminder?> = _loaded

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    init {
        if (isEditing) {
            viewModelScope.launch {
                val reminder = dao.getById(reminderId)
                if (reminder != null) {
                    originalDueAt = reminder.dueAt
                    _dueAt.value = reminder.dueAt
                }
                _loaded.value = reminder
            }
        }
    }

    // The next full hour: 4:20 becomes 5:00
    private fun defaultDueAt(): Long = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun setDate(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _dueAt.value ?: defaultDueAt() }
        cal.set(year, month, day)
        _dueAt.value = cal.timeInMillis
        _error.value = null
    }

    fun setTime(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _dueAt.value ?: defaultDueAt() }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        _dueAt.value = cal.timeInMillis
        _error.value = null
    }

    fun clearError() {
        if (_error.value != null) _error.value = null
    }

    fun save(rawTitle: String) {
        val title = rawTitle.trim()
        if (title.isEmpty()) {
            _error.value = "Enter what to remind you about"
            return
        }
        val due = _dueAt.value ?: return
        // An already-overdue reminder can be renamed without moving its time
        val timeChanged = !isEditing || due != originalDueAt
        if (timeChanged && due <= System.currentTimeMillis()) {
            _error.value = "Pick a time in the future"
            return
        }
        if (saving) return
        saving = true
        viewModelScope.launch {
            if (isEditing) {
                val existing = dao.getById(reminderId)
                if (existing != null) {
                    dao.update(existing.copy(title = title, dueAt = due))
                    if (!existing.isCompleted) scheduleIfFuture(reminderId, due)
                }
            } else {
                val newId = dao.insert(
                    Reminder(title = title, dueAt = due, createdAt = System.currentTimeMillis())
                ).toInt()
                scheduleIfFuture(newId, due)
            }
            _saved.value = true
        }
    }

    fun delete() {
        if (!isEditing) return
        deleted = true
        viewModelScope.launch {
            ReminderScheduler.cancel(getApplication<Application>(), reminderId)
            dao.deleteById(reminderId)
            _saved.value = true   // same signal: the screen closes
        }
    }

    // A time already in the past would fire immediately, so only future times are scheduled
    private fun scheduleIfFuture(id: Int, due: Long) {
        if (due > System.currentTimeMillis()) {
            ReminderScheduler.schedule(getApplication<Application>(), id, due)
        } else {
            ReminderScheduler.cancel(getApplication<Application>(), id)
        }
    }
}