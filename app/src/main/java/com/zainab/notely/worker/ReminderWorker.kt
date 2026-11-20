package com.zainab.notely.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zainab.notely.MainActivity
import com.zainab.notely.R
import com.zainab.notely.data.local.DatabaseProvider
import com.zainab.notely.data.local.Reminder
import com.zainab.notely.util.ReminderFormat

class ReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt(KEY_REMINDER_ID, -1)
        if (id == -1) return Result.failure()

        val reminder = DatabaseProvider.getDatabase(applicationContext)
            .reminderDao().getById(id) ?: return Result.success()   // deleted meanwhile
        if (reminder.isCompleted) return Result.success()
        // Defensive: if the time was moved later, a newer job handles it
        if (reminder.dueAt > System.currentTimeMillis() + EARLY_TOLERANCE_MS) return Result.success()

        notify(reminder)
        return Result.success()
    }

    private fun notify(reminder: Reminder) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return   // the user said no: nothing to show

        val tapIntent = PendingIntent.getActivity(
            ctx,
            reminder.id,
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText("Reminder · ${ReminderFormat.time(reminder.dueAt)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(reminder.id, notification)
        } catch (_: SecurityException) {
            // permission revoked between the check and now: ignore
        }
    }

    companion object {
        const val CHANNEL_ID = "reminders"
        const val KEY_REMINDER_ID = "reminderId"
        private const val EARLY_TOLERANCE_MS = 60_000L
    }
}