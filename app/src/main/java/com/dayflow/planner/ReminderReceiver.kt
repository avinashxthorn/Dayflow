package com.dayflow.planner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val store = TaskStore(context)
        val task = store.tasks.firstOrNull { it.id == id && !it.completed } ?: return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "dayflow_reminders"
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Planning reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for your scheduled tasks and plans"
            }
        )
        val canNotify = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (canNotify) {
            val openApp = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_reminder)
                .setContentTitle(task.title)
                .setContentText(task.notes.ifBlank { "Your plan is coming up. You've got this!" })
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    task.notes.ifBlank { "Your plan is coming up. You've got this!" }
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openApp)
                .build()
            manager.notify(task.id.hashCode(), notification)
        }
        // Repeating plans remain active; their next occurrence is persisted and re-armed.
        if (task.repeat != "Never") {
            val next = ReminderScheduler.nextOccurrence(task.dueAt, task.repeat, System.currentTimeMillis())
            store.save(task.copy(dueAt = next))
        }
    }
}

class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TaskStore(context).rescheduleAll()
    }
}
