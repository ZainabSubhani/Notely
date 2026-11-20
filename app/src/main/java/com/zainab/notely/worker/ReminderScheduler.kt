package com.zainab.notely.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun workName(reminderId: Int) = "reminder_$reminderId"

    /** Schedules (or replaces) the notification for one reminder. */
    fun schedule(context: Context, reminderId: Int, dueAt: Long) {
        val delay = (dueAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_REMINDER_ID to reminderId))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(reminderId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, reminderId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(reminderId))
    }
}