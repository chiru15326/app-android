package com.example.mystudyplanner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val CHANNEL_ID = "study_reminders"

    fun scheduleNext(context: Context) {
        val prefs = StudyPrefs(context)
        if (!prefs.remindersEnabled()) {
            WorkManager.getInstance(context).cancelUniqueWork("daily_study_reminder")
            return
        }
        val target = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.of(prefs.reminderHour(), prefs.reminderMinute()))
        val next = if (target.isAfter(LocalDateTime.now())) target else target.plusDays(1)
        val delay = Duration.between(LocalDateTime.now(), next).toMillis().coerceAtLeast(1000)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "daily_study_reminder",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Study reminders", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Daily reminders to continue your study plan"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = StudyPrefs(applicationContext)
        if (!prefs.remindersEnabled()) return Result.success()
        ReminderScheduler.createChannel(applicationContext)
        val allowed = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (allowed) {
            val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time for your .NET Full Stack study")
                .setContentText("Open My Study Planner and continue today's lesson.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            try {
                NotificationManagerCompat.from(applicationContext).notify(1001, notification)
            } catch (_: SecurityException) {
                // Notification permission may have been revoked after the permission check.
            }
        }
        ReminderScheduler.scheduleNext(applicationContext)
        return Result.success()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) ReminderScheduler.scheduleNext(context)
    }
}
