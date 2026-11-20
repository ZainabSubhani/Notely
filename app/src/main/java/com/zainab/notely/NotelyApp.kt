package com.zainab.notely

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.zainab.notely.worker.ReminderWorker

class NotelyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderWorker.CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for your reminders" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}